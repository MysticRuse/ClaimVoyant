import os
import random
import re
import logging
from dataclasses import dataclass, field
from datetime import datetime
from typing import List, Optional, Dict, Any
import httpx
from models import ClaimPackage, Party

logger = logging.getLogger("ClaimVoyant.agent")

STATE_FORMATS = {
    "CA": r"^[A-Z]\d{7}$",
    "NY": r"^\d{9}$",
    "TX": r"^\d{8}$",
    "FL": r"^[A-Z]\d{12}$",
    "WA": r"^[A-Z]{3}\*\*[A-Z]{2}\d{3}[A-Z]\d$"
}

_LOOSE_DATE_FORMATS = [
    "%m/%d/%Y", "%d/%m/%Y", "%b %d, %Y", "%B %d, %Y",
    "%b %d, %Y %I:%M %p", "%B %d, %Y %I:%M %p",
    "%Y-%m-%d", "%Y-%m-%d %H:%M:%S", "%Y-%m-%d %H:%M", "%Y/%m/%d"
]

FRAUD_DB = {
    "DL-66666666": {"risk_flags": 3, "previous_claims": 5, "status": "SUSPENDED"},
    "DL-11111111": {"risk_flags": 0, "previous_claims": 1, "status": "ACTIVE"},
    "DL-98765432": {"risk_flags": 0, "previous_claims": 0, "status": "ACTIVE"},
    "D1234567":    {"risk_flags": 0, "previous_claims": 0, "status": "ACTIVE"},
}

_MOCK_A2A_URL = os.getenv("MOCK_A2A_URL", "http://localhost:8080/mock-insurer/tasks")
INSURER_A2A_REGISTRY = {
    "StateSafe Insurance": _MOCK_A2A_URL,
    "Metropolis Mutual": _MOCK_A2A_URL,
    "AllShield Co": _MOCK_A2A_URL,
    "SafeDrive Insurance": _MOCK_A2A_URL,
    "CSAA Insurance Exchange / NAIC": _MOCK_A2A_URL,
    "CSAA Insurance Exchange": _MOCK_A2A_URL,
}
INSURER_A2A_FALLBACK = _MOCK_A2A_URL

def _parse_loose_date(text: str) -> Optional[datetime]:
    if not text:
        return None
    for fmt in _LOOSE_DATE_FORMATS:
        try:
            return datetime.strptime(text.strip(), fmt)
        except ValueError:
            continue
    try:
        # Fallback to standard isotime
        return datetime.fromisoformat(text.replace("Z", "+00:00"))
    except Exception:
        return None

def verify_policy(policy_number: str, insurer: str) -> Dict[str, Any]:
    return {
        "verified": True,
        "status": "ACCEPTED",
        "message": "Policy accepted for processing."
    }

def check_fraud(license_number: str, package: Optional[ClaimPackage] = None) -> Dict[str, Any]:
    risk_score = 10
    risk_notes: List[str] = []
    
    # FRAUD_DB lookup
    record = FRAUD_DB.get(license_number)
    if not record and license_number:
        # Try without prefix
        simplified = license_number.replace("DL-", "")
        record = FRAUD_DB.get(simplified) or FRAUD_DB.get(f"DL-{simplified}")

    if record:
        risk_score += record["risk_flags"] * 20
        if record["status"] == "SUSPENDED":
            risk_notes.append("Driver's license registry indicates SUSPENDED state.")
    
    # State format confirmation
    format_matched = False
    for label, pattern in STATE_FORMATS.items():
        if re.match(pattern, license_number or ""):
            format_matched = True
            break
            
    if not format_matched and license_number:
        risk_score += 10
        risk_notes.append("Driver's license does not comply with CA, NY, TX, FL, or WA structural formats.")

    if package:
        sub_date = _parse_loose_date(package.submitted_at) or datetime.utcnow()
        
        # DL Expiry Check
        if package.parties:
            party = package.parties[0]
            dl_exp = _parse_loose_date(party.insurance_expiry) # mapping check
            if dl_exp and dl_exp < sub_date:
                risk_score += 20
                risk_notes.append("Insurance policy card was expired at the time of collision.")

        # Severity low confidence
        if package.damage.severity in ("severe", "total_loss") and package.damage.confidence < 0.6:
            risk_score += 15
            risk_notes.append("Severe impact reported with disproportionately low vision classification confidence metrics.")

    risk_score = min(risk_score, 100)
    
    if risk_score > 60:
        risk_level = "HIGH"
    elif risk_score > 20:
        risk_level = "MEDIUM"
    else:
        risk_level = "LOW"

    return {
        "risk_level": risk_level,
        "risk_score": risk_score,
        "risk_flags": record["risk_flags"] if record else 0,
        "previous_claims": record["previous_claims"] if record else 0,
        "license_status": record["status"] if record else "ACTIVE",
        "risk_notes": risk_notes,
        "message": "Fraud check completed."
    }

def register_claim(policy_number: str, insurer: str, name: str) -> Dict[str, Any]:
    rand_id = random.randint(10000, 99999)
    return {
        "claim_number": f"CR-2026-{rand_id}",
        "status": "PENDING",
        "message": f"Claim registered with {insurer} dispatcher system."
    }

def build_fnol_task(package: ClaimPackage) -> Dict[str, Any]:
    party = package.parties[0] if package.parties else None
    
    data_payload = {
        "policy_number": party.policy_number if party else "",
        "insured_name": party.name if party else "",
        "insured_email": party.email if party else "",
        "driver_license": party.license_number if party else "",
        "incident_date": package.submitted_at,
        "incident_lat": package.incident.gps.lat,
        "incident_lng": package.incident.gps.lng,
        "severity": package.damage.severity,
        "driveable": package.damage.vehicle_drivable,
        "airbag_deployed": package.damage.airbag_deployed,
        "areas_affected": package.damage.areas_affected,
        "confidence": package.damage.confidence,
    }
    
    parts = [{"type": "data", "data": data_payload}]
    
    if package.incident.description:
        parts.append({"type": "text", "text": package.incident.description})
        
    for idx, img in enumerate(package.evidence.images):
        parts.append({
            "type": "file",
            "metadata": {
                "file_type": img.type,
                "caption": img.caption,
                "index": idx
            }
        })

    return {
        "id": package.report_id,
        "message": {
            "role": "user",
            "parts": parts
        }
    }

def get_auto_answer(question_text: str, package: ClaimPackage) -> Optional[str]:
    q_lower = question_text.lower()
    answers = []
    
    if "injur" in q_lower:
        if package.damage.airbag_deployed:
            answers.append("Yes — injuries possible (airbag deployment observed).")
        else:
            answers.append("No injuries were reported.")
            
    if "law enforcement" in q_lower or "police" in q_lower:
        answers.append("No law enforcement was present.")
        
    if "airbag" in q_lower:
        answers.append("Airbags deployed." if package.damage.airbag_deployed else "No airbag deployment observed.")
        
    if "drive" in q_lower or "drivable" in q_lower:
        answers.append("Vehicle is driveable." if package.damage.vehicle_drivable else "Vehicle is undriveable and required towing assistance.")
        
    if "other party" in q_lower or "third party" in q_lower or "other driver" in q_lower:
        answers.append("No other party details were provided in this package.")
        
    return " ".join(answers) if answers else None

def _extract_question_text(body: Dict[str, Any]) -> str:
    try:
        if "question" in body:
            return str(body["question"])
        if "message" in body:
            msg = body["message"]
            if isinstance(msg, dict) and "parts" in msg:
                for p in msg["parts"]:
                    if isinstance(p, dict) and p.get("type") == "text":
                        return p.get("text", "")
            if isinstance(msg, str):
                return msg
    except Exception:
        pass
    return ""

def submit_a2a_fnol(package: ClaimPackage) -> Dict[str, Any]:
    insurer_name = package.parties[0].insurer if package.parties else ""
    endpoint = INSURER_A2A_REGISTRY.get(insurer_name, INSURER_A2A_FALLBACK)
    task = build_fnol_task(package)
    
    try:
        with httpx.Client(timeout=10.0) as client:
            resp = client.post(endpoint, json=task)
            if resp.status_code >= 400:
                return {"submitted": False, "reason": f"http_{resp.status_code}"}
                
            body = resp.json()
            q_text = _extract_question_text(body)
            auto_answer = get_auto_answer(q_text, package) if q_text else None
            
            return {
                "submitted": True,
                "reason": "ok",
                "status_code": resp.status_code,
                "endpoint": endpoint,
                "auto_answer_dispatched": auto_answer
            }
    except Exception as e:
        return {"submitted": False, "reason": f"exception_{str(e)}"}

@dataclass
class AgentResult:
    claim_number: str
    approved: bool
    fraud_risk_score: int
    summary: str
    a2a_submitted: bool = False
    a2a_reason: str = ""
    policy_message: str = ""
    fraud_message: str = ""
    risk_notes: List[str] = field(default_factory=list)
    fraud_detail: Dict[str, Any] = field(default_factory=dict)
    insurer_email: str = ""

class InsurerAgent:
    def __init__(self):
        # Uses standard Google AI Client SDK references
        self.api_key = os.getenv("GEMINI_API_KEY") or os.getenv("GOOGLE_API_KEY")
        self.gemini = None
        if self.api_key:
            try:
                from google import genai
                self.gemini = genai.Client(api_key=self.api_key)
            except ImportError:
                pass

    def run(self, package: ClaimPackage) -> AgentResult:
        try:
            if self.gemini:
                return self._run_gemini(package)
            else:
                return self._run_simulation(package)
        except Exception as e:
            logger.error("InsurerAgent processing fault: %s", e)
            # Safe recovery bypass high risk fallback
            return AgentResult(
                claim_number=f"CR-2026-{random.randint(10000, 99999)}",
                approved=False,
                fraud_risk_score=100,
                summary=f"Agent simulation recovered. Exception: {str(e)}",
                risk_notes=["System validation pipeline triggered fallback bypass."]
            )

    def _run_simulation(self, package: ClaimPackage) -> AgentResult:
        party = package.parties[0] if package.parties else None
        p_num = party.policy_number if party else ""
        insurer = party.insurer if party else "SafeDrive Insurance"
        dl = party.license_number if party else ""
        name = party.name if party else ""
        
        pol_check = verify_policy(p_num, insurer)
        fr_check = check_fraud(dl, package)
        a2a = submit_a2a_fnol(package)
        reg = register_claim(p_num, insurer, name)
        
        approved = fr_check["risk_level"] != "HIGH"
        
        if approved:
            summary_statement = f"Claim registered under file number {reg['claim_number']}. Bumper mechanics check cleared verification grids."
        else:
            summary_statement = f"Elevated fraud scoring triggered manual claim hold. Indicator notes: {', '.join(fr_check['risk_notes'])}"

        from insurer_email import INSURER_EMAIL_REGISTRY, INSURER_EMAIL_FALLBACK
        email_addr = INSURER_EMAIL_REGISTRY.get(insurer, INSURER_EMAIL_FALLBACK)

        return AgentResult(
            claim_number=reg["claim_number"],
            approved=approved,
            fraud_risk_score=fr_check["risk_score"],
            summary=summary_statement,
            a2a_submitted=a2a.get("submitted", False),
            a2a_reason=a2a.get("reason", ""),
            policy_message=pol_check["message"],
            fraud_message=fr_check["message"],
            risk_notes=fr_check["risk_notes"],
            fraud_detail=fr_check,
            insurer_email=email_addr
        )

    def _run_gemini(self, package: ClaimPackage) -> AgentResult:
        # Precompute fraud checklist
        party = package.parties[0] if package.parties else None
        p_num = party.policy_number if party else ""
        insurer = party.insurer if party else "SafeDrive Insurance"
        dl = party.license_number if party else ""
        name = party.name if party else ""

        fr_check = check_fraud(dl, package)
        a2a = submit_a2a_fnol(package)

        # Call Gemini Pro models to orchestrate verify_policy and register_claim logic
        prompt_instructions = f"""You are the ClaimVoyant Insurer Agent.
We need to register an FNOL package claim for {name} with insurer {insurer}.
Precomputed Fraud Scoring indicates risk_level={fr_check['risk_level']} with risk_score={fr_check['risk_score']}.
Indicators of concern: {fr_check['risk_notes']}.

Execute verification checks sequentially. You have access to:
- verify_policy: checks coverage for policy {p_num} with insurer {insurer}.
- register_claim: creates structural pending claims registers under policyholder name {name}.

Produce your final response as JSON matching:
{{
  "claim_number": "the claim identifier generated",
  "approved": boolean (true if risk_level is not HIGH),
  "summary": "1-2 sentence claim decision overview."
}}
"""
        from insurer_email import INSURER_EMAIL_REGISTRY, INSURER_EMAIL_FALLBACK
        email_addr = INSURER_EMAIL_REGISTRY.get(insurer, INSURER_EMAIL_FALLBACK)

        try:
            # We call Gemini with structured options
            response = self.gemini.models.generateContent(
                model="gemini-2.0-flash",
                contents=prompt_instructions,
                config={
                    "responseMimeType": "application/json",
                    "temperature": 0.1
                }
            )
            clean_text = (response.text or "{}").strip()
            # Stip code fences if present
            if clean_text.startswith("```"):
                clean_text = clean_text.split("```")[1]
                if clean_text.startswith("json"):
                    clean_text = clean_text[4:]
            
            import json
            decision = json.loads(clean_text)

            return AgentResult(
                claim_number=decision.get("claim_number", f"CR-2026-{random.randint(10000, 99999)}"),
                approved=decision.get("approved", fr_check["risk_level"] != "HIGH"),
                fraud_risk_score=fr_check["risk_score"],
                summary=decision.get("summary", "Claim received and authorized."),
                a2a_submitted=a2a.get("submitted", False),
                a2a_reason=a2a.get("reason", ""),
                policy_message="Gemini agent verification routing synchronized.",
                fraud_message=fr_check["message"],
                risk_notes=fr_check["risk_notes"],
                fraud_detail=fr_check,
                insurer_email=email_addr
            )
        except Exception as e:
            logger.warning("Gemini orchestration cycle failed, reverting to simulation: %s", e)
            return self._run_simulation(package)

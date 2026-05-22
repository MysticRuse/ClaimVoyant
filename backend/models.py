from pydantic import BaseModel, Field
from typing import List, Optional, Dict, Any

class GpsData(BaseModel):
    lat: float
    lng: float
    accuracy_m: float = 10.0

class IncidentInfo(BaseModel):
    timestamp: str
    gps: GpsData
    description: str

class Party(BaseModel):
    role: str
    name: str
    license_number: str
    insurer: str
    policy_number: str
    email: str = ""
    insured_names: str = ""
    insurance_expiry: str = ""
    vehicle_year: str = ""
    vehicle_make: str = ""
    vehicle_model: str = ""

class DamageInfo(BaseModel):
    severity: str
    areas_affected: List[str]
    description: str
    airbag_deployed: bool = False
    vehicle_drivable: bool = True
    confidence: float = 1.0

class EvidenceImage(BaseModel):
    type: str
    caption: str
    uri: str

class Evidence(BaseModel):
    images: List[EvidenceImage]

class ConfidenceScores(BaseModel):
    ocr_score: float
    narrative_score: float
    used_cloud_fallback: bool

class ClaimPackage(BaseModel):
    report_id: str
    schema_version: str = "1.0"
    submitted_at: str
    incident: IncidentInfo
    parties: List[Party]
    damage: DamageInfo
    evidence: Evidence
    confidence: ConfidenceScores

class ClaimResponse(BaseModel):
    claim_id: str
    status: str
    pending_claim_number: Optional[str] = None
    message: Optional[str] = None
    sse_url: Optional[str] = None

class ErrorResponse(BaseModel):
    error: str
    detail: Optional[str] = None

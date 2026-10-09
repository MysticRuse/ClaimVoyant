# ClaimVoyant — Pitch Slide Deck

---

## Slide 1 — Title

**ClaimVoyant**
*AI-First Multimodal FNOL & Insurance Orchestration*

- From crash scene to adjuster inbox in under 60 seconds
- Voice · Vision · AI Agents · Real-Time Streaming
- Built on Google Gemini · Deployed on Google Cloud Run

> **Team:** Mitil Roy · Hironmoy

---

## Slide 2 — The Problem

**Filing an auto insurance claim is broken**

- 6 million auto accidents per year in the US alone
- Drivers spend 45+ minutes on hold to file a single claim
- Average claim resolution takes **14 days** — mostly manual data entry
- The first hours are the most critical window for evidence; that window is wasted
- Insurers lose billions annually to fraud that could have been flagged immediately
- Root cause: **a broken information pipeline between accident scene and adjuster**

---

## Slide 3 — The Solution: How It Works

**A 6-step AI-guided workflow, entirely on a smartphone**

1. **Speak** — Driver dictates the incident; live voice-to-text seeds all downstream fields
2. **Film** — CameraX captures the scene; Gemini Flash extracts frames and classifies damage
3. **Scan** — On-device OCR reads the insurance card and driver's license automatically
4. **Review** — AI pre-populates a structured FNOL form; driver just verifies
5. **Transmit** — One tap submits the claim to the backend agent pipeline
6. **Watch** — A live SSE stream shows fraud check → policy check → A2A dispatch → adjuster email → claim filed

> At every step, **Aoede** — a Gemini 2.5 Flash voice guide — speaks empathetic, context-aware instructions to the driver

---

## Slide 4 — Technology

**Built entirely on Google's AI stack**

| Layer | Technology |
|---|---|
| Claim decision agent | Gemini 2.0 Flash (Google GenAI SDK) — fraud scoring, policy check, A2A dispatch |
| Video damage analysis | Gemini 1.5 Flash — multimodal frame analysis, structured JSON output |
| Voice guide (TTS) | Gemini 2.5 Flash — `responseModalities: ["AUDIO"]`, Aoede prebuilt voice |
| On-device fallback | ML Kit GenAI / Gemma Nano — offline inference, no connectivity required |
| Backend | FastAPI + Server-Sent Events, deployed on **Google Cloud Run** |
| Mobile | Kotlin · Jetpack Compose · CameraX · Android SpeechRecognizer |

- **Multimodal inputs**: voice, video, images, structured form data — all in one pipeline
- **Grounded decisions**: fraud DB lookups, license format validation, expiry cross-checks prevent hallucinations
- **Tiered resilience**: Cloud → On-device → Static fallback — always functional

---

## Slide 5 — Impact & Why Now

**ClaimVoyant is the future of work for insurance adjusters**

- Adjusters process hundreds of FNOL packets per week — almost entirely by hand
- ClaimVoyant automates evidence capture, document extraction, fraud scoring, and structured dispatch in **< 60 seconds**
- The A2A dispatch layer integrates directly into carrier Agent-to-Agent endpoints — a drop-in upgrade to existing insurer workflows
- Policyholders get a claim number and confirmation email **before the tow truck arrives**
- Early fraud signals are captured at the scene — not weeks later during manual review

**This is not a prototype. It is working software running on Google Cloud today.**

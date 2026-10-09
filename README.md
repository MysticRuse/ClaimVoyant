# 👁️ ClaimVoyant
**AI-First Multimodal FNOL & Insurance Orchestration System**

ClaimVoyant is a next-generation insurance claims platform that transforms the stressful "First Notice of Loss" (FNOL) experience into a seamless, high-integrity digital workflow. By leveraging **Gemini 1.5 Flash**, **Gemini 2.0 Flash**, **Gemini 2.5 Flash**, and **Gemma Nano**, ClaimVoyant sees, hears, and speaks to guide users through the immediate aftermath of a vehicle accident.

---

## 🚀 Key Features (Multimodal Innovation)

### 👁️ See: AI Damage Assessment
*   **Video Scene Analysis**: Extracts up to 5 frames from accident scene footage and sends them as multimodal input to **Gemini 1.5 Flash (Cloud)** for damage classification — damaged areas, severity, and adjuster field notes — returned as structured JSON.
*   **On-Device Fallback (Gemma Nano)**: When cloud inference is unavailable, **ML Kit GenAI (Gemma Nano)** generates text-only scene field notes entirely on-device, with no connectivity required.
*   **Tiered Resilience**: Three-tier inference strategy — Gemini 1.5 Flash (cloud, multimodal) → Gemma Nano (on-device, text) → static heuristic fallback. Always functional.

### 🎙️ Hear & Speak: Voice-First Interaction
*   **Live Voice Dictation**: Android `SpeechRecognizer` captures accident narratives with partial-result streaming and animated waveform feedback — no system dialog, no network call.
*   **Voice Email Input**: `SpeechRecognizer` also captures email addresses by voice (e.g. "jane dot doe at gmail dot com") and normalizes them automatically.
*   **AI Voice Guide (Aoede)**: `VoiceGuideAgent` powered by **Gemini 2.5 Flash** with `responseModalities: ["AUDIO"]` speaks empathetic, context-aware guidance throughout the claim — using the `Aoede` prebuilt voice. Adapts its script based on claim outcome (`claim_filed`, `flagged`).

### 🧭 Live Claim Ledger (SSE Streaming)
*   **Real-Time Transparency**: A persistent Server-Sent Events connection streams live pipeline progress from the backend — rendered as a sequential event log on-device.
*   **Automated Verification Pipeline**: `status → fraud_check → policy_check → a2a_submitted → email_sent → claim_filed | flagged`
*   **Dual Email Dispatch**: HTML loss summary sent to the insurance adjuster; confirmation email sent to the insured party.

---

## 🛠️ Tech Stack

### Mobile (Android)
*   **Language**: Kotlin
*   **UI Framework**: Jetpack Compose (Material 3, editorial warm-linen theme)
*   **Architecture**: MVVM — `ClaimViewModel` owns all state; Compose UI reads only; all coroutine work runs in `viewModelScope`
*   **AI Integration**:
    *   **Google GenAI SDK** (`generativeai 0.9.0`): Gemini 1.5 Flash for multimodal video frame analysis
    *   **ML Kit GenAI** (`genai-prompt 1.0.0-beta2`): Gemma Nano on-device text inference (invoked via reflection for beta API stability)
    *   **Android SpeechRecognizer**: On-device live STT with partial results
*   **Camera**: CameraX (`camera-video`, `camera-lifecycle`) for on-scene video recording
*   **Networking**: Retrofit + Gson (REST), OkHttp + `okhttp-sse` (SSE streaming)
*   **Image Loading**: Coil
*   **Dependency Injection**: Hilt

### Backend (Python / FastAPI)
*   **Framework**: FastAPI + `uvicorn`
*   **AI Agents**:
    *   **`InsurerAgent`** — Gemini 2.0 Flash via `google-genai` SDK: orchestrates fraud check, policy verification, A2A dispatch, and returns a structured JSON claim decision
    *   **`VoiceGuideAgent`** — Gemini 2.5 Flash with audio output: produces spoken guidance in the `Aoede` voice, context-switched per claim event
*   **Communication**: Server-Sent Events (SSE) for real-time pipeline status
*   **Email**: SMTP via Gmail — HTML loss report to adjuster, confirmation to insured
*   **Deployment**: **Google Cloud Run** (`claimvoyant-api-395261322066.us-central1.run.app`)

---

## 🏗️ System Architecture

1.  **Interaction Layer**: Android app captures voice narrative (on-device STT) and video footage (CameraX).
2.  **Edge Intelligence**: Gemma Nano (on-device) generates text scene notes when offline. On-device OCR extracts policy card and license data.
3.  **Cloud Vision Analysis**: Gemini 1.5 Flash analyzes up to 4 extracted video frames for damage classification and structured adjuster field notes.
4.  **Agentic Orchestration**: FastAPI backend `InsurerAgent` (Gemini 2.0 Flash) runs sequentially:
    *   Fraud risk scoring against registry, license format, and policy expiry
    *   Policy verification
    *   A2A FNOL packet dispatch to insurer endpoint
    *   Claim registration
5.  **Voice Finalization**: `VoiceGuideAgent` (Gemini 2.5 Flash / Aoede) announces the claim outcome to the user by voice.
6.  **Finalization**: HTML loss summary emailed to adjuster; confirmation emailed to insured. Claim file number returned to app.

---

## ⚖️ Salient Points

### 1. Innovation & Multimodal UX
*   **Beyond Text**: ClaimVoyant replaces rigid forms with voice dictation, video capture, and spoken AI guidance — a continuous "Live" experience, not turn-based interaction.
*   **Four AI Models, One Journey**: Gemini 1.5 Flash (vision), Gemma Nano (on-device fallback), Gemini 2.0 Flash (agent orchestration), Gemini 2.5 Flash (voice/audio output) — each chosen for a specific capability.

### 2. Technical Implementation & Agent Architecture
*   **Google Cloud Native**: Deployed on Cloud Run; uses Google GenAI SDK across both Android and Python backend.
*   **Agent Logic**: `InsurerAgent` runs a committed, repeatable four-tool workflow. Gemini reasons over pre-computed grounded signals — it never generates a risk score, it interprets one.
*   **Grounding**: Fraud decisions are grounded in a license registry lookup, state-specific DL format validation (CA, NY, TX, FL, WA regex), and policy expiry cross-check against the incident timestamp — preventing hallucinated risk assessments.

### 3. Demo & Presentation
*   **The Story**: Solving the high-friction, low-trust problem of manual FNOL filing.
*   **The Proof**: Architecture demonstrates Gemini multimodal capabilities — interleaved vision, structured JSON output, and audio modality in a single claim workflow.
*   **Working Software**: Fully functional Android app connected to a live FastAPI backend on Cloud Run. Shows `● LIVE — Cloud Run Backend` badge when connected.

---

## 🏁 Getting Started

### Backend
1.  Navigate to `/backend`.
2.  Install dependencies: `pip install -r requirements.txt`.
3.  Set environment variables: `GEMINI_API_KEY`, `SMTP_USER`, `SMTP_PASSWORD`.
4.  Run the server: `uvicorn main:app --reload`.

### Android
1.  Open `/android` in Android Studio (Ladybug or later).
2.  Add the following to `android/local.properties`:
    ```
    gemini.api.key=YOUR_GEMINI_API_KEY
    BASE_URL=https://claimvoyant-api-395261322066.us-central1.run.app/
    ```
3.  Build and run on an Android device (Min SDK 26 / Android 8.0).

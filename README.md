# 👁️ ClaimVoyant
**AI-First Multimodal FNOL & Insurance Orchestration System**

ClaimVoyant is a next-generation insurance claims platform that transforms the stressful "First Notice of Loss" (FNOL) experience into a seamless, high-integrity digital workflow. By leveraging **Gemini 2.0 Flash**, **Gemini Vision**, and **Gemma Nano**, ClaimVoyant "sees" and "understands" to guide users through the immediate aftermath of a vehicle accident.

---

## 🚀 Key Features (Multimodal Innovation)

### 👁️ See: AI Damage Assessment
*   **Video Scene Analysis**: Analyzes accident scene footage using **Gemini 1.5/2.0 Flash (Cloud)** to extract key frames and classify damage.
*   **On-Device Fallback (Gemma Nano)**: Utilizes **ML Kit GenAI (Gemma Nano)** to generate scene field notes locally on the device when connectivity is limited, ensuring data privacy and robustness.
*   **Technical Narrative Generation**: Automatically creates high-fidelity technical descriptions and incident narratives for insurance adjusters.

### 🎙️ Hear: Voice-First Narrative Capture
*   **Intelligent Onboarding**: Naturally extracts incident details, injury reports, and policy information through conversational dictation.
*   **Speech-to-Text Integration**: Uses high-accuracy STT to seed the carrier pre-population layers, moving away from rigid manual forms.

### 🧭 Live Claim Ledger (SSE Streaming)
*   **Real-Time Transparency**: A progressive onboarding flow where users watch their claim move through "The Ledger" in real-time via **Server-Sent Events (SSE)**.
*   **Automated Verification**: Sequential execution of fraud checks, policy validation, and carrier A2A (App-to-App) integration.

---

## 🛠️ Tech Stack

### Mobile (Android)
*   **Language**: Kotlin
*   **UI Framework**: Jetpack Compose (Modern Material 3 Editorial Theme)
*   **AI Integration**: 
    *   **Google GenAI SDK**: For Gemini Flash cloud inference.
    *   **ML Kit GenAI**: For on-device **Gemma Nano** inference.
    *   **SpeechRecognizer**: For real-time STT input.
*   **Networking**: OkHttp (SSE support), Coil (Image loading).

### Backend (FastAPI)
*   **Framework**: FastAPI (Python)
*   **Orchestration**: **Gemini 2.0 Flash** via the `google-genai` SDK.
*   **Communication**: Server-Sent Events (SSE) for real-time status updates.
*   **Deployment**: Google Cloud Platform (GCP).

---

## 🏗️ System Architecture

1.  **Interaction Layer**: Android App captures voice narrative (STT) and video/images.
2.  **Edge Intelligence**: Gemma Nano (On-Device) performs initial scene description and OCR extraction.
3.  **Cloud Analysis**: Gemini Vision analyzes video frames for granular damage classification.
4.  **Agentic Orchestration**: FastAPI backend uses a **Gemini 2.0 Agent** to:
    *   Validate policy data.
    *   Run fraud risk scoring.
    *   Dispatch A2A XML packets to insurer endpoints.
5.  **Finalization**: Automated email dispatch to both the claimant and the insurance adjuster with a full loss summary.

---

## ⚖️ Salient Points

### 1. Innovation & Multimodal UX
*   **Beyond Text**: ClaimVoyant moves away from rigid forms to a "Listen & See" model for data entry.
*   **Fluidity**: The transition from voice-assisted onboarding to video analysis to the live streaming ledger creates a "Live" experience that feels continuous and context-aware.

### 2. Technical Implementation & Agent Architecture
*   **Google Cloud Native**: Fully integrated with Gemini 2.0 Flash and deployed on Google Cloud.
*   **Agent Logic**: The `InsurerAgent` handles complex routing, fraud scoring, and A2A integration, demonstrating a robust system design.
*   **Grounding**: Uses real-time data from fraud databases and policy registries to prevent hallucinations in claim decisions.(TBD)

### 3. Demo & Presentation
*   **The Story**: Solving the high-friction, low-trust problem of manual FNOL filing.
*   **The Proof**: Architecture clearly utilizes Gemini's multimodal capabilities (Interleaved inputs and structured processing).
*   **Working Software**: Fully functional Android app integrated with a live FastAPI streaming backend.

---

## 🏁 Getting Started

### Backend
1.  Navigate to `/backend`.
2.  Install dependencies: `pip install -r requirements.txt`.
3.  Set environment variables: `GEMINI_API_KEY`.
4.  Run the server: `uvicorn main:app --reload`.

### Android
1.  Open `/android` in Android Studio (Ladybug or later).
2.  Add your `gemini.api.key` to `local.properties`.
3.  Build and run on an Android device (Min SDK 26).

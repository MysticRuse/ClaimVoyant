# ClaimVoyant System Design Document

## 1. Executive Summary
ClaimVoyant is a multimodal, agentic insurance orchestration system designed to automate the **First Notice of Loss (FNOL)** process. It integrates on-device computer vision, cloud-based generative AI, and an agentic backend to transform a manual, high-friction process into a transparent, real-time digital experience.

---

## 2. High-Level Architecture

The system follows a **Client-Server-Agent** architecture:
- **Mobile Client (Android)**: Handles multimodal data capture (voice, video, images) and performs edge-based AI inference.
- **Backend Orchestrator (FastAPI)**: Manages claim state, coordinates external integrations, and streams real-time updates to the client.
- **Agentic Layer (Gemini 2.0)**: Acts as the "Decision Engine" for fraud detection, policy validation, and carrier routing.

---

## 3. Component Breakdown

### 3.1 Mobile Architecture (Android)
Built using **Jetpack Compose** and **MVVM** pattern.
- **`ClaimViewModel`**: The central state machine. It manages the 6-step workflow, handles navigation, and coordinates background tasks.
- **`VideoAnalyzer`**: A multi-tier inference engine:
    - **Tier 1 (Cloud)**: Gemini 1.5/2.0 Flash for multimodal frame analysis.
    - **Tier 2 (Edge)**: Gemma Nano (via ML Kit) for offline text generation.
- **Evidence Collector**: Manages CameraX integration and Gallery pickers for image/video artifacts.

### 3.2 Backend Architecture (FastAPI)
A stateless Python backend designed for high-concurrency event streaming.
- **`InsurerAgent`**: A Gemini 2.0-powered agent that orchestrates verification tasks.
- **`SSE (Server-Sent Events) Pipeline`**: Provides real-time "Ledger" updates to the mobile client during the 5-stage verification process.
- **`A2A Integration`**: A mock App-to-App interface simulating XML packet dispatch to carrier endpoints (e.g., StateSafe, Metropolis Mutual).

### 3.3 AI Model Stack
| Layer | Model | Purpose |
| :--- | :--- | :--- |
| **Multimodal Vision** | Gemini 1.5/2.0 Flash | Damage classification & technical narrative generation from video frames. |
| **Edge Intelligence** | Gemma Nano | Localized scene description (offline/privacy mode). |
| **Agentic Reasoning** | Gemini 2.0 Flash | Fraud scoring, policy verification, and decision orchestration. |
| **Speech** | Android STT | Voice-to-text narrative capture. |

---

## 4. Sequential Data Flow

### Step 1: Voice Onboarding
The user dictates the accident scenario. The app uses **Speech-to-Text** to populate the initial incident narrative.

### Step 2: Video Capture & Analysis
1.  User records a video of the damage.
2.  App extracts 5 key frames.
3.  **VideoAnalyzer** sends frames to Gemini (Cloud) or processes via Gemma (Edge).
4.  Result: Damage areas, severity, and a technical field note.

### Step 3: Evidence Collection & OCR
User captures photos of licenses and insurance cards. The app triggers **On-Device OCR** to extract structured data (Name, Policy #, etc.).

### Step 4: Agentic Submission (The Ledger)
Once the user reviews and submits:
1.  **POST `/api/claims`**: Mobile sends the full `ClaimPackage`.
2.  **Verification Pipeline**:
    - `status`: Initialization.
    - `fraud_check`: Agent scans license format and registry (mocked).
    - `policy_check`: Validates policy boundaries and expiration.
    - `a2a_submitted`: Dispatches FNOL packet to the carrier.
    - `email_sent`: Sends loss summary emails via MIME/HTML.
    - `claim_filed`: Returns final Claim Number.

---

## 5. Security & Validation Logic

- **Fraud Scoring**: The `InsurerAgent` calculates a risk score based on:
    - License format structural compliance (NY, CA, FL, etc.).
    - Registry lookups for suspended licenses.
    - Discrepancies between reported severity and vision confidence scores.
- **Data Integrity**: Uses Pydantic models for strict type checking between the mobile client and the FastAPI backend.

---

## 6. Deployment Strategy

- **Backend**: Containerized via Docker and deployed on **Google Cloud Run**.
- **Edge AI**: Gemma Nano binaries are managed via the **Google Play Services** dynamic module delivery to minimize initial app size.
- **Observability**: Real-time logging of agent decisions and SSE event latency.

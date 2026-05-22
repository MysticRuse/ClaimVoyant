# ClaimVoyant — AI Insurance Accident Documentation Agent

**Award Track Nomination:** Live Agents (Real-Time Audio / Interactive Form Pre-population)  
**Industry Vertical Focus:** Future of Work (Accident workflow optimization)  

---

## 🎯 Strategic Hackathon Objectives & Achievements

### 1. Innovation & Multimodal UX (40%)
- **Speech-to-form extraction**: First-class voice onboarding powered by a real-time speech visualizer. The system extracts insurance carriers, policy identifiers, and accidental severity metrics from your described story to pre-populate the interactive form.
- **Micro-animations & Audio feedback**: Uses professional HTML5 visualizers and native `SpeechSynthesis` backed by multi-modal TTS endpoints to narrate pipeline events progressively.

### 2. Architectural Ingenuity (30%)
- **On-Device Sandbox Classifier representation**: Emulates multi-modal vision classifiers running hybrid filters.
- **Progressive SSE Claim Ledger**: Real-time server-sent events guide users continuously through carrier gateway checks without requiring raw terminal telemetries.

### 3. Demo Polish
- Includes a video recording blueprint in `docs/VIDEO_SCRIPT.md` and a clean vector roadmap in `docs/architecture.svg`.

---

## 🚀 Architectural Design Map

ClaimVoyant coordinates the onboarding, damage estimation, card parsing, and registration checks inside a unified interface:

![ClaimVoyant System Map](docs/architecture.svg)

---

## 🏎️ Walkthrough Guide

1. **Onboarding Audio Dialogue**: Run through the conversation or speak to describe the scenario.
2. **Card Parser Selection**: Choose simulated photos (e.g., *Frontal Crash*, *Severe T-Bone*) to load OCR targets.
3. **Multi-modal Review**: Verify details filled automatically from your text.
4. **Live Execution Stream**: Hit Submit to watch carrier APIs perform compliance checks in real-time.

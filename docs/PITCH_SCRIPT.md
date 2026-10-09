# ClaimVoyant — 5-Minute Pitch Script

> **HP** introduces the business need (~75 sec)  
> **Mitil** demos the app live on Android (~3 min 30 sec)  
> **HP** closes with tech & impact (~30 sec)
>
> ⏱ Total target: **5 minutes**

---

## HP — Business Need (0:00 – 1:15)

> *Stand facing camera. No app visible yet.*

"Every year, more than **six million** auto accidents happen in the United States alone.

And almost every single one ends the same way — a confused, shaken driver standing on the side of the road, trying to remember their policy number, trading paper insurance cards with a stranger, and eventually spending **forty-five minutes on hold** waiting to file a claim.

That call kicks off a process that takes on average **fourteen days** to resolve. The adjuster gets a voice message. Maybe some blurry photos. A hand-written incident report. And then they manually key everything into a legacy system.

The first few hours after an accident are the most critical window for capturing accurate evidence. The scene changes. Memories fade. Documents get lost. And insurers lose billions every year to fraud that could have been caught immediately — if only they had the right data at the right time.

**The problem isn't just inconvenience. It's a broken information pipeline.**

ClaimVoyant fixes that pipeline. It turns a smartphone into a full FNOL field agent — one that sees the damage, hears the story, extracts the documents, checks for fraud in real time, and routes a structured claim to the adjuster before the tow truck even arrives.

I'll hand it over to Mitil to show you exactly how."

---

## Mitil — Live App Demo (1:15 – 4:45)

> *Hold device so screen is visible to camera or screen-cast to display.*  
> *Speak directly to what's on screen — do not rush.*

---

### Landing Screen (1:15 – 1:30) `~15 sec`

> *Show the landing screen — warm linen editorial design.*

"This is ClaimVoyant. The moment someone is in an accident, they open this. One tap — **Start Claim Conversation** — and we begin walking them through a six-step guided workflow.

Notice we're already connected to our live Cloud Run backend — you can see that indicator in the top right. Everything from here is real."

---

### Step 1 — Voice Narrative (1:30 – 2:10) `~40 sec`

> *Tap 'Start Claim Conversation'. Step 1 appears: "Describe Accident Scenario".*

"Step one: the driver describes what happened — in their own words, out loud.

I'm going to tap the **Record** button."

> *Tap Record. Waveform animates red. Speak into the mic:*  
> *"I was rear-ended on the I-280 exit ramp. The other driver changed lanes without signalling. My front bumper took the hit, no airbags deployed, vehicle is still driveable. Their insurer is SafeDrive Insurance, policy number POL-9999."*

"Watch the waveform — that's live microphone input. The transcript is appearing in real time as I speak. When I stop, the AI has the full incident narrative ready to seed every downstream field.

This isn't a form. It's a conversation."

> *Tap 'Process Scene & Confirm'.*

---

### Step 2 & 3 — Video Capture + AI Analysis (2:10 – 2:55) `~45 sec`

> *Navigate to the Video Capture screen.*

"Step two — we capture the scene. The driver points their camera at the damaged vehicle and records a short clip."

> *Show the camera recording briefly or load a pre-recorded clip.*

"That video is sent directly to **Gemini Flash** in the cloud. The analyzer extracts five evenly-spaced frames and sends them to the model as a multimodal input — along with a structured prompt asking for damaged areas, severity classification, and a technical incident narrative ready for an adjuster.

On the screen right now you can see the result: **moderate severity**, front bumper and hood affected, confidence score coming back at 0.87.

And if we're offline? We fall back to **Gemma Nano on-device** — no connectivity required."

---

### Step 4 — Document Scan (2:55 – 3:20) `~25 sec`

> *Navigate to the Evidence & Document Scan screen.*

"Step four — document artifacts. The driver can photograph their insurance card or driver's license right here.

We run **on-device OCR extraction** against those images. That pulls the policy number, carrier name, license ID, and expiry date — automatically — and pre-populates the next screen.

No typing. No squinting at a tiny card on the side of the road."

> *Tap 'Run On-Device OCR Analysis'.*

---

### Step 5 — Review Parsed Records (3:20 – 3:45) `~25 sec`

> *Navigate to the Review screen. Fields are already populated.*

"Step five — the driver just **verifies** what the AI already extracted. Name, policy number, insurer, license — all filled in from the voice input and the OCR scan.

They can still edit anything. And notice the email field — they can even **dictate their email address** by voice. Say 'jane dot doe at gmail dot com' and the app normalizes it automatically.

One tap — **File Claim & Transmit**."

> *Tap 'File Claim & Transmit'.*

---

### Step 6 — Claim Automation Ledger / SSE Stream (3:45 – 4:45) `~60 sec`

> *Navigate to the Streaming screen. Events begin appearing one by one.*

"This is the live claim ledger — and this is where things get interesting.

Watch these events populating in real time. This is a **Server-Sent Events stream** coming back from our FastAPI backend running on **Google Cloud Run**.

**FNOL PACKET RECEIVED** — the claim has been registered.

**FRAUD CHECK** — our `InsurerAgent`, powered by **Gemini 2.0 Flash**, is cross-referencing the license number against a risk database, checking policy expiry, and scoring anomalies. Risk score: 15 out of 100 — low risk, we're clear.

**POLICY CHECK** — carrier registry verified.

**A2A SUBMITTED** — the structured FNOL packet has been dispatched directly to the insurer's Agent-to-Agent endpoint. No phone call. No fax.

**EMAIL SENT** — and right now, the adjuster has just received a full HTML loss summary report in their inbox with GPS coordinates, damage photos, severity badge, and the fraud assessment.

**CLAIM FILED.** File reference CR-2026 assigned.

The whole pipeline — from tap to adjuster inbox — took under **60 seconds**."

> *Let the green 'Claim Logged Successfully' card sit on screen for a beat.*

---

## HP — Close (4:45 – 5:00) `~15 sec`

> *Back on camera.*

"ClaimVoyant is built entirely on Google's AI stack — **Gemini 2.0 Flash** for agent orchestration, **Gemini 1.5 Flash** for multimodal video analysis, **Gemma Nano** for on-device fallback, and deployed on **Google Cloud Run**.

It's not a prototype. It's a working system. And it's just the beginning of what agentic AI can do for the future of work in insurance.

Thank you."

---

## Timing Summary

| Segment | Speaker | Duration |
|---|---|---|
| Business Need | HP | 1 min 15 sec |
| Landing Screen | Mitil | 15 sec |
| Voice Narrative | Mitil | 40 sec |
| Video Capture + AI Analysis | Mitil | 45 sec |
| Document Scan | Mitil | 25 sec |
| Review Screen | Mitil | 25 sec |
| Claim Ledger (SSE) | Mitil | 60 sec |
| Close | HP | 15 sec |
| **Total** | | **~5 min 00 sec** |

---

## Demo Prep Checklist

- [ ] Device charged, screen brightness max, Do Not Disturb on
- [ ] App connected to Cloud Run backend (check `● LIVE` badge on Step 6)
- [ ] Pre-record a short accident scene video clip to avoid fumbling with camera during demo
- [ ] Pre-load insurance card photo in camera roll for OCR step
- [ ] Test full flow end-to-end once immediately before recording
- [ ] Check adjuster inbox — confirm email arrives for the live run
- [ ] Keep `DL-66666666` license ID ready if you want to demo the **high fraud / manual hold** path as an alternate ending

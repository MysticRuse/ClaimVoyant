# ClaimVoyant Agents Playbook

This document defines design guidelines, routing sequences, and validation requirements for the ClaimVoyant system.

---

## 🎨 Creative Theme Parameters

- **Font Face**: Georgia, serif (Headings & display italics) + Inter, sans-serif (Structured inputs & body copy).
- **Surface Canvas**: `#F7F6F2` (Warm linen editorial tone) paired with solid `#1A1A1A` borders/shadow blocks.
- **Micro-animation**: Pulsing bar scales representing real-time microphone interactions.

---

## 🧭 Progressive SSE Events Order

An accident claim pipeline must execute standard verification stages sequentially:
1. `status` — Receipt and initialization confirmation.
2. `fraud_check` — Registry scanning.
3. `policy_check` — Ledger and coverage validation.
4. `a2a_submitted` — Insurer XML integration dispatch.
5. `email_sent` — Adjustor notifications.
6. `claim_filed` | `flagged` — Final compliance state resolution.

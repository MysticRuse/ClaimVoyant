import os
import logging
from typing import Dict, Any, Optional

logger = logging.getLogger("ClaimVoyant.voice")

class VoiceGuideAgent:
    def __init__(self):
        self.api_key = os.getenv("GEMINI_API_KEY") or os.getenv("GOOGLE_API_KEY")
        self.ai = None
        if self.api_key:
            try:
                from google import genai
                self.ai = genai.Client(apiKey=self.api_key)
            except ImportError:
                pass

    async def get_response_and_speech(self, prompt: str, event: Optional[str] = None, claim_number: Optional[str] = None, insurer: Optional[str] = None) -> Dict[str, Any]:
        system_prompt = (
            "You are Aoede, a warm, professional, Empathetic AI Insurance Claims Assistant for ClaimVoyant.\n"
            "The user has just been in a car accident. Maintain extreme empathy, calm composure, and speak concisely in 1-2 short sentences maximum.\n\n"
            "Help them file their FNOL (First Notice of Loss). If they ask questions, answer them based on insurance best practices.\n"
            "Always ask follow-up questions sequentially if details are missing:\n"
            "1. What happened? Or a summary of the incident.\n"
            "2. Any injuries reported?\n"
            "3. Is their vehicle driveable?\n"
            "4. What is their insurer's name and policy number?"
        )

        if event == "claim_filed":
            system_prompt = "You are Aoede, the ClaimVoyant Assistant. Express great relief and congratulations. Politely declare that their claim has been registered."
            prompt = f"Announce that their claim {claim_number or 'CR-2026-94303'} has been successfully filed with {insurer or 'SafeDrive Insurance'} and an agent will follow up."
        elif event == "flagged":
            system_prompt = "You are Aoede, the ClaimVoyant Assistant. Be highly supportive and professional."
            prompt = "Kindly explain that due to some policy verification parameters, their claim in under manual review by an adjuster, and they shouldn't worry."
        elif event == "stats":
            system_prompt = "Provide an objective, friendly, concise summary for the claim events."

        if not self.ai:
            # Fallback when key is missing or SDK is not initialized
            fallback_text = (
                f"Great news. Your claim {claim_number or 'CR-2026'} has been submitted successfully to {insurer or 'your insurer'}. You will receive a confirmation email shortly."
                if event == "claim_filed"
                else "Your claim requires manual review by an adjuster. We've compiled the data and forwarded it."
                if event == "flagged"
                else "I've processed that detail. Let's make sure we have your insurer and policy details."
            )
            return {"text": fallback_text, "useClientSpeech": True}

        try:
            # Use gemini-2.5-flash with voice responses
            response = self.ai.models.generateContent(
                model="gemini-2.5-flash",
                contents=prompt or "Hello, describe the accident sequence.",
                config={
                    "systemInstruction": system_prompt,
                    "responseModalities": ["AUDIO"],
                    "speechConfig": {
                        "voiceConfig": {
                            "prebuiltVoiceConfig": {
                                "voiceName": "Aoede" # warm voice prebuilt identifier
                            }
                        }
                    }
                }
            )
            
            # Find audio block
            part = response.candidates[0].content.parts[0] if response.candidates else None
            base64_audio = None
            if part and part.inline_data:
                import base64
                base64_audio = base64.b64encode(part.inline_data.data).decode("utf-8")
                
            spoken_text = response.text or "I have logged that down. Let's proceed."
            
            if base64_audio:
                return {
                    "text": spoken_text,
                    "audio": f"data:audio/mp3;base64,{base64_audio}"
                }
                
            return {
                "text": spoken_text,
                "useClientSpeech": True
            }
        except Exception as e:
            logger.warning("TTS API call failed, fell back to client audio: %s", e)
            return {
                "text": "I possess noted these details down. Let's make sure we proceed with the images.",
                "useClientSpeech": True
            }

package com.android.mr.claimvoyantapp.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.mr.claimvoyantapp.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class AppStep {
    LANDING,
    VOICE_GUIDE,
    ARTIFACT_COLLECTION,
    ANALYSIS_COMPILING,
    REVIEWS,
    STREAMING_LEDGER
}

class ClaimViewModel : ViewModel() {
    
    var currentStep by mutableStateOf(AppStep.LANDING)
        private set

    // Onboarding Voice state
    var voiceSummary by mutableStateOf("")
    var voiceIsRecording by mutableStateOf(false)
    var extractedName by mutableStateOf("Jane Smith")
    var extractedInsurer by mutableStateOf("SafeDrive Insurance")
    var extractedPolicy by mutableStateOf("POL-9999")
    var extractedExpiry by mutableStateOf("2026-12-31")
    var airbagDeployed by mutableStateOf(false)
    var isDrivable by mutableStateOf(true)

    // Evidence Artifacts
    val capturedImages = mutableListOf<String>()
    var ocrStatusMsg by mutableStateOf("")

    // Review fields
    var formName by mutableStateOf("Jane Smith")
    var formLicense by mutableStateOf("D1234567")
    var formInsurer by mutableStateOf("SafeDrive Insurance")
    var formPolicy by mutableStateOf("POL-9999")
    var formExpiry by mutableStateOf("2026-12-31")
    var formDamageDesc by mutableStateOf("Localized bumper crush with minor panel misalignment.")

    // Processing Ledgers
    var isApiLoading by mutableStateOf(false)
    var apiError by mutableStateOf<String?>(null)
    var claimId by mutableStateOf("")
    val sseEvents = mutableListOf<SseEvent>()
    var streamFinalResult by mutableStateOf<SseEventData?>(null)
    var streamIsRunning by mutableStateOf(false)

    fun startClaim() {
        currentStep = AppStep.VOICE_GUIDE
    }

    fun completeVoiceOnboarding(summary: String, name: String, insurer: String, policy: String, airbag: Boolean, drivable: Boolean) {
        voiceSummary = summary
        extractedName = name
        extractedInsurer = insurer
        extractedPolicy = policy
        airbagDeployed = airbag
        isDrivable = drivable

        // Seed reviews
        formName = name
        formInsurer = insurer
        formPolicy = policy
        _syncReviewDetails()

        currentStep = AppStep.ARTIFACT_COLLECTION
    }

    private fun _syncReviewDetails() {
        formExpiry = "2026-12-31"
        formDamageDesc = if (voiceSummary.isNotEmpty()) voiceSummary else "Localized front bumper offset impact."
    }

    fun addCapturedImage(uri: String) {
        capturedImages.add(uri)
    }

    fun triggerOcrExtraction() {
        viewModelScope.launch {
            currentStep = AppStep.ANALYSIS_COMPILING
            delay(2500) // Simulated Gemma/OCR local visual processor wait
            currentStep = AppStep.REVIEWS
        }
    }

    fun performBackendSubmission() {
        currentStep = AppStep.STREAMING_LEDGER
        streamIsRunning = true
        sseEvents.clear()
        streamFinalResult = null

        val rId = "claim-${(100000..999999).random()}"
        claimId = rId

        viewModelScope.launch {
            // Emulate progressive streaming logs from our FastAPI engine over local channels
            _triggerMockSseStream()
        }
    }

    private suspend fun _triggerMockSseStream() {
        // Step 1: status initial receipt
        sseEvents.add(SseEvent("status", SseEventData(text = "FNOL packet received for validation. Initialized ledger routing ID $claimId.")))
        delay(2000)

        // Step 2: fraud_check
        val isHighRisk = formLicense == "DL-66666666" || formExpiry == "2025-05-10"
        val score = if (isHighRisk) 85 else 10
        val notes = if (isHighRisk) listOf("Driver's license registry indicates SUSPENDED state.") else emptyList()
        sseEvents.add(SseEvent("fraud_check", SseEventData(text = "Registry audit completed. Risk profile compiled against license indexes.", risk_score = score, risk_notes = notes)))
        delay(2200)

        // Step 3: policy_check
        sseEvents.add(SseEvent("policy_check", SseEventData(text = "Carrier registry database verified. Policy accepted for processing.")))
        delay(1800)

        // Step 4: a2a_submitted
        sseEvents.add(SseEvent("a2a_submitted", SseEventData(text = "FNOL XML structural envelope successfully matched with carrier endpoint.")))
        delay(1500)

        // Step 5: email_sent
        sseEvents.add(SseEvent("email_sent", SseEventData(text = "MIME HTML loss summary email dispatched to hironmoy@gmail.com.")))
        delay(1500)

        // Step 6: final compiler result state check
        val result = SseEventData(
            claim_number = "CR-2026-${(10000..99999).random()}",
            approved = !isHighRisk,
            insurer_email = "hironmoy@gmail.com",
            summary = if (!isHighRisk) "Claim registered. Assigned Claim File Number CR-2026." else "Elevated fraud scoring triggered manual claim hold. Registry indicators: Suspended status."
        )

        streamFinalResult = result
        sseEvents.add(SseEvent(if (!isHighRisk) "claim_filed" else "flagged", result))
        streamIsRunning = false
    }

    fun restartClaimWorkflow() {
        currentStep = AppStep.LANDING
        voiceSummary = ""
        capturedImages.clear()
        sseEvents.clear()
        streamFinalResult = null
    }

    fun handleBack() {
        when (currentStep) {
            AppStep.VOICE_GUIDE -> currentStep = AppStep.LANDING
            AppStep.ARTIFACT_COLLECTION -> currentStep = AppStep.VOICE_GUIDE
            AppStep.REVIEWS -> currentStep = AppStep.ARTIFACT_COLLECTION
            else -> {}
        }
    }
}

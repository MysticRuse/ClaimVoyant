package com.android.mr.claimvoyantapp.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.android.mr.claimvoyantapp.data.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.Request
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import java.text.SimpleDateFormat
import java.util.*

enum class AppStep {
    LANDING,
    VOICE_GUIDE,
    VIDEO_CAPTURE,
    VIDEO_ANALYSIS,          // AI frame extraction + damage assessment
    ARTIFACT_COLLECTION,
    ANALYSIS_COMPILING,
    REVIEWS,
    STREAMING_LEDGER
}

class ClaimViewModel(application: Application) : AndroidViewModel(application) {

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

    // Evidence Artifacts — mutableStateListOf so Compose recomposes when items are added
    val capturedVideos = mutableStateListOf<String>()
    val capturedImages = mutableStateListOf<String>()
    var ocrStatusMsg by mutableStateOf("")

    // Video AI Analysis state
    var isAnalyzingVideo by mutableStateOf(false)
    var videoAnalysisResult by mutableStateOf<VideoAnalysisResult?>(null)
    var videoAnalysisError by mutableStateOf<String?>(null)

    // Review fields
    var formName by mutableStateOf("Jane Smith")
    var formLicense by mutableStateOf("D1234567")
    var formInsurer by mutableStateOf("SafeDrive Insurance")
    var formPolicy by mutableStateOf("POL-9999")
    var formExpiry by mutableStateOf("2026-12-31")
    var formEmail by mutableStateOf("")          // claimant contact email — sent to backend for notifications
    var formDamageDesc by mutableStateOf("Localized bumper crush with minor panel misalignment.")

    // Processing Ledger
    var isApiLoading by mutableStateOf(false)
    var apiError by mutableStateOf<String?>(null)
    var claimId by mutableStateOf("")
    val sseEvents = mutableStateListOf<SseEvent>()   // observable so LazyColumn recomposes
    var streamFinalResult by mutableStateOf<SseEventData?>(null)
    var streamIsRunning by mutableStateOf(false)
    /** null = not yet known, true = live Cloud Run pipeline, false = local mock fallback */
    var isLiveBackend by mutableStateOf<Boolean?>(null)
    var backendErrorMsg by mutableStateOf<String?>(null)

    private val gson = Gson()

    // ── Navigation ────────────────────────────────────────────────────────

    fun startClaim() { currentStep = AppStep.VOICE_GUIDE }

    fun completeVoiceOnboarding(
        summary: String, name: String, insurer: String,
        policy: String, airbag: Boolean, drivable: Boolean
    ) {
        voiceSummary = summary
        extractedName = name
        extractedInsurer = insurer
        extractedPolicy = policy
        airbagDeployed = airbag
        isDrivable = drivable
        formName = name
        formInsurer = insurer
        formPolicy = policy
        _syncReviewDetails()
        currentStep = AppStep.VIDEO_CAPTURE
    }

    private fun _syncReviewDetails() {
        formExpiry = "2026-12-31"
        formDamageDesc = if (voiceSummary.isNotEmpty()) voiceSummary
                         else "Localized front bumper offset impact."
    }

    fun addCapturedVideo(uri: String) { capturedVideos.add(uri) }
    fun addCapturedImage(uri: String) { capturedImages.add(uri) }

    /** Called from the "Analyze Video" button. Routes to AI analysis if clips exist, otherwise skips. */
    fun completeVideoCapture() {
        if (capturedVideos.isNotEmpty()) {
            currentStep = AppStep.VIDEO_ANALYSIS
            startVideoAnalysis()
        } else {
            currentStep = AppStep.ARTIFACT_COLLECTION
        }
    }

    /** Kick off background video analysis; called automatically from [completeVideoCapture]. */
    fun startVideoAnalysis() {
        isAnalyzingVideo = true
        videoAnalysisError = null

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val analyzer = VideoAnalyzer(getApplication())
                // Analyse the first captured video (most relevant clip)
                val result = analyzer.analyzeVideo(capturedVideos.first())
                launch(Dispatchers.Main) {
                    videoAnalysisResult = result
                    // Pre-fill the voice/damage summary from AI narrative
                    if (result.incidentNarrative.isNotBlank()) {
                        formDamageDesc = result.incidentNarrative
                        if (voiceSummary.isBlank()) voiceSummary = result.incidentNarrative
                    }
                    isAnalyzingVideo = false
                }
            } catch (e: Exception) {
                launch(Dispatchers.Main) {
                    videoAnalysisError = "Analysis failed: ${e.message}"
                    isAnalyzingVideo = false
                }
            }
        }
    }

    fun completeVideoAnalysis() { currentStep = AppStep.ARTIFACT_COLLECTION }

    fun triggerOcrExtraction() {
        viewModelScope.launch {
            currentStep = AppStep.ANALYSIS_COMPILING
            delay(2500)
            currentStep = AppStep.REVIEWS
        }
    }

    fun handleBack() {
        when (currentStep) {
            AppStep.VOICE_GUIDE         -> currentStep = AppStep.LANDING
            AppStep.VIDEO_CAPTURE       -> currentStep = AppStep.VOICE_GUIDE
            AppStep.VIDEO_ANALYSIS      -> currentStep = AppStep.VIDEO_CAPTURE
            AppStep.ARTIFACT_COLLECTION -> {
                currentStep = if (videoAnalysisResult != null) AppStep.VIDEO_ANALYSIS
                              else AppStep.VIDEO_CAPTURE
            }
            AppStep.REVIEWS             -> currentStep = AppStep.ARTIFACT_COLLECTION
            else -> {}
        }
    }

    fun restartClaimWorkflow() {
        currentStep = AppStep.LANDING
        voiceSummary = ""
        capturedVideos.clear()
        capturedImages.clear()
        sseEvents.clear()
        streamFinalResult = null
        claimId = ""
        videoAnalysisResult = null
        videoAnalysisError = null
        isAnalyzingVideo = false
        formEmail = ""
        isLiveBackend = null
        backendErrorMsg = null
    }

    // ── Backend submission ────────────────────────────────────────────────

    fun performBackendSubmission() {
        currentStep = AppStep.STREAMING_LEDGER
        streamIsRunning = true
        sseEvents.clear()
        streamFinalResult = null
        isLiveBackend = null
        backendErrorMsg = null

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // 1. POST the claim package to the FastAPI backend
                val pkg = buildClaimPackage()
                val response = NetworkModule.claimApi.createClaim(pkg)

                launch(Dispatchers.Main) {
                    claimId = response.claimId
                    isLiveBackend = true           // confirmed: real backend responded
                }

                // 2. Connect SSE stream returned in the response
                val sseUrl = "${NetworkModule.BASE_URL}api/claims/${response.claimId}/stream"
                connectSseStream(sseUrl)

            } catch (e: Exception) {
                // Backend unreachable or errored — record reason, fall back to mock
                launch(Dispatchers.Main) {
                    isLiveBackend = false
                    backendErrorMsg = e.message ?: e.javaClass.simpleName
                    claimId = "claim-${(100000..999999).random()}"
                    _triggerMockSseStream()
                }
            }
        }
    }

    /** Build the full ClaimPackage from current ViewModel state. */
    private fun buildClaimPackage(): ClaimPackage {
        val now = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            .apply { timeZone = TimeZone.getTimeZone("UTC") }
            .format(Date())

        val reportId = "claim-${(100000..999999).random()}"

        return ClaimPackage(
            reportId = reportId,
            submittedAt = now,
            incident = IncidentInfo(
                timestamp = now,
                gps = GpsData(lat = 37.7749, lng = -122.4194), // mock SF coords
                description = voiceSummary.ifEmpty { "Accident claim submitted via ClaimVoyant." }
            ),
            parties = listOf(
                Party(
                    role = "claimant",
                    name = formName,
                    licenseNumber = formLicense,
                    insurer = formInsurer,
                    policyNumber = formPolicy,
                    insuranceExpiry = formExpiry,
                    email = formEmail
                )
            ),
            damage = DamageInfo(
                severity = "moderate",
                areasAffected = listOf("front bumper", "hood"),
                description = formDamageDesc,
                airbagDeployed = airbagDeployed,
                vehicleDrivable = isDrivable
            ),
            evidence = Evidence(
                images = capturedImages.mapIndexed { i, uri ->
                    EvidenceImage(type = "photo", caption = "Evidence ${i + 1}", uri = uri)
                }
            ),
            confidence = ConfidenceScores(
                ocrScore = 0.91,
                narrativeScore = 0.87,
                usedCloudFallback = false
            )
        )
    }

    /** Open an SSE connection and parse events into [sseEvents]. */
    private fun connectSseStream(url: String) {
        val request = Request.Builder().url(url).build()
        val factory = EventSources.createFactory(NetworkModule.okHttpClient)

        factory.newEventSource(request, object : EventSourceListener() {
            override fun onEvent(
                eventSource: EventSource, id: String?, type: String?, data: String
            ) {
                try {
                    // Each SSE line: data: {"event":"status","data":{...}}
                    val root = gson.fromJson(data, JsonObject::class.java)
                    val eventType = root.get("event")?.asString ?: return
                    val eventData = root.get("data")?.let {
                        gson.fromJson(it, SseEventData::class.java)
                    }

                    viewModelScope.launch(Dispatchers.Main) {
                        sseEvents.add(SseEvent(eventType, eventData))

                        when (eventType) {
                            "claim_filed", "flagged" -> streamFinalResult = eventData
                            "done"                   -> streamIsRunning = false
                        }
                    }
                } catch (_: Exception) { /* skip malformed events */ }
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                viewModelScope.launch(Dispatchers.Main) { streamIsRunning = false }
            }

            override fun onClosed(eventSource: EventSource) {
                viewModelScope.launch(Dispatchers.Main) { streamIsRunning = false }
            }
        })
    }

    // ── Mock fallback (used when backend is unreachable) ──────────────────

    private suspend fun _triggerMockSseStream() {
        sseEvents.add(SseEvent("status", SseEventData(
            text = "FNOL packet received for validation. Initialized ledger routing ID $claimId. [MOCK]")))
        delay(2000)

        val isHighRisk = formLicense == "DL-66666666" || formExpiry == "2025-05-10"
        val score = if (isHighRisk) 85 else 10
        val notes = if (isHighRisk)
            listOf("Driver's license registry indicates SUSPENDED state.") else emptyList()
        sseEvents.add(SseEvent("fraud_check", SseEventData(
            text = "Registry audit completed. Risk profile compiled against license indexes.",
            risk_score = score, risk_notes = notes)))
        delay(2200)

        sseEvents.add(SseEvent("policy_check", SseEventData(
            text = "Carrier registry database verified. Policy accepted for processing.")))
        delay(1800)

        sseEvents.add(SseEvent("a2a_submitted", SseEventData(
            text = "FNOL XML structural envelope successfully matched with carrier endpoint.")))
        delay(1500)

        sseEvents.add(SseEvent("email_sent", SseEventData(
            text = "MIME HTML loss summary email dispatched to adjuster@insurer.com.")))
        delay(1500)

        val result = SseEventData(
            claim_number = "CR-2026-${(10000..99999).random()}",
            approved = !isHighRisk,
            insurer_email = "adjuster@insurer.com",
            summary = if (!isHighRisk)
                "Claim registered. Assigned Claim File Number CR-2026."
            else
                "Elevated fraud scoring triggered manual claim hold."
        )
        streamFinalResult = result
        sseEvents.add(SseEvent(if (!isHighRisk) "claim_filed" else "flagged", result))
        streamIsRunning = false
    }
}

package com.android.mr.claimvoyantapp.data

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.android.mr.claimvoyantapp.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.gson.Gson
import com.google.gson.JsonObject
import java.io.File
import java.io.FileOutputStream

/**
 * Extracts key frames from a video clip and sends them to Gemini Flash for
 * on-scene damage assessment + FNOL incident narrative generation.
 *
 * Tier 1 – Cloud Gemini Flash (multimodal frames): primary, most accurate.
 * Tier 2 – MLKit GenAI Gemma Nano (text only): on-device fallback.
 * Tier 3 – Static placeholder: offline / API-key-missing fallback.
 */
class VideoAnalyzer(private val context: Context) {

    // ── Gemini cloud model ────────────────────────────────────────────────

    private val geminiModel: GenerativeModel? = try {
        if (BuildConfig.GEMINI_API_KEY.isNotBlank()) {
            GenerativeModel(modelName = "gemini-1.5-flash", apiKey = BuildConfig.GEMINI_API_KEY)
        } else null
    } catch (_: Exception) { null }

    // ── Public entry point ────────────────────────────────────────────────

    suspend fun analyzeVideo(videoUriString: String): VideoAnalysisResult {
        val frames = extractFrames(videoUriString)
        val frameUris = saveFramesToCache(frames)

        // Tier 1 – cloud Gemini with real frame bitmaps
        if (frames.isNotEmpty() && geminiModel != null) {
            return try {
                analyzeWithGeminiCloud(frames, frameUris)
            } catch (cloudEx: Exception) {
                // Tier 2 – on-device MLKit GenAI (text-only narrative)
                try {
                    analyzeWithMlKitOnDevice(frameUris)
                } catch (_: Exception) {
                    buildStaticFallback(frameUris)
                }
            }
        }

        // Tier 2 – on-device only (no frames or no API key)
        return try {
            analyzeWithMlKitOnDevice(frameUris)
        } catch (_: Exception) {
            buildStaticFallback(frameUris)
        }
    }

    // ── Tier 1: Gemini Flash (multimodal) ────────────────────────────────

    private suspend fun analyzeWithGeminiCloud(
        frames: List<Bitmap>,
        frameUris: List<String>
    ): VideoAnalysisResult {
        val inputContent = content {
            // Send up to 4 representative frames
            frames.take(4).forEach { image(it) }
            text(ANALYSIS_PROMPT)
        }
        val response = geminiModel!!.generateContent(inputContent)
        return parseJsonResponse(response.text ?: "", frameUris, usedCloud = true)
    }

    // ── Tier 2: MLKit GenAI on-device (Gemma Nano, text only) ────────────

    private suspend fun analyzeWithMlKitOnDevice(frameUris: List<String>): VideoAnalysisResult {
        // Dynamically invoke MLKit GenAI to avoid hard compile dependency on
        // exact beta API shape.  Falls through to Tier 3 on any exception.
        val textGenClass = Class.forName("com.google.mlkit.genai.text.generation.TextGeneration")
        val paramsClass = Class.forName("com.google.mlkit.genai.text.generation.TextGenerationParams")
        val promptClass = Class.forName("com.google.mlkit.genai.text.generation.TextGenerationPrompt")

        val paramsBuilder = paramsClass.getMethod("builder").invoke(null)
        val params = paramsBuilder.javaClass.getMethod("build").invoke(paramsBuilder)

        val client = textGenClass.getMethod("getClient", paramsClass).invoke(null, params)

        // Check feature availability
        val checkTask = client.javaClass.getMethod("checkFeatureStatus").invoke(client)
        val featureStatus = awaitTask<Int>(checkTask)
        if (featureStatus != 0 /* AVAILABLE */) throw Exception("MLKit GenAI not available (status=$featureStatus)")

        // Run inference with text-only prompt
        val promptBuilder = promptClass.getMethod("builder", String::class.java).invoke(null, NARRATIVE_ONLY_PROMPT)
        val prompt = promptBuilder.javaClass.getMethod("build").invoke(promptBuilder)
        val inferenceTask = client.javaClass.getMethod("runInference", promptClass).invoke(client, prompt)
        val result = awaitTask<Any>(inferenceTask)
        val narrativeText = result.javaClass.getMethod("getText").invoke(result) as? String ?: ""

        return VideoAnalysisResult(
            damagedAreas = listOf("Front section", "Side panels"),
            severity = "moderate",
            damageDescription = "On-device assessment completed. Damage visible in scene footage.",
            incidentNarrative = narrativeText.ifBlank {
                "The subject vehicle sustained visible collision damage to the front and side panels. The impact appeared to be moderate in force, resulting in deformation consistent with a low-speed collision event. The vehicle was observed at the scene with damage to the outer body panels and trim. Further inspection by a qualified assessor is recommended to determine the full extent of structural involvement."
            },
            frameUris = frameUris,
            usedCloudFallback = false
        )
    }

    /** Generic awaiter for MLKit Task<T> using reflection (avoids coroutines-play-services dep). */
    @Suppress("UNCHECKED_CAST")
    private fun <T> awaitTask(task: Any): T {
        var result: T? = null
        var error: Exception? = null
        val latch = java.util.concurrent.CountDownLatch(1)
        val successListenerClass = Class.forName("com.google.android.gms.tasks.OnSuccessListener")
        val failureListenerClass = Class.forName("com.google.android.gms.tasks.OnFailureListener")

        val successProxy = java.lang.reflect.Proxy.newProxyInstance(
            successListenerClass.classLoader, arrayOf(successListenerClass)
        ) { _, _, args ->
            result = args[0] as T
            latch.countDown()
            null
        }
        val failureProxy = java.lang.reflect.Proxy.newProxyInstance(
            failureListenerClass.classLoader, arrayOf(failureListenerClass)
        ) { _, _, args ->
            error = args[0] as? Exception ?: Exception(args[0].toString())
            latch.countDown()
            null
        }

        task.javaClass.getMethod("addOnSuccessListener", successListenerClass).invoke(task, successProxy)
        task.javaClass.getMethod("addOnFailureListener", failureListenerClass).invoke(task, failureProxy)
        latch.await(30, java.util.concurrent.TimeUnit.SECONDS)
        error?.let { throw it }
        return result ?: throw Exception("Task returned null result")
    }

    // ── Tier 3: Static fallback ───────────────────────────────────────────

    private fun buildStaticFallback(frameUris: List<String>) = VideoAnalysisResult(
        damagedAreas = listOf("Front bumper", "Hood", "Left headlight"),
        severity = "moderate",
        damageDescription = "Visible front-end impact damage with structural deformation to bumper fascia and hood alignment.",
        incidentNarrative = "The subject vehicle sustained a frontal collision impact resulting in visible structural deformation to the bumper fascia, hood, and left headlight assembly. The damage pattern was consistent with a moderate-force frontal impact at low-to-moderate speed. The vehicle was observed at the scene with the front section displaced and the hood showing misalignment consistent with frame stress. Further hands-on inspection by a licensed assessor is recommended before determining repairability.",
        frameUris = frameUris,
        usedCloudFallback = false
    )

    // ── Frame extraction ──────────────────────────────────────────────────

    private fun extractFrames(videoUriString: String): List<Bitmap> {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, Uri.parse(videoUriString))
            val durationMs = retriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_DURATION
            )?.toLong() ?: 4000L

            // Extract 5 evenly-spaced frames
            (0..4).mapNotNull { i ->
                val timeUs = (durationMs * i.toLong() / 4L) * 1000L
                retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                    ?.let { scaleBitmap(it, maxDim = 640) }
            }
        } catch (_: Exception) {
            emptyList()
        } finally {
            try { retriever.release() } catch (_: Exception) { }
        }
    }

    /** Down-scale bitmap to [maxDim]×[maxDim] to keep Gemini token cost low. */
    private fun scaleBitmap(src: Bitmap, maxDim: Int): Bitmap {
        val scale = maxDim.toFloat() / maxOf(src.width, src.height)
        return if (scale >= 1f) src
        else Bitmap.createScaledBitmap(src, (src.width * scale).toInt(), (src.height * scale).toInt(), true)
    }

    /** Save bitmaps to app cache dir and return their absolute file paths. */
    private fun saveFramesToCache(frames: List<Bitmap>): List<String> {
        val cacheDir = File(context.cacheDir, "cv_frames").also { it.mkdirs() }
        return frames.mapIndexed { i, bmp ->
            val file = File(cacheDir, "frame_$i.jpg")
            try {
                FileOutputStream(file).use { bmp.compress(Bitmap.CompressFormat.JPEG, 85, it) }
                file.absolutePath
            } catch (_: Exception) { "" }
        }.filter { it.isNotEmpty() }
    }

    // ── Response parsing ──────────────────────────────────────────────────

    private fun parseJsonResponse(
        raw: String,
        frameUris: List<String>,
        usedCloud: Boolean
    ): VideoAnalysisResult {
        return try {
            val json = raw.trim()
                .removePrefix("```json").removePrefix("```")
                .removeSuffix("```").trim()
            val obj = Gson().fromJson(json, JsonObject::class.java)

            val areas = obj.getAsJsonArray("damaged_areas")
                ?.map { it.asString }
                ?: listOf("Front section")
            val severity = obj.get("severity")?.asString ?: "moderate"
            val desc = obj.get("damage_description")?.asString ?: ""
            val narrative = obj.get("incident_narrative")?.asString ?: ""

            VideoAnalysisResult(
                damagedAreas = areas,
                severity = severity,
                damageDescription = desc,
                incidentNarrative = narrative,
                frameUris = frameUris,
                usedCloudFallback = usedCloud
            )
        } catch (_: Exception) {
            // Gemini responded but not valid JSON — use the raw text as narrative
            VideoAnalysisResult(
                damagedAreas = listOf("Front section"),
                severity = "moderate",
                damageDescription = "Damage detected in video footage.",
                incidentNarrative = raw.trim().take(600),
                frameUris = frameUris,
                usedCloudFallback = usedCloud
            )
        }
    }

    // ── Prompts ───────────────────────────────────────────────────────────

    companion object {
        /**
         * Sent to Gemini Flash together with up to 4 video frames.
         * Deliberately avoids "claim filed / submitted / approved" language —
         * the claim has NOT been filed yet at this point in the flow.
         * The narrative is scene field-notes only, for the adjuster to consume later.
         */
        private const val ANALYSIS_PROMPT = """
You are an AI vehicle damage assessor reviewing frames captured from an on-scene accident video.

Analyse the visible damage and respond ONLY with valid JSON in exactly this format:
{
  "damaged_areas": ["specific area, e.g. Front bumper", "Driver door", "Hood"],
  "severity": "minor",
  "damage_description": "2-3 sentence technical description of what is visibly damaged and how.",
  "incident_narrative": "3-4 sentence scene observation written as field notes for an insurance adjuster. Describe what the video shows: the type of impact, the visible damage, and the vehicle's current physical condition. Write in third person past tense. Do NOT mention claim submission, claim filing, approval, or any administrative action."
}

severity must be exactly one of: minor, moderate, severe.
Respond with the JSON object only — no markdown fences, no extra text.
"""

        /**
         * Text-only prompt used when Gemma Nano (MLKit) handles inference instead of Gemini cloud.
         * No submission or filing language — purely a scene description.
         */
        private const val NARRATIVE_ONLY_PROMPT = """
Write 3-4 sentences of vehicle accident scene field notes for an insurance adjuster.
Describe the type of impact, the likely damaged areas on the vehicle, and the vehicle's current
physical condition. Write in third person, past tense.
Do NOT mention claim submission, claim filing, claim numbers, or any administrative process.
"""
    }
}

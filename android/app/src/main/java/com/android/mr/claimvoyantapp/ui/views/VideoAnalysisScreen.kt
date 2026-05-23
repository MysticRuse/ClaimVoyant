package com.android.mr.claimvoyantapp.ui.views

import android.speech.tts.TextToSpeech
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.android.mr.claimvoyantapp.data.VideoAnalysisResult
import com.android.mr.claimvoyantapp.ui.ClaimViewModel
import com.android.mr.claimvoyantapp.ui.theme.CanvasBackground
import com.android.mr.claimvoyantapp.ui.theme.SolidBorder
import java.util.Locale

@Composable
fun VideoAnalysisScreen(
    viewModel: ClaimViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // ── Text-to-Speech engine ─────────────────────────────────────────────
    var ttsReady by remember { mutableStateOf(false) }
    var isSpeaking by remember { mutableStateOf(false) }
    val tts = remember {
        TextToSpeech(context) { status ->
            ttsReady = (status == TextToSpeech.SUCCESS)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    // ── Layout ────────────────────────────────────────────────────────────
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CanvasBackground)
            .padding(24.dp)
    ) {
        // Header
        Text(
            text = "STEP 3 of 6",
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = Color.Gray,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "AI Damage Assessment",
            fontSize = 28.sp,
            fontFamily = FontFamily.Serif,
            fontStyle = FontStyle.Italic,
            color = Color(0xFF1A1A1A),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (viewModel.isAnalyzingVideo) {
            // ── Analyzing state ───────────────────────────────────────────
            AnalyzingCard()
        } else if (viewModel.videoAnalysisError != null && viewModel.videoAnalysisResult == null) {
            // ── Error state ───────────────────────────────────────────────
            ErrorCard(errorMsg = viewModel.videoAnalysisError!!)
            Spacer(modifier = Modifier.height(16.dp))
            ContinueButton(viewModel)
        } else {
            // ── Results state ─────────────────────────────────────────────
            val result = viewModel.videoAnalysisResult

            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {

                // Frame strip
                if (result != null && result.frameUris.isNotEmpty()) {
                    Text(
                        text = "Extracted Frames (${result.frameUris.size})",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    FrameStrip(frameUris = result.frameUris)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Source badge
                if (result != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        SourceBadge(usedCloud = result.usedCloudFallback)
                    }
                }

                // Damage Assessment card
                if (result != null) {
                    DamageAssessmentCard(result = result)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Incident Narrative card
                if (result != null) {
                    NarrativeCard(
                        narrative = result.incidentNarrative,
                        isSpeaking = isSpeaking,
                        ttsReady = ttsReady,
                        onPlayPause = {
                            if (isSpeaking) {
                                tts.stop()
                                isSpeaking = false
                            } else {
                                tts.language = Locale.US
                                tts.speak(
                                    result.incidentNarrative,
                                    TextToSpeech.QUEUE_FLUSH,
                                    null,
                                    "narrative_tts"
                                )
                                isSpeaking = true
                                // Auto-clear speaking state when done
                                tts.setOnUtteranceProgressListener(
                                    object : android.speech.tts.UtteranceProgressListener() {
                                        override fun onStart(utteranceId: String?) {}
                                        override fun onDone(utteranceId: String?) { isSpeaking = false }
                                        @Deprecated("Deprecated in Java")
                                        override fun onError(utteranceId: String?) { isSpeaking = false }
                                    }
                                )
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // No-result placeholder (shouldn't normally be reached)
                if (result == null) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .border(1.dp, Color.LightGray)
                    ) {
                        Text(
                            text = "No analysis result available.",
                            color = Color.Gray,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { viewModel.handleBack() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier.border(1.dp, SolidBorder)
                ) {
                    Text("Back", fontFamily = FontFamily.Monospace)
                }
                ContinueButton(viewModel)
            }
        }
    }
}

// ── Sub-composables ───────────────────────────────────────────────────────

@Composable
private fun AnalyzingCard() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(800, easing = EaseInOutSine),
            RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, SolidBorder),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(color = SolidBorder, strokeWidth = 2.dp)

            Text(
                text = "[ GEMINI VISION ANALYSIS RUNNING ]",
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2563EB).copy(alpha = alpha)
            )
            Text(
                text = "Extracting key frames · Classifying damage areas · Generating FNOL narrative",
                fontSize = 12.sp,
                color = Color.Gray,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun FrameStrip(frameUris: List<String>) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(frameUris) { path ->
            AsyncImage(
                model = java.io.File(path),
                contentDescription = "Video frame",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(100.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(4.dp))
                    .border(1.dp, SolidBorder)
            )
        }
    }
}

@Composable
private fun SourceBadge(usedCloud: Boolean) {
    val (label, bg, fg) = if (usedCloud)
        Triple("☁ Gemini Flash (Cloud)", Color(0xFFEFF6FF), Color(0xFF1D4ED8))
    else
        Triple("◈ Gemma Nano (On-Device)", Color(0xFFF0FDF4), Color(0xFF15803D))

    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(4.dp))
            .border(0.5.dp, fg.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(label, fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = fg, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DamageAssessmentCard(result: VideoAnalysisResult) {
    val severityColor = when (result.severity.lowercase()) {
        "severe" -> Color(0xFFEF4444)
        "moderate" -> Color(0xFFF97316)
        else -> Color(0xFF10B981)
    }
    val severityBg = when (result.severity.lowercase()) {
        "severe" -> Color(0xFFFEF2F2)
        "moderate" -> Color(0xFFFFF7ED)
        else -> Color(0xFFECFDF5)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, severityColor.copy(alpha = 0.5f)),
        colors = CardDefaults.cardColors(containerColor = severityBg)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DAMAGE ASSESSMENT",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
                Box(
                    modifier = Modifier
                        .background(severityColor, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = result.severity.uppercase(),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Areas affected
            Text(
                text = "Areas Affected",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            result.damagedAreas.forEach { area ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(severityColor, RoundedCornerShape(3.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = area, fontSize = 13.sp, color = Color.Black)
                }
            }

            if (result.damageDescription.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Technical Notes",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = result.damageDescription,
                    fontSize = 13.sp,
                    color = Color.Black,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
private fun NarrativeCard(
    narrative: String,
    isSpeaking: Boolean,
    ttsReady: Boolean,
    onPlayPause: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SolidBorder),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SCENE FIELD NOTES",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Text(
                        text = "AI-observed damage description · not a claim",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color.LightGray
                    )
                }
                // TTS play/stop button
                IconButton(
                    onClick = onPlayPause,
                    enabled = ttsReady && narrative.isNotBlank(),
                    modifier = Modifier
                        .size(36.dp)
                        .border(
                            1.dp,
                            if (isSpeaking) Color(0xFF2563EB) else SolidBorder,
                            RoundedCornerShape(4.dp)
                        )
                        .background(
                            if (isSpeaking) Color(0xFFEFF6FF) else Color.Transparent,
                            RoundedCornerShape(4.dp)
                        )
                ) {
                    Text(
                        text = if (isSpeaking) "■" else "▶",
                        fontSize = 14.sp,
                        color = if (isSpeaking) Color(0xFF2563EB) else Color.DarkGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = narrative.ifBlank { "Narrative will be generated after analysis completes." },
                fontSize = 13.sp,
                color = if (narrative.isBlank()) Color.Gray else Color.Black,
                lineHeight = 22.sp,
                fontStyle = if (narrative.isBlank()) FontStyle.Italic else FontStyle.Normal
            )

            if (isSpeaking) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "● Reading narrative aloud...",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF2563EB),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ErrorCard(errorMsg: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFEF4444)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "ANALYSIS ERROR",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFEF4444)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = errorMsg, fontSize = 13.sp, color = Color.Black)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "You can continue to photo collection and submit the claim manually.",
                fontSize = 12.sp,
                color = Color.Gray,
                fontStyle = FontStyle.Italic
            )
        }
    }
}

@Composable
private fun ContinueButton(viewModel: ClaimViewModel) {
    Button(
        onClick = { viewModel.completeVideoAnalysis() },
        enabled = !viewModel.isAnalyzingVideo,
        colors = ButtonDefaults.buttonColors(containerColor = SolidBorder)
    ) {
        Text(
            text = "Continue → Photo Artifacts",
            color = Color.White,
            fontFamily = FontFamily.Monospace
        )
    }
}

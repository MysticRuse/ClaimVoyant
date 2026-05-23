package com.android.mr.claimvoyantapp.ui.views

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.android.mr.claimvoyantapp.ui.ClaimViewModel
import com.android.mr.claimvoyantapp.ui.theme.CanvasBackground
import com.android.mr.claimvoyantapp.ui.theme.SolidBorder
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceGuideScreen(
    viewModel: ClaimViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // ── State ──────────────────────────────────────────────────────────────
    // isPlaceholder = true  → showing the representative example text (italic/gray)
    // isPlaceholder = false → user has started recording or typing real content
    val isPlaceholder = remember { mutableStateOf(viewModel.voiceSummary.isEmpty()) }
    val descTextState = remember {
        mutableStateOf(viewModel.voiceSummary)   // empty on first visit; real text on return
    }
    var mockWaveScale by remember { mutableStateOf(1f) }

    // ── SpeechRecognizer (inline, no system dialog) ────────────────────────
    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }
    val recognizerIntent = remember {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Describe the accident scenario")
        }
    }

    DisposableEffect(Unit) {
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}

            // Partial results → replace placeholder immediately, show live transcript
            override fun onPartialResults(partialResults: Bundle?) {
                val partial = partialResults
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                if (!partial.isNullOrEmpty()) {
                    isPlaceholder.value = false
                    descTextState.value = partial
                }
            }

            // Final result → commit to ViewModel
            override fun onResults(results: Bundle?) {
                val text = results
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                if (!text.isNullOrEmpty()) {
                    isPlaceholder.value = false
                    descTextState.value = text
                    viewModel.voiceSummary = text
                }
                viewModel.voiceIsRecording = false
            }

            override fun onEndOfSpeech() {
                viewModel.voiceIsRecording = false
            }

            override fun onError(error: Int) {
                viewModel.voiceIsRecording = false
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        onDispose { speechRecognizer.destroy() }
    }

    // ── Runtime permission launcher ────────────────────────────────────────
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.voiceIsRecording = true
            speechRecognizer.startListening(recognizerIntent)
        }
    }

    // ── Waveform animation ─────────────────────────────────────────────────
    LaunchedEffect(viewModel.voiceIsRecording) {
        if (viewModel.voiceIsRecording) {
            while (viewModel.voiceIsRecording) {
                mockWaveScale = Random.nextFloat() * (1.8f - 1.0f) + 1.0f
                kotlinx.coroutines.delay(120)
            }
        } else {
            mockWaveScale = 1f
        }
    }

    // ── UI ─────────────────────────────────────────────────────────────────
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CanvasBackground)
            .padding(24.dp)
    ) {
        Text(
            text = "STEP 1 of 6",
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = Color.Gray,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "Describe Accident Scenario",
            fontSize = 28.sp,
            fontFamily = FontFamily.Serif,
            fontStyle = FontStyle.Italic,
            color = Color(0xFF1A1A1A),
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Text(
            text = "Use the voice dictation node or edit the narrative to seed the carrier pre-population layers.",
            fontSize = 14.sp,
            color = Color.DarkGray,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Waveform / mic controller card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, SolidBorder)
                .padding(vertical = 32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Mic Button
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            if (viewModel.voiceIsRecording) Color(0xFFEF4444)
                            else Color(0xFF1A1A1A)
                        )
                        .clickable {
                            if (viewModel.voiceIsRecording) {
                                // Stop recording
                                speechRecognizer.stopListening()
                                viewModel.voiceIsRecording = false
                            } else {
                                // Start recording — check permission first
                                if (ContextCompat.checkSelfPermission(
                                        context, Manifest.permission.RECORD_AUDIO
                                    ) == PackageManager.PERMISSION_GRANTED
                                ) {
                                    viewModel.voiceIsRecording = true
                                    speechRecognizer.startListening(recognizerIntent)
                                } else {
                                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                        }
                ) {
                    Text(
                        text = if (viewModel.voiceIsRecording) "Stop" else "Record",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (viewModel.voiceIsRecording)
                        "Empathetic Assistant is Listening..."
                    else
                        "Tap to dictate the accident scenario verbally",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Waveform bars
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.height(30.dp)
                ) {
                    for (i in 1..8) {
                        val ht = (10..40).random() * mockWaveScale
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 2.dp)
                                .width(3.dp)
                                .height(ht.dp)
                                .background(
                                    if (viewModel.voiceIsRecording) Color(0xFFEF4444)
                                    else Color.LightGray
                                )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Transcription field ────────────────────────────────────────────
        // Label row: field title + EXAMPLE badge when placeholder is active
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
        ) {
            Text(
                text = "Accident Narrative",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )
            if (isPlaceholder.value) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFFFFF7ED), RoundedCornerShape(4.dp))
                        .border(0.5.dp, Color(0xFFFBBF24), RoundedCornerShape(4.dp))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "EXAMPLE — speak or type to replace",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF92400E)
                    )
                }
            }
        }

        OutlinedTextField(
            // Show placeholder text when no real input yet; real text otherwise
            value = if (isPlaceholder.value) NARRATIVE_PLACEHOLDER else descTextState.value,
            onValueChange = { newVal ->
                isPlaceholder.value = false          // first keystroke exits placeholder mode
                descTextState.value = newVal
                viewModel.voiceSummary = newVal
            },
            placeholder = {
                Text(
                    "Tap Record and describe what happened…",
                    fontStyle = FontStyle.Italic,
                    color = Color.LightGray,
                    fontSize = 13.sp
                )
            },
            textStyle = TextStyle(
                // Italic + muted colour while example; normal black once real
                fontStyle  = if (isPlaceholder.value) FontStyle.Italic  else FontStyle.Normal,
                color      = if (isPlaceholder.value) Color(0xFF9CA3AF) else Color(0xFF111827),
                fontSize   = 13.sp,
                lineHeight = 20.sp
            ),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(if (isPlaceholder.value) Color(0xFFFAFAF8) else Color.White),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = if (isPlaceholder.value) Color(0xFFFBBF24) else SolidBorder,
                unfocusedBorderColor = if (isPlaceholder.value) Color(0xFFD1C4A0) else SolidBorder,
                focusedTextColor     = Color(0xFF111827),
                unfocusedTextColor   = Color(0xFF9CA3AF)
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

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

            Button(
                onClick = {
                    // Don't commit example placeholder text as the real summary
                    val summary = if (isPlaceholder.value) "" else descTextState.value
                    viewModel.completeVoiceOnboarding(
                        summary = summary,
                        name = "Jane Smith",
                        insurer = "SafeDrive Insurance",
                        policy = "POL-9999",
                        airbag = false,
                        drivable = true
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = SolidBorder)
            ) {
                Text(
                    "Process Scene & Confirm",
                    color = Color.White,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

/**
 * Representative sample narrative shown as a placeholder until the user
 * records or types their own description.  Styled italic + muted grey in
 * the field so it is visually distinct from real user input.
 */
private const val NARRATIVE_PLACEHOLDER =
    "Rear-end collision on the I-280 freeway exit ramp travelling at approximately " +
    "35 mph. The third-party vehicle changed lanes abruptly without signalling, " +
    "causing contact with the front bumper and hood of my vehicle. Airbags did not " +
    "deploy. Vehicle remains driveable with visible cosmetic damage to the front fascia " +
    "and bonnet. No injuries reported at scene. Other driver confirmed SafeDrive " +
    "Insurance coverage, policy number POL-9999."

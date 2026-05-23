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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
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
    // Connected to the ViewModel so the text survives screen navigation
    val descTextState = remember {
        mutableStateOf(
            viewModel.voiceSummary.ifEmpty {
                "Rear-end impact on freeway exit. Other driver merged abruptly. " +
                "Insurer matches SafeDrive policy POL-9999. Airbags did not deploy. Car is driveable."
            }
        )
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

            // Partial results → update text in real-time as user speaks
            override fun onPartialResults(partialResults: Bundle?) {
                val partial = partialResults
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                if (!partial.isNullOrEmpty()) {
                    descTextState.value = partial
                }
            }

            // Final result → commit to ViewModel
            override fun onResults(results: Bundle?) {
                val text = results
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                if (!text.isNullOrEmpty()) {
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

        // Transcription text field — reads from and writes to ViewModel
        OutlinedTextField(
            value = descTextState.value,
            onValueChange = {
                descTextState.value = it
                viewModel.voiceSummary = it     // keep ViewModel in sync with manual edits
            },
            label = { Text("Transcribed Accident Narrative Summary") },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.White),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SolidBorder,
                unfocusedBorderColor = SolidBorder
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
                    viewModel.completeVoiceOnboarding(
                        summary = descTextState.value,
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

package com.android.mr.claimvoyantapp.ui.views

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.mr.claimvoyantapp.ui.ClaimViewModel
import com.android.mr.claimvoyantapp.ui.theme.CanvasBackground
import com.android.mr.claimvoyantapp.ui.theme.SolidBorder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    viewModel: ClaimViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    // ── Email speech-to-text ──────────────────────────────────────────────
    var isListeningEmail by remember { mutableStateOf(false) }
    var emailRawTranscript by remember { mutableStateOf("") }

    val emailRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }

    DisposableEffect(Unit) {
        emailRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partial: Bundle?) {
                // Show live partial transcription in the field while speaking
                val live = partial?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull() ?: return
                emailRawTranscript = live
                viewModel.formEmail = normalizeSpokenEmail(live)
            }
            override fun onResults(results: Bundle) {
                val final = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull() ?: ""
                viewModel.formEmail = normalizeSpokenEmail(final)
                emailRawTranscript = ""
                isListeningEmail = false
            }
            override fun onError(error: Int) {
                emailRawTranscript = ""
                isListeningEmail = false
            }
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        onDispose {
            emailRecognizer.cancel()
            emailRecognizer.destroy()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CanvasBackground)
            .padding(24.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "STEP 5 of 6",
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = Color.Gray,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "Review Parsed Records",
            fontSize = 28.sp,
            fontFamily = FontFamily.Serif,
            fontStyle = FontStyle.Italic,
            color = Color(0xFF1A1A1A),
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Text(
            text = "OCR scans and verbal algorithms have integrated into the editable fields below. Verify accuracy before transmitting to Cloud Run pipelines.",
            fontSize = 14.sp,
            color = Color.DarkGray,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Card containing Policy details
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SolidBorder)
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "01. Policy Party Information",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = viewModel.formName,
                    onValueChange = { viewModel.formName = it },
                    label = { Text("Full Legal Name") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SolidBorder)
                )

                OutlinedTextField(
                    value = viewModel.formEmail,
                    onValueChange = { viewModel.formEmail = it },
                    label = { Text("Contact Email") },
                    placeholder = {
                        Text(
                            text = if (isListeningEmail) "Listening… say your email address"
                                   else "you@example.com",
                            color = if (isListeningEmail) Color(0xFF2563EB) else Color.LightGray
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    trailingIcon = {
                        // Tap once → start listening; tap again → cancel
                        IconButton(
                            onClick = {
                                if (isListeningEmail) {
                                    emailRecognizer.cancel()
                                    isListeningEmail = false
                                } else {
                                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
                                        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                                        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                                        // Prompt shown on some devices' built-in UI (ignored on others)
                                        putExtra(RecognizerIntent.EXTRA_PROMPT,
                                            "Say your email — e.g. \"john dot smith at gmail dot com\"")
                                    }
                                    isListeningEmail = true
                                    emailRecognizer.startListening(intent)
                                }
                            }
                        ) {
                            Text(
                                text = if (isListeningEmail) "■" else "🎙",
                                fontSize = 18.sp,
                                color = if (isListeningEmail) Color(0xFF2563EB) else Color.DarkGray
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp)
                        .then(
                            if (isListeningEmail)
                                Modifier.border(1.5.dp, Color(0xFF2563EB))
                            else Modifier
                        ),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SolidBorder)
                )
                // Hint row below the email field
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Text(
                        text = if (isListeningEmail)
                            "● Listening — say e.g. \"jane dot doe at gmail dot com\""
                        else
                            "Tap 🎙 to dictate, or type. Updates sent to this address.",
                        fontSize = 10.sp,
                        color = if (isListeningEmail) Color(0xFF2563EB) else Color.Gray,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = if (isListeningEmail) FontWeight.Bold else FontWeight.Normal
                    )
                }

                OutlinedTextField(
                    value = viewModel.formLicense,
                    onValueChange = { viewModel.formLicense = it },
                    label = { Text("Driver's License ID") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SolidBorder)
                )
                Text(
                    text = "Use 'DL-66666666' to test high fraud risk scoring routes.",
                    fontSize = 10.sp,
                    color = Color.Gray,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = viewModel.formInsurer,
                    onValueChange = { viewModel.formInsurer = it },
                    label = { Text("Insurance Carrier") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SolidBorder)
                )

                OutlinedTextField(
                    value = viewModel.formPolicy,
                    onValueChange = { viewModel.formPolicy = it },
                    label = { Text("Policy ID Number") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SolidBorder)
                )

                OutlinedTextField(
                    value = viewModel.formExpiry,
                    onValueChange = { viewModel.formExpiry = it },
                    label = { Text("Expiry Date") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SolidBorder)
                )
                Text(
                    text = "Use expiry date in the past (e.g. '2025-05-10') to trigger compliance checks.",
                    fontSize = 10.sp,
                    color = Color.Gray,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }

        // Damage Assessor Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SolidBorder)
                .padding(bottom = 24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "02. On-Device Classification Metrics",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.weight(1f).background(CanvasBackground).padding(8.dp)) {
                        Column {
                            Text("Assesses Severity", fontSize = 9.sp, color = Color.Gray, fontFamily = FontFamily.Monospace)
                            Text("MODERATE", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Box(modifier = Modifier.weight(1f).background(CanvasBackground).padding(8.dp)) {
                        Column {
                            Text("Airbag State", fontSize = 9.sp, color = Color.Gray, fontFamily = FontFamily.Monospace)
                            Text(if (viewModel.airbagDeployed) "DEPLOYED" else "OK MODEL", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                OutlinedTextField(
                    value = viewModel.formDamageDesc,
                    onValueChange = { viewModel.formDamageDesc = it },
                    label = { Text("Damage Notes Tracker") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SolidBorder)
                )
            }
        }

        // Navigation bottom
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { viewModel.handleBack() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.Black),
                modifier = Modifier.border(1.dp, SolidBorder)
            ) {
                Text("Back", fontFamily = FontFamily.Monospace)
            }

            Button(
                onClick = { viewModel.performBackendSubmission() },
                colors = ButtonDefaults.buttonColors(containerColor = SolidBorder)
            ) {
                Text("File Claim & Transmit", color = Color.White, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

/**
 * Converts a spoken email string into a valid email address format.
 *
 * Examples:
 *   "john dot smith at gmail dot com"  →  "john.smith@gmail.com"
 *   "jane underscore doe at yahoo dot co dot uk"  →  "jane_doe@yahoo.co.uk"
 *   "info dash team at acme dot io"  →  "info-team@acme.io"
 *
 * Word substitutions are applied in a deliberate order (at before dot)
 * so partial matches don't corrupt the result.
 */
private fun normalizeSpokenEmail(spoken: String): String {
    return spoken.trim().lowercase()
        // @ must come first so "at" inside domain words isn't mis-replaced later
        .replace(" at ", "@")
        // Symbol words
        .replace(" dot ", ".")
        .replace(" dash ", "-")
        .replace(" hyphen ", "-")
        .replace(" underscore ", "_")
        .replace(" plus ", "+")
        // Edge cases where the word runs into the boundary
        .replace("dot ", ".")
        .replace(" dot", ".")
        // Strip any leftover spaces (spaces between digits / letters spoken separately)
        .replace(" ", "")
}

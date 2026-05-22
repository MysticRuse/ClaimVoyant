package com.android.mr.claimvoyantapp.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.mr.claimvoyantapp.ui.ClaimViewModel
import com.android.mr.claimvoyantapp.ui.theme.CanvasBackground
import com.android.mr.claimvoyantapp.ui.theme.SolidBorder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceGuideScreen(
    viewModel: ClaimViewModel,
    modifier: Modifier = Modifier
) {
    var descText by remember { mutableStateOf("Rear-end impact on freeway exit. Other driver merged abruptly. Insurer matches SafeDrive policy POL-9999. Airbags did not deploy. Car is driveable.") }
    var mockWaveScale by remember { mutableStateOf(1f) }

    LaunchedEffect(viewModel.voiceIsRecording) {
        if (viewModel.voiceIsRecording) {
            while (viewModel.voiceIsRecording) {
                mockWaveScale = (1.0f..1.8f).random()
                kotlinx.coroutines.delay(120)
            }
        } else {
            mockWaveScale = 1.0f
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CanvasBackground)
            .padding(24.dp)
    ) {
        // Step header
        Text(
            text = "STEP 1 of 4",
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
                        .background(if (viewModel.voiceIsRecording) Color(0xFFEF4444) else Color(0xFF1A1A1A))
                        .clickable { viewModel.voiceIsRecording = !viewModel.voiceIsRecording }
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
                    text = if (viewModel.voiceIsRecording) "Empathetic Assistant is Listening..." else "Tap to dictation describe scenario verbally",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Microphone pulse indicator UI items
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
                                .background(if (viewModel.voiceIsRecording) Color(0xFFEF4444) else Color.LightGray)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Large text field
        OutlinedTextField(
            value = descText,
            onValueChange = { descText = it },
            label = { Text("Transcribed Accident Narrative Summary") },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.White),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = SolidBorder,
                unfocusedBorderColor = SolidBorder
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Nav actions bottom
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
                onClick = {
                    viewModel.completeVoiceOnboarding(
                        summary = descText,
                        name = "Jane Smith",
                        insurer = "SafeDrive Insurance",
                        policy = "POL-9999",
                        airbag = false,
                        drivable = true
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = SolidBorder)
            ) {
                Text("Process Scene & Confirm", color = Color.White, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

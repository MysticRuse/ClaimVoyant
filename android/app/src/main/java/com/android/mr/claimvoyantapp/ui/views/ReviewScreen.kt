package com.android.mr.claimvoyantapp.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CanvasBackground)
            .padding(24.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "STEP 3 of 4",
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
                    colors = TextFieldDefaults.outlinedTextFieldColors(focusedBorderColor = SolidBorder)
                )

                OutlinedTextField(
                    value = viewModel.formLicense,
                    onValueChange = { viewModel.formLicense = it },
                    label = { Text("Driver's License ID") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(focusedBorderColor = SolidBorder)
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
                    colors = TextFieldDefaults.outlinedTextFieldColors(focusedBorderColor = SolidBorder)
                )

                OutlinedTextField(
                    value = viewModel.formPolicy,
                    onValueChange = { viewModel.formPolicy = it },
                    label = { Text("Policy ID Number") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(focusedBorderColor = SolidBorder)
                )

                OutlinedTextField(
                    value = viewModel.formExpiry,
                    onValueChange = { viewModel.formExpiry = it },
                    label = { Text("Expiry Date") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(focusedBorderColor = SolidBorder)
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
                    colors = TextFieldDefaults.outlinedTextFieldColors(focusedBorderColor = SolidBorder)
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

package com.android.mr.claimvoyantapp.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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

@Composable
fun StreamingScreen(
    viewModel: ClaimViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CanvasBackground)
            .padding(24.dp)
    ) {
        // Timeline Header
        Text(
            text = "STEP 4 of 4",
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = Color.Gray,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "Claim Automation Ledger",
            fontSize = 28.sp,
            fontFamily = FontFamily.Serif,
            fontStyle = FontStyle.Italic,
            color = Color(0xFF1A1A1A),
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Text(
            text = if (viewModel.streamIsRunning) "● Live Pipeline Processing..." else "✓ Pipeline Assessment Completed",
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            color = if (viewModel.streamIsRunning) Color(0xFF2563EB) else Color(0xFF10B981),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Events Log list view
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(viewModel.sseEvents) { index, msg ->
                val isFraudAlert = msg.event == "fraud_check" && (msg.data?.risk_score ?: 0) > 50
                val bg = if (isFraudAlert) Color(0xFFFEF2F2) else Color.White
                val borderCol = if (isFraudAlert) Color(0xFFEF4444) else SolidBorder

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, borderCol),
                    colors = CardDefaults.cardColors(containerColor = bg)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Num
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(24.dp)
                                .background(if (isFraudAlert) Color(0xFFFEE2E2) else CanvasBackground)
                                .border(1.dp, SolidBorder)
                        ) {
                            Text((index + 1).toString(), fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = msg.event.replace("_", " ").uppercase(),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = msg.data?.text ?: "",
                                fontSize = 13.sp,
                                color = Color.Black
                            )

                            // Show sub fraud items
                            if (msg.event == "fraud_check" && msg.data != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF9FAFB))
                                        .border(0.5.dp, Color.LightGray)
                                        .padding(8.dp)
                                ) {
                                    Column {
                                        Text("Risk Score: ${msg.data.risk_score} / 100", fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                                        msg.data.risk_notes?.forEach { note ->
                                            Text("• $note", fontSize = 10.sp, color = Color.DarkGray)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Final Outbound result block
        viewModel.streamFinalResult?.let { result ->
            val approved = result.approved == true
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, if (approved) Color(0xFF10B981) else Color(0xFFEF4444))
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = if (approved) Color(0xFFECFDF5) else Color(0xFFFEF2F2))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (approved) "Claim Logged Successfully" else "Manual Adjuster Hold Pending",
                        fontSize = 18.sp,
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "File reference: ${result.claim_number}",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color.DarkGray
                    )
                    Text(
                        text = result.summary ?: "",
                        fontSize = 12.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }

        // Action reset
        if (!viewModel.streamIsRunning) {
            Button(
                onClick = { viewModel.restartClaimWorkflow() },
                colors = ButtonDefaults.buttonColors(containerColor = SolidBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Start Over / New Claim", color = Color.White, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

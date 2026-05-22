package com.android.mr.claimvoyantapp.ui.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
fun ArtifactCollectionScreen(
    viewModel: ClaimViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CanvasBackground)
            .padding(24.dp)
    ) {
        Text(
            text = "STEP 2 of 4",
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = Color.Gray,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "Evidence & Document Scan",
            fontSize = 28.sp,
            fontFamily = FontFamily.Serif,
            fontStyle = FontStyle.Italic,
            color = Color(0xFF1A1A1A),
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Text(
            text = "Add photographs of vehicle panels, driving license cards, or carrier policy documents for on-device OCR extraction.",
            fontSize = 14.sp,
            color = Color.DarkGray,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Capture placeholder camera preview viewport
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .border(1.5.dp, SolidBorder),
            colors = CardDefaults.cardColors(containerColor = Color.Black)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "[ CAMERA VIEWFINDER STANDBY ]",
                        color = Color.Green,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            viewModel.addCapturedImage("Mock evidence image #${viewModel.capturedImages.size + 1}")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                    ) {
                        Text("Capture Photo Artifact", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Document library previews
        Text(
            text = "Captured Scannable items (${viewModel.capturedImages.size})",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (viewModel.capturedImages.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp)
                    .border(1.dp, Color.LightGray)
            ) {
                Text("No items captured. Capture photos above or tap continue.", fontSize = 12.sp, color = Color.Gray)
            }
        } else {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(65.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(viewModel.capturedImages) { item ->
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .width(100.dp)
                            .fillMaxHeight()
                            .background(Color.White)
                            .border(1.dp, SolidBorder)
                    ) {
                        Text(item, fontSize = 10.sp, color = Color.DarkGray, modifier = Modifier.padding(4.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Layout Navigation bottom
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
                    if (viewModel.capturedImages.isEmpty()) {
                        viewModel.addCapturedImage("Front bumper deformation image")
                        viewModel.addCapturedImage("Driving License scan")
                    }
                    viewModel.triggerOcrExtraction()
                },
                colors = ButtonDefaults.buttonColors(containerColor = SolidBorder)
            ) {
                Text("Run On-Device OCR Analysis", color = Color.White, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

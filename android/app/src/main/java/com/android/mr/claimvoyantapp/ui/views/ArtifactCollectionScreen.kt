package com.android.mr.claimvoyantapp.ui.views

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.android.mr.claimvoyantapp.ui.ClaimViewModel
import com.android.mr.claimvoyantapp.ui.theme.CanvasBackground
import com.android.mr.claimvoyantapp.ui.theme.SolidBorder

@Composable
fun ArtifactCollectionScreen(
    viewModel: ClaimViewModel,
    modifier: Modifier = Modifier
) {
    // ── Gallery picker — allows selecting multiple images at once ──────────
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        uris.forEach { uri -> viewModel.addCapturedImage(uri.toString()) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CanvasBackground)
            .padding(24.dp)
    ) {
        // Step header
        Text(
            text = "STEP 4 of 6",
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

        // Camera viewfinder card
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
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "[ CAMERA VIEWFINDER STANDBY ]",
                        color = Color.Green,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    )

                    // Capture from camera (mock — real CameraX wiring left for next iteration)
                    Button(
                        onClick = {
                            viewModel.addCapturedImage("camera://capture-${viewModel.capturedImages.size + 1}")
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        )
                    ) {
                        Text("Capture Photo Artifact", fontWeight = FontWeight.Bold)
                    }

                    // ── Gallery picker button ──────────────────────────────
                    Button(
                        onClick = {
                            galleryLauncher.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1A1A1A),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "＋ Add from Camera Roll",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Captured items strip
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
                    .height(80.dp)
                    .border(1.dp, Color.LightGray)
            ) {
                Text(
                    "No items captured. Use camera or add from Camera Roll.",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        } else {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(viewModel.capturedImages) { uriString ->
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .border(1.dp, SolidBorder)
                            .background(Color.White)
                    ) {
                        if (uriString.startsWith("content://")) {
                            // Real image from gallery — show thumbnail
                            AsyncImage(
                                model = uriString,
                                contentDescription = "Evidence photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            // Mock camera capture — show placeholder label
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFF1A1A1A))
                            ) {
                                Text(
                                    text = "📷",
                                    fontSize = 24.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Bottom navigation
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
                    if (viewModel.capturedImages.isEmpty()) {
                        viewModel.addCapturedImage("Front bumper deformation image")
                        viewModel.addCapturedImage("Driving License scan")
                    }
                    viewModel.triggerOcrExtraction()
                },
                colors = ButtonDefaults.buttonColors(containerColor = SolidBorder)
            ) {
                Text(
                    "Run On-Device OCR Analysis",
                    color = Color.White,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

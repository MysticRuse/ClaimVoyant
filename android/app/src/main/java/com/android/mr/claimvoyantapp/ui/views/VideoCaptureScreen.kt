package com.android.mr.claimvoyantapp.ui.views

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.android.mr.claimvoyantapp.ui.ClaimViewModel
import com.android.mr.claimvoyantapp.ui.theme.CanvasBackground
import com.android.mr.claimvoyantapp.ui.theme.SolidBorder

@Composable
fun VideoCaptureScreen(
    viewModel: ClaimViewModel,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    // Holds the MediaStore URI we create before launching the camera
    var pendingVideoUri by remember { mutableStateOf<Uri?>(null) }

    // ── Permission handling ───────────────────────────────────────────────
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted - user can now tap the record button again
            println("Camera permission granted")
        }
    }

    // ── Camera video recording ────────────────────────────────────────────
    val cameraVideoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CaptureVideo()
    ) { saved ->
        if (saved) {
            pendingVideoUri?.let { viewModel.addCapturedVideo(it.toString()) }
        }
        pendingVideoUri = null
    }

    // ── Gallery video picker ──────────────────────────────────────────────
    val galleryVideoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        uris.forEach { uri -> viewModel.addCapturedVideo(uri.toString()) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CanvasBackground)
            .padding(24.dp)
    ) {
        // Step header
        Text(
            text = "STEP 2 of 6",
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = Color.Gray,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            text = "Scene Video Documentation",
            fontSize = 28.sp,
            fontFamily = FontFamily.Serif,
            fontStyle = FontStyle.Italic,
            color = Color(0xFF1A1A1A),
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Text(
            text = "Record a walkthrough of the scene or upload existing footage for AI motion analysis and damage classification.",
            fontSize = 14.sp,
            color = Color.DarkGray,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Video viewfinder card
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
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(horizontal = 32.dp)
                ) {
                    // Film-strip decoration
                    FilmStripDecoration()

                    Text(
                        text = "[ VIDEO CAPTURE STANDBY ]",
                        color = Color(0xFF00FF41),      // matrix green — same as photo screen
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Record via camera
                    Button(
                        onClick = {
                            val permissionCheck = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            )
                            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                                val cv = ContentValues().apply {
                                    put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                                    put(
                                        MediaStore.Video.Media.DISPLAY_NAME,
                                        "claim_video_${System.currentTimeMillis()}.mp4"
                                    )
                                }
                                val uri = context.contentResolver.insert(
                                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI, cv
                                )
                                uri?.let {
                                    pendingVideoUri = it
                                    cameraVideoLauncher.launch(it)
                                }
                            } else {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "⬤  Record Video",
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Upload from gallery
                    Button(
                        onClick = {
                            galleryVideoLauncher.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.VideoOnly
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1A1A1A),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "＋  Upload from Camera Roll",
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Captured videos strip
        Text(
            text = "Captured Video Clips (${viewModel.capturedVideos.size})",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (viewModel.capturedVideos.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .border(1.dp, Color.LightGray)
            ) {
                Text(
                    text = "No clips added. Record or upload video above.",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(viewModel.capturedVideos) { uriString ->
                    VideoThumbnailItem(index = viewModel.capturedVideos.indexOf(uriString) + 1)
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
                onClick = { viewModel.completeVideoCapture() },
                colors = ButtonDefaults.buttonColors(containerColor = SolidBorder)
            ) {
                Text(
                    text = if (viewModel.capturedVideos.isEmpty()) "Skip → Photo Artifacts"
                           else "Analyze Video →",
                    color = Color.White,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

// ── Reusable sub-composables ──────────────────────────────────────────────

@Composable
private fun FilmStripDecoration() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        repeat(9) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(12.dp)
                    .background(Color(0xFF333333))
                    .border(0.5.dp, Color(0xFF555555))
            )
        }
    }
}

@Composable
private fun VideoThumbnailItem(index: Int) {
    Box(
        modifier = Modifier
            .width(80.dp)
            .height(80.dp)
            .clip(RoundedCornerShape(4.dp))
            .border(1.dp, SolidBorder)
            .background(Color(0xFF1A1A1A)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "▶",
                fontSize = 22.sp,
                color = Color.White
            )
            Text(
                text = "CLIP $index",
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF00FF41),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

package com.android.mr.claimvoyantapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import com.android.mr.claimvoyantapp.ui.AppStep
import com.android.mr.claimvoyantapp.ui.ClaimViewModel
import com.android.mr.claimvoyantapp.ui.theme.ClaimVoyantTheme
import com.android.mr.claimvoyantapp.ui.theme.CanvasBackground
import com.android.mr.claimvoyantapp.ui.theme.SolidBorder
import com.android.mr.claimvoyantapp.ui.views.ArtifactCollectionScreen
import com.android.mr.claimvoyantapp.ui.views.ReviewScreen
import com.android.mr.claimvoyantapp.ui.views.StreamingScreen
import com.android.mr.claimvoyantapp.ui.views.VoiceGuideScreen

class MainActivity : ComponentActivity() {
    private val viewModel: ClaimViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ClaimVoyantTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = CanvasBackground
                ) {
                    AppMainLayout(viewModel)
                }
            }
        }
    }
}

@Composable
fun AppMainLayout(viewModel: ClaimViewModel) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Masthead header bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CanvasBackground)
                .border(bottom = 1.dp, color = SolidBorder)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(
                    text = "MVVM COMPOSE FLIGHT",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "ClaimVoyant Mobile",
                    fontSize = 20.sp,
                    fontFamily = FontFamily.Serif,
                    fontStyle = FontStyle.Italic,
                    color = Color(0xFF1A1A1A)
                )
            }
            Text(
                text = "v1.0.0",
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = Color.Gray
            )
        }

        // Router screens viewport container
        Box(modifier = Modifier.weight(1f)) {
            when (viewModel.currentStep) {
                AppStep.LANDING -> LandingScreen(onStart = { viewModel.startClaim() })
                AppStep.VOICE_GUIDE -> VoiceGuideScreen(viewModel)
                AppStep.ARTIFACT_COLLECTION -> ArtifactCollectionScreen(viewModel)
                AppStep.ANALYSIS_COMPILING -> AnalysisCompilingScreen()
                AppStep.REVIEWS -> ReviewScreen(viewModel)
                AppStep.STREAMING_LEDGER -> StreamingScreen(viewModel)
            }
        }
    }
}

@Composable
fun LandingScreen(onStart: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasBackground)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "FUTURE OF WORK SPECIAL BRIEF",
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = Color.Gray,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Accident Documentation On-Scene Engine",
            fontSize = 36.sp,
            fontFamily = FontFamily.Serif,
            fontStyle = FontStyle.Italic,
            color = Color(0xFF1A1A1A),
            lineHeight = 44.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = "An intelligent mobile application ensuring rapid First Notice of Loss registration. Integrates custom voice transcribers, on-device document OCR, and progressive audit streams to Cloud Run FastAPI backends.",
            fontSize = 15.sp,
            color = Color.DarkGray,
            lineHeight = 22.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Button(
            onClick = onStart,
            colors = ButtonDefaults.buttonColors(containerColor = SolidBorder),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.White)
        ) {
            Text(
                text = "Start Claim Conversation",
                color = Color.White,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(vertical = 6.dp)
            )
        }
    }
}

@Composable
fun AnalysisCompilingScreen() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasBackground)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = SolidBorder)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "[ Gemma-OCR Classifier running... ]",
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                color = Color.DarkGray
            )
        }
    }
}

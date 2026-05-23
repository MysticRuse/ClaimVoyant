package com.android.mr.claimvoyantapp

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.mr.claimvoyantapp.ui.AppStep
import com.android.mr.claimvoyantapp.ui.ClaimViewModel
import com.android.mr.claimvoyantapp.ui.theme.ClaimVoyantTheme
import com.android.mr.claimvoyantapp.ui.theme.CanvasBackground
import com.android.mr.claimvoyantapp.ui.theme.SolidBorder
import com.android.mr.claimvoyantapp.ui.views.ArtifactCollectionScreen
import com.android.mr.claimvoyantapp.ui.views.VideoAnalysisScreen
import com.android.mr.claimvoyantapp.ui.views.ReviewScreen
import com.android.mr.claimvoyantapp.ui.views.StreamingScreen
import com.android.mr.claimvoyantapp.ui.views.VideoCaptureScreen
import com.android.mr.claimvoyantapp.ui.views.VoiceGuideScreen

class MainActivity : ComponentActivity() {
    private val viewModel: ClaimViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        // enableEdgeToEdge() makes the app draw behind the status bar and
        // nav bar.  AppMainLayout then adds windowInsetsPadding(systemBars)
        // so no content is ever obscured — works consistently on API 26–35+.
        enableEdgeToEdge()
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

// ── Persistent app shell ──────────────────────────────────────────────────

@Composable
fun AppMainLayout(viewModel: ClaimViewModel) {
    // windowInsetsPadding(systemBars) pushes the whole layout below the status
    // bar and above the navigation bar, preventing any overlap on all devices.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        // Navbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CanvasBackground)
                .drawBehind {
                    val strokeWidth = 1.dp.toPx()
                    val y = size.height - strokeWidth / 2
                    drawLine(SolidBorder, Offset(0f, y), Offset(size.width, y), strokeWidth)
                }
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ClaimVoyant",
                fontSize = 22.sp,
                fontFamily = FontFamily.Serif,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
            Column(horizontalAlignment = Alignment.End) {
                Text("Release: v2.4.0 Live voice", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = Color.Gray)
                Text("Target: Future of Work",     fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = Color.Gray)
            }
        }

        // Screen router
        Box(modifier = Modifier.weight(1f)) {
            when (viewModel.currentStep) {
                AppStep.LANDING             -> LandingScreen(onStart = { viewModel.startClaim() })
                AppStep.VOICE_GUIDE         -> VoiceGuideScreen(viewModel)
                AppStep.VIDEO_CAPTURE       -> VideoCaptureScreen(viewModel)
                AppStep.VIDEO_ANALYSIS      -> VideoAnalysisScreen(viewModel)
                AppStep.ARTIFACT_COLLECTION -> ArtifactCollectionScreen(viewModel)
                AppStep.ANALYSIS_COMPILING  -> AnalysisCompilingScreen()
                AppStep.REVIEWS             -> ReviewScreen(viewModel)
                AppStep.STREAMING_LEDGER    -> StreamingScreen(viewModel)
            }
        }
    }
}

// ── Landing screen ────────────────────────────────────────────────────────

private data class FeatureItem(
    val icon: String,
    val iconBg: Color,
    val title: String,
    val description: String
)

private val FEATURE_ITEMS = listOf(
    FeatureItem(
        icon = "🎙",
        iconBg = Color(0xFFEFF6FF),
        title = "Real-time Voice",
        description = "Describe the collision verbally. Our conversational agent registers details automatically."
    ),
    FeatureItem(
        icon = "⚡",
        iconBg = Color(0xFFF0FDF4),
        title = "On-device Assessor",
        description = "Automatic vehicle damage and OCR card parsing via Gemini Multi-modal vision analysis."
    ),
    FeatureItem(
        icon = "🛡",
        iconBg = Color(0xFFF5F3FF),
        title = "Verified Submission",
        description = "Immediate fraud metrics and instant A2A FNOL carrier notification pipeline."
    )
)

@Composable
fun LandingScreen(onStart: () -> Unit) {
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val itemSpacing = if (isLandscape) 20.dp else 28.dp

    // BoxWithConstraints gives us the available height so we can force the
    // inner Column to be *at least* that tall.  Combined with
    // Arrangement.spacedBy(…, CenterVertically) this genuinely centers the
    // content both axes.  If content is taller than the screen it scrolls
    // naturally — heightIn(min) just becomes a no-op in that case.
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(CanvasBackground)
    ) {
        val screenHeight = maxHeight

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = screenHeight)          // ensures CenterVertically has room to work
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = if (isLandscape) 16.dp else 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            // spacedBy + CenterVertically = items spaced AND the whole group centred
            verticalArrangement = Arrangement.spacedBy(itemSpacing, Alignment.CenterVertically)
        ) {

            // ── Hero text ─────────────────────────────────────────────────
            Text(
                text = "Speak your incident. Record the scene. Let Gemini assess the damage and file your FNOL claim — before you leave the kerb.",
                //text = "AI-powered accident documentation. Talk to our digital voice agent, " +
                //       "take photos, and automatically register your claim under 5 minutes.",
                fontSize = if (isLandscape) 18.sp else 22.sp,
                color = Color(0xFF1A1A1A),
                textAlign = TextAlign.Center,
                lineHeight = if (isLandscape) 26.sp else 32.sp
            )

            // ── Feature cards — Column portrait / Row landscape ───────────
            if (isLandscape) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FEATURE_ITEMS.forEach { item ->
                        FeatureCard(
                            item = item,
                            isLandscape = true,
                            modifier = Modifier.weight(1f).height(160.dp)
                        )
                    }
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    FEATURE_ITEMS.forEach { item ->
                        FeatureCard(
                            item = item,
                            isLandscape = false,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // ── CTA button ────────────────────────────────────────────────
            Button(
                onClick = onStart,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF111827)),
                shape = RoundedCornerShape(50.dp),
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .height(if (isLandscape) 48.dp else 56.dp)
            ) {
                Text(
                    text = "Start Claim Conversation  →",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.3.sp
                )
            }
        }
    }
}

// ── Feature card ──────────────────────────────────────────────────────────

@Composable
private fun FeatureCard(
    item: FeatureItem,
    isLandscape: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        if (isLandscape) {
            // Vertical stack: icon → title → description
            Column(
                modifier = Modifier.fillMaxSize().padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconCircle(icon = item.icon, bg = item.iconBg, size = 40)
                Text(item.title,       fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                Text(item.description, fontSize = 11.sp, color = Color(0xFF6B7280), lineHeight = 15.sp)
            }
        } else {
            // Horizontal: icon left, title + description right
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                IconCircle(icon = item.icon, bg = item.iconBg, size = 46)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(item.title,       fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
                    Text(item.description, fontSize = 12.sp, color = Color(0xFF6B7280), lineHeight = 18.sp)
                }
            }
        }
    }
}

@Composable
private fun IconCircle(icon: String, bg: Color, size: Int) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .background(bg, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(text = icon, fontSize = (size * 0.46f).sp)
    }
}

// ── Analysis compiling interstitial ──────────────────────────────────────

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

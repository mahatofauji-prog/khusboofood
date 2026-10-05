package com.example.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import kotlinx.coroutines.delay

val SPLASH_BACKGROUND = Color(0xFF080808)
val SPLASH_GOLD = Color(0xFFF5C542)
val SPLASH_GOLD_LIGHT = Color(0xFFFFDF7A)
val SPLASH_SUBTITLE = Color(0xFFDFD0A8)
val SPLASH_CONTAINER_BG = Color(0xFF0D0D0D)

@Composable
fun KhushbooSplashScreen(
    onSplashFinished: () -> Unit
) {
    val logoScale = remember { Animatable(0.70f) }
    val logoAlpha = remember { Animatable(0f) }
    val titleAlpha = remember { Animatable(0f) }
    val subtitleAlpha = remember { Animatable(0f) }
    val glowAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Step 1: Deep Black #080808 background appears.
        // Step 2 & 3: Round Khusboo Food logo smoothly fades & scales into normal size (approx 650ms)
        logoAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
        )
        logoScale.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
        )
        glowAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 350, easing = LinearOutSlowInEasing)
        )

        // Step 4: "KHUSBOO FOOD" text fades in (Gold)
        delay(120L)
        titleAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing)
        )

        // Step 5: "Taste Of India" text fades in (soft white/light gold)
        delay(80L)
        subtitleAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
        )

        // Step 6: Brief hold (total animation around 1.8 seconds), then smoothly transition to Home screen
        delay(400L)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SPLASH_BACKGROUND),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            // Circular Logo Badge
            Box(
                modifier = Modifier
                    .size(144.dp)
                    .scale(logoScale.value)
                    .alpha(logoAlpha.value),
                contentAlignment = Alignment.Center
            ) {
                // Subtle radial gold glow behind logo
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .alpha(glowAlpha.value * 0.4f)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    SPLASH_GOLD.copy(alpha = 0.45f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        )
                )

                // Perfect circle with Gold outer ring/accent and dark inner background
                Box(
                    modifier = Modifier
                        .size(134.dp)
                        .clip(CircleShape)
                        .background(SPLASH_CONTAINER_BG)
                        .border(
                            BorderStroke(
                                width = 2.5.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        SPLASH_GOLD_LIGHT,
                                        SPLASH_GOLD,
                                        Color(0xFFC79A2B),
                                        SPLASH_GOLD
                                    )
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Logo inside circle - maintain aspect ratio without stretching or distorting
                    Image(
                        painter = painterResource(id = R.drawable.img_uploaded_logo),
                        contentDescription = "Khusboo Food Logo",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .padding(4.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // KHUSBOO FOOD = Gold
            Text(
                text = "KHUSBOO FOOD",
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp,
                letterSpacing = 2.5.sp,
                color = SPLASH_GOLD,
                modifier = Modifier.alpha(titleAlpha.value)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Taste Of India = soft white / light gold
            Text(
                text = "Taste Of India",
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Medium,
                fontSize = 14.5.sp,
                letterSpacing = 1.2.sp,
                color = SPLASH_SUBTITLE,
                modifier = Modifier.alpha(subtitleAlpha.value)
            )
        }
    }
}

package com.smashsonic.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smashsonic.R
import com.smashsonic.ui.theme.Cyan
import kotlinx.coroutines.delay

private val placeholderImages = listOf(
    R.drawable.vinyl,
    R.drawable.cassette,
    R.drawable.cd,
    R.drawable.boombox,
)

@Composable
fun LaunchScreen(
    onFinished: () -> Unit,
) {
    var currentIndex by remember { mutableIntStateOf(0) }
    var loadingDots by remember { mutableStateOf("") }
    var alpha by remember { mutableFloatStateOf(1f) }

    // Image cycling timer (0.4s)
    LaunchedEffect(Unit) {
        while (true) {
            delay(400)
            currentIndex = (currentIndex + 1) % placeholderImages.size
        }
    }

    // Loading dots timer (0.3s)
    LaunchedEffect(Unit) {
        while (true) {
            delay(300)
            loadingDots = if (loadingDots.length >= 3) "" else loadingDots + "."
        }
    }

    // Auto-dismiss after 2s + 0.5s fade
    LaunchedEffect(Unit) {
        delay(2000)
        // Fade out
        val steps = 10
        for (i in 1..steps) {
            alpha = 1f - (i.toFloat() / steps)
            delay(50)
        }
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(alpha)
            .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            Spacer(Modifier.weight(1f))

            AnimatedContent(
                targetState = currentIndex,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "placeholder",
            ) { index ->
                Image(
                    painter = painterResource(placeholderImages[index]),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .size(180.dp)
                        .clip(RoundedCornerShape(12.dp)),
                )
            }

            Text(
                "SmashSonic",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = Color.White,
            )

            // Progress dots
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (i in 0 until 4) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(if (i <= currentIndex) Cyan else Color.Gray.copy(alpha = 0.3f)),
                    )
                }
            }

            Text(
                "Loading$loadingDots",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Monospace,
                color = Color.Gray,
                modifier = Modifier.widthIn(min = 120.dp),
            )

            Spacer(Modifier.weight(1f))
        }
    }
}

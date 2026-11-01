package com.smashsonic.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.smashsonic.data.model.BackgroundType
import com.smashsonic.ui.settings.BackgroundViewModel
import com.smashsonic.ui.theme.TextPrimary

@Composable
fun SmashSonicBackground(
    modifier: Modifier = Modifier,
    viewModel: BackgroundViewModel = hiltViewModel(),
    content: @Composable () -> Unit,
) {
    val backgroundType by viewModel.backgroundType.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        // Always paint the deep base colour first so text contrast is consistent
        // regardless of which background (or none) is selected.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        )

        when {
            backgroundType.isSolidColor -> {
                backgroundType.solidColor?.let { color ->
                    Box(Modifier.fillMaxSize().background(color))
                }
            }
            backgroundType.isPixelArt -> {
                backgroundType.imageRes?.let { resId ->
                    Image(
                        painter = painterResource(resId),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .alpha(0.6f),
                    )
                    // Subtle dark scrim keeps foreground text readable over the
                    // brightest parts of the art (e.g. the neon grid lines).
                    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.25f)))
                }
            }
        }

        // Default every piece of text/icon drawn directly on the background to
        // bright white. Without this, text outside a Material Surface falls back to
        // the default black content colour and disappears on the dark background.
        CompositionLocalProvider(LocalContentColor provides TextPrimary) {
            content()
        }
    }
}

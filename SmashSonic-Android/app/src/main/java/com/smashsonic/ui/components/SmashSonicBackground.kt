package com.smashsonic.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha

import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.smashsonic.data.model.BackgroundType
import com.smashsonic.ui.settings.BackgroundViewModel

@Composable
fun SmashSonicBackground(
    modifier: Modifier = Modifier,
    viewModel: BackgroundViewModel = hiltViewModel(),
    content: @Composable () -> Unit,
) {
    val backgroundType by viewModel.backgroundType.collectAsState()

    Box(modifier = modifier.fillMaxSize()) {
        // Background layer
        when {
            backgroundType.isSolidColor -> {
                backgroundType.solidColor?.let { color ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(color)
                    )
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
                            .alpha(0.3f),
                    )
                }
            }
            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                )
            }
        }

        // Content layer
        content()
    }
}

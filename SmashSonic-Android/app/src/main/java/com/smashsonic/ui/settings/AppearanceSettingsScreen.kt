package com.smashsonic.ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smashsonic.data.model.BackgroundType
import com.smashsonic.ui.components.SmashSonicBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSettingsScreen(
    onBack: () -> Unit,
    viewModel: BackgroundViewModel = hiltViewModel(),
) {
    val currentBackground by viewModel.backgroundType.collectAsState()

    SmashSonicBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Appearance") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                )
            },
            containerColor = Color.Transparent,
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Default section
                Text("Default", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                BackgroundOptionRow(
                    displayName = BackgroundType.NONE.displayName,
                    isSelected = currentBackground == BackgroundType.NONE,
                    onClick = { viewModel.setBackgroundType(BackgroundType.NONE) },
                    preview = {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Default.Block, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                        }
                    },
                )

                HorizontalDivider()

                // Solid Colors section
                Text("Solid Colors", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Text("Simple solid color backgrounds.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                BackgroundType.solidColors.forEach { type ->
                    BackgroundOptionRow(
                        displayName = type.displayName,
                        isSelected = currentBackground == type,
                        onClick = { viewModel.setBackgroundType(type) },
                        preview = {
                            type.solidColor?.let { color ->
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(color)
                                        .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                                )
                            }
                        },
                    )
                }

                HorizontalDivider()

                // Pixel Art section
                Text("Pixel Art", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Text("8-bit themed background images.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                BackgroundType.pixelArtBackgrounds.forEach { type ->
                    BackgroundOptionRow(
                        displayName = type.displayName,
                        isSelected = currentBackground == type,
                        onClick = { viewModel.setBackgroundType(type) },
                        preview = {
                            type.imageRes?.let { resId ->
                                Image(
                                    painter = painterResource(resId),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                )
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun BackgroundOptionRow(
    displayName: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    preview: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.4f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            preview()
            Text(displayName, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            if (isSelected) {
                Icon(Icons.Default.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

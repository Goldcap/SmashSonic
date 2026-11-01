package com.smashsonic.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.smashsonic.R

// 8-bit display font. Press Start 2P is a single-weight pixel font and renders
// visually ~1.6x its point size, so pixel styles use smaller sizes + generous
// line height. It is applied ONLY to display/header/title styles; body and list
// text stay in the default sans for readability at small sizes.
val PixelFont = FontFamily(Font(R.font.press_start_2p))

val SmashSonicTypography = Typography(
    // --- 8-bit pixel styles (titles, headers, screen names) ---
    displayLarge = TextStyle(
        fontFamily = PixelFont,
        fontSize = 22.sp,
        lineHeight = 32.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = PixelFont,
        fontSize = 18.sp,
        lineHeight = 28.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = PixelFont,
        fontSize = 15.sp,
        lineHeight = 24.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = PixelFont,
        fontSize = 15.sp,
        lineHeight = 24.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = PixelFont,
        fontSize = 10.sp,
        lineHeight = 16.sp,
    ),

    // --- readable sans styles (track/album names, metadata, body) ---
    titleMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
    ),
    labelMedium = TextStyle(
        fontSize = 12.sp,
    ),
    labelSmall = TextStyle(
        fontSize = 10.sp,
    ),
)

package com.smashsonic.data.model

import com.smashsonic.R
import com.smashsonic.ui.theme.*
import androidx.compose.ui.graphics.Color

enum class BackgroundType(val key: String, val displayName: String) {
    NONE("none", "None"),
    SOLID_CYAN("solidBlack", "Cyan"),
    SOLID_LIGHT_GRAY("solidDarkGray", "Light Gray"),
    SOLID_NAVY("solidNavy", "Navy"),
    SOLID_PURPLE("solidPurple", "Purple"),
    SOLID_FOREST("solidForest", "Forest"),
    SOLID_MAGENTA("solidMagenta", "Magenta"),
    STARS("stars", "8-Bit Stars"),
    NOTES("notes", "8-Bit Notes"),
    GRID("grid", "Retro Grid"),
    SPACE("space", "Deep Space");

    val imageRes: Int?
        get() = when (this) {
            STARS -> R.drawable.bg_stars
            NOTES -> R.drawable.bg_notes
            GRID -> R.drawable.bg_grid
            SPACE -> R.drawable.bg_space
            else -> null
        }

    val solidColor: Color?
        get() = when (this) {
            SOLID_CYAN -> Cyan
            SOLID_LIGHT_GRAY -> Color(0xFFB3B3B3)
            SOLID_NAVY -> Color(0xFF1A1A4D)
            SOLID_PURPLE -> Color(0xFF331A4D)
            SOLID_FOREST -> Color(0xFF0D3319)
            SOLID_MAGENTA -> Magenta
            else -> null
        }

    val isPixelArt: Boolean get() = imageRes != null
    val isSolidColor: Boolean get() = solidColor != null

    companion object {
        val solidColors = listOf(SOLID_CYAN, SOLID_LIGHT_GRAY, SOLID_NAVY, SOLID_PURPLE, SOLID_FOREST, SOLID_MAGENTA)
        val pixelArtBackgrounds = listOf(STARS, NOTES, GRID, SPACE)

        fun fromKey(key: String): BackgroundType =
            entries.find { it.key == key } ?: SOLID_CYAN
    }
}

data class AppSettings(
    val backgroundType: BackgroundType = BackgroundType.SOLID_CYAN,
)

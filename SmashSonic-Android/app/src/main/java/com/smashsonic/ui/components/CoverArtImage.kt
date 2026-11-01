package com.smashsonic.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil3.compose.AsyncImage
import com.smashsonic.R

private val placeholders = listOf(
    R.drawable.vinyl,
    R.drawable.cassette,
    R.drawable.cd,
    R.drawable.boombox,
)

@Composable
fun PlaceholderArt(modifier: Modifier = Modifier) {
    val imageRes = remember { placeholders.random() }
    Image(
        painter = painterResource(imageRes),
        contentDescription = "Placeholder",
        contentScale = ContentScale.Crop,
        modifier = modifier,
    )
}

@Composable
fun CoverArtImage(
    url: String?,
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
) {
    // Use AsyncImage (a plain Layout), NOT SubcomposeAsyncImage. SubcomposeAsyncImage
    // runs composition inside the measure pass, and nested in lazy lists that re-measure
    // their children it produces hundreds of subcompositions per frame — a multi-second,
    // CPU-bound measure stall on low-end devices. Painter-based placeholder/error keeps
    // the retro fallback without the subcomposition cost.
    val fallback = painterResource(remember(url) { placeholders.random() })
    if (url != null) {
        AsyncImage(
            model = url,
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = modifier,
            placeholder = fallback,
            error = fallback,
            fallback = fallback,
        )
    } else {
        PlaceholderArt(modifier)
    }
}

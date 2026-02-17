package com.smashsonic.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil3.compose.AsyncImage
import coil3.compose.SubcomposeAsyncImage
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
        modifier = modifier.fillMaxSize(),
    )
}

@Composable
fun CoverArtImage(
    url: String?,
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
) {
    if (url != null) {
        SubcomposeAsyncImage(
            model = url,
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = modifier,
            error = {
                PlaceholderArt(modifier)
            },
        )
    } else {
        PlaceholderArt(modifier)
    }
}

package com.example.brainbites.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.brainbites.R
import com.example.brainbites.ui.theme.DarkGreenPrimary

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.padding
import com.example.brainbites.ui.theme.BrainBitesTheme

/**
 * BrainBites Branded Logo Component.
 * Renders the custom app logo image.
 */
@Composable
fun BrainBitesLogo(
    modifier: Modifier = Modifier.size(120.dp),
    color: Color = DarkGreenPrimary
) {
    Image(
        painter = painterResource(id = R.drawable.app_logo),
        contentDescription = "BrainBites Logo",
        contentScale = ContentScale.Fit,
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun BrainBitesLogoPreview() {
    BrainBitesTheme {
        BrainBitesLogo(modifier = Modifier.padding(16.dp))
    }
}

package com.example.brainbites.ui.util

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * A utility class to handle Composable to Bitmap capture.
 */
class ComposableCaptureController(val graphicsLayer: GraphicsLayer) {
    suspend fun captureToBitmap(): Bitmap {
        return graphicsLayer.toImageBitmap().asAndroidBitmap()
    }
}

@Composable
fun rememberComposableCaptureController(): ComposableCaptureController {
    val graphicsLayer = rememberGraphicsLayer()
    return remember(graphicsLayer) { ComposableCaptureController(graphicsLayer) }
}

fun Modifier.captureComposable(controller: ComposableCaptureController): Modifier {
    return this.drawWithCache {
        onDrawWithContent {
            controller.graphicsLayer.record {
                this@onDrawWithContent.drawContent()
            }
            drawLayer(controller.graphicsLayer)
        }
    }
}

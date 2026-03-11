package com.jetpack.compose.pdfloader

import android.graphics.Bitmap
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ZoomableImage(bitmap: Bitmap) {
    val animatableScale = remember { Animatable(1f) }
    val animatableOffsetX = remember { Animatable(0f) }
    val animatableOffsetY = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        val newScale = (animatableScale.value * zoomChange).coerceIn(1f, 5f)

        coroutineScope.launch {
            // smooth scale
            animatableScale.snapTo(newScale)

            if (newScale == 1f) {
                // smooth reset offset when back to normal
                launch { animatableOffsetX.animateTo(0f, animationSpec = spring()) }
                launch { animatableOffsetY.animateTo(0f, animationSpec = spring()) }
            } else {
                // smooth pan when zoomed
                launch { animatableOffsetX.snapTo(animatableOffsetX.value + offsetChange.x) }
                launch { animatableOffsetY.snapTo(animatableOffsetY.value + offsetChange.y) }
            }
        }
    }

    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = null,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clipToBounds()
            .graphicsLayer(
                scaleX = animatableScale.value,
                scaleY = animatableScale.value,
                translationX = animatableOffsetX.value,
                translationY = animatableOffsetY.value,
                clip = true
            )
            .transformable(
                state = transformState,
                lockRotationOnZoomPan = true,
                canPan = { animatableScale.value > 1f }
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        // smooth reset on double tap
                        coroutineScope.launch {
                            launch {
                                animatableScale.animateTo(
                                    1f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                            }
                            launch {
                                animatableOffsetX.animateTo(
                                    0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                            }
                            launch {
                                animatableOffsetY.animateTo(
                                    0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                            }
                            }
                    }
                )
            },
        contentScale = ContentScale.FillWidth
    )
}
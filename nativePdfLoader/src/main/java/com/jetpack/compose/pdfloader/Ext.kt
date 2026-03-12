package com.jetpack.compose.pdfloader

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt

fun String.toComposeColor(): Color {
    return Color(this.toColorInt())
}
package com.earthrevealed.immaru.common

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun ErrorMessage(message: String) {
    Row {
        Icon(Icons.Filled.Warning, "Error")
        Text(
            message, color = Color.Red
        )
    }
}
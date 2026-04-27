package com.earthrevealed.immaru.common

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun ErrorMessage(message: String) {
    Row {
        Icon(Icons.Filled.Warning, "Error")
        Text(
            message, color = Color.Red
        )
    }
}

@Composable
@Preview(showSystemUi = true)
private fun ErrorMessagePreview() {
    ErrorMessage("This is an error message")
}
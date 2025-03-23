package com.earthrevealed.immaru.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role

@Composable
fun SimpleIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    Box(
        modifier =
            modifier
                .clickable(
                    onClick = onClick,
                    enabled = enabled,
                    role = Role.Button,
                ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}
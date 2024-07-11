package com.earthrevealed.immaru.collections

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun CollectionSelector(collections: List<Collection>, onSelect: (Collection) -> Unit = {}) {
    Column(Modifier.padding(10.dp)) {
        collections.forEach {
            ListItem(
                headlineContent = { Text(it.name) },
                modifier = Modifier.clickable {
                    onSelect(it)
                },

                )
            HorizontalDivider()
        }
    }
}
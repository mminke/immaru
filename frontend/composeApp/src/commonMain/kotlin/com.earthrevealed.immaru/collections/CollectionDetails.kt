package com.earthrevealed.immaru.collections

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun CollectionDetails(
    collection: Collection,
    onChange: (collection: Collection) -> Unit
) {
    Column(
        Modifier.fillMaxWidth().padding(10.dp)
    ) {
        TextField(
            value = collection.name, onValueChange = {
                onChange(
                    collection.copy(name = it)
                )
            },
            Modifier.fillMaxWidth()
        )
        Text(collection.createdAt)
    }
}

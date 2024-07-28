package com.earthrevealed.immaru.collections

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun CollectionDetails(
    collection: Collection,
    onChange: () -> Unit
) {
    val name = remember { mutableStateOf(collection.name) }

    Column(
        Modifier.fillMaxWidth().padding(10.dp)
    ) {
        TextField(
            value = name.value, onValueChange = {
                name.value = it
            },
            Modifier.fillMaxWidth()
        )
        Text(collection.createdAt)
    }
}

package com.earthrevealed.immaru.collections

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.earthrevealed.immaru.common.ErrorMessage

@Composable
fun CollectionScreen(
    collectionRepository: CollectionRepository,
    collectionViewModel: CollectionsViewModel = viewModel {
        CollectionsViewModel(
            collectionRepository
        )
    },
    onCollectionSelected: (Collection) -> Unit = {},
    onCollectionInfo: (Collection) -> Unit = {},
    onNewCollection: () -> Unit = {}
) {
    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Immaru")

        val isLoading = collectionViewModel.isLoading
        val errorMessage = collectionViewModel.errorMessage
        val collections = collectionViewModel.collections

        if (isLoading.value) {
            CircularProgressIndicator()
        } else {
            if (errorMessage.value.isNotBlank()) {
                ErrorMessage(errorMessage.value)
            } else {
                CollectionSelector(
                    collections = collections.value,
                    onSelect = onCollectionSelected,
                    onInfo = onCollectionInfo
                )

                SmallFloatingActionButton(
                    onClick = onNewCollection, Modifier.align(Alignment.End).padding(12.dp)
                ) {
                    Icon(Icons.Filled.Add, "Add a new collection.")
                }
            }
        }
    }
}
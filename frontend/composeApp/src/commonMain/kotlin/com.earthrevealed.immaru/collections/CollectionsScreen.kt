package com.earthrevealed.immaru.collections

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.earthrevealed.immaru.common.ErrorMessage
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionsScreen(
    collectionViewModel: CollectionsViewModel = koinViewModel(),
    onCollectionSelected: (Collection) -> Unit = {},
    onCollectionInfo: (Collection) -> Unit = {},
    onNewCollection: () -> Unit = {},
    onOpenConfiguration: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Immaru")
                },
                actions = {
                    IconButton(onClick = { onOpenConfiguration() }) {
                        Icon(
                            Icons.Filled.Settings,
                            contentDescription = "Settings"
                        )
                    }
                }

            )
        },
        content = { innerPadding ->
            Column(
                Modifier.fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .consumeWindowInsets(innerPadding)
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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

                    }
                }
            }
        },
        floatingActionButton = {
            SmallFloatingActionButton(
                onClick = onNewCollection
            ) {
                Icon(Icons.Filled.Add, "Add a new collection.")
            }
        }
    )
}
package com.earthrevealed.immaru.collections

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.earthrevealed.immaru.common.ErrorMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionDetailsScreen(
    viewModel: CollectionDetailsViewModel,
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { onNavigateBack() }) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Back to overview"
                        )
                    }
                },
                actions = {
                    //TODO: Disable if no changes were made yet
                    IconButton(onClick = {
                        viewModel.saveChanges()
                    }) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = "Save changes"
                        )
                    }
                }

            )
        },
        content = { innerPadding ->
            Column(
                // consume insets as scaffold doesn't do it by default
                modifier = Modifier.consumeWindowInsets(innerPadding)
                    .padding(innerPadding),
            ) {
                if (viewModel.state.value == CollectionDetailsViewModel.State.SAVING) {
                    CircularProgressIndicator()
                } else {
                    if (viewModel.errorMessage.value.isNotBlank()) {
                        ErrorMessage(viewModel.errorMessage.value)
                    } else {
                        CollectionDetails(viewModel.collection.value, onChange = {
                            viewModel.collection.value = it
                        })
                    }
                }
            }
        }
    )
}
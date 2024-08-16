package com.earthrevealed.immaru.collections

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.earthrevealed.immaru.common.ErrorMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionDetailsScreen(
    viewModel: CollectionDetailsViewModel,
    onNavigateBack: () -> Unit,
) {
    val showConfirmDeleteDialog = remember { mutableStateOf(false) }
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
                        viewModel.saveChanges(onSuccess = { onNavigateBack() })
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
                if (viewModel.state.value == CollectionDetailsViewModel.State.PROCESSING) {
                    CircularProgressIndicator()
                } else {
                    if (viewModel.errorMessage.value.isNotBlank()) {
                        ErrorMessage(viewModel.errorMessage.value)
                    } else {
                        CollectionDetails(viewModel.collection.value, onChange = {
                            viewModel.collection.value = it
                        })

                        if (!viewModel.isNew) {
                            FilledTonalButton(onClick = {
                                showConfirmDeleteDialog.value = true
                            }) {
                                Text("Delete")
                            }
                        }
                    }
                }
            }
            if (showConfirmDeleteDialog.value) {
                SimpleAlertDialog(
                    "Are you sure?",
                    onConfirm = {
                        viewModel.deleteCollection(onSuccess = { onNavigateBack() })
                        showConfirmDeleteDialog.value = false
                    },
                    onDismiss = { showConfirmDeleteDialog.value = false }
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleAlertDialog(
    text: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    BasicAlertDialog(
        onDismissRequest = { onDismiss() },
    ) {
        Surface(
            modifier = Modifier.wrapContentWidth().wrapContentHeight(),
            shape = MaterialTheme.shapes.large,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = text)
                Spacer(modifier = Modifier.height(24.dp))
                Row {
                    TextButton(
                        onClick = { onDismiss() }
                    ) {
                        Text("Cancel")
                    }
                    TextButton(
                        onClick = { onConfirm() },
                    ) {
                        Text("Confirm")
                    }
                }
            }
        }
    }
}
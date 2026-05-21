package com.earthrevealed.immaru.collections

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.earthrevealed.immaru.collections.CollectionDetailsViewModel.State.ISDIRTY
import com.earthrevealed.immaru.collections.CollectionDetailsViewModel.State.PROCESSING
import com.earthrevealed.immaru.common.CenteredProgressIndicator
import com.earthrevealed.immaru.common.ErrorMessage
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionDetailsScreen(
    collectionId: CollectionId? = null,
    isNew: Boolean = false,
    viewModel: CollectionDetailsViewModel = koinViewModel { parametersOf(collectionId) },
    onNavigateBack: () -> Unit,
) {
    val collection = viewModel.collection.collectAsState()

    val showConfirmDeleteDialog = remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { onNavigateBack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to overview"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.saveChanges(onSuccess = { onNavigateBack() })
                        },
                        enabled = viewModel.state.value == ISDIRTY
                    ) {
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
                modifier = Modifier
                    .consumeWindowInsets(innerPadding)
                    .padding(innerPadding)
                    .fillMaxWidth()
            ) {
                if (viewModel.state.value == PROCESSING) {
                   CenteredProgressIndicator()
                } else {
                    if (viewModel.errorMessage.value.isNotBlank()) {
                        ErrorMessage(viewModel.errorMessage.value)
                    } else {
                        CollectionDetails(collection.value!!, onChange = {
                            viewModel.updateCollection(it)
                        })

                        if (!isNew) {
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
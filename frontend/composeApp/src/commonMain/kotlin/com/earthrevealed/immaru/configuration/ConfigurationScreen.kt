package com.earthrevealed.immaru.configuration

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.earthrevealed.immaru.common.CenteredProgressIndicator
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigurationScreen(
    configuration: Configuration,
    viewModel: ConfigurationViewModel = koinViewModel {
        parametersOf(configuration)
    },
    onNavigateBack: (() -> Unit) = {},
) {
    val showAddDialog = remember { mutableStateOf(false) }
    val editingConfiguration = remember { mutableStateOf<ServerConfiguration?>(null) }

    LaunchedEffect(configuration) {
        viewModel.setConfiguration(configuration)
    }

    if (showAddDialog.value) {
        ConfigurationDialog(
            viewModel = viewModel,
            onConfirm = { _, name, url ->
                if (viewModel.addConfiguration(name, url)) {
                    showAddDialog.value = false
                }
            },
            onDismiss = {
                viewModel.clearValidationErrors()
                showAddDialog.value = false
            }
        )
    }
    editingConfiguration.value?.let { config ->
        ConfigurationDialog(
            viewModel = viewModel,
            configuration = config,
            onConfirm = { originalName, name, url ->
                if (viewModel.updateConfiguration(originalName!!, name, url)) {
                    editingConfiguration.value = null
                }
            },
            onDismiss = {
                viewModel.clearValidationErrors()
                editingConfiguration.value = null
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Configuration")
                },
                navigationIcon = {
                    IconButton(onClick = { onNavigateBack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showAddDialog.value = true }
                    ) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = "Add configuration"
                        )
                    }
                    IconButton(
                        onClick = {
                            viewModel.saveChanges(onSuccess = { onNavigateBack() })
                        },
                        enabled = viewModel.state.value == State.IS_DIRTY
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
                Modifier.fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .consumeWindowInsets(innerPadding)
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (viewModel.state.value == State.PROCESSING) {
                    CenteredProgressIndicator()
                } else {
                    viewModel.serverConfigurations.value.forEach { serverConfig ->
                        ListItem(
                            headlineContent = { Text(serverConfig.name) },
                            supportingContent = { Text(serverConfig.url) },
                            leadingContent = {
                                Checkbox(
                                    checked = viewModel.activeServerConfigurationName.value == serverConfig.name,
                                    onCheckedChange = { _ -> viewModel.setActiveConfiguration(serverConfig.name) }
                                )
                            },
                            trailingContent = {
                                Row {
                                    IconButton(onClick = { editingConfiguration.value = serverConfig }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                                    }
                                    IconButton(onClick = { viewModel.removeConfiguration(serverConfig.name) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remove")
                                    }
                                }
                            },
                            modifier = Modifier.clickable {
                                viewModel.setActiveConfiguration(serverConfig.name)
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        },
    )
}

@Composable
fun ConfigurationDialog(
    viewModel: ConfigurationViewModel,
    configuration: ServerConfiguration? = null,
    onConfirm: (originalName: String?, name: String, url: String) -> Unit,
    onDismiss: () -> Unit
) {
    val name = remember { mutableStateOf(configuration?.name ?: "") }
    val url = remember { mutableStateOf(configuration?.url ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (configuration == null) "Add Configuration" else "Edit Configuration") },
        text = {
            Column {
                TextField(
                    value = name.value,
                    onValueChange = { name.value = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    isError = viewModel.nameError.value != null,
                    supportingText = { viewModel.nameError.value?.let { Text(it) } }
                )
                TextField(
                    value = url.value,
                    onValueChange = { url.value = it },
                    label = { Text("URL") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = viewModel.urlError.value != null,
                    supportingText = { viewModel.urlError.value?.let { Text(it) } }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(configuration?.name, name.value, url.value) },
                enabled = name.value.isNotBlank() && url.value.isNotBlank()
            ) {
                Text(if (configuration == null) "Add" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

package com.earthrevealed.immaru.configuration

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigurationScreen(
    initialServerUrl: String,
    viewModel: ConfigurationViewModel = koinViewModel(key = initialServerUrl) {
        parametersOf(initialServerUrl)
    },
) {
    val serverUrl = viewModel.serverUrl

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Configuration" )
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
                TextField(
                    label = { Text("Server url") },
                    value = serverUrl.value,
                    onValueChange = {
                        viewModel.updateUrl(it)
                    }
                )
                Button(onClick = {
                    viewModel.saveConfiguration()
                 }) { Text("Configure") }

            }
        },
    )
}
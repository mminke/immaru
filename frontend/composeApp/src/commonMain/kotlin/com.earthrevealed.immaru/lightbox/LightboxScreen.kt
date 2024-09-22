package com.earthrevealed.immaru.lightbox

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LightboxScreen(
    viewModel: LightboxViewModel,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Immaru")
                },
                navigationIcon = {
                    IconButton(onClick = { onNavigateBack() }) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Back to overview"
                        )
                    }
                },
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
                if (viewModel.isLoading.value) {
                    CircularProgressIndicator()
                } else {
                    if (viewModel.errorMessage.value.isNotBlank()) {
                        ErrorMessage(viewModel.errorMessage.value)
                    } else {
                        viewModel.assets.value.forEach {
                            Column {
                                Text(it.id.toString())
                                Text(it.name)
                            }
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            SmallFloatingActionButton(
                onClick = {}
            ) {
                Icon(Icons.Filled.Add, "Add a assets.")
            }
        }
    )
}
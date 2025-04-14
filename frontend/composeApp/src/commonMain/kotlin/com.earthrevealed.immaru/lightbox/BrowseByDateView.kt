package com.earthrevealed.immaru.lightbox

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.earthrevealed.immaru.assets.Category
import com.earthrevealed.immaru.assets.DateFilter
import com.earthrevealed.immaru.collections.Collection
import com.earthrevealed.immaru.common.CenteredProgressIndicator
import com.earthrevealed.immaru.common.ErrorMessage
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseByDateView(
    collection: Collection,
    onNavigateBack: () -> Unit,
    viewModel: BrowseByDateViewViewModel = koinViewModel { parametersOf(collection) },
) {
    val items = viewModel.items.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Browse by Date")
                },
                navigationIcon = {
                    IconButton(onClick = {
                        onNavigateBack()
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to overview"
                        )
                    }
                },
            )
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .consumeWindowInsets(innerPadding)
                    .padding(innerPadding),
            ) {
                if (viewModel.selectableDates.isLoading.value) {
                    CenteredProgressIndicator()
                } else {
                    if (viewModel.selectableDates.errorMessage.value.isNotBlank()) {
                        ErrorMessage(viewModel.selectableDates.errorMessage.value)
                    } else {
                        FilterBar(
                            viewModel.activeFilters.value,
                            onFilterEvent = { event ->
                                viewModel.handleFilterEvent(event)
                            },
                        )

                        ShowDateSelector(items.value, onItemSelected = { item ->
                            viewModel.selectItem(item)
                        })
                    }
                }

            }
        },
    )
}

@Composable
fun ShowDateSelector(
    items: List<Item<Category>>,
    onItemSelected: (Item<Category>) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 128.dp)
    ) {
        items(items) { item ->
            GridItem(item, onClick = onItemSelected)
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GridItem(
    item: Item<Category>,
    selected: Boolean = false,
    onClick: (Item<Category>) -> Unit = {},
) {
    Box(
        contentAlignment = Alignment.BottomStart,
        modifier = Modifier
            .aspectRatio(1f)
            .border(
                width = 2.dp,
                color = if (selected) Color.Blue else Color.White
            )
            .combinedClickable(
                onClick = { onClick(item) },
            )
    ) {
        //TODO: Show example assets from the selected year/month/day
        Box(
            modifier = Modifier
//                .background(Color.Gray.copy(alpha = 0.5f))
                .align(Alignment.TopEnd)
                .padding(4.dp)
        ) {
            Column {
                Text("[${item.value.numberOfItems}]", color = Color.DarkGray)
            }
        }

        Box(
            modifier = Modifier
                .background(Color.Gray.copy(alpha = 0.5f))
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Column {
                Text(item.value.caption, color = Color.White)
            }
        }
    }
}

data class Item<T>(
    val value: T
)

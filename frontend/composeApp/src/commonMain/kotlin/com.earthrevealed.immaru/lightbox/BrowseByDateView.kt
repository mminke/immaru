package com.earthrevealed.immaru.lightbox

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.lifecycle.ViewModel
import com.earthrevealed.immaru.collections.Collection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseByDateView(
    collection: Collection,
    onNavigateBack: () -> Unit,
    viewModel: BrowseByDateViewViewModel = koinViewModel<BrowseByDateViewViewModel>()
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
                FilterBar(
                    viewModel.dateFilter.value?.let { listOf(it) } ?: listOf(),
                    onFilterRemoved = { filter -> viewModel.selectItem(null) }
                )
                Breadcrumbs(
                    viewModel.breadcrumbs.value,
                    onSelected = { breadCrumb -> viewModel.selectItem(breadCrumb?.let { Item(value = it) }) }
                )

                ShowDateSelector(items.value, onItemSelected = { item ->
                    viewModel.selectItem(item)
                })
            }
        },
    )
}

@Composable
fun FilterBar(
    filters: List<Filter>,
    onFilterRemoved: (filter: Filter?) -> Unit
) {
    Row {
        filters.forEach { filter ->
            FilterChip(
                selected = true,
                enabled = true,
                label = {
                    Text(filter.caption)
                },
                trailingIcon = {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Remove filter"
                    )
                },
                onClick = { onFilterRemoved(filter) }
            )
        }
    }
}

interface Filter {
    val caption: String
}

class DateFilter(
    val year: Year,
    val month: Month? = null,
    val day: Day? = null
) : Filter {
    override val caption: String
        get() = listOf(year, month, day).filterNotNull().joinToString("-") { it.caption }
}


@Composable
fun Breadcrumbs(breadcrumbs: List<Category?>, onSelected: (item: Category?) -> Unit) {
    Row {
        breadcrumbs.take(breadcrumbs.size - 1).forEach { breadcrumb ->
            FilterChip(
                selected = true,
                enabled = true,
                label = {
                    Text(breadcrumb?.caption ?: "<")
                },
                onClick = { onSelected(breadcrumb) },
            )
        }
        breadcrumbs.takeLast(1).forEach { breadcrumb ->
            FilterChip(
                selected = true,
                enabled = false,
                label = { Text(breadcrumb?.caption ?: "<") },
                onClick = { onSelected(breadcrumb) },
            )
        }
    }
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

class BrowseByDateViewViewModel() : ViewModel() {
    private val _items = MutableStateFlow<List<Item<Category>>>(emptyList())
    val items = _items.asStateFlow()

    private val _breadcrumbs = MutableStateFlow<List<Category?>>(listOf(null))
    val breadcrumbs = _breadcrumbs.asStateFlow()

    private val _dateFilter = MutableStateFlow<DateFilter?>(null)
    val dateFilter = _dateFilter.asStateFlow()


    init {
        _items.value = exampleData.map { Item(value = it) }
    }

    fun selectItem(item: Item<Category>?) {
        if (item == null) {
            _dateFilter.value = null
            _breadcrumbs.value = listOf(null)
            _items.value = exampleData.map { Item(value = it) }
        } else {
            when (item.value) {
                is Year -> {
                    _dateFilter.value = DateFilter(item.value)
                    _breadcrumbs.value = _breadcrumbs.value.take(1) + item.value
                    _items.value = item.value.months.map { Item(value = it) }
                }

                is Month -> {
                    _dateFilter.value = DateFilter(_dateFilter.value!!.year, item.value)
                    _breadcrumbs.value = _breadcrumbs.value.take(2) + item.value
                    _items.value = item.value.days.map { Item(value = it) }
                }

                is Day -> {
                    _dateFilter.value =
                        DateFilter(_dateFilter.value!!.year, _dateFilter.value!!.month, item.value)
                    _breadcrumbs.value = _breadcrumbs.value.take(3) + item.value
                    _items.value = emptyList()
                }
            }
        }
    }
}


data class Item<T>(
    val value: T
)

private val exampleData = listOf(
    Year(
        "2021",
        234,
        listOf(
            Month(
                "01",
                24,
                days = listOf(
                    Day("15", 12),
                    Day("16", 3),
                    Day("17", 4)
                )
            ),
            Month(
                "07",
                23,
                days = listOf(
                    Day("15", 12),
                    Day("16", 3),
                    Day("17", 4)
                )
            ),
            Month(
                "08",
                56,
                days = listOf(
                    Day("15", 12),
                    Day("16", 3),
                    Day("17", 4)
                )
            )
        )
    ),
    Year(
        "2022",
        1234,
        listOf(
            Month(
                "01",
                34,
                days = listOf(
                    Day("15", 12),
                    Day("16", 3),
                    Day("17", 4)
                )
            ),
            Month(
                "07",
                243,
                days = listOf(
                    Day("15", 12),
                    Day("16", 3),
                    Day("17", 4)
                )
            ),
            Month(
                "08",
                2345,
                days = listOf(
                    Day("15", 12),
                    Day("16", 3),
                    Day("17", 4)
                )
            ),
            Month(
                "09",
                1234,
                days = listOf(
                    Day("15", 12),
                    Day("16", 3),
                    Day("17", 4)
                )
            ),
            Month(
                "10",
                234,
                days = listOf(
                    Day("15", 12),
                    Day("16", 3),
                    Day("17", 4)
                )
            ),
            Month(
                "12",
                12,
                days = listOf(
                    Day("15", 12),
                    Day("16", 3),
                    Day("17", 4)
                )
            )
        )
    ),
)

interface Category {
    val caption: String
    val numberOfItems: Int
}

data class Year(
    override val caption: String,
    override val numberOfItems: Int,
    val months: List<Month>
) : Category

data class Month(
    override val caption: String,
    override val numberOfItems: Int,
    val days: List<Day>
) : Category

data class Day(
    override val caption: String,
    override val numberOfItems: Int,
) : Category
package com.earthrevealed.immaru.lightbox

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.earthrevealed.immaru.assets.AssetRepository
import com.earthrevealed.immaru.assets.Category
import com.earthrevealed.immaru.assets.DateFilter
import com.earthrevealed.immaru.assets.SelectableDay
import com.earthrevealed.immaru.assets.SelectableMonth
import com.earthrevealed.immaru.assets.SelectableYear
import com.earthrevealed.immaru.collections.Collection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoadableListContent(
    val contentLoader: suspend () -> List<Item<Category>>
) {
    private val _items = MutableStateFlow<List<Item<Category>>>(emptyList())
    val items = _items.asStateFlow()
    val errorMessage = mutableStateOf("")
    val isLoading = mutableStateOf(true)

    suspend fun load() {
        isLoading.value = true
        try {
            _items.value = contentLoader()
        } catch (exception: Exception) {
            exception.printStackTrace()
            errorMessage.value = "Cannot load content."
        }
        isLoading.value = false
    }
}

class BrowseByDateViewViewModel(
    private val assetRepository: AssetRepository,
    private val currentCollection: Collection
) : ViewModel() {
    val selectableDates = LoadableListContent {
        assetRepository.findSelectableDates(currentCollection.id).map { Item(value = it) }
    }

    private val _items = MutableStateFlow<List<Item<Category>>>(emptyList())
    val items = _items.asStateFlow()

    private val _breadcrumbs = MutableStateFlow<List<Category?>>(listOf(null))
    val breadcrumbs = _breadcrumbs.asStateFlow()

    private val _dateFilter = MutableStateFlow<DateFilter?>(null)
    val dateFilter = _dateFilter.asStateFlow()

    init {
        refreshContent()
    }

    private fun refreshContent() {
        viewModelScope.launch {
            selectableDates.load()
            _items.value = selectableDates.items.value
        }
    }

    fun selectItem(item: Item<Category>?) {
        if (item == null) {
            _dateFilter.value = null
            _breadcrumbs.value = listOf(null)
            _items.value = selectableDates.items.value
        } else {
            when (item.value) {
                is SelectableYear -> {
                    _dateFilter.value = DateFilter(item.value)
                    _breadcrumbs.value = _breadcrumbs.value.take(1) + item.value
                    _items.value = item.value.selectableMonths.map { Item(value = it) }
                }

                is SelectableMonth -> {
                    _dateFilter.value = DateFilter(_dateFilter.value!!.selectableYear, item.value)
                    _breadcrumbs.value = _breadcrumbs.value.take(2) + item.value
                    _items.value = item.value.selectableDays.map { Item(value = it) }
                }

                is SelectableDay -> {
                    _dateFilter.value =
                        DateFilter(_dateFilter.value!!.selectableYear, _dateFilter.value!!.selectableMonth, item.value)
                    _breadcrumbs.value = _breadcrumbs.value.take(3) + item.value
                    _items.value = emptyList()
                }
            }
        }
    }
}

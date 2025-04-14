package com.earthrevealed.immaru.lightbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.earthrevealed.immaru.assets.AssetRepository
import com.earthrevealed.immaru.assets.Category
import com.earthrevealed.immaru.assets.DateFilter
import com.earthrevealed.immaru.assets.Filter
import com.earthrevealed.immaru.assets.SelectableDay
import com.earthrevealed.immaru.assets.SelectableMonth
import com.earthrevealed.immaru.assets.SelectableYear
import com.earthrevealed.immaru.collections.Collection
import com.earthrevealed.immaru.common.LoadableListContent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BrowseByDateViewViewModel(
    private val assetRepository: AssetRepository,
    private val currentCollection: Collection
) : ViewModel() {
    val selectableDates = LoadableListContent {
        assetRepository.findSelectableDates(currentCollection.id).map { Item(value = it) }
    }

    private val _items = MutableStateFlow<List<Item<Category>>>(emptyList())
    val items = _items.asStateFlow()

    private val _activeFilters = MutableStateFlow<List<Filter>>(emptyList())
    val activeFilters = _activeFilters.asStateFlow()

    init {
        refreshContent()
    }

    private fun refreshContent() {
        viewModelScope.launch {
            selectableDates.load()
            _items.value = selectableDates.items.value
        }
    }

    private fun removeFilter(filter: Filter) {
        _activeFilters.value = _activeFilters.value.filter { it != filter }
        _items.value = selectableDates.items.value
    }

    private fun handleFilterChanged(changedFilter: Filter) {
        _activeFilters.value = emptyList()
        _activeFilters.value = listOf(changedFilter)

        if (changedFilter is DateFilter) {
            if (changedFilter.selectableDay != null) {
                _items.value = emptyList()
            } else if (changedFilter.selectableMonth != null) {
                _items.value =
                    changedFilter.selectableMonth!!.selectableDays.map { Item(value = it) }
            } else {
                _items.value =
                    changedFilter.selectableYear.selectableMonths.map { Item(value = it) }
            }
        }
    }

    fun selectItem(item: Item<Category>?) {
        if (item == null) {
            _activeFilters.value = emptyList()
            _items.value = selectableDates.items.value
        } else {
            when (item.value) {
                is SelectableYear -> {
                    _activeFilters.value = listOf(DateFilter(item.value))
                    _items.value = item.value.selectableMonths.map { Item(value = it) }
                }

                is SelectableMonth -> {
                    val currentDateFilter =
                        _activeFilters.value.filterIsInstance<DateFilter>().first()
                    val newDateFilter = DateFilter(currentDateFilter.selectableYear, item.value)
                    _activeFilters.value = listOf(newDateFilter)
                    _items.value = item.value.selectableDays.map { Item(value = it) }
                }

                is SelectableDay -> {
                    val currentDateFilter =
                        _activeFilters.value.filterIsInstance<DateFilter>().first()
                    val newDateFilter = DateFilter(
                        currentDateFilter.selectableYear,
                        currentDateFilter.selectableMonth,
                        item.value
                    )
                    _activeFilters.value = listOf(newDateFilter)
                    _items.value = emptyList()
                }
            }
        }
    }

    fun handleFilterEvent(event: FilterEvent) {
        when (event) {
            is RemoveFilterEvent -> {
                removeFilter(event.filter)
            }

            is RemoveDatePartEvent -> {
                (event.filter as DateFilter).removeLastDateFilterPart()
                    ?.also { handleFilterChanged(it) } ?: removeFilter(event.filter)
            }
        }

    }
}

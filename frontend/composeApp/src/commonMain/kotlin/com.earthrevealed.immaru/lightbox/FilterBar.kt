package com.earthrevealed.immaru.lightbox

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowLeft
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.earthrevealed.immaru.assets.DateFilter
import com.earthrevealed.immaru.assets.Filter
import com.earthrevealed.immaru.common.SimpleIconButton

@Composable
fun FilterBar(
    filters: List<Filter>,
    onFilterEvent: (event: FilterEvent) -> Unit
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
                    Row {
                        SimpleIconButton(onClick = { onFilterEvent(RemoveDatePartEvent(filter as DateFilter)) }) {
                            Icon(
                                Icons.Filled.ArrowLeft,
                                contentDescription = "Date part"
                            )
                        }
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Remove filter"
                        )
                    }
                },
                onClick = { onFilterEvent(RemoveFilterEvent(filter)) },
            )
        }
    }
}

abstract class FilterEvent(
    val filter: Filter
)

class RemoveFilterEvent(
    filter: Filter
) : FilterEvent(filter)

class RemoveDatePartEvent(
    filter: DateFilter
) : FilterEvent(filter)
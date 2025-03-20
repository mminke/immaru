package com.earthrevealed.immaru.assets


interface Filter {
    val caption: String
}

class DateFilter(
    val selectableYear: SelectableYear,
    val selectableMonth: SelectableMonth? = null,
    val selectableDay: SelectableDay? = null
) : Filter {
    override val caption: String
        get() = listOfNotNull(selectableYear, selectableMonth, selectableDay).joinToString("-") { it.caption }
}

package com.earthrevealed.immaru.assets


interface Filter {
    val caption: String
}

class DateFilter(
    val year: Year,
    val month: Month? = null,
    val day: Day? = null
) : Filter {
    override val caption: String
        get() = listOfNotNull(year, month, day).joinToString("-") { it.caption }
}

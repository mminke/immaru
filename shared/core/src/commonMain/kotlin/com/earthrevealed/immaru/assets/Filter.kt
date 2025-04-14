package com.earthrevealed.immaru.assets

import kotlin.uuid.Uuid


interface Filter {
    val id: Uuid
    val caption: String
}

class DateFilter(
    override val id: Uuid,
    val selectableYear: SelectableYear,
    val selectableMonth: SelectableMonth? = null,
    val selectableDay: SelectableDay? = null
) : Filter {
    constructor(
        selectableYear: SelectableYear,
        selectableMonth: SelectableMonth? = null,
        selectableDay: SelectableDay? = null
    ) : this(
        Uuid.random(), selectableYear, selectableMonth, selectableDay
    )

    override val caption: String
        get() = listOfNotNull(
            selectableYear,
            selectableMonth,
            selectableDay
        ).joinToString("-") { it.caption }

    fun removeLastDateFilterPart(): DateFilter? {
        if (selectableDay != null) return DateFilter(id, selectableYear, selectableMonth)
        if (selectableMonth != null) return DateFilter(id, selectableYear)
        return null
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DateFilter) return false

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}

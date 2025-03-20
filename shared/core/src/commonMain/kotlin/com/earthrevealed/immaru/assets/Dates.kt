package com.earthrevealed.immaru.assets

import kotlinx.serialization.Serializable


interface Category {
    val caption: String
    val numberOfItems: Int
}

@Serializable
data class SelectableYear(
    override val caption: String,
    val selectableMonths: List<SelectableMonth>
) : Category {
    override val numberOfItems: Int
        get() = selectableMonths.sumOf { it.numberOfItems }
}

@Serializable
data class SelectableMonth(
    override val caption: String,
    val selectableDays: List<SelectableDay>
) : Category {
    override val numberOfItems: Int
        get() = selectableDays.sumOf { it.numberOfItems }
}

@Serializable
data class SelectableDay(
    override val caption: String,
    override val numberOfItems: Int,
) : Category


class SelectableYearBuilder() {
    var caption: String = ""
    val selectableMonthBuilders: MutableList<SelectableMonthBuilder>  = mutableListOf()

    fun month(init: SelectableMonthBuilder.() -> Unit) {
        val builder = SelectableMonthBuilder()
        builder.init()
        selectableMonthBuilders.add(builder)
    }

    fun build(): SelectableYear {
        return SelectableYear(caption, selectableMonthBuilders.map { it.build() })
    }
}

class SelectableMonthBuilder() {
    var caption: String = ""
    val selectableDayBuilders: MutableList<SelectableDayBuilder>  = mutableListOf()

    fun day(init: SelectableDayBuilder.() -> Unit) {
        val builder = SelectableDayBuilder()
        builder.init()
        selectableDayBuilders.add(builder)
    }

    fun build(): SelectableMonth {
        return SelectableMonth(caption, selectableDayBuilders.map { it.build() })
    }
}

class SelectableDayBuilder {
    var caption: String = ""
    var numberOfItems: Int = 0

    fun build(): SelectableDay {
        return SelectableDay(caption, numberOfItems)
    }
}

fun selectableYear(init: SelectableYearBuilder.() -> Unit): SelectableYear {
    val builder = SelectableYearBuilder()
    builder.init()
    return builder.build()
}

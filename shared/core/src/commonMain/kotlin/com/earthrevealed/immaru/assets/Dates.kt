package com.earthrevealed.immaru.assets


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

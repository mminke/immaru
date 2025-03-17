package com.earthrevealed.immaru.assets

import kotlinx.serialization.Serializable


interface Category {
    val caption: String
    val numberOfItems: Int
}


@Serializable
data class Year(
    override val caption: String,
    override val numberOfItems: Int,
    val months: List<Month>
) : Category

@Serializable
data class Month(
    override val caption: String,
    override val numberOfItems: Int,
    val days: List<Day>
) : Category

@Serializable
data class Day(
    override val caption: String,
    override val numberOfItems: Int,
) : Category

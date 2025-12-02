package com.dev.thecodecup.model.item

/**
 * Represents filter and sort selections collected from the filter UI.
 */
data class FilterPreferences(
    val priceRange: ClosedFloatingPointRange<Float> = 0f..50f,
    val minRating: Int = 3,
    val sortOption: SortOption = SortOption.POPULARITY,
    val onlyInStock: Boolean = false
)

enum class SortOption {
    POPULARITY,
    PRICE_LOW_HIGH,
    PRICE_HIGH_LOW,
    RATING
}

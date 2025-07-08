package com.supernova.ui.model

import com.supernova.data.entities.ProfileEntity

sealed class ProfileDisplayItem {
    data class RealProfile(val profile: ProfileEntity) : ProfileDisplayItem()
    object PlaceholderProfile : ProfileDisplayItem()
}

data class CarouselState(
    val items: List<ProfileDisplayItem>,
    val centerIndex: Int = 0
) {
    val centerItem: ProfileDisplayItem?
        get() = items.getOrNull(centerIndex)

    val leftItem: ProfileDisplayItem?
        get() = items.getOrNull(centerIndex - 1)

    val rightItem: ProfileDisplayItem?
        get() = items.getOrNull(centerIndex + 1)

    val farLeftItem: ProfileDisplayItem?
        get() = items.getOrNull(centerIndex - 2)

    val farRightItem: ProfileDisplayItem?
        get() = items.getOrNull(centerIndex + 2)

    fun canMoveLeft(): Boolean = centerIndex > 0
    fun canMoveRight(): Boolean = centerIndex < items.lastIndex

    fun moveLeft(): CarouselState = if (canMoveLeft()) copy(centerIndex = centerIndex - 1) else this
    fun moveRight(): CarouselState = if (canMoveRight()) copy(centerIndex = centerIndex + 1) else this
}

fun List<ProfileEntity>.toCarouselItems(): List<ProfileDisplayItem> {
    val realProfiles = this.map { ProfileDisplayItem.RealProfile(it) }
    val placeholders = (0 until (3 - this.size).coerceAtLeast(0)).map { ProfileDisplayItem.PlaceholderProfile }
    return realProfiles + placeholders
}
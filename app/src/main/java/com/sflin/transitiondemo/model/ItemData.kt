package com.sflin.transitiondemo.model

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes

data class ItemData constructor(
    @DrawableRes val resId: Int,
    val text: String?,
    @ColorInt val bgColor: Int
)
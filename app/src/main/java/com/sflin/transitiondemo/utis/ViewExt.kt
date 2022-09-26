package com.sflin.transitiondemo.utis

import android.util.DisplayMetrics
import android.util.TypedValue
import com.sflin.transitiondemo.MyApp

private const val DP_TO_PX = TypedValue.COMPLEX_UNIT_DIP
private const val SP_TO_PX = TypedValue.COMPLEX_UNIT_SP
private const val PX_TO_DP = TypedValue.COMPLEX_UNIT_MM + 1
private const val PX_TO_SP = TypedValue.COMPLEX_UNIT_MM + 2

private fun applyDimension(unit: Int, value: Float, metrics: DisplayMetrics?): Float {
    if (metrics != null) {
        when (unit) {
            DP_TO_PX -> return TypedValue.applyDimension(unit, value, metrics)
            SP_TO_PX -> return TypedValue.applyDimension(unit, value, metrics)
            PX_TO_DP -> return value / metrics.density
            PX_TO_SP -> return value / metrics.scaledDensity
        }
    }
    return 0f
}

private val sMetrics by lazy {
    MyApp.application?.resources?.displayMetrics
}

fun Number.dp2pxFloat(): Float {
    return applyDimension(DP_TO_PX, this.toFloat(), sMetrics)
}

fun Number.dp2pxInt(): Int {
    return applyDimension(DP_TO_PX, this.toFloat(), sMetrics).toInt()
}
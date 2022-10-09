package com.sflin.transitiondemo.utis

import android.annotation.SuppressLint
import android.graphics.Matrix
import android.os.Build
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.View
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


// <editor-fold desc="transformMatrix">
fun View.transformMatrixToGlobalCompat(matrix: Matrix) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        this.transformMatrixToGlobal(matrix)
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        transformMatrixToGlobalFor21(this, matrix)
    } else {
        transformMatrixToGlobalBefore21(this, matrix)
    }
}

fun View.transformMatrixToLocalCompat(matrix: Matrix) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        this.transformMatrixToLocal(matrix)
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        transformMatrixToLocalFor21(this, matrix)
    } else {
        transformMatrixToLocalBefore21(this, matrix)
    }
}

private fun transformMatrixToGlobalBefore21(view: View, matrix: Matrix) {
    val parent = view.parent
    if (parent is View) {
        val vp = parent as View
        transformMatrixToGlobalBefore21(vp, matrix)
        matrix.preTranslate(-vp.scrollX.toFloat(), -vp.scrollY.toFloat())
    }
    matrix.preTranslate(view.left.toFloat(), view.top.toFloat())
    val vm = view.matrix
    if (!vm.isIdentity) {
        matrix.preConcat(vm)
    }
}

private var sTryHiddenTransformMatrixToGlobal = true

/**
 * False when linking of the hidden transformMatrixToLocal method has previously failed.
 */
private var sTryHiddenTransformMatrixToLocal = true

@SuppressLint("NewApi") // Lint doesn't know about the hidden method.
private fun transformMatrixToGlobalFor21(view: View, matrix: Matrix) {
    if (sTryHiddenTransformMatrixToGlobal) {
        // Since this was an @hide method made public, we can link directly against it with
        // a try/catch for its absence instead of doing the same through reflection.
        try {
            view.transformMatrixToGlobal(matrix)
        } catch (e: NoSuchMethodError) {
            sTryHiddenTransformMatrixToGlobal = false
        }
    }
    if (!sTryHiddenTransformMatrixToGlobal) {
        transformMatrixToGlobalBefore21(view, matrix)
    }
}

@SuppressLint("NewApi") // Lint doesn't know about the hidden method.
private fun transformMatrixToLocalFor21(view: View, matrix: Matrix) {
    if (sTryHiddenTransformMatrixToLocal) {
        // Since this was an @hide method made public, we can link directly against it with
        // a try/catch for its absence instead of doing the same through reflection.
        try {
            view.transformMatrixToLocal(matrix)
        } catch (e: NoSuchMethodError) {
            sTryHiddenTransformMatrixToLocal = false
        }
    }
    if (!sTryHiddenTransformMatrixToLocal) {
        transformMatrixToLocalBefore21(view, matrix)
    }
}

private fun transformMatrixToLocalBefore21(view: View, matrix: Matrix) {
    val parent = view.parent
    if (parent is View) {
        val vp = parent as View
        transformMatrixToLocalBefore21(vp, matrix)
        matrix.postTranslate(vp.scrollX.toFloat(), vp.scrollY.toFloat())
    }
    matrix.postTranslate(-view.left.toFloat(), -view.top.toFloat())
    val vm = view.matrix
    if (!vm.isIdentity) {
        val inverted = Matrix()
        if (vm.invert(inverted)) {
            matrix.postConcat(inverted)
        }
    }
}
// </editor-fold>
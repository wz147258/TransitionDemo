package com.sflin.transitiondemo.utis

import android.os.Build
import android.transition.Transition
import android.transition.TransitionValues

fun Transition.isTransitionRequiredCompat(
    startValues: TransitionValues?,
    endValues: TransitionValues?
): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        return isTransitionRequired(startValues, endValues)
    } else {
        var valuesChanged = false
        // if startValues null, then transition didn't care to stash values,
        // and won't get canceled
        if (startValues != null && endValues != null) {
            val properties: Array<String> = transitionProperties
            val count = properties.size
            for (i in 0 until count) {
                if (isValueChanged(startValues, endValues, properties[i])) {
                    valuesChanged = true
                    break
                }
            }
        }
        return valuesChanged
    }
}

private fun isValueChanged(
    oldValues: TransitionValues, newValues: TransitionValues,
    key: String
): Boolean {
    if (oldValues.values.containsKey(key) != newValues.values.containsKey(key)) {
        // The transition didn't care about this particular value, so we don't care, either.
        return false
    }
    val oldValue = oldValues.values[key]
    val newValue = newValues.values[key]
    val changed: Boolean = if (oldValue == null && newValue == null) {
        // both are null
        false
    } else if (oldValue == null || newValue == null) {
        // one is null
        true
    } else {
        // neither is null
        oldValue != newValue
    }
    return changed
}
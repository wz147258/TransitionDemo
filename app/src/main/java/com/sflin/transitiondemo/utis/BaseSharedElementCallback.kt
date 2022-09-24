package com.sflin.transitiondemo.utis

import android.app.SharedElementCallback
import android.os.Build
import android.view.View
import androidx.annotation.CallSuper
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
open class BaseSharedElementCallback(val isEnter: Boolean) : SharedElementCallback() {
    companion object {
        const val STATE_CALLING_EXIT = 1
        const val STATE_CALLING_REENTER = 2

        const val STATE_CALLED_ENTER = 3
        const val STATE_CALLED_RETURN = 4

        const val STEP_START = 1
        const val STEP_END = 2

        fun Int.state2Str(): String {
            return when (this) {
                STATE_CALLING_EXIT -> {
                    "STATE_CALLING_EXIT"
                }

                STATE_CALLING_REENTER -> {
                    "STATE_CALLING_REENTER"
                }

                STATE_CALLED_ENTER -> {
                    "STATE_CALLED_ENTER"
                }

                STATE_CALLED_RETURN -> {
                    "STATE_CALLED_RETURN"
                }

                else -> {
                    "state:$this"
                }
            }
        }

        fun Int.step2Str(): String {
            return when (this) {
                STEP_START -> {
                    "STEP_START"
                }

                STEP_END -> {
                    "STEP_END"
                }

                else -> {
                    "step:$this"
                }
            }
        }
    }

    var state: Int = if (isEnter) STATE_CALLED_ENTER else STATE_CALLING_EXIT
        private set

    var step: Int = STEP_START
        private set

    @CallSuper
    override fun onSharedElementStart(
        sharedElementNames: MutableList<String>,
        sharedElements: MutableList<View>,
        sharedElementSnapshots: MutableList<View>
    ) {
        state = if (isEnter) {
            if (step == STEP_START) STATE_CALLED_ENTER else STATE_CALLED_RETURN
        } else {
            STATE_CALLING_REENTER
        }
        step = STEP_START
    }

    @CallSuper
    override fun onSharedElementEnd(
        sharedElementNames: MutableList<String>,
        sharedElements: MutableList<View>,
        sharedElementSnapshots: MutableList<View>
    ) {
        state = if (isEnter) {
            if (step == STEP_START) STATE_CALLED_ENTER else STATE_CALLED_RETURN
        } else {
            STATE_CALLING_REENTER
        }
        step = STEP_END
    }
}
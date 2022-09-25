package com.sflin.transitiondemo.snapshotdemo

import android.os.Build
import androidx.annotation.RequiresApi
import com.sflin.transitiondemo.utis.BaseSharedElementCallbackWrapper

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class CallingSharedElementCallback constructor(isEnter: Boolean, tag: String) :
    BaseSharedElementCallbackWrapper(isEnter, tag) {
}
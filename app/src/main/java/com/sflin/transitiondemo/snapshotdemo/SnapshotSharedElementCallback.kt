package com.sflin.transitiondemo.snapshotdemo

import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import com.sflin.transitiondemo.R
import com.sflin.transitiondemo.utis.BaseSharedElementCallbackWrapper

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class SnapshotSharedElementCallback(isEnter: Boolean, tag: String) : BaseSharedElementCallbackWrapper(isEnter, tag) {
    override fun onSharedElementStart(
        sharedElementNames: MutableList<String>,
        sharedElements: MutableList<View>,
        sharedElementSnapshots: MutableList<View>
    ) {
        super.onSharedElementStart(sharedElementNames, sharedElements, sharedElementSnapshots)
        setTag(sharedElements, sharedElementSnapshots)
    }

    override fun onSharedElementEnd(
        sharedElementNames: MutableList<String>,
        sharedElements: MutableList<View>,
        sharedElementSnapshots: MutableList<View>
    ) {
        super.onSharedElementEnd(sharedElementNames, sharedElements, sharedElementSnapshots)
        setTag(sharedElements, sharedElementSnapshots)
    }

    private fun setTag(sharedElements: MutableList<View>, sharedElementSnapshots: MutableList<View>) {
        val view = sharedElements[0]
        val snapshot = sharedElementSnapshots[0]
        if (state == STATE_CALLED_ENTER && step == STEP_START) {
            view.setTag(R.id.tag_snap_shot, snapshot)
        } else if (state == STATE_CALLED_RETURN && step == STEP_END) {
            view.setTag(R.id.tag_snap_shot, snapshot)
        }
    }
}
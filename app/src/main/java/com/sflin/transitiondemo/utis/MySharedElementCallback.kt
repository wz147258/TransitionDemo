package com.sflin.transitiondemo.utis

import android.app.SharedElementCallback
import android.content.Context
import android.graphics.Matrix
import android.graphics.RectF
import android.os.Build
import android.os.Parcelable
import android.view.View
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class MySharedElementCallback constructor(val tag: String, val oriCallback: SharedElementCallback? = null) : SharedElementCallback() {
    override fun onSharedElementStart(
        sharedElementNames: MutableList<String>?,
        sharedElements: MutableList<View>?,
        sharedElementSnapshots: MutableList<View>?
    ) {
        LogUtils.printInfoWithDefaultTag(tag)
        if (oriCallback != null) {
            oriCallback.onSharedElementStart(sharedElementNames, sharedElements, sharedElementSnapshots)
            return
        }
        super.onSharedElementStart(sharedElementNames, sharedElements, sharedElementSnapshots)
    }

    override fun onSharedElementEnd(
        sharedElementNames: MutableList<String>?,
        sharedElements: MutableList<View>?,
        sharedElementSnapshots: MutableList<View>?
    ) {
        LogUtils.printInfoWithDefaultTag(tag)
        if (oriCallback != null) {
            oriCallback.onSharedElementEnd(sharedElementNames, sharedElements, sharedElementSnapshots)
            return
        }
        super.onSharedElementEnd(sharedElementNames, sharedElements, sharedElementSnapshots)
    }

    override fun onRejectSharedElements(rejectedSharedElements: MutableList<View>?) {
        LogUtils.printInfoWithDefaultTag(tag)
        if (oriCallback != null) {
            oriCallback.onRejectSharedElements(rejectedSharedElements)
            return
        }
        super.onRejectSharedElements(rejectedSharedElements)
    }

    override fun onMapSharedElements(names: MutableList<String>?, sharedElements: MutableMap<String, View>?) {
        LogUtils.printInfoWithDefaultTag(tag)
        if (oriCallback != null) {
            oriCallback.onMapSharedElements(names, sharedElements)
            return
        }
        super.onMapSharedElements(names, sharedElements)
    }

    override fun onCaptureSharedElementSnapshot(
        sharedElement: View?,
        viewToGlobalMatrix: Matrix?,
        screenBounds: RectF?
    ): Parcelable {
        LogUtils.printInfoWithDefaultTag(tag)
        if (oriCallback != null) {
            return oriCallback.onCaptureSharedElementSnapshot(sharedElement, viewToGlobalMatrix, screenBounds)
        }
        return super.onCaptureSharedElementSnapshot(sharedElement, viewToGlobalMatrix, screenBounds)
    }

    override fun onCreateSnapshotView(context: Context?, snapshot: Parcelable?): View {
        LogUtils.printInfoWithDefaultTag(tag)
        if (oriCallback != null) {
            return oriCallback.onCreateSnapshotView(context, snapshot)
        }
        return super.onCreateSnapshotView(context, snapshot)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onSharedElementsArrived(
        sharedElementNames: MutableList<String>?,
        sharedElements: MutableList<View>?,
        listener: OnSharedElementsReadyListener?
    ) {
        LogUtils.printInfoWithDefaultTag(tag)
        if (oriCallback != null) {
            oriCallback.onSharedElementsArrived(sharedElementNames, sharedElements, listener)
            return
        }
        super.onSharedElementsArrived(sharedElementNames, sharedElements, listener)
    }
}
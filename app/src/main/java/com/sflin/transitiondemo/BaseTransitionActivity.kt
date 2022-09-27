package com.sflin.transitiondemo

import android.app.Instrumentation
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

open class BaseTransitionActivity : AppCompatActivity() {
    override fun onStop() {
        // 解决切到后台后return动画不播放
        // https://stackoverflow.com/questions/60876188/android-clears-activity-to-activity-shared-element-transition-exit-animation-aft/62381012#62381012
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !isFinishing) {
            Instrumentation().callActivityOnSaveInstanceState(this, Bundle())
        }
        super.onStop()
    }
}
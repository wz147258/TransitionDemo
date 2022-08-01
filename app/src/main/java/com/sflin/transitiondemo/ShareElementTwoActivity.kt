package com.sflin.transitiondemo

import android.annotation.TargetApi
import android.app.Instrumentation
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.sflin.transitiondemo.utis.MySharedElementCallback
import kotlinx.android.synthetic.main.activity_after_two.*

class ShareElementTwoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setEnterSharedElementCallback(MySharedElementCallback("B:Enter"))
            setExitSharedElementCallback(MySharedElementCallback("B:Exit"))
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share_element_two)
        init()
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun init() {
        val url = intent.getIntExtra("url", 0)
        ViewCompat.setTransitionName(img, intent.getStringExtra("transitionName"))
        Glide.with(this)
            .load(url)
            .apply(RequestOptions().skipMemoryCache(true))
            .into(img)
    }

    override fun onStop() {
        // 解决切到后台后return动画不播放
        // https://stackoverflow.com/questions/60876188/android-clears-activity-to-activity-shared-element-transition-exit-animation-aft/62381012#62381012
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !isFinishing) {
            Instrumentation().callActivityOnSaveInstanceState(this, Bundle())
        }
        super.onStop()
    }
}

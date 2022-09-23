package com.sflin.transitiondemo

import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.transition.platform.MaterialContainerTransform
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import com.sflin.transitiondemo.utis.BaseSharedElementCallbackWrapper
import kotlinx.android.synthetic.main.activity_after_two.img

class ShareElementTwoActivityV2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)

            findViewById<View>(android.R.id.content).transitionName = intent.getStringExtra("transitionName")

            setEnterSharedElementCallback(
                BaseSharedElementCallbackWrapper(
                    true,
                    "B:Enter",
                    MaterialContainerTransformSharedElementCallback()
                )
            )
            setExitSharedElementCallback(BaseSharedElementCallbackWrapper(false, "B:Exit"))

            window.sharedElementEnterTransition = MaterialContainerTransform().apply {
                addTarget(android.R.id.content)
            }
        }
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share_element_two_v2)
        init()
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun init() {
        val url = intent.getIntExtra("url", 0)

        Glide.with(this)
            .load(url)
            .apply(RequestOptions().skipMemoryCache(true))
            .into(img)
    }
}

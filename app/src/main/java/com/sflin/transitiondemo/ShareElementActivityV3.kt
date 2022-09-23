package com.sflin.transitiondemo

import android.app.ActivityOptions
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import com.sflin.transitiondemo.adapter.ShareElementListAdapterV3
import com.sflin.transitiondemo.databinding.ActivityShareElementV3Binding
import com.sflin.transitiondemo.utis.BaseSharedElementCallbackWrapper

class ShareElementActivityV3 : AppCompatActivity() {
    private lateinit var binding: ActivityShareElementV3Binding
    private lateinit var mListData: ArrayList<Int>

    private lateinit var mAdapter: ShareElementListAdapterV3

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
            // Attach a callback used to capture the shared elements from this Activity to be used
            // by the container transform transition
            setEnterSharedElementCallback(BaseSharedElementCallbackWrapper(true, "A:Enter"))
            setExitSharedElementCallback(
                BaseSharedElementCallbackWrapper(
                    false,
                    "A:Exit", MaterialContainerTransformSharedElementCallback()
                )
            )

            // Keep system bars (status bar, navigation bar) persistent throughout the transition.
            window.sharedElementsUseOverlay = false
        }
        super.onCreate(savedInstanceState)
        binding = ActivityShareElementV3Binding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    private fun init() {
        initListData()
        mAdapter = ShareElementListAdapterV3(this@ShareElementActivityV3, mListData)

        mAdapter.setOnClickListener(object : ShareElementListAdapterV3.OnCallBack {
            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onClick(view: View, url: Int) {
                val transitionName = "shareImg${url}"
                startActivity(
                    Intent(this@ShareElementActivityV3, ShareElementTwoActivityV3::class.java).apply {
                        putExtra("url", url)
                        putExtra("transitionName", transitionName)
                    },
                    ActivityOptions.makeSceneTransitionAnimation(this@ShareElementActivityV3, view, transitionName)
                        .toBundle()
                )
            }
        })
        binding.list.layoutManager = GridLayoutManager(this, 2)
        binding.list.adapter = mAdapter
    }

    private fun initListData() {
        mListData = ArrayList()
        mListData.add(R.mipmap.img1)
        mListData.add(R.mipmap.img2)
        mListData.add(R.mipmap.img3)
        mListData.add(R.mipmap.img4)
    }


}

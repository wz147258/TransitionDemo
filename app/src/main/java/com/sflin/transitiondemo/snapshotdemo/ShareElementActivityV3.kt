package com.sflin.transitiondemo.snapshotdemo

import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.GridLayoutManager
import com.sflin.transitiondemo.BaseTraditionalActivity
import com.sflin.transitiondemo.R
import com.sflin.transitiondemo.adapter.ShareElementListAdapterV3
import com.sflin.transitiondemo.databinding.ActivityShareElementV3Binding
import com.sflin.transitiondemo.model.ItemData

class ShareElementActivityV3 : BaseTraditionalActivity() {
    private lateinit var binding: ActivityShareElementV3Binding
    private lateinit var mListData: ArrayList<ItemData>

    private lateinit var mAdapter: ShareElementListAdapterV3

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
            // Attach a callback used to capture the shared elements from this Activity to be used
            // by the container transform transition
            setEnterSharedElementCallback(CallingSharedElementCallback(true, "A:Enter"))
            setExitSharedElementCallback(CallingSharedElementCallback(false, "A:Exit"))

            // Keep system bars (status bar, navigation bar) persistent throughout the transition.
//            window.sharedElementsUseOverlay = false

            window.sharedElementEnterTransition = null
            window.sharedElementExitTransition = null
            window.enterTransition = null
            window.exitTransition = null
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
        mListData.add(
            ItemData(
                resId = R.mipmap.img1,
                text = "position:0,${getString(R.string.item_text)}",
                bgColor = Color.RED
            )
        )
        mListData.add(
            ItemData(
                resId = R.mipmap.img2,
                text = "position:1,${getString(R.string.item_text)}",
                bgColor = Color.YELLOW
            )
        )
        mListData.add(
            ItemData(
                resId = R.mipmap.img3,
                text = "position:2,${getString(R.string.item_text)}",
                bgColor = Color.BLUE
            )
        )
        mListData.add(
            ItemData(
                resId = R.mipmap.img4,
                text = "position:3,${getString(R.string.item_text)}",
                bgColor = Color.GREEN
            )
        )
    }
}

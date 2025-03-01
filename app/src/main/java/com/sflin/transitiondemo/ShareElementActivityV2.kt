package com.sflin.transitiondemo

import android.annotation.TargetApi
import android.app.ActivityOptions
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import com.sflin.transitiondemo.adapter.ShareElementListAdapter
import com.sflin.transitiondemo.utis.BaseSharedElementCallbackWrapper
import kotlinx.android.synthetic.main.activity_share_element.img5
import kotlinx.android.synthetic.main.activity_share_element.img6
import kotlinx.android.synthetic.main.activity_share_element.list

class ShareElementActivityV2 : BaseTraditionalActivity() {

    private lateinit var mListData: ArrayList<Int>

    private lateinit var mAdapter: ShareElementListAdapter

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
        setContentView(R.layout.activity_share_element)
        init()
        initClickListener()
    }

    private fun init() {
        initListData()
        mAdapter = ShareElementListAdapter(this@ShareElementActivityV2, mListData)

        mAdapter.setOnClickListener(object : ShareElementListAdapter.OnCallBack {
            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onClick(view: View, url: Int) {
                val transitionName = "shareImg${url}"
                startActivity(
                    Intent(this@ShareElementActivityV2, ShareElementTwoActivityV2::class.java).apply {
                        putExtra("url", url)
                        putExtra("transitionName", transitionName)
                    },
                    ActivityOptions.makeSceneTransitionAnimation(this@ShareElementActivityV2, view, transitionName)
                        .toBundle()
                )
            }
        })
        list.layoutManager = GridLayoutManager(this, 2)
        list.adapter = mAdapter
    }

    private fun initListData() {
        mListData = ArrayList()
        mListData.add(R.mipmap.img1)
        mListData.add(R.mipmap.img2)
        mListData.add(R.mipmap.img3)
        mListData.add(R.mipmap.img4)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private fun initClickListener() {
        img5.setOnClickListener {
            val one = Pair<View, String>(img5, "shareImg5")
            val two = Pair<View, String>(img6, "shareImg6")
            val pairs = arrayOf(one, two)
            val transitionActivityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(this, *pairs)
            startActivity(
                Intent(this@ShareElementActivityV2, ShareElementThreeActivity::class.java),
                transitionActivityOptions.toBundle()
            )
        }
        img6.setOnClickListener {
            val one = Pair<View, String>(img5, "shareImg5")
            val two = Pair<View, String>(img6, "shareImg6")
            val pairs = arrayOf(one, two)
            val transitionActivityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(this, *pairs)
            startActivity(
                Intent(this@ShareElementActivityV2, ShareElementThreeActivity::class.java),
                transitionActivityOptions.toBundle()
            )
        }
    }

}

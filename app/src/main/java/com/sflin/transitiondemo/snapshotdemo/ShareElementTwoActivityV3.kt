package com.sflin.transitiondemo.snapshotdemo

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.transition.TransitionValues
import android.view.ViewGroup
import android.view.Window
import androidx.core.math.MathUtils
import androidx.customview.widget.ViewDragHelper
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.sflin.transitiondemo.BaseTransitionActivity
import com.sflin.transitiondemo.databinding.ActivityShareElementTwoV2Binding
import com.sflin.transitiondemo.utis.BaseSharedElementCallbackWrapper
import com.sflin.transitiondemo.view.ActExitGestureFrameLayout
import com.sflin.transitiondemo.view.ActExitGesturePresenter

class ShareElementTwoActivityV3 : BaseTransitionActivity() {

    private lateinit var binding: ActivityShareElementTwoV2Binding
    private val viewContent: ViewGroup by lazy { findViewById(android.R.id.content) }
    private var presenter: ActExitGesturePresenter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
            setEnterSharedElementCallback(
                BaseSharedElementCallbackWrapper(true, "B:Enter", SnapshotSharedElementCallback(true))
            )
            setExitSharedElementCallback(
                BaseSharedElementCallbackWrapper(false, "B:Exit", SnapshotSharedElementCallback(false))
            )
        }
        super.onCreate(savedInstanceState)

        binding = ActivityShareElementTwoV2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.enterTransition = null
            window.exitTransition = null

            binding.root.transitionName = intent.getStringExtra("transitionName")
            window.sharedElementEnterTransition = SnapshotSharedElementTransition(true).also {
                it.addTarget(binding.root)
                it.duration = 300L
            }
            window.sharedElementReturnTransition = SnapshotSharedElementTransition(false).also {
                it.addTarget(binding.root)
                it.duration = 300L
                it.returnAnimConfig = ::returnAnimConfig
            }
            window.sharedElementExitTransition = null
            postponeEnterTransition()
        }

        init()
    }

    private fun init() {
        val url = intent.getIntExtra("url", 0)

        Glide.with(this)
            .load(url)
            .apply(RequestOptions().skipMemoryCache(true))
            .addListener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        startPostponedEnterTransition()
                    }
                    return false
                }
            })
            .into(binding.img)

        presenter = ActExitGesturePresenter(this).also {
            it.initGestureListener(
                targetView = binding.root,
                dragListener = object : ActExitGestureFrameLayout.DragListener.DragListenerStub() {
                    override fun canDrag(): Boolean {
                        return true
                    }

                    override fun canDragRight(): Boolean {
                        return true
                    }

                    override fun canDragDown(): Boolean {
                        return false
                    }

                    override fun triggerExit(): Boolean {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            finishAfterTransition()
                        } else {
                            finish()
                        }
                        return true
                    }
                },
                config = {
                    enableDragChangeBgAlpha = true
                    mTrackingEdges = ViewDragHelper.EDGE_LEFT
                }
            )
        }
    }

    private fun returnAnimConfig(
        sceneRoot: ViewGroup,
        startValues: TransitionValues?,
        endValues: TransitionValues?,
        animator: ValueAnimator
    ) {
        val parent = binding.root.parent as? ViewGroup ?: return

        val background: Drawable = if (parent.background == null) {
            ColorDrawable(Color.BLACK).apply {
                parent.background = this
            }
        } else {
            parent.background.mutate().apply {
                if (this != parent.background) {
                    parent.background = this
                }
            }
        }

        val startAlpha = background.alpha / 255f
        val endAlpha = 0f

        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                background.alpha = (MathUtils.clamp(startAlpha, 0f, 1f) * 255).toInt()
            }

            override fun onAnimationEnd(animation: Animator?) {
                background.alpha = (MathUtils.clamp(endAlpha, 0f, 1f) * 255).toInt()
            }
        })
        animator.addUpdateListener {
            val value = it.animatedValue as Float
            val currentAlpha = startAlpha + (endAlpha - startAlpha) * value
            background.alpha = (MathUtils.clamp(currentAlpha, 0f, 1f) * 255).toInt()
        }
    }
}

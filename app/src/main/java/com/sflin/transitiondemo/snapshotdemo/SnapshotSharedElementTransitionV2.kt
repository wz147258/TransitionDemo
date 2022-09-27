package com.sflin.transitiondemo.snapshotdemo

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.graphics.Point
import android.graphics.Rect
import android.os.Build
import android.transition.Transition
import android.transition.TransitionValues
import android.view.View
import android.view.ViewGroup
import com.sflin.transitiondemo.R

/**
 * 使用snapshot，添加到view布局中
 */
class SnapshotSharedElementTransitionV2(val isEnter: Boolean) : Transition() {

    companion object {
        private const val PROPNAME_BOUNDS = "MySharedElementTransition:bounds"
        private const val PROPNAME_SCREEN_POSITION = "MySharedElementTransition:screen_position"

        private val sTransitionProperties = arrayOf(
            PROPNAME_BOUNDS,
            PROPNAME_SCREEN_POSITION
        )
    }

    private val tempArray = IntArray(2)

    override fun getTransitionProperties(): Array<String> {
        return sTransitionProperties
    }

    override fun captureStartValues(transitionValues: TransitionValues) {
        captureValues(transitionValues, true)
    }

    override fun captureEndValues(transitionValues: TransitionValues) {
        captureValues(transitionValues, false)
    }

    private fun captureValues(transitionValues: TransitionValues, isStart: Boolean) {
        val view = transitionValues.view
        transitionValues.values[PROPNAME_BOUNDS] = Rect(view.left, view.top, view.right, view.bottom)
        view.getLocationOnScreen(tempArray)
        transitionValues.values[PROPNAME_SCREEN_POSITION] = Point(tempArray[0], tempArray[1])
    }

    override fun createAnimator(
        sceneRoot: ViewGroup,
        startValues: TransitionValues?,
        endValues: TransitionValues?
    ): Animator? {
        return if (isEnter) {
            createEnterAnimator(sceneRoot, startValues, endValues)
        } else {
            createReturnAnimator(sceneRoot, startValues, endValues)
        }
    }

    private fun createEnterAnimator(
        sceneRoot: ViewGroup,
        startValues: TransitionValues?,
        endValues: TransitionValues?
    ): Animator? {
        if (startValues?.view == null || endValues?.view == null) {
            return null
        }
        val view = endValues.view
        val snapshotView: View? = view.getTag(R.id.tag_snap_shot) as? View
        view.setTag(R.id.tag_snap_shot, null)

        val startBounds: Rect = startValues.values[PROPNAME_BOUNDS] as Rect
        val startScreenPosition: Point = startValues.values[PROPNAME_SCREEN_POSITION] as Point

        val endBounds: Rect = endValues.values[PROPNAME_BOUNDS] as Rect
        val endScreenPosition: Point = endValues.values[PROPNAME_SCREEN_POSITION] as Point

        val animator = ValueAnimator.ofFloat(0f, 1f)

        val contentView = if (view is ViewGroup) {
            view.getChildAt(0)
        } else {
            view
        }

        // 1.snapshot
        if (snapshotView != null) {
            val widthSpec = View.MeasureSpec.makeMeasureSpec(view.width, View.MeasureSpec.EXACTLY)
            val heightSpec = View.MeasureSpec.makeMeasureSpec(
                (snapshotView.height * (view.width * 1f / snapshotView.width)).toInt(),
                View.MeasureSpec.EXACTLY
            )
            snapshotView.measure(widthSpec, heightSpec)
            snapshotView.layout(0, 0, view.width, (snapshotView.height * (view.width * 1f / snapshotView.width)).toInt())
            val snapshotLP = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            (view as? ViewGroup)?.addView(snapshotView, 0, snapshotLP)

            // 2.fadeOut, scale
            val startSnapshotAlpha = 1f
            val endSnapshotAlpha = 0f

            animator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) {
                    snapshotView.alpha = startSnapshotAlpha
                }

                override fun onAnimationCancel(animation: Animator?) {
                }

                override fun onAnimationEnd(animation: Animator?) {
                    snapshotView.alpha = endSnapshotAlpha

                    // remove snapshotView
                    (view as? ViewGroup)?.removeView(snapshotView)
                }
            })
            animator.addUpdateListener {
                val value = it.animatedValue as Float
                snapshotView.alpha = 1f - value
            }
        }

        // 2.view fadeIn, scale
        val startViewAlpha = 0f
        val endViewAlpha = 1f

        val startViewTranslation = Point(startBounds.left, startBounds.top)
        val endViewTranslation = Point(0, 0)

        val viewPivotX = 0f
        val viewPivotY = 0f
        val startViewScale = startBounds.width() * 1f / endBounds.width()
        val endViewScale = 1f

        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                contentView.alpha = startViewAlpha

                view.translationX = startViewTranslation.x.toFloat()
                view.translationY = startViewTranslation.y.toFloat()

                view.pivotX = viewPivotX
                view.pivotY = viewPivotY
                view.scaleX = startViewScale
                view.scaleY = startViewScale
            }

            override fun onAnimationEnd(animation: Animator?) {
                contentView.alpha = endViewAlpha

                view.translationX = endViewTranslation.x.toFloat()
                view.translationY = endViewTranslation.y.toFloat()

                view.scaleX = endViewScale
                view.scaleY = endViewScale

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    view.resetPivot()
                }
            }
        })
        animator.addUpdateListener {
            val value = it.animatedValue as Float
            contentView.alpha = value

            view.translationX = startViewTranslation.x + (endViewTranslation.x - startViewTranslation.x) * value
            view.translationY = startViewTranslation.y + (endViewTranslation.y - startViewTranslation.y) * value

            view.scaleX = startViewScale + (endViewScale - startViewScale) * value
            view.scaleY = view.scaleX
        }

        return animator
    }

    private fun createReturnAnimator(
        sceneRoot: ViewGroup,
        startValues: TransitionValues?,
        endValues: TransitionValues?
    ): Animator? {
        if (startValues?.view == null || endValues?.view == null) {
            return null
        }
        val view = endValues.view
        val snapshotView: View? = view.getTag(R.id.tag_snap_shot) as? View
        view.setTag(R.id.tag_snap_shot, null)

        val startBounds: Rect = startValues.values[PROPNAME_BOUNDS] as Rect
        val startScreenPosition: Point = startValues.values[PROPNAME_SCREEN_POSITION] as Point

        val endBounds: Rect = endValues.values[PROPNAME_BOUNDS] as Rect
        val endScreenPosition: Point = endValues.values[PROPNAME_SCREEN_POSITION] as Point

        val animator = ValueAnimator.ofFloat(0f, 1f)

        val contentView = if (view is ViewGroup) {
            view.getChildAt(0)
        } else {
            view
        }

        // 1.snapshot
        if (snapshotView != null) {
            // 1.overlay
            val snapshotLP =
                ViewGroup.LayoutParams(startBounds.width(), (snapshotView.height * (startBounds.width() * 1f / snapshotView.width)).toInt())
            (view as? ViewGroup)?.addView(snapshotView, 0, snapshotLP)

            // 2.fadeOut, scale
            val startSnapshotAlpha = 0f
            val endSnapshotAlpha = 1f

            animator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) {
                    snapshotView.alpha = startSnapshotAlpha
                }

                override fun onAnimationEnd(animation: Animator?) {
                    snapshotView.alpha = endSnapshotAlpha

                    // remove snapshotView
//                    (view as? ViewGroup)?.removeView(snapshotView)
                }
            })
            animator.addUpdateListener {
                val value = it.animatedValue as Float
                snapshotView.alpha = startSnapshotAlpha + (endSnapshotAlpha - startSnapshotAlpha) * value
            }
        }

        // 2.view
        val startViewAlpha = view.alpha
        val endViewAlpha = 0f

        val viewPivotX = 0f
        val viewPivotY = 0f
        val startViewScale = view.scaleX
        val endViewScale = endBounds.width() * 1f / startBounds.width()

        val startViewTranslation = Point(0, 0)
        val endViewTranslation = Point(endBounds.left - startBounds.left, endBounds.top - startBounds.top)

        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                val widthSpec = View.MeasureSpec.makeMeasureSpec(startBounds.width(), View.MeasureSpec.EXACTLY)
                val heightSpec = View.MeasureSpec.makeMeasureSpec(startBounds.height(), View.MeasureSpec.EXACTLY)
                view.measure(widthSpec, heightSpec)
                view.layout(startBounds.left, startBounds.top, startBounds.right, startBounds.bottom)

                contentView.alpha = startViewAlpha

                view.translationX = startViewTranslation.x.toFloat()
                view.translationY = startViewTranslation.y.toFloat()

                view.pivotX = viewPivotX
                view.pivotY = viewPivotY
                view.scaleX = startViewScale
                view.scaleY = startViewScale
            }

            override fun onAnimationEnd(animation: Animator?) {
                contentView.alpha = endViewAlpha

                view.translationX = endViewTranslation.x.toFloat()
                view.translationY = endViewTranslation.y.toFloat()

                view.scaleX = endViewScale
                view.scaleY = endViewScale

                /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    view.resetPivot()
                }*/
            }
        })
        animator.addUpdateListener {
            val value = it.animatedValue as Float
            contentView.alpha = startViewAlpha + (endViewAlpha - startViewAlpha) * value

            view.translationX = startViewTranslation.x + (endViewTranslation.x - startViewTranslation.x) * value
            view.translationY = startViewTranslation.y + (endViewTranslation.y - startViewTranslation.y) * value

            view.scaleX = startViewScale + (endViewScale - startViewScale) * value
            view.scaleY = view.scaleX
        }

        return animator
    }
}
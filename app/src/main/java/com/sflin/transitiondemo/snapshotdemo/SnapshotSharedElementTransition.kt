package com.sflin.transitiondemo.snapshotdemo

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.RectEvaluator
import android.animation.ValueAnimator
import android.graphics.Matrix
import android.graphics.Point
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.os.Build
import android.transition.Transition
import android.transition.TransitionValues
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroupOverlay
import com.sflin.transitiondemo.R
import com.sflin.transitiondemo.utis.isTransitionRequiredCompat
import com.sflin.transitiondemo.utis.transformMatrixToGlobalCompat
import kotlin.math.min

/**
 * 使用snapshot和overlay
 */
class SnapshotSharedElementTransition constructor(val isEnter: Boolean) : Transition() {

    companion object {
        private const val PROPNAME_PREFIX = "MySharedElementTransition"
        private const val PROPNAME_BOUNDS = "$PROPNAME_PREFIX:bounds"
        private const val PROPNAME_SCREEN_POSITION = "$PROPNAME_PREFIX:screen_position"

        private val sTransitionProperties = arrayOf(
            PROPNAME_BOUNDS,
            PROPNAME_SCREEN_POSITION
        )

        private val sRectEvaluator = RectEvaluator()
    }

    var returnAnimConfig: ((
        sceneRoot: ViewGroup,
        startValues: TransitionValues?,
        endValues: TransitionValues?,
        animator: ValueAnimator
    ) -> Unit)? = null


    private val tempMatrix = Matrix()

    override fun getTransitionProperties(): Array<String> {
        return sTransitionProperties
    }

    override fun isTransitionRequired(startValues: TransitionValues?, endValues: TransitionValues?): Boolean {
        return if (isEnter) super.isTransitionRequired(startValues, endValues) else true
    }

    override fun captureStartValues(transitionValues: TransitionValues) {
        captureValues(transitionValues, true)
    }

    override fun captureEndValues(transitionValues: TransitionValues) {
        captureValues(transitionValues, false)
    }

    private fun captureValues(transitionValues: TransitionValues, isStart: Boolean) {
        val view = transitionValues.view
        transitionValues.values[PROPNAME_BOUNDS] = RectF(
            view.left.toFloat(), view.top.toFloat(), view.right.toFloat(),
            view.bottom.toFloat()
        )

        tempMatrix.reset()
        view.transformMatrixToGlobalCompat(tempMatrix)
        val sreenBounds = RectF(0f, 0f, view.width.toFloat(), view.height.toFloat())
        tempMatrix.mapRect(sreenBounds)
        transitionValues.values[PROPNAME_SCREEN_POSITION] = sreenBounds
    }

    override fun createAnimator(
        sceneRoot: ViewGroup,
        startValues: TransitionValues?,
        endValues: TransitionValues?
    ): Animator? {
        return if (isEnter) {
            createEnterAnimator(sceneRoot, startValues, endValues)
        } else {
            createReturnAnimator(sceneRoot, startValues, endValues)?.also {
                if (it is ValueAnimator && returnAnimConfig != null) {
                    returnAnimConfig!!.invoke(sceneRoot, startValues, endValues, it)
                }
            }
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

        val startBounds: RectF = startValues.values[PROPNAME_BOUNDS] as RectF

        val endBounds: RectF = endValues.values[PROPNAME_BOUNDS] as RectF

        val animator = ValueAnimator.ofFloat(0f, 1f)

        // 1.snapshot
        if (snapshotView != null) {
            // 2.fadeOut, scale
            val startSnapshotAlpha = 1f
            val endSnapshotAlpha = 0f

            val startSnapshotTranslation = Point(0, 0)
            val endSnapshotTranslation = Point((endBounds.left - startBounds.left).toInt(), (endBounds.top - startBounds.top).toInt())

            val snapshotPivotX = 0f
            val snapshotPivotY = 0f
            val startSnapshotScale = 1f
            val endSnapshotScale = endBounds.width() * 1f / startBounds.width()

            animator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) {
                    // 1.overlay
                    (sceneRoot.overlay as ViewGroupOverlay).add(snapshotView)

                    snapshotView.alpha = startSnapshotAlpha

                    snapshotView.translationX = startSnapshotTranslation.x.toFloat()
                    snapshotView.translationY = startSnapshotTranslation.y.toFloat()


                    snapshotView.pivotX = snapshotPivotX
                    snapshotView.pivotY = snapshotPivotY
                    snapshotView.scaleX = startSnapshotScale
                    snapshotView.scaleY = startSnapshotScale
                }

                override fun onAnimationCancel(animation: Animator?) {
                }

                override fun onAnimationEnd(animation: Animator?) {
                    snapshotView.alpha = endSnapshotAlpha

                    snapshotView.translationX = endSnapshotTranslation.x.toFloat()
                    snapshotView.translationY = endSnapshotTranslation.y.toFloat()

                    snapshotView.scaleX = endSnapshotScale
                    snapshotView.scaleY = endSnapshotScale

                    // remove snapshotView
                    (sceneRoot.overlay as ViewGroupOverlay).remove(snapshotView)
                }
            })
            animator.addUpdateListener {
                val value = it.animatedValue as Float
                snapshotView.alpha = 1f - value

                snapshotView.translationX =
                    startSnapshotTranslation.x + (endSnapshotTranslation.x - startSnapshotTranslation.x) * value
                snapshotView.translationY =
                    startSnapshotTranslation.y + (endSnapshotTranslation.y - startSnapshotTranslation.y) * value

                snapshotView.scaleX = startSnapshotScale + (endSnapshotScale - startSnapshotScale) * value
                snapshotView.scaleY = snapshotView.scaleX
            }
        }

        // 2.view fadeIn, scale
        val startViewAlpha = 0f
        val endViewAlpha = 1f

        val startViewTranslation = Point(startBounds.left.toInt(), startBounds.top.toInt())
        val endViewTranslation = Point(0, 0)


        val startViewPivot = PointF(0f, 0f)
        val endViewPivot = PointF(endBounds.width() / 2f, endBounds.height() / 2f)
        val startViewScale = startBounds.width() * 1f / endBounds.width()
        val endViewScale = 1f

        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                view.alpha = startViewAlpha

                view.translationX = startViewTranslation.x.toFloat()
                view.translationY = startViewTranslation.y.toFloat()

                view.pivotX = startViewPivot.x
                view.pivotY = startViewPivot.y
                view.scaleX = startViewScale
                view.scaleY = startViewScale
            }

            override fun onAnimationEnd(animation: Animator?) {
                view.alpha = endViewAlpha

                view.translationX = endViewTranslation.x.toFloat()
                view.translationY = endViewTranslation.y.toFloat()

                view.scaleX = endViewScale
                view.scaleY = endViewScale

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    view.resetPivot()
                } else {
                    view.pivotX = endViewPivot.x
                    view.pivotY = endViewPivot.y
                }
            }
        })
        animator.addUpdateListener {
            val value = it.animatedValue as Float
            view.alpha = value

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
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !super.isTransitionRequired(startValues, endValues))
            || !isTransitionRequiredCompat(startValues, endValues)
        ) {
            return createReturnEqualsAnimator(sceneRoot, startValues, endValues)
        }

        if (startValues?.view == null || endValues?.view == null) {
            return null
        }
        val view = endValues.view
        val snapshotView: View? = view.getTag(R.id.tag_snap_shot) as? View
        view.setTag(R.id.tag_snap_shot, null)

        val startBounds: RectF = startValues.values[PROPNAME_BOUNDS] as RectF
        val startScreenPosition: RectF = startValues.values[PROPNAME_SCREEN_POSITION] as RectF
        val visualStartBounds = RectF(0f, 0f, startBounds.width(), startBounds.height())

        val endBounds: RectF = endValues.values[PROPNAME_BOUNDS] as RectF
        val endScreenPosition: RectF = endValues.values[PROPNAME_SCREEN_POSITION] as RectF
        val visualEndBounds = RectF(0f, 0f, endBounds.width(), endBounds.height())

        view.matrix.let {
            it.mapRect(visualStartBounds)
            visualStartBounds.offset(startBounds.left, startBounds.top)

            it.mapRect(visualEndBounds)
            visualEndBounds.offset(endBounds.left, endBounds.top)
        }

        val animator = ValueAnimator.ofFloat(0f, 1f)

        // 1.snapshot
        if (snapshotView != null) {
            // 2.fadeOut, scale
            val startSnapshotAlpha = 0f
            val endSnapshotAlpha = 1f

            val startSnapshotTranslation = Point(
                (startScreenPosition.left - endScreenPosition.left).toInt(),
                (startScreenPosition.top - endScreenPosition.top).toInt()
            )
            val endSnapshotTranslation = Point(0, 0)

            val snapshotPivotX = 0f
            val snapshotPivotY = 0f
            val startSnapshotScale = startScreenPosition.width() * 1f / endScreenPosition.width()
            val endSnapshotScale = 1f

            animator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) {
                    // 1.overlay
                    (sceneRoot.overlay as ViewGroupOverlay).add(snapshotView)

                    snapshotView.alpha = startSnapshotAlpha

                    snapshotView.translationX = startSnapshotTranslation.x.toFloat()
                    snapshotView.translationY = startSnapshotTranslation.y.toFloat()


                    snapshotView.pivotX = snapshotPivotX
                    snapshotView.pivotY = snapshotPivotY
                    snapshotView.scaleX = startSnapshotScale
                    snapshotView.scaleY = startSnapshotScale
                }

                override fun onAnimationEnd(animation: Animator?) {
                    snapshotView.alpha = endSnapshotAlpha

                    snapshotView.translationX = endSnapshotTranslation.x.toFloat()
                    snapshotView.translationY = endSnapshotTranslation.y.toFloat()

                    snapshotView.scaleX = endSnapshotScale
                    snapshotView.scaleY = endSnapshotScale
                }
            })
            animator.addUpdateListener {
                val value = it.animatedValue as Float
                snapshotView.alpha = startSnapshotAlpha + (endSnapshotAlpha - startSnapshotAlpha) * value

                snapshotView.translationX =
                    startSnapshotTranslation.x + (endSnapshotTranslation.x - startSnapshotTranslation.x) * value
                snapshotView.translationY =
                    startSnapshotTranslation.y + (endSnapshotTranslation.y - startSnapshotTranslation.y) * value

                snapshotView.scaleX = startSnapshotScale + (endSnapshotScale - startSnapshotScale) * value
                snapshotView.scaleY = snapshotView.scaleX
            }
        }

        // 2.view
        val startViewAlpha = 1f
        val endViewAlpha = 0f

        val startViewTranslation = Point(visualStartBounds.left.toInt(), visualStartBounds.top.toInt())
        val endViewTranslation = Point(visualEndBounds.left.toInt(), visualEndBounds.top.toInt())

        val viewPivotX = 0f
        val viewPivotY = 0f
        val startViewScale = visualStartBounds.width() / startBounds.width()
        val endViewScale = visualEndBounds.width() * 1f / startBounds.width()

        val startClipBounds = view.clipBounds ?: Rect(0, 0, startBounds.width().toInt(), startBounds.height().toInt())
        val endClipBounds =
            Rect(0, 0, startBounds.width().toInt(), min((visualEndBounds.height() / endViewScale), startBounds.height()).toInt())

        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                val widthSpec = View.MeasureSpec.makeMeasureSpec(startBounds.width().toInt(), View.MeasureSpec.EXACTLY)
                val heightSpec = View.MeasureSpec.makeMeasureSpec(startBounds.height().toInt(), View.MeasureSpec.EXACTLY)
                view.measure(widthSpec, heightSpec)
                view.layout(startBounds.left.toInt(), startBounds.top.toInt(), startBounds.right.toInt(), startBounds.bottom.toInt())

                view.alpha = startViewAlpha

                view.translationX = startViewTranslation.x.toFloat()
                view.translationY = startViewTranslation.y.toFloat()

                view.pivotX = viewPivotX
                view.pivotY = viewPivotY
                view.scaleX = startViewScale
                view.scaleY = startViewScale

                view.clipBounds = startClipBounds
            }

            override fun onAnimationEnd(animation: Animator?) {
                view.alpha = endViewAlpha

                view.translationX = endViewTranslation.x.toFloat()
                view.translationY = endViewTranslation.y.toFloat()

                view.scaleX = endViewScale
                view.scaleY = endViewScale

                view.clipBounds = endClipBounds
            }
        })
        animator.addUpdateListener {
            val value = it.animatedValue as Float
            view.alpha = startViewAlpha + (endViewAlpha - startViewAlpha) * value

            view.translationX = startViewTranslation.x + (endViewTranslation.x - startViewTranslation.x) * value
            view.translationY = startViewTranslation.y + (endViewTranslation.y - startViewTranslation.y) * value

            view.scaleX = startViewScale + (endViewScale - startViewScale) * value
            view.scaleY = view.scaleX

            view.clipBounds = sRectEvaluator.evaluate(value, startClipBounds, endClipBounds)
        }

        return animator
    }

    /**
     * 做alpha动画
     */
    private fun createReturnEqualsAnimator(
        sceneRoot: ViewGroup,
        startValues: TransitionValues?,
        endValues: TransitionValues?
    ): Animator? {
        if (startValues?.view == null) {
            return null
        }
        val view = startValues.view
        val startBounds: RectF = startValues.values[PROPNAME_BOUNDS] as RectF
        val visualStartBounds = RectF(0f, 0f, startBounds.width(), startBounds.height())

        view.matrix.let {
            it.mapRect(visualStartBounds)
            visualStartBounds.offset(startBounds.left, startBounds.top)
        }

        val startViewAlpha = 1f
        val endViewAlpha = 0f

        val viewPivotX = 0f
        val viewPivotY = 0f
        val startViewScale = visualStartBounds.width() / startBounds.width()
        val endViewScale = min(0.1f, startViewScale)

        val startViewTranslation = Point(visualStartBounds.left.toInt(), visualStartBounds.top.toInt())
        val endViewTranslation =
            Point(
                (startBounds.width() / 2f - endViewScale * startBounds.width() / 2f).toInt(),
                (startBounds.height() / 2f - endViewScale * startBounds.height() / 2f).toInt()
            )

        val animator = ValueAnimator.ofFloat(0f, 1f)

        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                val widthSpec = View.MeasureSpec.makeMeasureSpec(startBounds.width().toInt(), View.MeasureSpec.EXACTLY)
                val heightSpec = View.MeasureSpec.makeMeasureSpec(startBounds.height().toInt(), View.MeasureSpec.EXACTLY)
                view.measure(widthSpec, heightSpec)
                view.layout(startBounds.left.toInt(), startBounds.top.toInt(), startBounds.right.toInt(), startBounds.bottom.toInt())

                view.alpha = startViewAlpha

                view.translationX = startViewTranslation.x.toFloat()
                view.translationY = startViewTranslation.y.toFloat()

                view.pivotX = viewPivotX
                view.pivotY = viewPivotY
                view.scaleX = startViewScale
                view.scaleY = startViewScale
            }

            override fun onAnimationEnd(animation: Animator?) {
                view.alpha = endViewAlpha

                view.translationX = endViewTranslation.x.toFloat()
                view.translationY = endViewTranslation.y.toFloat()

                view.scaleX = endViewScale
                view.scaleY = endViewScale
            }
        })
        animator.addUpdateListener {
            val value = it.animatedValue as Float
            view.alpha = startViewAlpha + (endViewAlpha - startViewAlpha) * value

            view.translationX = startViewTranslation.x + (endViewTranslation.x - startViewTranslation.x) * value
            view.translationY = startViewTranslation.y + (endViewTranslation.y - startViewTranslation.y) * value

            view.scaleX = startViewScale + (endViewScale - startViewScale) * value
            view.scaleY = view.scaleX
        }

        return animator
    }
}
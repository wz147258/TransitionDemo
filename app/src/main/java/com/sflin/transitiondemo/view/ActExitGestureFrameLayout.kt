package com.sflin.transitiondemo.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Context
import android.graphics.Color
import android.graphics.PointF
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.animation.Interpolator
import android.widget.FrameLayout
import androidx.annotation.FloatRange
import androidx.core.math.MathUtils.clamp
import com.sflin.transitiondemo.utis.dp2pxInt
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.sqrt


/**
 * 触发时是竖滑的话支持缩放和上下左右偏移，触发时是横滑的话只支持左右偏移不支持缩放和上下偏移。
 */
class ActExitGestureFrameLayout : FrameLayout {
    companion object {
        const val SHARED_ELEMENT_ANIM_RETURN_DURATION = 200L
        const val DRAGGING_RECOVER_ANIM_DURATION = 200L

        val DRAGGING_TRIGGER_EXIT_DISTANCE by lazy { 80f.dp2pxInt() }

        fun createDefaultInterpolator(): Interpolator {
            return CubicBezierInterpolator(PointF(0.5f, 1f), PointF(0.89f, 1f))
        }

        fun getLocationInAncestor(child: View?, ancestor: View?): IntArray? {
            if (child == null || ancestor == null) {
                return null
            }
            val location = IntArray(2)
            val position = FloatArray(2)
            position[1] = 0.0f
            position[0] = position[1]
            position[0] += child.left.toFloat()
            position[1] += child.top.toFloat()
            var matched = false
            var viewParent = child.parent
            while (viewParent is View) {
                val view = viewParent as View
                if (viewParent === ancestor) {
                    matched = true
                    break
                }
                position[0] -= view.scrollX.toFloat()
                position[1] -= view.scrollY.toFloat()
                position[0] += view.left.toFloat()
                position[1] += view.top.toFloat()
                viewParent = view.parent
            }
            if (!matched) {
                return null
            }
            location[0] = (position[0] + 0.5f).toInt()
            location[1] = (position[1] + 0.5f).toInt()
            return location
        }
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private var triggerView: View? = null

    private val touchSlop: Float = ViewConfiguration.get(context).scaledTouchSlop.toFloat()
    private val posInvalid: Float = -1f
    private var startX: Float = posInvalid
    private var startY: Float = posInvalid
    private var touchAnimView: Boolean = false
    private var descendantHandledMove: Boolean = false
    private var dragging: Boolean = false
    private var exiting: Boolean = false
    private var enableCheckTouch: Boolean = false

    private var recoverAnimator: ObjectAnimator? = null
    var dragListener: DragListener? = null
    var translucentListener: TranslucentListener? = null

    init {
        isClickable = true
    }

    var enableDragChangeBgAlpha = false

    fun doScaleOutCenterAnim(listener: Animator.AnimatorListener) {
        val startParentBgAlpha = currentBgAlpha
        val endParentBgAlpha = 0f

        animView()?.apply {
            val cX = scaleX
            val cY = scaleY
            val pX = pivotX
            val pY = pivotY
            val newPX = width.shr(1).toFloat()
            val newPY = height.shr(1).toFloat()
            pivotX = newPX
            pivotY = newPY
            ObjectAnimator.ofPropertyValuesHolder(
                this,
                PropertyValuesHolder.ofFloat(View.SCALE_X, cX, 0f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, cY, 0f),
                PropertyValuesHolder.ofFloat(View.TRANSLATION_X, translationX - (newPX - pX) * (1f - cX), 0f),
                PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, translationY - (newPY - pY) * (1f - cY), 0f),
                PropertyValuesHolder.ofFloat(View.ALPHA, 1f, 0f)
            ).apply {
                duration = SHARED_ELEMENT_ANIM_RETURN_DURATION
                interpolator = createDefaultInterpolator()
                addListener(listener)
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationStart(animation: Animator) {
                        translucentListener?.convertToTranslucent()
                    }
                })
                addUpdateListener {
                    if (startParentBgAlpha != null) {
                        setBgAlpha(startParentBgAlpha + (endParentBgAlpha - startParentBgAlpha) * it.animatedFraction)
                    }
                }
                start()
            }
        }
    }

    fun setTriggerDragView(triggerView: View?) {
        this.triggerView = triggerView
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (recoverAnimator?.isStarted == true) {
            return true
        }
        val dragged = dragging
        val action = event.action
        if (action == MotionEvent.ACTION_DOWN) {
//            enableCheckTouch = translucentListener?.isTranslucent() != false
            enableCheckTouch = true
        }
        val consumed = super.dispatchTouchEvent(event)
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            requestDisallowParentInterceptTouchEvent(false)
            if (dragged && !exiting) {
                animView()?.also { recoverAnimViewStatus(it) }
                dragListener?.onDragEnd()
            }
            startX = posInvalid
            startY = posInvalid
            touchAnimView = false
            descendantHandledMove = false
            dragging = false
        }
        return consumed
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        val dragListener = this.dragListener
        val triggerView = triggerView()
        var consumeMove = false
        if (enableCheckTouch && dragListener != null && dragListener.canDrag() && triggerView != null) {
            when (ev.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (pointInAncestor(ev.x.toInt(), ev.y.toInt(), triggerView, this)) {
                        requestDisallowParentInterceptTouchEvent(true)
                        startX = ev.x
                        startY = ev.y
                        touchAnimView = true
                    } else {
                        requestDisallowParentInterceptTouchEvent(false)
                        startX = posInvalid
                        startY = posInvalid
                        touchAnimView = false
                    }
                    descendantHandledMove = false
                    dragging = false
                }

                MotionEvent.ACTION_MOVE -> {
                    if (touchAnimView && !dragging && !descendantHandledMove) {
                        val deltaX = ev.x - startX
                        val deltaY = ev.y - startY
                        val absDeltaX = deltaX.absoluteValue
                        val absDeltaY = deltaY.absoluteValue
                        if (absDeltaX > touchSlop) {
                            if (descendantCanScrollX(triggerView, deltaX.toInt(), ev.x.toInt(), ev.y.toInt())) {
                                descendantHandledMove = true
                            } else {
                                consumeMove = if (deltaX > 0f) dragListener.canDragRight() else dragListener.canDragLeft()
                            }
                        }
                        if (absDeltaY > touchSlop && !descendantHandledMove) {
                            if (descendantCanScrollY(triggerView, deltaY.toInt(), ev.x.toInt(), ev.y.toInt())) {
                                descendantHandledMove = true
                                consumeMove = false
                            } else if (!consumeMove) {
                                consumeMove = if (deltaY > 0f) dragListener.canDragDown() else dragListener.canDragUp()
                            }
                        }
                    }
                }
            }
        }
        return dragging || consumeMove || super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        val dragListener = this.dragListener
        val triggerView = triggerView()
        if (enableCheckTouch && dragListener != null && dragListener.canDrag() && triggerView != null) {
            val x = ev.x
            val y = ev.y
            when (ev.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (pointInAncestor(x.toInt(), y.toInt(), triggerView, this)) {
                        requestDisallowParentInterceptTouchEvent(true)
                        startX = x
                        startY = y
                        touchAnimView = true
                    } else {
                        requestDisallowParentInterceptTouchEvent(false)
                        startX = posInvalid
                        startY = posInvalid
                        touchAnimView = false
                    }
                    descendantHandledMove = false
                    dragging = false
                }

                MotionEvent.ACTION_MOVE -> {
                    if (touchAnimView) {
                        val deltaX = x - startX
                        val deltaY = y - startY
                        if (!dragging && !descendantHandledMove) {
                            val absDeltaX = deltaX.absoluteValue
                            val absDeltaY = deltaY.absoluteValue
                            var drag = false
                            if (absDeltaX > touchSlop) {
                                if (descendantCanScrollX(triggerView, deltaX.toInt(), ev.x.toInt(), ev.y.toInt())) {
                                    descendantHandledMove = true
                                } else {
                                    drag = if (deltaX > 0f) dragListener.canDragRight() else dragListener.canDragLeft()
                                }
                            }
                            if (absDeltaY > touchSlop && !descendantHandledMove) {
                                if (descendantCanScrollY(triggerView, deltaY.toInt(), ev.x.toInt(), ev.y.toInt())) {
                                    descendantHandledMove = true
                                    drag = false
                                } else if (!drag) {
                                    drag = if (deltaY > 0f) dragListener.canDragDown() else dragListener.canDragUp()
                                }
                            }
                            if (drag) {
                                dragging = drag
                                animView()?.also { setViewPivot(it, startX, startY, deltaX, deltaY, dragListener) }
                                dragListener.onDragStart()
                                translucentListener?.convertToTranslucent()
                            }
                        }
                        if (dragging) {
                            animView()?.also { updateAnimViewStatus(it, deltaX, deltaY, dragListener) }
                        }
                    }
                }

                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (dragging) {
                        val deltaX = x - startX
                        val deltaY = y - startY
                        if (sqrt(deltaX * deltaX + deltaY * deltaY) > DRAGGING_TRIGGER_EXIT_DISTANCE) {
                            exiting = dragListener.triggerExit()
                        }
                    }
                }
            }
        }
        return dragging || super.onTouchEvent(ev)
    }

    private fun triggerView(): View? = triggerView ?: getChildAt(0)

    private fun animView(): View? = getChildAt(0)

    private fun updateAnimViewStatus(view: View, dx: Float, dy: Float, dragListener: DragListener) {
        val h = height
        val w = width
        if (h > 0 && w > 0) {
            val scale = (1f - max(dx.absoluteValue / w.toFloat(), dy.absoluteValue / h.toFloat())).coerceIn(0.5f, 1f)
            view.scaleX = scale
            view.scaleY = scale
            view.translationX = dx
            view.translationY = dy

            setBgAlpha((scale - 0.5f) / 0.5f)
        }
    }

    private fun recoverAnimViewStatus(view: View) {
        recoverAnimator?.takeIf { it.isStarted }?.cancel()
        val startParentBgAlpha = currentBgAlpha
        val endParentBgAlpha = 1f

        recoverAnimator = ObjectAnimator.ofPropertyValuesHolder(
            view,
            PropertyValuesHolder.ofFloat(View.SCALE_X, view.scaleX, 1f),
            PropertyValuesHolder.ofFloat(View.SCALE_Y, view.scaleY, 1f),
            PropertyValuesHolder.ofFloat(View.TRANSLATION_X, view.translationX, 0f),
            PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, view.translationY, 0f)
        ).apply {
            duration = DRAGGING_RECOVER_ANIM_DURATION
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    super.onAnimationEnd(animation)
                    resetViewPivot(view)
                    if (tempBg != null) {
                        setBgAlpha(1f)
                    }
                    translucentListener?.convertFromTranslucent()
                }
            })
            addUpdateListener {
                if (startParentBgAlpha != null) {
                    setBgAlpha(startParentBgAlpha + (endParentBgAlpha - startParentBgAlpha) * it.animatedFraction)
                }
            }
            start()
        }
    }

    private fun descendantCanScrollX(view: View, dx: Int, x: Int, y: Int): Boolean {
        if (view is ViewGroup) {
            /*if (view is IWebIntercept) {
                return true
            }*/
            for (i in 0 until view.childCount) {
                val child = view.getChildAt(i)
                val xForChild = x + view.scrollX
                val yForChild = y + view.scrollY
                if (pointInView(xForChild, yForChild, child) &&
                    descendantCanScrollX(child, dx, xForChild - child.left, yForChild - child.top)
                ) {
                    return true
                }
            }
        }
        return view.canScrollHorizontally(-dx)
    }

    private fun descendantCanScrollY(view: View, dy: Int, x: Int, y: Int): Boolean {
        if (view is ViewGroup) {
            /*if (view is IWebIntercept) {
                return true
            }*/
            val xForChild = x + view.scrollX
            val yForChild = y + view.scrollY
            for (i in 0 until view.childCount) {
                val child = view.getChildAt(i)
                if (pointInView(xForChild, yForChild, child) &&
                    descendantCanScrollY(child, dy, xForChild - child.left, yForChild - child.top)
                ) {
                    return true
                }
            }
        }
        return view.canScrollVertically(-dy)
    }

    private fun pointInAncestor(x: Int, y: Int, view: View, ancestor: View): Boolean {
        val viewWidth = view.width
        val viewHeight = view.height
        if (view.visibility != VISIBLE || viewWidth <= 0 || viewHeight <= 0) {
            return false
        }
        val locations: IntArray? = getLocationInAncestor(view, ancestor)
        return if (locations == null) {
            false
        } else {
            x >= locations[0] &&
                    x < locations[0] + viewWidth &&
                    y >= locations[1] &&
                    y < locations[1] + viewHeight
        }
    }

    private fun pointInView(x: Int, y: Int, view: View): Boolean {
        if (view.visibility != VISIBLE || view.right <= view.left || view.bottom <= view.top) {
            return false
        }
        return x >= view.left &&
                x < view.right &&
                y >= view.top &&
                y < view.bottom
    }

    private fun setViewPivot(view: View, x: Float, y: Float, dx: Float, dy: Float, dragListener: DragListener) {
        val locations: IntArray? = getLocationInAncestor(view, this)
        if (locations == null) {
            resetViewPivot(view)
        } else {
            view.pivotX = x - locations[0]
            view.pivotY = y - locations[1]
        }
    }

    private fun resetViewPivot(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            view.resetPivot()
        } else {
            view.pivotX = view.width.shr(1).toFloat()
            view.pivotY = view.height.shr(1).toFloat()
        }
    }

    private fun requestDisallowParentInterceptTouchEvent(disallowIntercept: Boolean) {
        parent?.requestDisallowInterceptTouchEvent(disallowIntercept)
    }

    private var tempBg: Drawable? = null
    private val currentBgAlpha: Float?
        get() {
            if (tempBg == null) {
                return null
            }
            return tempBg!!.alpha / 255f
        }

    private fun setBgAlpha(@FloatRange(from = 0.0, to = 1.0) alpha: Float) {
        if (!enableDragChangeBgAlpha) {
            return
        }
        val finalAlpha: Int = (clamp(alpha, 0f, 1f) * 255).toInt()
        val background: Drawable = if (background == null) {
            ColorDrawable(Color.BLACK).apply {
                background = this
            }
        } else {
            if (tempBg == background) {
                tempBg!!
            } else {
                background.mutate().apply {
                    background = this
                }
            }
        }
        tempBg = background
        background.alpha = finalAlpha
    }

    interface DragListener {
        fun canDrag(): Boolean

        /**
         * 手指向右滑
         */
        fun canDragRight(): Boolean
        fun canDragLeft(): Boolean

        /**
         * 手指向下滑
         */
        fun canDragDown(): Boolean
        fun canDragUp(): Boolean
        fun triggerHorExit(): Boolean

        /**
         * 处理退出回调
         */
        fun triggerExit(): Boolean
        fun onDragStart()
        fun onDragEnd()

        open class DragListenerStub : DragListener {
            override fun canDrag(): Boolean = true

            override fun canDragRight(): Boolean = false

            override fun canDragLeft(): Boolean = false

            override fun canDragDown(): Boolean = false

            override fun canDragUp(): Boolean = false

            override fun triggerHorExit(): Boolean = triggerExit()

            override fun triggerExit(): Boolean = false

            override fun onDragStart() {}

            override fun onDragEnd() {}
        }
    }

    interface TranslucentListener {
        fun isTranslucent(): Boolean
        fun convertToTranslucent()
        fun convertFromTranslucent()
    }
}
package com.sflin.transitiondemo.view

import android.animation.Animator
import android.app.Activity
import android.app.ActivityOptions
import android.os.Build
import android.view.View
import android.view.ViewGroup
import com.sflin.transitiondemo.R
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

/**
 * @author zhuangyouxin on 2022/7/20
 *
 */
class ActExitGesturePresenter constructor(private val activity: Activity) {

    private var moveView: View? = null
    private var gestureLayout: ActExitGestureFrameLayout? = null

    private var transformStartSlide: Boolean = false

    private var mTranslucentConversionListenerClass: Class<*>? = null
    private var mTranslucentConversionListener: Any? = null

    @JvmOverloads
    fun initGestureListener(
        targetView: View? = null,
        dragListener: ActExitGestureFrameLayout.DragListener,
        config: (ActExitGestureFrameLayout.() -> Unit)? = null
    ) {
        if (gestureLayout != null) {
            return
        }

        val rootParent: ViewGroup
        val rootView: View
        if (targetView != null) {
            rootParent = targetView.parent as? ViewGroup ?: return
            rootView = targetView
        } else {
            rootParent = activity.findViewById<View>(android.R.id.content) as? ViewGroup ?: return
            rootView = rootParent.getChildAt(0) ?: return
        }
        val rootViewIndexInParent = rootParent.indexOfChild(rootView)

        val gestureLayout = ActExitGestureFrameLayout(activity).also {
            it.dragListener = dragListener
            it.translucentListener = object : ActExitGestureFrameLayout.TranslucentListener {
                override fun isTranslucent(): Boolean = transformStartSlide

                override fun convertToTranslucent() {
                    this@ActExitGesturePresenter.convertToTranslucent()
                }

                override fun convertFromTranslucent() {
                    this@ActExitGesturePresenter.convertFromTranslucent()
                }
            }
            config?.invoke(it)
        }

        val rootViewLP =
            rootView.layoutParams ?: ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        rootParent.removeViewAt(rootViewIndexInParent)
        gestureLayout.addView(rootView, rootViewLP)
        rootParent.addView(
            gestureLayout,
            rootViewIndexInParent,
            ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        )
        this.moveView = rootView
        this.gestureLayout = gestureLayout
    }

    fun setTriggerDragView(animView: View?) {
        gestureLayout?.setTriggerDragView(animView)
    }

    fun enterTransitionEnd() {
        convertToTranslucent()
    }

    fun doScaleOutCenterAnim(listener: Animator.AnimatorListener) {
        gestureLayout?.doScaleOutCenterAnim(listener)
    }

    /**
     * 利用反射将window转为不透明
     */
    private fun convertFromTranslucent() {
        if (activity.isTaskRoot) {
            //当前为根Activity，不允许窗口透明转换
            return
        }
        try {
            val convertFromTranslucent = Activity::class.java.getDeclaredMethod("convertFromTranslucent")
            convertFromTranslucent.isAccessible = true
            convertFromTranslucent.invoke(activity)
            transformStartSlide = false
        } catch (ignored: Throwable) {
        }
    }

    /**
     * 利用反射将window转为透明
     */
    private fun convertToTranslucent() {
        if (activity.isTaskRoot) {
            //当前为根Activity，不允许窗口透明转换
            return
        }
        //获取透明转换监听类
        val listenerClass = getTranslucentConversionListenerClass()
        //获取透明转换监听对象，回调时标记转换完成
        val listener = getTranslucentConversionListener(listenerClass)
        //若监听器为null，直接标记透明转换已完成，否则标记未完成
        setTranslucentCompleted(listener == null)
        try {
            // Android5.0开始，窗口透明转换API有改动，这里要做区分
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                var options: Any? = null
                try {
                    //反射获取ActivityOptions对象
                    val getActivityOptions = Activity::class.java.getDeclaredMethod("getActivityOptions")
                    getActivityOptions.isAccessible = true
                    options = getActivityOptions.invoke(activity)
                } catch (ignored: Exception) {
                }
                val convertToTranslucent =
                    Activity::class.java.getDeclaredMethod("convertToTranslucent", listenerClass, ActivityOptions::class.java)
                convertToTranslucent.isAccessible = true
                convertToTranslucent.invoke(activity, listener, options)
            } else {
                val convertToTranslucent = Activity::class.java.getDeclaredMethod("convertToTranslucent", listenerClass)
                convertToTranslucent.isAccessible = true
                convertToTranslucent.invoke(activity, listener)
            }
        } catch (ignored: Throwable) {
            setTranslucentCompleted(false)
        }
    }

    /**
     * 获取Window透明转换监听的class
     */
    private fun getTranslucentConversionListenerClass(): Class<*>? {
        if (mTranslucentConversionListenerClass == null) {
            for (clazz in Activity::class.java.declaredClasses) {
                if (clazz.simpleName == "TranslucentConversionListener") {
                    mTranslucentConversionListenerClass = clazz
                }
            }
        }
        return mTranslucentConversionListenerClass
    }

    /**
     * 获取Window透明转换监听器，在回调时通过[.setTranslucentCompleted]标记转换已完成
     */
    private fun getTranslucentConversionListener(translucentConversionListenerClass: Class<*>?): Any? {
        if (mTranslucentConversionListener == null && translucentConversionListenerClass != null) {
            mTranslucentConversionListener = Proxy.newProxyInstance(
                translucentConversionListenerClass.classLoader,
                arrayOf(translucentConversionListenerClass),
                object : InvocationHandler {
                    override fun invoke(proxy: Any?, method: Method?, args: Array<out Any>?): Any? {
                        //标记转换已完成
                        setTranslucentCompleted(true)
                        transformStartSlide = true
                        return null
                    }
                }
            )
        }
        return mTranslucentConversionListener
    }

    /**
     * 标记透明转换是否完成
     */
    private fun setTranslucentCompleted(completed: Boolean) {
        if (!completed || moveView == null) {
            return
        }
        activity.window.setBackgroundDrawableResource(R.color.transparent)
    }
}
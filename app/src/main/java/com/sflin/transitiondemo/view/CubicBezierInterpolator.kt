package com.sflin.transitiondemo.view

import android.graphics.PointF
import android.view.animation.Interpolator

class CubicBezierInterpolator(point1: PointF, point2: PointF) : Interpolator {

    companion object {
        private const val ACCURACY = 4096

        /**
         * @return 返回一个0.25,0.1,0.25,1的三次贝塞尔曲线插值器
         */
        fun getDefaultInterpolator(): CubicBezierInterpolator = CubicBezierInterpolator(PointF(0.25f, 0.1f), PointF(0.25f, 1f))
    }

    private val x1 = point1.x.toDouble()
    private val y1 = point1.y.toDouble()
    private val x2 = point2.x.toDouble()
    private val y2 = point2.y.toDouble()
    private var lastI = 0
    private var lastInput = 0f

    override fun getInterpolation(input: Float): Float {
        var t = input.toDouble()
        // 近似求解t的值[0,1]
        if (input > lastInput) {
            for (i in lastI until ACCURACY) {
                t = i.toDouble() / ACCURACY
                val x: Double = cubicCurves(t, 0.0, x1, x2, 1.0)
                if (x >= input) {
                    lastI = i
                    break
                }
            }
        } else {
            for (i in lastI downTo 0) {
                t = i.toDouble() / ACCURACY
                val x: Double = cubicCurves(t, 0.0, x1, x2, 1.0)
                if (x <= input) {
                    lastI = i
                    break
                }
            }
        }

        lastInput = input
        return cubicCurves(t, 0.0, y1, y2, 1.0).toFloat()
    }

    private fun cubicCurves(
        t: Double, value0: Double, value1: Double,
        value2: Double, value3: Double
    ): Double {
        var value: Double
        val u = 1 - t
        val tt = t * t
        val uu = u * u
        val uuu = uu * u
        val ttt = tt * t
        value = value0 * uuu
        value += 3 * value1 * uu * t
        value += 3 * value2 * u * tt
        value += value3 * ttt
        return value
    }
}
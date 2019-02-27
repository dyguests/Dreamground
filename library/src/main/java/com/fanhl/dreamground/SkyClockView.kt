package com.fanhl.dreamground

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.Nullable

/**
 * 天空
 *
 * 用天空来显示时钟
 */
class SkyClockView @JvmOverloads constructor(
    context: Context,
    @Nullable attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val hourDialPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
        }
    }

    /** 时针中心坐标 */
    private val hourCenter = PointF()
    private var hourDialRadius = 0f

    @ColorInt
    private val backgroundColor = Color.BLUE
    @Dimension(unit = Dimension.PX)
    private val hourDialStrokeWidth = 20f
    @ColorInt
    private val hourDialStrokeColor = Color.WHITE

    init {
        hourDialPaint.apply {
            strokeWidth = hourDialStrokeWidth
            color = hourDialStrokeColor
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        val validWidth = w - paddingLeft - paddingRight
        val validHeight = h - paddingTop - paddingBottom

        hourCenter.apply {
            x = paddingLeft + validWidth / 2f
            y = paddingTop + validHeight * 1.25f
        }
        hourDialRadius = validHeight * 0.75f
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas ?: return)
        canvas.drawColor(Color.RED)
        drawHourDial(canvas)
        invalidate()
    }

    private fun drawHourDial(canvas: Canvas) {
        canvas.drawCircle(hourCenter.x, hourCenter.y, hourDialRadius, hourDialPaint)
    }
}
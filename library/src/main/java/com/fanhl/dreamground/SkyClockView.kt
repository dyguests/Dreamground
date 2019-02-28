package com.fanhl.dreamground

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
import androidx.annotation.IntRange
import androidx.annotation.Nullable
import java.util.*

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

    // ---------- 固定值 ----------

    private val hourDialPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
        }
    }
    private val hourTextPaint by lazy {
        Paint()
    }

    // ---------- 输入参数 ----------

    @ColorInt
    private val backgroundColor = Color.BLUE
    @Dimension(unit = Dimension.PX)
    private val hourDialStrokeWidth = 10f
    @ColorInt
    private val hourDialColor = Color.WHITE
    @Dimension(unit = Dimension.PX)
    private val hourTextSize = 100f
    /** 是否使用24小时制 */
    private var is24Hour = true

    // ---------- 变量 ----------

    /** 时针中心坐标 */
    private val hourCenter = PointF()
    private var hourDialRadius = 0f
    /**刻度的间距角度*/
    var spaceAngle = 0f
    /** 时针方向（表盘） */
    private var hourDegree = 0f
    /** 0 为 am,1 为 pm */
    @IntRange(from = 0, to = 1)
    private var amPm = 0

    // ---------- 临时变量区 ----------

    private val tmpRect = Rect()
    private val tmpRectF = RectF()

    init {

        hourDialPaint.apply {
            strokeWidth = hourDialStrokeWidth
            color = hourDialColor
            strokeCap = Paint.Cap.ROUND
        }
        hourTextPaint.apply {
            textSize = hourTextSize
            color = hourDialColor
        }

        setBackgroundColor(backgroundColor)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        val validWidth = w - paddingLeft - paddingRight
        val validHeight = h - paddingTop - paddingBottom

        hourCenter.apply {
            x = paddingLeft + validWidth / 2f
            y = paddingTop + validHeight * 1.25f
        }
        hourDialRadius = validHeight * 1f

        hourTextPaint.getTextBounds("24", 0, 2, tmpRect)
        spaceAngle = (tmpRect.width() * 360f / 2 / Math.PI / hourDialRadius * 1.5f/*额外空余百分比*/).toFloat()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas ?: return)
        updateTimeDegree()
        drawHourDial(canvas)
        invalidate()
    }

    private fun updateTimeDegree() {
        val calendar = Calendar.getInstance()
        val milliSecond = calendar.get(Calendar.MILLISECOND)
        val second = calendar.get(Calendar.SECOND) + milliSecond / 1000f
        val minute = calendar.get(Calendar.MINUTE) + second / 60f
        val hour = calendar.get(Calendar.HOUR) + minute / 60f

        hourDegree = hour / 12f * 360
        amPm = hour.toInt() / 12
    }

    private fun drawHourDial(canvas: Canvas) {
        val saveCount = canvas.save()

        // 表盘刻线
        tmpRectF.apply {
            left = hourCenter.x - hourDialRadius
            top = hourCenter.y - hourDialRadius
            right = hourCenter.x + hourDialRadius
            bottom = hourCenter.y + hourDialRadius
        }

        //先旋转画布（使表盘旋转）
        canvas.rotate(-hourDegree, hourCenter.x, hourCenter.y)

        for (i in 0 until 12) {
            // 表盘刻线
            canvas.drawArc(tmpRectF, -90f + spaceAngle / 2f, 30f - spaceAngle, false, hourDialPaint)

            val timeText = getHourText(i)
            hourTextPaint.getTextBounds(timeText, 0, timeText.length, tmpRect)

            canvas.drawText(timeText, hourCenter.x - tmpRect.exactCenterX(), hourCenter.y - hourDialRadius - tmpRect.exactCenterY(), hourTextPaint)
            canvas.rotate(30f, hourCenter.x, hourCenter.y)
        }

        canvas.restoreToCount(saveCount)
    }

    /**
     * @param i 第几个文字（从正上方的12点钟开始）
     */
    private fun getHourText(i: Int): String {
        var hourInt = i
        if (hourInt == 0) {
            hourInt += 12
        }

        return if (is24Hour) {
            if (amPm == 1) {
                hourInt += 12
            }
            hourInt.toString()
        } else {
            hourInt.toString() /*+ if (amPm == 0) "\nam" else "\npm"*/
        }
    }
}
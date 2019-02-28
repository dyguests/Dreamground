package com.fanhl.dreamground

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.text.TextPaint
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
        TextPaint()
    }
    private val minuteDialPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
        }
    }
    private val minuteTextPaint by lazy {
        TextPaint()
    }
    private val secondDialPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
        }
    }
    private val secondTextPaint by lazy {
        TextPaint()
    }
    private val handPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG)
    }

    // ---------- 输入参数 ----------

    @ColorInt
    private var darkColor: Int = 0
    @ColorInt
    private var lightColor: Int = 0
    @ColorInt
    private var mBackgroundColor = 0
    @Dimension(unit = Dimension.PX)
    private val hourDialStrokeWidth = 4f
    @ColorInt
    private var hourDialColor = 0
    @Dimension(unit = Dimension.PX)
    private val hourTextSize = 40f
    @Dimension(unit = Dimension.PX)
    private val minuteDialStrokeWidth = 4f
    @ColorInt
    private var minuteDialColor = 0
    @Dimension(unit = Dimension.PX)
    private val minuteTextSize = 35f
    @Dimension(unit = Dimension.PX)
    private val secondDialStrokeWidth = 4f
    @ColorInt
    private var secondDialColor = 0
    @Dimension(unit = Dimension.PX)
    private val secondTextSize = 30f
    /** 是否使用24小时制 */
    private var is24Hour = true

    // ---------- 变量 ----------

    /** 时针中心坐标 */
    private val hourCenter = PointF()
    private var hourDialRadius = 0f
    /**刻度的间距角度*/
    private var hourSpaceAngle = 0f
    /** 分钟中心坐标 */
    private val minuteCenter = PointF()
    private var minuteDialRadius = 0f
    /**刻度的间距角度*/
    private var minuteSpaceAngle = 0f
    /** 秒钟中心坐标 */
    private val secondCenter = PointF()
    private var secondDialRadius = 0f
    /**刻度的间距角度*/
    private var secondSpaceAngle = 0f

    /** 时针方向（表盘） */
    private var hourDegree = 0f
    private var minuteDegree = 0f
    private var secondDegree = 0f
    /** 0 为 am,1 为 pm */
    @IntRange(from = 0, to = 1)
    private var amPm = 0

    // ---------- 临时变量区 ----------

    private val tmpRect = Rect()
    private val tmpRectF = RectF()
    private val tmpPath = Path()

    init {
        darkColor = Color.parseColor("#80ffffff")
        lightColor = Color.parseColor("#ffffff")
        mBackgroundColor = Color.parseColor("#237EAD")

        hourDialColor = darkColor
        minuteDialColor = darkColor
        secondDialColor = darkColor

        hourDialPaint.apply {
            strokeWidth = hourDialStrokeWidth
            color = hourDialColor
            strokeCap = Paint.Cap.ROUND
        }
        hourTextPaint.apply {
            textSize = hourTextSize
            color = hourDialColor
        }
        minuteDialPaint.apply {
            strokeWidth = minuteDialStrokeWidth
            color = minuteDialColor
            strokeCap = Paint.Cap.ROUND
        }
        minuteTextPaint.apply {
            textSize = minuteTextSize
            color = minuteDialColor
        }
        secondDialPaint.apply {
            strokeWidth = secondDialStrokeWidth
            color = secondDialColor
            strokeCap = Paint.Cap.ROUND
        }
        secondTextPaint.apply {
            textSize = secondTextSize
            color = secondDialColor
        }
        handPaint.apply {
            color = darkColor
        }

        setBackgroundColor(mBackgroundColor)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        val side = minOf(w - paddingLeft - paddingRight, h - paddingTop - paddingBottom)

        val validWidth = side
        val validHeight = side

        hourCenter.apply {
            x = paddingLeft + validWidth / 2f
            y = paddingTop + validHeight * 0.67f
        }
        hourDialRadius = hourCenter.y - (paddingTop + validHeight * 0.3f)
        hourTextPaint.getTextBounds("24", 0, 2, tmpRect)
        hourSpaceAngle = (tmpRect.width() * 360f / 2 / Math.PI / hourDialRadius * 1.5f/*额外空余百分比*/).toFloat()

        minuteCenter.apply {
            x = paddingLeft + validWidth / 2f
            y = paddingTop + validHeight * 0.684f
        }
        minuteDialRadius = minuteCenter.y - (paddingTop + validHeight * 0.378f)
        minuteTextPaint.getTextBounds("60", 0, 2, tmpRect)
        minuteSpaceAngle = (tmpRect.width() * 360f / 2 / Math.PI / minuteDialRadius * 1.5f/*额外空余百分比*/).toFloat()

        secondCenter.apply {
            x = paddingLeft + validWidth / 2f
            y = paddingTop + validHeight * 0.7f
        }
        secondDialRadius = secondCenter.y - (paddingTop + validHeight * 0.5f)
        secondTextPaint.getTextBounds("60", 0, 2, tmpRect)
        secondSpaceAngle = (tmpRect.width() * 360f / 2 / Math.PI / secondDialRadius * 1.5f/*额外空余百分比*/).toFloat()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas ?: return)
        updateTimeDegree()
        drawHourDial(canvas)
        drawHourHand(canvas)
        drawMinuteDial(canvas)
        drawMinuteHand(canvas)
        drawSecondDial(canvas)
        drawSecondHand(canvas)
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
        minuteDegree = minute / 60f * 360
        secondDegree = second / 60f * 360
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
            canvas.drawArc(tmpRectF, -90f + hourSpaceAngle / 2f, 30f - hourSpaceAngle, false, hourDialPaint)

            val timeText = getHourText(i)
            hourTextPaint.getTextBounds(timeText, 0, timeText.length, tmpRect)

            canvas.drawText(timeText, hourCenter.x - tmpRect.exactCenterX(), hourCenter.y - hourDialRadius - tmpRect.exactCenterY(), hourTextPaint)
            canvas.rotate(30f, hourCenter.x, hourCenter.y)
        }

        canvas.restoreToCount(saveCount)
    }

    private fun drawHourHand(canvas: Canvas) {
        tmpPath.reset()
        tmpPath.moveTo(hourCenter.x, hourCenter.y - hourDialRadius + secondDialRadius * 0.15f)
        tmpPath.lineTo(hourCenter.x + secondDialRadius * 0.05f, hourCenter.y - hourDialRadius + secondDialRadius * 0.25f)
        tmpPath.lineTo(hourCenter.x - secondDialRadius * 0.05f, hourCenter.y - hourDialRadius + secondDialRadius * 0.25f)
        tmpPath.close()
        canvas.drawPath(tmpPath, handPaint)
    }

    private fun drawMinuteDial(canvas: Canvas) {
        val saveCount = canvas.save()

        // 表盘刻线
        tmpRectF.apply {
            left = minuteCenter.x - minuteDialRadius
            top = minuteCenter.y - minuteDialRadius
            right = minuteCenter.x + minuteDialRadius
            bottom = minuteCenter.y + minuteDialRadius
        }

        //先旋转画布（使表盘旋转）
        canvas.rotate(-minuteDegree, minuteCenter.x, minuteCenter.y)

        val timesPerUnit = 5
        val frequency = 12 * timesPerUnit
        for (i in 0 until frequency) {
            if (i % timesPerUnit != 0) {
                canvas.drawLine(
                    minuteCenter.x, minuteCenter.y - minuteDialRadius - minuteDialRadius * 0.03f,
                    minuteCenter.x, minuteCenter.y - minuteDialRadius + minuteDialRadius * 0.03f,
                    minuteDialPaint
                )
            } else {
                val timeText = getMinuteText(i / timesPerUnit)
                minuteTextPaint.getTextBounds(timeText, 0, timeText.length, tmpRect)

                canvas.drawText(timeText, minuteCenter.x - tmpRect.exactCenterX(), minuteCenter.y - minuteDialRadius - tmpRect.exactCenterY(), minuteTextPaint)
            }

            canvas.rotate(360f / frequency, minuteCenter.x, minuteCenter.y)
        }

        canvas.restoreToCount(saveCount)
    }

    private fun drawMinuteHand(canvas: Canvas) {
        tmpPath.reset()
        tmpPath.moveTo(minuteCenter.x, minuteCenter.y - minuteDialRadius + secondDialRadius * 0.15f)
        tmpPath.lineTo(minuteCenter.x + secondDialRadius * 0.05f, minuteCenter.y - minuteDialRadius + secondDialRadius * 0.25f)
        tmpPath.lineTo(minuteCenter.x - secondDialRadius * 0.05f, minuteCenter.y - minuteDialRadius + secondDialRadius * 0.25f)
        tmpPath.close()
        canvas.drawPath(tmpPath, handPaint)
    }

    private fun drawSecondDial(canvas: Canvas) {
        val saveCount = canvas.save()

        // 表盘刻线
        tmpRectF.apply {
            left = secondCenter.x - secondDialRadius
            top = secondCenter.y - secondDialRadius
            right = secondCenter.x + secondDialRadius
            bottom = secondCenter.y + secondDialRadius
        }

        //先旋转画布（使表盘旋转）
        canvas.rotate(-secondDegree, secondCenter.x, secondCenter.y)

        val timesPerUnit = 5 * 2
        val frequency = 12 * timesPerUnit
        for (i in 0 until frequency) {
            if (i % timesPerUnit != 0) {
                canvas.drawLine(
                    secondCenter.x, secondCenter.y - secondDialRadius,
                    secondCenter.x, secondCenter.y - secondDialRadius + secondDialRadius * 0.1f,
                    secondDialPaint
                )
            } else {
                canvas.drawLine(
                    secondCenter.x, secondCenter.y - secondDialRadius - secondDialRadius * 0.1f,
                    secondCenter.x, secondCenter.y - secondDialRadius + secondDialRadius * 0.1f,
                    secondDialPaint
                )

                val timeText = getSecondText(i / timesPerUnit)
                secondTextPaint.getTextBounds(timeText, 0, timeText.length, tmpRect)

                canvas.drawText(timeText, secondCenter.x - tmpRect.exactCenterX(), secondCenter.y - secondDialRadius - secondDialRadius * 0.1f - tmpRect.height() / 2f, secondTextPaint)
            }

            canvas.rotate(360f / frequency, secondCenter.x, secondCenter.y)
        }

        canvas.restoreToCount(saveCount)
    }

    private fun drawSecondHand(canvas: Canvas) {
        tmpPath.reset()
        tmpPath.moveTo(secondCenter.x, secondCenter.y - secondDialRadius + secondDialRadius * 0.15f)
        tmpPath.lineTo(secondCenter.x + secondDialRadius * 0.05f, secondCenter.y - secondDialRadius + secondDialRadius * 0.25f)
        tmpPath.lineTo(secondCenter.x - secondDialRadius * 0.05f, secondCenter.y - secondDialRadius + secondDialRadius * 0.25f)
        tmpPath.close()
        canvas.drawPath(tmpPath, handPaint)
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

    /**
     * @param i 第几个文字（从正上方的60分开始）
     */
    private fun getMinuteText(i: Int): String {
        var minuteInt = i
        if (minuteInt == 0) {
            minuteInt += 12
        }

        return (minuteInt * 5).toString()
    }

    /**
     * @param i 第几个文字（从正上方的60分开始）
     */
    private fun getSecondText(i: Int): String {
        var secondInt = i
        if (secondInt == 0) {
            secondInt += 12
        }

        return (secondInt * 5).toString()
    }
}
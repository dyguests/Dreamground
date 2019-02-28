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
    private val minuteDialPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
        }
    }
    private val minuteTextPaint by lazy {
        Paint()
    }
    private val secondDialPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
        }
    }
    private val secondTextPaint by lazy {
        Paint()
    }

    // ---------- 输入参数 ----------

    @ColorInt
    private var darkColor: Int = 0
    @ColorInt
    private var lightColor: Int = 0
    @ColorInt
    private var mBackgroundColor = 0
    @Dimension(unit = Dimension.PX)
    private val hourDialStrokeWidth = 10f
    @ColorInt
    private val hourDialColor = Color.WHITE
    @Dimension(unit = Dimension.PX)
    private val hourTextSize = 100f
    @Dimension(unit = Dimension.PX)
    private val minuteDialStrokeWidth = 8f
    @ColorInt
    private val minuteDialColor = Color.WHITE
    @Dimension(unit = Dimension.PX)
    private val minuteTextSize = 80f
    @Dimension(unit = Dimension.PX)
    private val secondDialStrokeWidth = 4f
    @ColorInt
    private var secondDialColor = 0
    @Dimension(unit = Dimension.PX)
    private val secondTextSize = 40f
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

    init {
        darkColor = Color.parseColor("#80ffffff")
        lightColor = Color.parseColor("#ffffff")
        mBackgroundColor = Color.parseColor("#237EAD")



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

        setBackgroundColor(mBackgroundColor)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        val validWidth = w - paddingLeft - paddingRight
        val validHeight = h - paddingTop - paddingBottom

        hourCenter.apply {
            x = paddingLeft + validWidth / 2f
            y = paddingTop + validHeight * 1.3f
        }
        hourDialRadius = hourCenter.y - (paddingTop + validHeight * 0.167f)
        hourTextPaint.getTextBounds("24", 0, 2, tmpRect)
        hourSpaceAngle = (tmpRect.width() * 360f / 2 / Math.PI / hourDialRadius * 1.5f/*额外空余百分比*/).toFloat()

        minuteCenter.apply {
            x = paddingLeft + validWidth / 2f
            y = paddingTop + validHeight * 1f
        }
        minuteDialRadius = minuteCenter.y - (paddingTop + validHeight * 0.33f)
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
        drawMinuteDial(canvas)
        drawSecondDial(canvas)
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

        for (i in 0 until 12) {
            // 表盘刻线
            canvas.drawArc(tmpRectF, -90f + minuteSpaceAngle / 2f, 30f - minuteSpaceAngle, false, minuteDialPaint)

            val timeText = getMinuteText(i)
            minuteTextPaint.getTextBounds(timeText, 0, timeText.length, tmpRect)

            canvas.drawText(timeText, minuteCenter.x - tmpRect.exactCenterX(), minuteCenter.y - minuteDialRadius - tmpRect.exactCenterY(), minuteTextPaint)
            canvas.rotate(30f, minuteCenter.x, minuteCenter.y)
        }

        canvas.restoreToCount(saveCount)
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

        val timesPerHourUnit = 5 * 2
        val frequency = 12 * timesPerHourUnit
        for (i in 0 until frequency) {
            if (i % timesPerHourUnit != 0) {
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

                val timeText = getSecondText(i / timesPerHourUnit)
                secondTextPaint.getTextBounds(timeText, 0, timeText.length, tmpRect)

                canvas.drawText(timeText, secondCenter.x - tmpRect.exactCenterX(), secondCenter.y - secondDialRadius - secondDialRadius * 0.1f - tmpRect.height() / 2f, secondTextPaint)
            }

            canvas.rotate(360f / frequency, secondCenter.x, secondCenter.y)
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
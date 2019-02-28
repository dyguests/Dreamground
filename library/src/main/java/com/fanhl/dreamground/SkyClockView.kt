package com.fanhl.dreamground

import android.animation.PropertyValuesHolder
import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Camera
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.text.TextPaint
import android.util.AttributeSet
import android.view.MotionEvent
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

    /* 时钟半径，不包括padding值 */
    private var mRadius: Float = 0f
    /* 触摸时作用在Camera的矩阵 */
    private val mCameraMatrix = Matrix()
    /* 照相机，用于旋转时钟实现3D效果 */
    private val mCamera = Camera()
    /* camera绕X轴旋转的角度 */
    private var mCameraRotateX: Float = 0f
    /* camera绕Y轴旋转的角度 */
    private var mCameraRotateY: Float = 0f
    /* camera旋转的最大角度 */
    private val mMaxCameraRotate = 10f
    /* 指针的在x轴的位移 */
    private var mCanvasTranslateX: Float = 0f
    /* 指针的在y轴的位移 */
    private var mCanvasTranslateY: Float = 0f
    /* 指针的最大位移 */
    private var mMaxCanvasTranslate: Float = 0f
    /* 手指松开时时钟晃动的动画 */
    private var mShakeAnim: ValueAnimator? = null
    /** 手指按下时的位置 */
    private val downPointF = PointF()

    // ---------- 临时变量区 ----------

    private val tmpRect = Rect()
    private val tmpRectF = RectF()
    private val tmpPath = Path()
    private val tmpPointF = PointF()

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

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                if (mShakeAnim?.isRunning == true) {
                    mShakeAnim?.cancel()
                }
                downPointF.apply {
                    x = event.x
                    y = event.y
                }
//                getCameraRotate(event)
//                getCanvasTranslate(event)
            }
            MotionEvent.ACTION_MOVE -> {
                //根据手指坐标计算camera应该旋转的大小
                tmpPointF.apply {
                    x = event.x - downPointF.x
                    y = event.y - downPointF.y
                }
                getCameraRotate(tmpPointF)
                getCanvasTranslate(tmpPointF)
            }
            MotionEvent.ACTION_UP ->
                //松开手指，时钟复原并伴随晃动动画
                startShakeAnim()
        }
        return true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(measureDimension(widthMeasureSpec), measureDimension(heightMeasureSpec))
    }

    private fun measureDimension(measureSpec: Int): Int {
        var result: Int
        val mode = View.MeasureSpec.getMode(measureSpec)
        val size = View.MeasureSpec.getSize(measureSpec)
        if (mode == View.MeasureSpec.EXACTLY) {
            result = size
        } else {
            result = 800
            if (mode == View.MeasureSpec.AT_MOST) {
                result = Math.min(result, size)
            }
        }
        return result
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        val valid1Width = w - paddingLeft - paddingRight
        val valid1Height = h - paddingTop - paddingBottom
        val side = minOf(valid1Width, valid1Height)

        mRadius = side / 2f
        mMaxCanvasTranslate = 0.02f * mRadius

        val validWidth = side
        val validHeight = side

        val mPaddingLeft = paddingLeft + (valid1Width - validWidth) / 2f
        val mPaddingTop = paddingTop + (valid1Height - validHeight) / 2f
//        val mPaddingRight = paddingRight - (valid1Width - validWidth) / 2f
//        val mPaddingBottom = paddingBottom - (valid1Height - validHeight) / 2f

        val hourCenterY = mPaddingTop + validHeight * 0.67f
        val totalOffsetY = hourCenterY - (mPaddingTop + validHeight * 0.5f)

        hourCenter.apply {
            x = mPaddingLeft + validWidth / 2f
            y = hourCenterY - totalOffsetY
        }
        hourDialRadius = hourCenter.y - (mPaddingTop + validHeight * 0.3f) + totalOffsetY
        hourTextPaint.getTextBounds("24", 0, 2, tmpRect)
        hourSpaceAngle = (tmpRect.width() * 360f / 2 / Math.PI / hourDialRadius * 1.5f/*额外空余百分比*/).toFloat()

        minuteCenter.apply {
            x = mPaddingLeft + validWidth / 2f
            y = mPaddingTop + validHeight * 0.685f - totalOffsetY
        }
        minuteDialRadius = minuteCenter.y - (mPaddingTop + validHeight * 0.3775f) + totalOffsetY
        minuteTextPaint.getTextBounds("60", 0, 2, tmpRect)
        minuteSpaceAngle = (tmpRect.width() * 360f / 2 / Math.PI / minuteDialRadius * 1.5f/*额外空余百分比*/).toFloat()

        secondCenter.apply {
            x = mPaddingLeft + validWidth / 2f
            y = mPaddingTop + validHeight * 0.7f - totalOffsetY
        }
        secondDialRadius = secondCenter.y - (mPaddingTop + validHeight * 0.5f) + totalOffsetY
        secondTextPaint.getTextBounds("60", 0, 2, tmpRect)
        secondSpaceAngle = (tmpRect.width() * 360f / 2 / Math.PI / secondDialRadius * 1.5f/*额外空余百分比*/).toFloat()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas ?: return)
        updateTimeDegree()
        setCameraRotate(canvas)
        drawHourDial(canvas)
        drawHourHand(canvas)
        drawMinuteDial(canvas)
        drawMinuteHand(canvas)
        drawSecondDial(canvas)
        drawSecondHand(canvas)
        invalidate()
    }

    /**
     * 获取camera旋转的大小
     *
     * @param event motionEvent
     */
    private fun getCameraRotate(event: PointF) {
        val rotateX = -(event.y /*- height / 2*/)
        val rotateY = event.x /*- width / 2*/
        //求出此时旋转的大小与半径之比
        val percentArr = getPercent(rotateX, rotateY)
        //最终旋转的大小按比例匀称改变
        mCameraRotateX = percentArr[0] * mMaxCameraRotate
        mCameraRotateY = percentArr[1] * mMaxCameraRotate
    }


    /**
     * 当拨动时钟时，会发现时针、分针、秒针和刻度盘会有一个较小的偏移量，形成近大远小的立体偏移效果
     * 一开始我打算使用 matrix 和 camera 的 mCamera.translate(x, y, z) 方法改变 z 的值
     * 但是并没有效果，所以就动态计算距离，然后在 onDraw()中分零件地 mCanvas.translate(x, y)
     *
     * @param event motionEvent
     */
    private fun getCanvasTranslate(event: PointF) {
        val translateX = event.x /*- width / 2*/
        val translateY = event.y /*- height / 2*/
        //求出此时位移的大小与半径之比
        val percentArr = getPercent(translateX, translateY)
        //最终位移的大小按比例匀称改变
        mCanvasTranslateX = percentArr[0] * mMaxCanvasTranslate
        mCanvasTranslateY = percentArr[1] * mMaxCanvasTranslate
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

    private fun setCameraRotate(canvas: Canvas) {
        mCameraMatrix.reset()
        mCamera.save()
        mCamera.rotateX(mCameraRotateX)//绕x轴旋转角度
        mCamera.rotateY(mCameraRotateY)//绕y轴旋转角度
        mCamera.getMatrix(mCameraMatrix)//相关属性设置到matrix中
        mCamera.restore()
        //camera在view左上角那个点，故旋转默认是以左上角为中心旋转
        //故在动作之前pre将matrix向左移动getWidth()/2长度，向上移动getHeight()/2长度
        mCameraMatrix.preTranslate((-width / 2).toFloat(), (-height / 2).toFloat())
        //在动作之后post再回到原位
        mCameraMatrix.postTranslate((width / 2).toFloat(), (height / 2).toFloat())
        canvas.concat(mCameraMatrix)//matrix与canvas相关联
    }

    private fun drawHourDial(canvas: Canvas) {
        val saveCount = canvas.save()

        canvas.translate(mCanvasTranslateX, mCanvasTranslateY)

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
        val saveCount = canvas.save()

        canvas.translate(mCanvasTranslateX, mCanvasTranslateY)

        tmpPath.reset()
        tmpPath.moveTo(hourCenter.x, hourCenter.y - hourDialRadius + secondDialRadius * 0.1f)
        tmpPath.lineTo(hourCenter.x + secondDialRadius * 0.05f, hourCenter.y - hourDialRadius + secondDialRadius * 0.2f)
        tmpPath.lineTo(hourCenter.x - secondDialRadius * 0.05f, hourCenter.y - hourDialRadius + secondDialRadius * 0.2f)
        tmpPath.close()
        canvas.drawPath(tmpPath, handPaint)

        canvas.restoreToCount(saveCount)
    }

    private fun drawMinuteDial(canvas: Canvas) {
        val saveCount = canvas.save()

        canvas.translate(mCanvasTranslateX * 1.6f, mCanvasTranslateY * 1.6f)

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
        val saveCount = canvas.save()

        canvas.translate(mCanvasTranslateX * 1.6f, mCanvasTranslateY * 1.6f)

        tmpPath.reset()
        tmpPath.moveTo(minuteCenter.x, minuteCenter.y - minuteDialRadius + secondDialRadius * 0.1f)
        tmpPath.lineTo(minuteCenter.x + secondDialRadius * 0.05f, minuteCenter.y - minuteDialRadius + secondDialRadius * 0.2f)
        tmpPath.lineTo(minuteCenter.x - secondDialRadius * 0.05f, minuteCenter.y - minuteDialRadius + secondDialRadius * 0.2f)
        tmpPath.close()
        canvas.drawPath(tmpPath, handPaint)

        canvas.restoreToCount(saveCount)
    }

    private fun drawSecondDial(canvas: Canvas) {
        val saveCount = canvas.save()

        canvas.translate(mCanvasTranslateX * 2f, mCanvasTranslateY * 2f)


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
        val saveCount = canvas.save()

        canvas.translate(mCanvasTranslateX * 2f, mCanvasTranslateY * 2f)

        tmpPath.reset()
        tmpPath.moveTo(secondCenter.x, secondCenter.y - secondDialRadius + secondDialRadius * 0.15f)
        tmpPath.lineTo(secondCenter.x + secondDialRadius * 0.05f, secondCenter.y - secondDialRadius + secondDialRadius * 0.25f)
        tmpPath.lineTo(secondCenter.x - secondDialRadius * 0.05f, secondCenter.y - secondDialRadius + secondDialRadius * 0.25f)
        tmpPath.close()
        canvas.drawPath(tmpPath, handPaint)

        canvas.restoreToCount(saveCount)
    }

    /**
     * 时钟晃动动画
     */
    private fun startShakeAnim() {
        val cameraRotateXName = "cameraRotateX"
        val cameraRotateYName = "cameraRotateY"
        val canvasTranslateXName = "canvasTranslateX"
        val canvasTranslateYName = "canvasTranslateY"
        val cameraRotateXHolder = PropertyValuesHolder.ofFloat(cameraRotateXName, mCameraRotateX, 0f)
        val cameraRotateYHolder = PropertyValuesHolder.ofFloat(cameraRotateYName, mCameraRotateY, 0f)
        val canvasTranslateXHolder = PropertyValuesHolder.ofFloat(canvasTranslateXName, mCanvasTranslateX, 0f)
        val canvasTranslateYHolder = PropertyValuesHolder.ofFloat(canvasTranslateYName, mCanvasTranslateY, 0f)
        mShakeAnim = ValueAnimator.ofPropertyValuesHolder(
            cameraRotateXHolder,
            cameraRotateYHolder, canvasTranslateXHolder, canvasTranslateYHolder
        )
        mShakeAnim?.interpolator = ShakeInterpolator
        mShakeAnim?.duration = 1000
        mShakeAnim?.addUpdateListener { animation ->
            mCameraRotateX = animation.getAnimatedValue(cameraRotateXName) as Float
            mCameraRotateY = animation.getAnimatedValue(cameraRotateYName) as Float
            mCanvasTranslateX = animation.getAnimatedValue(canvasTranslateXName) as Float
            mCanvasTranslateY = animation.getAnimatedValue(canvasTranslateYName) as Float
        }
        mShakeAnim?.start()
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

    /**
     * 获取一个操作旋转或位移大小的比例
     *
     * @param x x大小
     * @param y y大小
     * @return 装有xy比例的float数组
     */
    private fun getPercent(x: Float, y: Float): FloatArray {
        val percentArr = FloatArray(2)
        var percentX = x / mRadius
        var percentY = y / mRadius
        if (percentX > 1) {
            percentX = 1f
        } else if (percentX < -1) {
            percentX = -1f
        }
        if (percentY > 1) {
            percentY = 1f
        } else if (percentY < -1) {
            percentY = -1f
        }
        percentArr[0] = percentX
        percentArr[1] = percentY
        return percentArr
    }

    /**
     * 抖动插值器
     */
    object ShakeInterpolator : TimeInterpolator {
        override fun getInterpolation(t: Float): Float {
            var t = t
            // _b(t) = t * t * 8
            // bs(t) = _b(t) for t < 0.3535
            // bs(t) = _b(t - 0.54719) + 0.7 for t < 0.7408
            // bs(t) = _b(t - 0.8526) + 0.9 for t < 0.9644
            // bs(t) = _b(t - 1.0435) + 0.95 for t <= 1.0
            // b(t) = bs(t * 1.1226)
            t *= 1.1226f
            return if (t < 0.3535f)
                bounce(t)
            else if (t < 0.7408f)
                bounce(t - 0.54719f) + 0.7f
            else if (t < 0.9644f)
                bounce(t - 0.8526f) + 0.9f
            else
                bounce(t - 1.0435f) + 0.95f
        }

        private fun bounce(t: Float): Float {
            return t * t * 8.0f
        }
    }
}

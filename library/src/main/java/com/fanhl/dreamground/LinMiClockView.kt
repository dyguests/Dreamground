package com.fanhl.dreamground

import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Camera
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.SweepGradient
import android.text.TextPaint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.OvershootInterpolator
import androidx.annotation.Nullable
import java.util.*


class LinMiClockView @JvmOverloads constructor(
    context: Context,
    @Nullable attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    // ---------- 输入变量 ----------
    private var mDarkColor: Int = 0
    private var mLightColor: Int = 0
    /** 背景色 */
    private var mBackgroundColor: Int = 0

    /** 小时圆圈线条宽度 */
    private var mCircleStrokeWidth: Float = 0f

    // ---------- 内部变量 ----------
    /** 小时圆圈画笔 */
    private var mCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    /** 刻度圆弧画笔 */
    private var mScaleArcPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    /** 小时的文字 */
    private var mTextPaint = TextPaint()
    /** 秒针刻度线 */
    private var mScaleLinePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    /** 时针画笔 */
    private val mHourHandPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    /** 分针画笔 */
    private val mMinuteHandPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    /** 秒针画笔 */
    private val mSecondHandPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    /** View的最小边半径 */
    private var mRadius: Float = 0f
    /** 加一个默认的padding值，为了防止用camera旋转时钟时造成四周超出view大小 */
    private var mDefaultPadding: Int = 0
    /** 实际padding */
    private var mPaddingLeft: Float = 0f
    private var mPaddingTop: Float = 0f
    private var mPaddingRight: Float = 0f
    private var mPaddingBottom: Float = 0f
    /** 刻度线长度 */
    private var mScaleLength: Float = 0f
    /** 梯度扫描渐变 */
    private lateinit var mSweepGradient: SweepGradient
    /** 渐变矩阵，作用在SweepGradient */
    private val mGradientMatrix: Matrix = Matrix()
    /** 触摸时作用在Camera的矩阵 */
    private val mCameraMatrix: Matrix = Matrix()
    /** 照相机，用于旋转时钟实现3D效果 */
    private val mCamera: Camera = Camera()
    /** camera绕X轴旋转的角度 */
    private var mCameraRotateX: Float = 0f
    /** camera绕Y轴旋转的角度 */
    private var mCameraRotateY: Float = 0f
    /** camera旋转的最大角度 */
    private val mMaxCameraRotate = 10f

    /** 时针角度 */
    private var mHourDegree: Float = 0f
    /** 分针角度 */
    private var mMinuteDegree: Float = 0f
    /** 秒针角度 */
    private var mSecondDegree: Float = 0f

    /* 手指松开时时钟晃动的动画 */
    private var mShakeAnim: ValueAnimator? = null

    /* -------------- 计算用临时存放区 -------------- */
    private var mScaleArcRectF = RectF()
    private var mCircleRectF = RectF()
    private var mTextRect = Rect()
    /** 秒针路径 */
    private val mSecondHandPath = Path()
    /** 时针路径 */
    private val mHourHandPath = Path()
    /** 分针路径 */
    private val mMinuteHandPath = Path()

    init {
        mDarkColor = Color.parseColor("#80ffffff")
        mLightColor = Color.parseColor("#ffffff")
        mBackgroundColor = Color.parseColor("#237EAD")

        mCircleStrokeWidth = 10f

        mCirclePaint.apply {
            style = Paint.Style.STROKE
            strokeWidth = mCircleStrokeWidth
            color = mDarkColor
        }
        mHourHandPaint.apply {
            style = Paint.Style.FILL
            color = mDarkColor
        }
        mMinuteHandPaint.color = mLightColor
        mScaleArcPaint.style = Paint.Style.STROKE
        mTextPaint.apply {
            textSize = 40f
            color = Color.WHITE
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> getCameraRotate(event)
            MotionEvent.ACTION_MOVE ->
                //根据手指坐标计算camera应该旋转的大小
                getCameraRotate(event)
            MotionEvent.ACTION_UP ->
                //松开手指，时钟复原并伴随晃动动画
                startShakeAnim()
        }
        return true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        //宽和高分别去掉padding值，取min的一半即表盘的半径
        mRadius = minOf(w - paddingLeft - paddingRight, h - paddingTop - paddingBottom) / 2f
        //加一个默认的padding值，为了防止用camera旋转时钟时造成四周超出view大小
        //根据比例确定默认padding大小
        mDefaultPadding = (0.12f * mRadius).toInt()
        //为了适配控件大小match_parent、wrap_content、精确数值以及padding属性
        mPaddingLeft = mDefaultPadding + w / 2 - mRadius + paddingLeft
        mPaddingTop = mDefaultPadding + h / 2 - mRadius + paddingTop
        mPaddingRight = mPaddingLeft
        mPaddingBottom = mPaddingTop
        //根据比例确定刻度线长度
        mScaleLength = 0.12f * mRadius
        //刻度盘的弧宽
        mScaleArcPaint.strokeWidth = mScaleLength
        //刻度线的宽度
        mScaleLinePaint.strokeWidth = 0.012f * mRadius
        //梯度扫描渐变，以(w/2,h/2)为中心点，两种起止颜色梯度渐变
        //float数组表示，[0,0.75)为起始颜色所占比例，[0.75,1}为起止颜色渐变所占比例
        mSweepGradient = SweepGradient(w / 2f, h / 2f, intArrayOf(mDarkColor, mLightColor), floatArrayOf(0.75f, 1f))
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return

        setCameraRotate(canvas)
        getTimeDegree()

        // ---------- 绘制背景 ----------

        canvas.drawColor(mBackgroundColor)

        // ---------- 先画最外层边框 ----------

        //圆弧
        getTimeDegree(canvas)
        //文字
        drawTimeText(canvas)
        //绘制刻度盘
        drawScaleLine(canvas)
        drawSecondHand(canvas)
        drawHourHand(canvas)
        drawMinuteHand(canvas)
        drawCoverCircle(canvas)
        invalidate()
    }

    private fun getTimeDegree(canvas: Canvas) {
        mCircleRectF.set(
            mPaddingLeft + mTextRect.height() / 2 + mCircleStrokeWidth / 2,
            mPaddingTop + mTextRect.height() / 2 + mCircleStrokeWidth / 2,
            width - mPaddingRight - mTextRect.height() / 2 + mCircleStrokeWidth / 2,
            height - mPaddingBottom - mTextRect.height() / 2 + mCircleStrokeWidth / 2
        )
        for (i in 0..3) {
            canvas.drawArc(mCircleRectF, 5 + 90f * i, 80f, false, mCirclePaint)
        }
    }

    private fun drawTimeText(canvas: Canvas) {
        var timeText = "12"
        mTextPaint.getTextBounds(timeText, 0, timeText.length, mTextRect)
        val textLargeWidth = mTextRect.width()//两位数字的宽
        canvas.drawText("12", width / 2f - textLargeWidth / 2f, mPaddingTop + mTextRect.height(), mTextPaint)
        timeText = "3"
        mTextPaint.getTextBounds(timeText, 0, timeText.length, mTextRect)
        val textSmallWidth = mTextRect.width()//一位数字的宽
        canvas.drawText("3", width - mPaddingRight - mTextRect.height() / 2f - textSmallWidth / 2f, height / 2f + mTextRect.height() / 2f, mTextPaint)
        canvas.drawText("6", width / 2f - textSmallWidth / 2f, height - mPaddingBottom, mTextPaint)
        canvas.drawText("9", mPaddingLeft + mTextRect.height() / 2f - textSmallWidth / 2f, height / 2f + mTextRect.height() / 2f, mTextPaint)
    }

    /**
     * 画一圈梯度渲染的亮暗色渐变圆弧，重绘时不断旋转，上面盖一圈背景色的刻度线
     */
    private fun drawScaleLine(canvas: Canvas) {
        mScaleArcRectF.set(
            mPaddingLeft + 1.5f * mScaleLength + mTextRect.height() / 2,
            mPaddingTop + 1.5f * mScaleLength + mTextRect.height() / 2,
            width - mPaddingRight - mTextRect.height() / 2 - 1.5f * mScaleLength,
            height - mPaddingBottom - mTextRect.height() / 2 - 1.5f * mScaleLength
        )
        //matrix默认会在三点钟方向开始颜色的渐变，为了吻合
        //钟表十二点钟顺时针旋转的方向，把秒针旋转的角度减去90度
        mGradientMatrix.setRotate(mSecondDegree - 90, width / 2f, height / 2f)
        mSweepGradient.setLocalMatrix(mGradientMatrix)
        mScaleArcPaint.shader = mSweepGradient
        canvas.drawArc(mScaleArcRectF, 0f, 360f, false, mScaleArcPaint)

        //画背景色刻度线
        val saveCount = canvas.save()
        for (i in 0 until 200) {
            canvas.drawLine(
                width / 2f,
                mPaddingTop + mScaleLength + mTextRect.height() / 2f,
                width / 2f,
                mPaddingTop + 2 * mScaleLength + mTextRect.height() / 2f,
                mScaleLinePaint
            )
            canvas.rotate(1.8f, width / 2f, height / 2f)
        }
        canvas.restoreToCount(saveCount)
    }

    /**
     * 画秒针
     */
    private fun drawSecondHand(canvas: Canvas) {
        canvas.save()
        canvas.rotate(mSecondDegree, width / 2f, height / 2f)

        mSecondHandPath.reset()
        val offset = mPaddingTop + mTextRect.height() / 2
        mSecondHandPath.moveTo(width / 2f, offset + 0.27f * mRadius)
        mSecondHandPath.lineTo(width / 2f - 0.05f * mRadius, offset + 0.35f * mRadius)
        mSecondHandPath.lineTo(width / 2f + 0.05f * mRadius, offset + 0.35f * mRadius)
        mSecondHandPath.close()
        mSecondHandPaint.color = mLightColor

        canvas.drawPath(mSecondHandPath, mSecondHandPaint)
        canvas.restore()
    }

    /**
     * 画时针，根据不断变化的时针角度旋转画布
     * 针头为圆弧状，使用二阶贝塞尔曲线
     */
    private fun drawHourHand(canvas: Canvas) {
        canvas.save()
        canvas.rotate(mHourDegree, width / 2f, height / 2f)
        mHourHandPath.reset()
        val offset = mPaddingTop + mTextRect.height() / 2f
        mHourHandPath.moveTo(width / 2 - 0.02f * mRadius, height / 2f)
        mHourHandPath.lineTo(width / 2 - 0.01f * mRadius, offset + 0.5f * mRadius)
        mHourHandPath.quadTo(
            width / 2f, offset + 0.48f * mRadius,
            width / 2f + 0.01f * mRadius, offset + 0.5f * mRadius
        )
        mHourHandPath.lineTo(width / 2 + 0.02f * mRadius, height / 2f)
        mHourHandPath.close()
        canvas.drawPath(mHourHandPath, mHourHandPaint)
        canvas.restore()
    }

    private fun drawMinuteHand(canvas: Canvas) {
        canvas.save()
        canvas.rotate(mMinuteDegree, width / 2f, height / 2f)
        mMinuteHandPath.reset()
        val offset = mPaddingTop + mTextRect.height() / 2f
        mMinuteHandPath.moveTo(width / 2f - 0.01f * mRadius, height / 2f)
        mMinuteHandPath.lineTo(width / 2f - 0.008f * mRadius, offset + 0.38f * mRadius)
        mMinuteHandPath.quadTo(
            width / 2f, offset + 0.36f * mRadius,
            width / 2f + 0.008f * mRadius, offset + 0.38f * mRadius
        )
        mMinuteHandPath.lineTo(width / 2f + 0.01f * mRadius, height / 2f)
        mMinuteHandPath.close()
        canvas.drawPath(mMinuteHandPath, mMinuteHandPaint)
        canvas.restore()
    }

    /**
     * 画指针的连接圆圈，盖住指针path在圆心的连接线
     */
    private fun drawCoverCircle(canvas: Canvas) {
        canvas.drawCircle(width / 2f, height / 2f, 0.05f * mRadius, mSecondHandPaint)
        mSecondHandPaint.setColor(mBackgroundColor)
        canvas.drawCircle(width / 2f, height / 2f, 0.025f * mRadius, mSecondHandPaint)
    }

    /**
     * 获取当前 时分秒 所对应的角度
     * 为了不让秒针走得像老式挂钟一样僵硬，需要精确到毫秒
     */
    private fun getTimeDegree() {
        val calendar = Calendar.getInstance()
        val milliSecond = calendar.get(Calendar.MILLISECOND)
        val second = calendar.get(Calendar.SECOND) + milliSecond / 1000f
        val minute = calendar.get(Calendar.MINUTE) + second / 60f
        val hour = calendar.get(Calendar.HOUR) + minute / 60f
        mSecondDegree = second / 60f * 360
        mMinuteDegree = minute / 60f * 360
        mHourDegree = hour / 12f * 360
    }

    /**
     * 获取camera旋转的大小
     *
     * @param event motionEvent
     */
    private fun getCameraRotate(event: MotionEvent) {
        val rotateX = -(event.y - height / 2)
        val rotateY = event.x - width / 2
        //求出此时旋转的大小与半径之比
        val percentArr = getPercent(rotateX, rotateY)
        //最终旋转的大小按比例匀称改变
        mCameraRotateX = percentArr[0] * mMaxCameraRotate
        mCameraRotateY = percentArr[1] * mMaxCameraRotate
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
     * 设置3D时钟效果，触摸矩阵的相关设置、照相机的旋转大小
     * 应用在绘制图形之前，否则无效
     *
     */
    private fun setCameraRotate(canvas: Canvas) {
        mCameraMatrix.reset()
        mCamera.save()
        mCamera.rotateX(mCameraRotateX)//绕x轴旋转角度
        mCamera.rotateY(mCameraRotateY)//绕y轴旋转角度
        mCamera.getMatrix(mCameraMatrix)//相关属性设置到matrix中
        mCamera.restore()
        //camera在view左上角那个点，故旋转默认是以左上角为中心旋转
        //故在动作之前pre将matrix向左移动getWidth()/2长度，向上移动getHeight()/2长度
        mCameraMatrix.preTranslate(-width / 2f, -height / 2f)
        //在动作之后post再回到原位
        mCameraMatrix.postTranslate(width / 2f, height / 2f)
        canvas.concat(mCameraMatrix)//matrix与canvas相关联
    }

    /**
     * 使用OvershootInterpolator完成时钟晃动动画
     */
    private fun startShakeAnim() {
        val cameraRotateXName = "cameraRotateX"
        val cameraRotateYName = "cameraRotateY"
        val cameraRotateXHolder = PropertyValuesHolder.ofFloat(cameraRotateXName, mCameraRotateX, 0f)
        val cameraRotateYHolder = PropertyValuesHolder.ofFloat(cameraRotateYName, mCameraRotateY, 0f)
        mShakeAnim = ValueAnimator.ofPropertyValuesHolder(cameraRotateXHolder, cameraRotateYHolder)
        mShakeAnim?.interpolator = OvershootInterpolator(10f)
        mShakeAnim?.duration = 500
        mShakeAnim?.addUpdateListener { animation ->
            mCameraRotateX = animation.getAnimatedValue(cameraRotateXName) as Float
            mCameraRotateY = animation.getAnimatedValue(cameraRotateYName) as Float
        }
        mShakeAnim?.start()
    }
}
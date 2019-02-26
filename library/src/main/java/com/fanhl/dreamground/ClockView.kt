package com.fanhl.dreamground

import android.content.Context
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
import android.view.View
import androidx.annotation.Nullable
import java.util.*


class ClockView(
    context: Context,
    @Nullable attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    // ---------- 输入变量 ----------
    private var mDarkColor: Int = 0
    private var mLightColor: Int = 0

    /** 小时圆圈线条宽度 */
    private var mCircleStrokeWidth: Float = 0f

    // ---------- 内部变量 ----------
    /** 小时圆圈画笔 */
    private var mCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    /** 刻度圆弧画笔 */
    private var mScaleArcPaint = Paint()
    /** 小时的文字 */
    private var mTextPaint = TextPaint()
    /** 秒针刻度线 */
    private var mScaleLinePaint = Paint()
    /** 秒针画笔 */
    private val mSecondHandPaint = Paint()

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

    /** 时针角度 */
    private var mHourDegree: Float = 0f
    /** 分针角度 */
    private var mMinuteDegree: Float = 0f
    /** 秒针角度 */
    private var mSecondDegree: Float = 0f

    /* -------------- 计算用临时存放区 -------------- */
    private var mScaleArcRectF = RectF()
    private var mCircleRectF = RectF()
    private var mTextRect = Rect()
    /** 秒针路径 */
    private val mSecondHandPath = Path()

    init {
        mDarkColor = Color.GRAY
        mLightColor = Color.WHITE

        mCircleStrokeWidth = 10f

        mCirclePaint.apply {
            style = Paint.Style.STROKE
            strokeWidth = mCircleStrokeWidth
            color = mDarkColor
        }
        mScaleArcPaint.style = Paint.Style.STROKE
        mTextPaint.apply {
            textSize = 40f
            color = Color.WHITE
        }
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

        // ---------- 绘制背景 ----------

        canvas ?: return
        canvas.drawColor(Color.BLUE)

        // ---------- 先画最外层边框 ----------

        //文字
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
        //圆弧
        mCircleRectF.set(
            mPaddingLeft + mTextRect.height() / 2 + mCircleStrokeWidth / 2,
            mPaddingTop + mTextRect.height() / 2 + mCircleStrokeWidth / 2,
            width - mPaddingRight - mTextRect.height() / 2 + mCircleStrokeWidth / 2,
            height - mPaddingBottom - mTextRect.height() / 2 + mCircleStrokeWidth / 2
        )
        for (i in 0..3) {
            canvas.drawArc(mCircleRectF, 5 + 90f * i, 80f, false, mCirclePaint)
        }

        //绘制刻度盘
        drawScaleLine(canvas)
        drawSecondHand(canvas)
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
     * 获取当前 时分秒 所对应的角度
     * 为了不让秒针走得像老式挂钟一样僵硬，需要精确到毫秒
     */
    private fun getTimeDegree() {
        val calendar = Calendar.getInstance()
        val milliSecond = calendar.get(Calendar.MILLISECOND)
        val second = calendar.get(Calendar.SECOND) + milliSecond / 1000
        val minute = calendar.get(Calendar.MINUTE) + second / 60
        val hour = calendar.get(Calendar.HOUR) + minute / 60
        mSecondDegree = second / 60f * 360
        mMinuteDegree = minute / 60f * 360
        mHourDegree = hour / 12f * 360
    }
}
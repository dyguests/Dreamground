package com.fanhl.dreamground

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import java.util.*

/**
 * 星空
 */
class StarrySkyView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {
    private val paint by lazy {
        Paint().apply {
            style = Paint.Style.FILL_AND_STROKE
            strokeJoin = Paint.Join.ROUND
            strokeWidth = size.toFloat()
        }
    }

    private val random by lazy { Random() }

    /** 星星总数量 */
    private var starryCount: Int = 0
    private var starryAlpha255 = 0
    // ------------------------------- 输入参数 -------------------------------

    /**
     * 单位单积(1px*1px)内的星星的数量
     */
    private var density = DENSITY_DEFAULT
    /** 星星的半径 */
    private var size = 0
    private var starryAlpha = 1f
        set(value) {
            field = value
            starryAlpha255 = (value * 255).toInt()
        }

    /** 闪烁间隔 */
    private val flashIterval = 100

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.StarrySkyView, defStyleAttr, R.style.Widget_Dreamground_StarrySkyView)

        density = a.getFloat(R.styleable.StarrySkyView_density, DENSITY_DEFAULT)
        size = a.getDimensionPixelSize(R.styleable.StarrySkyView_size, dpToPx(2))
        starryAlpha = a.getFloat(R.styleable.StarrySkyView_starryAlpha, 1f)

        a.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // FIXME: 2018/5/25 fanhl 之后把宽高 提到onMeasure中去

        val width = canvas.width
        val height = canvas.height

        starryCount = (width * height * density).toInt()

        for (i in 0 until starryCount) {
            val x = random.nextInt(width)
            val y = random.nextInt(height)

            paint.color = Color.argb(starryAlpha255, random.nextInt(255), random.nextInt(255), random.nextInt(255))

            canvas.drawPoint(x.toFloat(), y.toFloat(), paint)
//            canvas.drawCircle(x.toFloat(), y.toFloat(), size, paint)
        }

    }

    internal fun dpToPx(dps: Int): Int {
        return Math.round(resources.displayMetrics.density * dps)
    }

    companion object {
        const val DENSITY_DEFAULT = 0.0001f
    }
}
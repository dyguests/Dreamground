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
            strokeWidth = size
        }
    }

    private val random by lazy { Random() }

    /** 星星总数量 */
    private var starryCount: Int = 0

    // ------------------------------- 输入参数 -------------------------------

    /**
     * 单位单积(1px*1px)内的星星的数量
     */
    private val density = 0.0001f
    /** 星星的半径 */
    private val size = 10f


    init {

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

            paint.color = Color.rgb(random.nextInt(255), random.nextInt(255), random.nextInt(255))

            canvas.drawPoint(x.toFloat(), y.toFloat(), paint)
//            canvas.drawCircle(x.toFloat(), y.toFloat(), size, paint)
        }

    }
}
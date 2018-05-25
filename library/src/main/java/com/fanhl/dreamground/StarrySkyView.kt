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
            style = Paint.Style.FILL
        }
    }

    private val random by lazy { Random() }

    init {

    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = canvas.width
        val height = canvas.height
        val centerX = width / 2.toFloat()
        val centerY = height / 2.toFloat()

        paint.color = Color.rgb(random.nextInt(255), random.nextInt(255), random.nextInt(255))
        canvas.drawPoint(centerX, centerY, paint)
    }
}
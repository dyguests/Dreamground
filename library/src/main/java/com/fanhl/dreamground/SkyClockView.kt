package com.fanhl.dreamground

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
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
    init {

    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas ?: return)
        canvas.drawColor(Color.RED)
        drawHourDial(canvas)
        invalidate()
    }

    private fun drawHourDial(canvas: Canvas) {

    }
}
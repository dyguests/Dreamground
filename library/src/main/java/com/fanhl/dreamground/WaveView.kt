package com.fanhl.dreamground

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.support.annotation.FloatRange
import android.util.AttributeSet

/**
 * 类似QQ启动页上的波浪效果那种
 */
class WaveView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : BaseTextureView(context, attrs, defStyleAttr) {
    /** 所有浪尖 */
    private val crestss: Array<Array<Crest>>

    private val paint = Paint()

    // ------------------------------------------ Input ------------------------------------------

    /** 列顶点数 （顶点数=面数+1） */
    private var columns: Int? = null
    /** 行顶点数 （顶点数=面数+1） */
    private var rows: Int? = null
    /** 面大小 （根据面的大小来计算出行列数） */
    private var cellSize: Float? = null

    private var foreLightColor: Int
    private var backLightColor: Int

    // ------------------------------------------ Operation ------------------------------------------


    init {

        //FIXME 这里要根据宽、高、宽高、密度来计算是实际的行列数
        columns = 4
        rows = 4
        cellSize = 100f

        foreLightColor = Color.WHITE
        backLightColor = Color.BLACK

        crestss = Array(columns!!) {
            Array(rows!!) {
                Crest()
            }
        }
    }

    override fun updateCanvas(canvas: Canvas) {
        initCanvas(canvas)
        val itemWidth = width.toFloat() / (columns!! - 1)
        val itemHeight = height.toFloat() / (rows!! - 1)

        crestss.forEachIndexed { column, crests ->
            crests.forEachIndexed { row, crest ->
                val x = (column + crest.x) * itemWidth - itemWidth / 2
                val y = (row + crest.y) * itemHeight - itemHeight / 2

                paint.color = backLightColor

                canvas.drawCircle(x, y, 100f, paint)
            }
        }
    }

    private fun initCanvas(canvas: Canvas) {
    }

    /**
     * 波峰
     *
     * 关键点
     */
    class Crest {
        // x,y,z 都是基于初起位置的偏移值

        @FloatRange(from = -0.5, to = 0.5)
        var x: Float = 0f
        @FloatRange(from = -0.5, to = 0.5)
        var y: Float = 0f
        @FloatRange(from = -0.5, to = 0.5)
        var z: Float = 0f
    }
}
package com.fanhl.dreamground

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
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
    /** 光线角度 */
    private var lightAngle: Int

    // ------------------------------------------ Operation ------------------------------------------

    private var itemWidth: Float = 0.0f
    private var itemHeight: Float = 0.0f

    /** 用来存放path数据 */
    private val path = Path()

    init {

        //FIXME 这里要根据宽、高、宽高、密度来计算是实际的行列数
        columns = 4
        rows = 4
        cellSize = 100f

        foreLightColor = Color.WHITE
        backLightColor = Color.RED

        lightAngle = 135



        crestss = Array(columns!!) {
            Array(rows!!) {
                Crest()
            }
        }
    }

    override fun updateCanvas(canvas: Canvas) {
        initCanvas(canvas)

        canvas.drawColor(Color.WHITE)

        crestss.forEachIndexed { column, crests ->
            crests.forEachIndexed rowLoop@{ row, crest ->
                if (column >= columns!! - 1 || row >= rows!! - 1) {
                    return@rowLoop
                }

                val coord00 = parseCoordinate(column, row)
                val coord10 = parseCoordinate(column + 1, row)
                val coord01 = parseCoordinate(column, row + 1)
                val coord11 = parseCoordinate(column + 1, row + 1)

                paint.color = backLightColor

                path.moveTo(coord00.first, coord00.second)
                path.lineTo(coord10.first, coord00.second)
                path.lineTo(coord11.first, coord11.second)
                path.lineTo(coord01.first, coord01.second)
                path.close()
                canvas.drawPath(path, paint)
                path.reset()
            }
        }
    }

    private fun initCanvas(canvas: Canvas) {
        if (itemWidth != 0f) {
            return
        }

        itemWidth = canvas.width.toFloat() / (columns!! - 2)
        itemHeight = canvas.height.toFloat() / (rows!! - 2)
    }

    /**
     * 取得对应点位的3d坐标
     */
    private fun parseCoordinate(column: Int, row: Int): Triple<Float, Float, Float> {
        val crest = crestss[column][row]
        val x0 = (column + crest.x) * itemWidth - itemWidth / 2
        val y0 = (row + crest.y) * itemHeight - itemHeight / 2
        val z0 = crest.z * (itemWidth + itemHeight) / 2
        return Triple(x0, y0, z0)
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
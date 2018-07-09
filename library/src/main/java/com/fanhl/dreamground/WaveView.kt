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
    /**
     * 所有浪尖
     *
     * 注意：这里存放的是相对于初始值的偏移点
     */
    private val crestss: Array<Array<Vector3>>

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
                Vector3()
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

                val normalVector = getNormalVector(coord00, coord10, coord01, coord11)

                paint.color = backLightColor

                path.moveTo(coord00.x, coord00.y)
                path.lineTo(coord10.x, coord00.y)
                path.lineTo(coord11.x, coord11.y)
                path.lineTo(coord01.x, coord01.y)
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
    private fun parseCoordinate(column: Int, row: Int): Vector3 {
        val crest = crestss[column][row]
        val x0 = (column + crest.x) * itemWidth - itemWidth / 2
        val y0 = (row + crest.y) * itemHeight - itemHeight / 2
        val z0 = crest.z * (itemWidth + itemHeight) / 2
        return Vector3(x0, y0, z0)
    }

    /**
     * 根据面上的点，返回法线向量
     */
    private fun getNormalVector(v1: WaveView.Vector3, v2: WaveView.Vector3, v3: WaveView.Vector3, v4: WaveView.Vector3): WaveView.Vector3 {
        return Vector3()
    }

    data class Vector3(
            var x: Float = 0f,
            var y: Float = 0f,
            var z: Float = 0f
    )
}
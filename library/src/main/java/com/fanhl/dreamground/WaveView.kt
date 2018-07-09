package com.fanhl.dreamground

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import com.fanhl.dreamground.util.ColorUtils
import java.util.*

/**
 * 类似QQ启动页上的波浪效果那种
 */
class WaveView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : BaseTextureView(context, attrs, defStyleAttr) {
    /**
     * 所有浪尖
     *
     * 注意：这里存放的是相对于初始值的偏移点
     *
     * 注2  x,y,z in (-0.5f,0.5f)
     */
    private val crestss: Array<Array<Vector3>>

    private val paint = Paint()

    private val random = Random()

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
    /** 横波幅度 */
    private var transverseWave: Float
    /** 纵波幅度 */
    private var longitudinalWave: Float

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

        foreLightColor = Color.BLUE
        backLightColor = Color.RED

        lightAngle = 135

        transverseWave = 0.2f
        longitudinalWave = 0.2f

        crestss = Array(columns!!) {
            Array(rows!!) {
                Vector3(
                        x = (random.nextFloat() - 0.5f) * transverseWave,
                        y = (random.nextFloat() - 0.5f) * transverseWave,
                        z = (random.nextFloat() - 0.5f) * longitudinalWave
                )
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

                paint.color = computePlaneColor(normalVector)

                path.moveTo(coord00.x, coord00.y)
                path.lineTo(coord10.x, coord10.y)
                path.lineTo(coord11.x, coord11.y)
                path.lineTo(coord01.x, coord01.y)
                path.close()
                canvas.drawPath(path, paint)
                path.reset()

                paint.color = Color.BLACK
                canvas.drawCircle(coord00.x, coord00.y, 10f, paint)
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
    private fun getNormalVector(p1: WaveView.Vector3, p2: WaveView.Vector3, p3: WaveView.Vector3, v4: WaveView.Vector3): WaveView.Vector3 {
        val a = ((p2.y - p1.y) * (p3.z - p1.z) - (p2.z - p1.z) * (p3.y - p1.y))
        val b = ((p2.z - p1.z) * (p3.x - p1.x) - (p2.x - p1.x) * (p3.z - p1.z))
        val c = ((p2.x - p1.x) * (p3.y - p1.y) - (p2.y - p1.y) * (p3.x - p1.x))

        val length = Math.sqrt((a * a + b * b + c * c).toDouble()).toFloat()

        // FIXME: 2018/7/9 fanhl 这里只求了3个点，之后改成4个点

        return Vector3(a / length, b / length, c / length)
    }

    /**
     * 根据法向量方向计算出颜色
     */
    private fun computePlaneColor(normalVector: Vector3): Int {
        val percent1 = normalVector.x + 0.5f
        val percent = when {
            percent1 > 1f -> 1f
            percent1 < 0f -> 0f
            else -> percent1
        }
        return ColorUtils.getColorGradient(foreLightColor, backLightColor, percent)
    }

    data class Vector3(
            var x: Float = 0f,
            var y: Float = 0f,
            var z: Float = 0f
    )
}
package com.fanhl.dreamground

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.TextureView
import java.util.*


/**
 * 星空
 */
class StarrySkyView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : TextureView(context, attrs, defStyleAttr), TextureView.SurfaceTextureListener {
    private val paint by lazy {
        Paint()
    }

    private val random by lazy { Random() }

    private var drawThread: RenderingThread? = null

    /** 星星总数量 */
    private var starryCount: Int = 0
    private var starryAlpha255 = 0
    // ------------------------------- 输入参数 -------------------------------

    /**
     * 单位单积(1px*1px)内的星星的数量
     */
    private var starryDensity = DENSITY_DEFAULT
    /** 星星的半径 */
    private var starrySize = 0
    private var starryAlpha = 1f
        set(value) {
            field = value
            starryAlpha255 = (value * 255).toInt()
        }

    /** 闪烁间隔 */
    private val flashIterval = 100

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.StarrySkyView, defStyleAttr, R.style.Widget_Dreamground_StarrySkyView)

        starryDensity = a.getFloat(R.styleable.StarrySkyView_starryDensity, DENSITY_DEFAULT)
        starrySize = a.getDimensionPixelSize(R.styleable.StarrySkyView_starrySize, dpToPx(1))
        starryAlpha = a.getFloat(R.styleable.StarrySkyView_starryAlpha, 1f)

        a.recycle()

        surfaceTextureListener = this
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

    }

    fun onDraw2(canvas: Canvas) {
        // FIXME: 2018/5/25 fanhl 之后把宽高 提到onMeasure中去

        val width = canvas.width
        val height = canvas.height

        starryCount = (width * height * starryDensity).toInt()

        for (i in 0 until starryCount) {
            val x = random.nextInt(width)
            val y = random.nextInt(height)

            paint.color = Color.argb(starryAlpha255, random.nextInt(255), random.nextInt(255), random.nextInt(255))

//            canvas.drawPoint(x.toFloat(), y.toFloat(), paint)
            canvas.drawCircle(x.toFloat(), y.toFloat(), starrySize.toFloat(), paint)
        }

    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
        drawThread = RenderingThread(this)
        drawThread?.start()
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
        drawThread?.stopRendering()
        drawThread = null
        return true
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {

    }

    private fun dpToPx(dps: Int): Int {
        return Math.round(resources.displayMetrics.density * dps)
    }

    companion object {
        const val DENSITY_DEFAULT = 0.0001f
    }

    internal class RenderingThread(private val mSurface: TextureView) : Thread() {
        @Volatile
        private var mRunning = true

        override fun run() {
            var x = 0.0f
            var y = 0.0f
            var speedX = 5.0f
            var speedY = 3.0f

            val paint = Paint()
            paint.color = -0xff0100

            while (mRunning && !Thread.interrupted()) {
                val canvas = mSurface.lockCanvas(null)
                try {
                    canvas.drawColor(0x00000000, PorterDuff.Mode.CLEAR)
                    canvas.drawRect(x, y, x + 20.0f, y + 20.0f, paint)
                } finally {
                    mSurface.unlockCanvasAndPost(canvas)
                }

                if (x + 20.0f + speedX >= mSurface.width || x + speedX <= 0.0f) {
                    speedX = -speedX
                }
                if (y + 20.0f + speedY >= mSurface.height || y + speedY <= 0.0f) {
                    speedY = -speedY
                }

                x += speedX
                y += speedY

                try {
                    Thread.sleep(15)
                } catch (e: InterruptedException) {
                    // Interrupted
                }

            }
        }

        internal fun stopRendering() {
            interrupt()
            mRunning = false
        }
    }
}
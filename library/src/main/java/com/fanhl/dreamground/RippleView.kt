package com.fanhl.dreamground

import android.content.Context
import android.graphics.SurfaceTexture
import android.os.Handler
import android.os.Message
import android.util.AttributeSet
import android.view.Surface
import android.view.TextureView

/**
 * 波纹
 *
 * https://blog.csdn.net/Holmofy/article/details/66583879
 *
 * @author fanhl
 */
class RippleView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : TextureView(context, attrs, defStyleAttr), TextureView.SurfaceTextureListener {
    private var mSurface: Surface? = null

    private val mHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            mCount++
            val canvas = mSurface?.lockCanvas(null)
            canvas?.drawColor(-0x1)//擦除原来的内容
            canvas?.drawText("Hello World$mCount", (width shr 1).toFloat(), (height shr 1).toFloat(), mPaint)
            mSurface.unlockCanvasAndPost(canvas)
            sendEmptyMessageDelayed(0x0001, DELAY)
        }
    }

    private var mCount: Int = 0

    init {

    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
        mSurface = Surface(surface)
        mHandler.sendEmptyMessageDelayed(0x0001, DELAY)
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    companion object {
        private val DELAY: Long = 1000

    }
}
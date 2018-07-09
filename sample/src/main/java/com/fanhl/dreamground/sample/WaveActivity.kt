package com.fanhl.dreamground.sample

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle

class WaveActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wave)
    }

    companion object {
        /**
         * 启动WaveActivity
         *
         * @param context 上下文
         */
        fun launch(context: Context) {
            context.startActivity(Intent(context, WaveActivity::class.java))
        }
    }
}

package com.fanhl.dreamground.sample

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle

class RippleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ripple)
    }

    companion object {
        fun launch(context: Context, bundle: Bundle? = null) {
            context.startActivity(Intent(context, RippleActivity::class.java), bundle)
        }
    }
}

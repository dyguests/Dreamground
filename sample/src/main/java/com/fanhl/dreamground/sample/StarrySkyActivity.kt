package com.fanhl.dreamground.sample

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle

class StarrySkyActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_starry_sky)
    }

    companion object {
        fun launch(context: Context, bundle: Bundle? = null) {
            context.startActivity(Intent(context, StarrySkyActivity::class.java), bundle)
        }
    }
}

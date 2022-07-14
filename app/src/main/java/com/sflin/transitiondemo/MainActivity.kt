package com.sflin.transitiondemo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.after
import kotlinx.android.synthetic.main.activity_main.before
import kotlinx.android.synthetic.main.activity_main.share_element

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initClickListener()
    }

    private fun initClickListener() {
        before.setOnClickListener {
            startActivity(Intent(this@MainActivity, BeforeActivity::class.java))
        }
        after.setOnClickListener {
            startActivity(Intent(this@MainActivity, AfterActivity::class.java))
        }
        share_element.setOnClickListener {
            startActivity(Intent(this@MainActivity, ShareElementActivity::class.java))
        }
    }
}

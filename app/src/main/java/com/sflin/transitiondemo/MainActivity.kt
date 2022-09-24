package com.sflin.transitiondemo

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.sflin.transitiondemo.snapshotdemo.ShareElementActivityV3
import kotlinx.android.synthetic.main.activity_main.*

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
        share_element_2.setOnClickListener {
            startActivity(Intent(this@MainActivity, ShareElementActivityV2::class.java))
        }
        share_element_3.setOnClickListener {
            startActivity(Intent(this@MainActivity, ShareElementActivityV3::class.java))
        }
    }
}

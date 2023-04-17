package com.sflin.transitiondemo

import androidx.appcompat.app.AppCompatActivity

open class BaseTraditionalActivity : AppCompatActivity() {

    override fun finishAfterTransition() {
        finish()
    }
}
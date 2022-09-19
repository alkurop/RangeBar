package com.example.rangebarsample

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.github.alkurop.rangebar.RangeBar

class LayoutPreviewActivity : AppCompatActivity(R.layout.activity_layout_preview) {
    val button by lazy { findViewById<View>(R.id.reset) }
    val bar by lazy { findViewById<RangeBar>(R.id.bar) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        findViewById<View>(R.id.activate).setOnClickListener {
            bar.isActivated = !bar.isActivated
        }
        findViewById<View>(R.id.reset).setOnClickListener {
            bar.setThumbIndices(0, 199)
        }
        bar.setOnRangeBarChangeListener {
            it
        }

    }

}
package com.example.rangebarsample

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.edmodo.rangebar.RangeBar

class MainActivity : AppCompatActivity() {
    // Initializes the RangeBar in the application
    private val rangebar by lazy { findViewById<RangeBar>(R.id.rangebar1) }
    private val refreshButton by lazy { findViewById<Button>(R.id.refresh) }
    private val thumbRadius by lazy { findViewById<TextView>(R.id.thumbRadius) }
    private val tickCount by lazy { findViewById<TextView>(R.id.tickCount) }
    private val tickCountSeek by lazy { findViewById<SeekBar>(R.id.tickCountSeek) }
    private val leftIndexValue by lazy { findViewById<TextView>(R.id.leftIndexValue) }
    private val tickHeight by lazy { findViewById<TextView>(R.id.tickHeight) }
    private val rightIndexValue by lazy { findViewById<TextView>(R.id.rightIndexValue) }
    private val thumbRadiusSeek by lazy { findViewById<SeekBar>(R.id.thumbRadiusSeek) }
    private val tickHeightSeek by lazy { findViewById<SeekBar>(R.id.tickHeightSeek) }
    private val barWeight by lazy { findViewById<TextView>(R.id.barWeight) }
    private val connectingLineWeight by lazy { findViewById<TextView>(R.id.connectingLineWeight) }
    private val barWeightSeek by lazy { findViewById<SeekBar>(R.id.barWeightSeek) }
    private val connectingLineWeightSeek by lazy { findViewById<SeekBar>(R.id.connectingLineWeightSeek) }

    override fun onRestoreInstanceState(bundle: Bundle) {
        super.onRestoreInstanceState(bundle)
        // Resets the index values every time the activity is changed
        leftIndexValue.text = "${rangebar.leftIndex}"
        rightIndexValue.text = "${rangebar!!.rightIndex}"
        // Sets focus to the main layout, not the index text fields
        findViewById<View>(R.id.mylayout).requestFocus()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.layout_activity) {
            startActivity(Intent(this, LayoutPreviewActivity::class.java))
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        rangebar.setOnRangeBarChangeListener(object : RangeBar.OnRangeBarChangeListener {
            override fun onIndexChangeListener(
                rangeBar: RangeBar?,
                leftThumbIndex: Int,
                rightThumbIndex: Int
            ) {
                leftIndexValue.text = "$leftThumbIndex"
                rightIndexValue.text = "$rightThumbIndex"
            }
        })

        // Sets the indices themselves upon input from the user
        refreshButton.setOnClickListener { // Gets the String values of all the texts
            val leftIndex = leftIndexValue.text.toString()
            val rightIndex = rightIndexValue.text.toString()

            try {
                if (leftIndex.isNotEmpty() && rightIndex.isNotEmpty()) {
                    val leftIntIndex = leftIndex.toInt()
                    val rightIntIndex = rightIndex.toInt()
                    rangebar!!.setThumbIndices(leftIntIndex, rightIntIndex)
                }
            } catch (e: IllegalArgumentException) {
            }
        }

        tickCountSeek.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(
                tickCountSeek: SeekBar,
                progress: Int,
                fromUser: Boolean
            ) {
                try {
                    rangebar!!.setTickCount(progress)
                } catch (e: IllegalArgumentException) {
                }
                tickCount.text = "tickCount = $progress"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        tickHeightSeek.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(
                tickHeightSeek: SeekBar,
                progress: Int,
                fromUser: Boolean
            ) {
                rangebar!!.setTickHeight(progress.toFloat())
                tickHeight.text = "tickHeight = $progress"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        barWeightSeek.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(
                barWeightSeek: SeekBar,
                progress: Int,
                fromUser: Boolean
            ) {
                rangebar!!.setBarWeight(progress.toFloat())
                barWeight.text = "barWeight = $progress"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        connectingLineWeightSeek.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(
                connectingLineWeightSeek: SeekBar,
                progress: Int,
                fromUser: Boolean
            ) {
                rangebar!!.setConnectingLineWeight(progress.toFloat())
                connectingLineWeight.text = "connectingLineWeight = $progress"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
        thumbRadiusSeek.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(
                thumbRadiusSeek: SeekBar,
                progress: Int,
                fromUser: Boolean
            ) {
                if (progress == 0) {
                    rangebar!!.setThumbRadius(-1f)
                    thumbRadius.text = "thumbRadius = N/A"
                } else {
                    rangebar!!.setThumbRadius(progress.toFloat())
                    thumbRadius.text = "thumbRadius = $progress"
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

}

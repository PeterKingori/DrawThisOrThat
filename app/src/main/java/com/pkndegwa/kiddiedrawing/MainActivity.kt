package com.pkndegwa.kiddiedrawing

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.pkndegwa.kiddiedrawing.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private var drawingView: DrawingView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        drawingView = binding.drawingView
        drawingView!!.setBrushSize(20.0.toFloat())
    }
}
package com.pkndegwa.kiddiedrawing

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.core.view.indices
import com.pkndegwa.kiddiedrawing.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private var drawingView: DrawingView? = null
    private var mImageButtonCurrentPaint: ImageButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        drawingView = binding.drawingView
        drawingView!!.setBrushSize(20.0.toFloat())

        val brushImageButton = binding.brushImageButton
        brushImageButton.setOnClickListener {
            selectBrushSizeDialog()
        }

        val paintColorsLayout = binding.paintColoursLayout
        mImageButtonCurrentPaint = paintColorsLayout[0] as ImageButton
        mImageButtonCurrentPaint!!.setImageDrawable(
            ContextCompat.getDrawable(this, R.drawable.pallet_selected)
        )

        for (i in paintColorsLayout.indices) {
            paintColorsLayout[i].setOnClickListener {
                selectPaintClicked(it)
            }
        }
    }

    private fun selectBrushSizeDialog() {
        val brushDialog = Dialog(this)
        brushDialog.setContentView(R.layout.dialog_brush_size)
        brushDialog.setTitle("Brush size: ")

        val smallBrush = brushDialog.findViewById<ImageButton>(R.id.small_brush_image_button)
        smallBrush.setOnClickListener {
            drawingView?.setBrushSize(10.0.toFloat())
            brushDialog.dismiss()
        }

        val mediumBrush = brushDialog.findViewById<ImageButton>(R.id.medium_brush_image_button)
        mediumBrush.setOnClickListener {
            drawingView?.setBrushSize(20.0.toFloat())
            brushDialog.dismiss()
        }

        val largeBrush = brushDialog.findViewById<ImageButton>(R.id.large_brush_image_button)
        largeBrush.setOnClickListener {
            drawingView?.setBrushSize(30.0.toFloat())
            brushDialog.dismiss()
        }
        brushDialog.show()
    }

    private fun selectPaintClicked(view: View) {
        if (view != mImageButtonCurrentPaint) {
            val imageButton = view as ImageButton
            val colorTag = imageButton.tag.toString()
            drawingView?.setColor(colorTag)

            imageButton.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.pallet_selected)
            )
            mImageButtonCurrentPaint?.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.pallet_normal)
            )
            mImageButtonCurrentPaint = view
        }
    }
}
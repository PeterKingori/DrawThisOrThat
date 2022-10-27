package com.pkndegwa.kiddiedrawing

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.core.view.indices
import com.pkndegwa.kiddiedrawing.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var drawingView: DrawingView? = null
    private var mImageButtonCurrentPaint: ImageButton? = null

    /**
     * A variable for an activity result launcher to open an intent.
     */
    private val openGalleryLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val imageBackground = binding.imageViewBackground
                imageBackground.setImageURI(result.data!!.data)
            }
        }

    /**
     * A variable for requesting permissions.
     */
    private val requestPermission: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            permissions.entries.forEach {
                val permissionName = it.key
                val isGranted = it.value

                if (isGranted) {
                    Toast.makeText(
                        this, "Permission granted. Now you can read the storage files.", Toast
                            .LENGTH_SHORT
                    ).show()

                    val selectImageIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    openGalleryLauncher.launch(selectImageIntent)
                } else {
                    if (permissionName == Manifest.permission.READ_EXTERNAL_STORAGE) {
                        Toast.makeText(
                            this, "You denied the permission.", Toast
                                .LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
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

        val galleryImageButton = binding.galleryButton
        galleryImageButton.setOnClickListener {
            requestStoragePermission()
        }
    }

    /**
     * This method launches a custom dialog to select a brush size.
     */
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

    /**
     * This method is called when a color is selected from the color pallet at the bottom.
     * @param view of type ImageButton that was clicked.
     */
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

    /**
     * This method communicates to the user why the app needs permission and asks for the permission.
     */
    private fun requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            showDialog(
                "Kiddie Drawing App",
                "The app needs to access your external storage to get a background image."
            )
        } else {
            requestPermission.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
        }
    }

    /**
     * Shows dialog saying why the app needs permission.
     * Only shown if the user has denied the permission request before.
     */
    private fun showDialog(title: String, message: String) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Request again") { dialog, _ ->
                requestPermission.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE))
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
        builder.create().show()
    }
}
package com.pkndegwa.kiddiedrawing

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
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
import androidx.lifecycle.lifecycleScope
import com.pkndegwa.kiddiedrawing.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var drawingView: DrawingView? = null
    private var mImageButtonCurrentPaint: ImageButton? = null
    var customProgressDialog: Dialog? = null

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

        val undoImageButton = binding.undoButton
        undoImageButton.setOnClickListener {
            drawingView!!.undoDrawing()
        }

        val saveButton = binding.saveButton
        saveButton.setOnClickListener {
            if (isReadStorageAllowed()) {
                showProgressDialog()
                lifecycleScope.launch {
                    val drawingViewFrameLayout = binding.drawingViewFrameLayout
                    saveBitmapFile(getBitmapFromView(drawingViewFrameLayout))
                }
            }
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

    private fun isReadStorageAllowed(): Boolean {
        val result = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        return result == PackageManager.PERMISSION_GRANTED
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
            requestPermission.launch(arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ))
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

    /**
     * Creates a bitmap from a combination of the background image and whatever has been drawn and returns it.
     */
    private fun getBitmapFromView(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val backgroundImage = view.background

        if (backgroundImage != null) backgroundImage.draw(canvas) else canvas.drawColor(Color.WHITE)
        view.draw(canvas)
        return bitmap
    }

    /**
     * A suspend function to save an image on a different thread from the main thread.
     */
    private suspend fun saveBitmapFile(bitmap: Bitmap?): String {
        var result = ""
        withContext(Dispatchers.IO) {
            if (bitmap != null) {
                try {
                    // Write a compressed version of the bitmap to the specified output stream.
                    val bytes = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, bytes)
                    val file = File(externalCacheDir?.absoluteFile.toString() + File.separator
                            + "KiddieDrawingApp_" + System.currentTimeMillis() / 1000 + ".png")
                    val fileOutput = FileOutputStream(file)
                    fileOutput.write(bytes.toByteArray())
                    fileOutput.close()

                    result = file.absolutePath

                    runOnUiThread {
                        cancelProgressDialog()
                        if (result.isNotEmpty()) {
                            Toast.makeText(
                                this@MainActivity,
                                "File saved successfully at $result",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                this@MainActivity,
                                "Something went wrong while saving the file",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    result = ""
                    e.printStackTrace()
                }
            }
        }
        return  result
    }

    /**
     * Method to show progress dialog
     */
    private fun showProgressDialog() {
        customProgressDialog = Dialog(this)
        customProgressDialog?.setContentView(R.layout.dialog_custom_progress)
        customProgressDialog?.show()
    }

    /**
     * Method to cancel progress dialog
     */
    private fun cancelProgressDialog() {
        if (customProgressDialog != null) customProgressDialog?.dismiss()
        customProgressDialog = null
    }
}
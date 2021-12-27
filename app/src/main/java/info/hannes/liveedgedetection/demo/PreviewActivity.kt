package info.hannes.liveedgedetection.demo

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.transition.TransitionManager
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.Snackbar
import info.hannes.github.AppUpdateHelper
import info.hannes.liveedgedetection.*
import info.hannes.liveedgedetection.activity.ScanActivity
import info.hannes.liveedgedetection.demo.databinding.ActivityPreviewBinding
import info.hannes.liveedgedetection.utils.*
import info.hannes.liveedgedetection.view.ScanSurfaceView
import timber.log.Timber
import java.io.File


class PreviewActivity : AppCompatActivity() {
    private var filePath: String? = null
    private lateinit var binding: ActivityPreviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPreviewBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        when(intent.action){
            ScanConstants.ACTION_START_SCAN -> {
                startScan()
            }
            ScanConstants.ACTION_START_PICK_IMAGE -> {
                startPickImage()
            }
        }

//        AppUpdateHelper.checkForNewVersion(
//                this,
//                BuildConfig.GIT_REPOSITORY,
//                BuildConfig.VERSION_NAME
//        )
    }

    private fun startScan() {
        val intent = Intent(this, ScanActivity::class.java)
        // optional, otherwise it's stored internal
        intent.action = ScanConstants.ACTION_START_SCAN
        intent.putExtra(ScanConstants.IMAGE_PATH, getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString())
        intent.putExtra(ScanConstants.TIME_HOLD_STILL, 700L)
        startActivityForResult(intent, REQUEST_CODE_START_SCAN)
    }

    private fun startPickImage() {
        ImagePicker.with(this).galleryOnly().start()
    }

    @SuppressLint("SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_START_SCAN) {
            if (resultCode == Activity.RESULT_OK) {
                data?.extras?.let { bundle ->
                    filePath = bundle.getString(ScanConstants.SCANNED_RESULT)
                    filePath?.let {
                        val baseBitmap = it.decodeBitmapFromFile()
                        binding.scannedImage.setImageBitmap(baseBitmap)
                        binding.scannedImage.scaleType = ImageView.ScaleType.FIT_CENTER

                        binding.textDensity.text = "Density ${baseBitmap.density}"
                        binding.textDimension.text = "${baseBitmap.width} / ${baseBitmap.height}"

                        showSnackbar(it)
                        Timber.i(it)
                    }

                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                finish()
            }
        }else if (requestCode == REQUEST_CODE_START_PICK_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                data?.extras?.let { bundle ->
                    filePath = bundle.getString(ScanConstants.SCANNED_RESULT)
                    filePath?.let {
                        val baseBitmap = it.decodeBitmapFromFile()
                        binding.scannedImage.setImageBitmap(baseBitmap)
                        binding.scannedImage.scaleType = ImageView.ScaleType.FIT_CENTER

                        binding.textDensity.text = "Density ${baseBitmap.density}"
                        binding.textDimension.text = "${baseBitmap.width} / ${baseBitmap.height}"

                        showSnackbar(it)
                        Timber.i(it)
                    }

                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                finish()
            }
        }else if(requestCode == ImagePicker.REQUEST_CODE){
            if (resultCode == Activity.RESULT_OK) {
                //Image Uri will not be null for RESULT_OK
                val uri: Uri = data?.data!!


                val intent = Intent(this, ScanActivity::class.java)
                // optional, otherwise it's stored internal
                intent.action = ScanConstants.ACTION_START_PICK_IMAGE
                intent.putExtra(ScanConstants.IMAGE_PATH, getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString())
//        intent.putExtra(ScanConstants.TIME_HOLD_STILL, 700L)
                intent.data = uri
                startActivityForResult(intent, REQUEST_CODE_START_PICK_IMAGE)

                // Use Uri object instead of File to avoid storage permissions
//                imageView.setImageURI(uri)
            } else if (resultCode == ImagePicker.RESULT_ERROR) {
                // Error
                //ImagePicker.getError(data)
                finish()
            } else {
                // etc
                // "Task Cancelled"
                finish()
            }
        }

        binding.buttonOpenExtern.setOnClickListener {
            externalCacheDir?.let { path ->
                filePath?.let { file ->
                    this.viewPdf(File(file).createPdf(path))
                }
            }
        }

        binding.buttonRotate.setOnClickListener {
            filePath?.let {
                var baseBitmap = it.decodeBitmapFromFile()
                baseBitmap = baseBitmap.rotate(90f)
                binding.scannedImage.setImageBitmap(baseBitmap)
                this.storeBitmap(baseBitmap, File(it))
            }
        }
    }

//    fun scaleImage(bm: Bitmap?, newWidth: Int, newHeight: Int): Bitmap? {
//        if (bm == null) {
//            return null
//        }
//        val width = bm.width
//        val height = bm.height
//        val scaleWidth = newWidth.toFloat() / width
//        val scaleHeight = newHeight.toFloat() / height
//
//        //Keep aspect ratio scaling, mainly long edges
//        val scaleRatio = Math.min(scaleHeight, scaleWidth)
//        val matrix = Matrix()
//        matrix.postScale(scaleRatio, scaleRatio)
//        val newBm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true)
//
//        //Create target size bitmap
//        val scaledImage = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888)
//        val canvas = Canvas(scaledImage)
//
//        //Draw background color
//        val paint = Paint()
//        paint.setColor(Color.GRAY)
//        paint.setStyle(Paint.Style.FILL)
//        canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), paint)
//
//        //Determine the screen position
//        var left = 0f
//        var top = 0f
//        if (width > height) {
//            top = ((newBm.width - newBm.height) / 2.0).toFloat()
//        } else {
//            left = ((newBm.height - newBm.width) / 2.0).toFloat()
//        }
//        canvas.drawBitmap(newBm, left, top, null)
//        if (!bm.isRecycled) {
//            bm.recycle()
//        }
//        return scaledImage
//    }

    private fun showSnackbar(text: String) {
        var viewPos: View? = findViewById(R.id.coordinatorLayout)
        if (viewPos == null) {
            viewPos = findViewById(android.R.id.content)
        }
        val snackbar = Snackbar.make(viewPos!!, text, Snackbar.LENGTH_INDEFINITE)
        val view = snackbar.view
        when (val params = view.layoutParams) {
            is CoordinatorLayout.LayoutParams -> {
                val paramsC = view.layoutParams as CoordinatorLayout.LayoutParams
                paramsC.gravity = Gravity.CENTER_VERTICAL
                view.layoutParams = paramsC
                snackbar.show()
            }
            is FrameLayout.LayoutParams -> {
                val paramsC = view.layoutParams as FrameLayout.LayoutParams
                paramsC.gravity = Gravity.BOTTOM
                view.layoutParams = paramsC
                snackbar.show()
            }
            else -> {
                Toast.makeText(this, text + " " + params.javaClass.simpleName, Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val REQUEST_CODE_START_SCAN = 101
        private const val REQUEST_CODE_START_PICK_IMAGE = 102
    }
}
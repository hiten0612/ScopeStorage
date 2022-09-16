package com.example.scopestoragedemo

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.*
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.scopestoragedemo.databinding.ActivityMainBinding
import java.io.*
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.net.URLConnection
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity() {

    private var imageBitmap: Bitmap? = null
    private val REQUEST_CODE_SINGLE_SELECT = 100
    private lateinit var binding: ActivityMainBinding
    private val PICK_PDF_FILE = 2
    private val PHOTO_PICKER_REQUEST_CODE = 200
    private var uri: Uri? = null

    @RequiresApi(33)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)


        val myExecutor = Executors.newSingleThreadExecutor()
        val myHandler = Handler(Looper.getMainLooper())
        val mWebPath =
            "https://media.geeksforgeeks.org/wp-content/uploads/20210224040124/JSBinCollaborativeJavaScriptDebugging6-300x160.png"
        binding.btnFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/pdf"

                // Optionally, specify a URI for the file that should appear in the
                // system file picker when it loads.
                putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri)
            }
            startActivityForResult(intent, PICK_PDF_FILE)
        }
        binding.btnImage.setOnClickListener {

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PHOTO_PICKER_REQUEST_CODE)
        }

        binding.btnCamera.setOnClickListener {

            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            try {
                startActivityForResult(takePictureIntent, REQUEST_CODE_SINGLE_SELECT)
            } catch (e: Exception) {

                Log.e("messs", e.message!!)
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                // display error state to the user
            }
        }
        binding.download.setOnClickListener {
            // mSaveMediaToStorage(imageBitmap)

            myExecutor.execute {
                imageBitmap = mLoad(mWebPath)
                myHandler.post {
                    binding.img.setImageBitmap(imageBitmap)
                    if (imageBitmap != null) {

                        mSaveMediaToStorage(imageBitmap)
                    }
                }
            }

        }

    }

    private fun mLoad(string: String): Bitmap? {
        val url: URL = mStringToURL(string)!!
        val connection: HttpURLConnection?
        try {
            connection = url.openConnection() as HttpURLConnection
            connection.connect()
            val inputStream: InputStream = connection.inputStream
            val bufferedInputStream = BufferedInputStream(inputStream)
            return BitmapFactory.decodeStream(bufferedInputStream)
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(applicationContext, "Error", Toast.LENGTH_LONG).show()
        }
        return null
    }

    private fun mStringToURL(string: String): URL? {
        try {
            return URL(string)
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        }
        return null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {

                REQUEST_CODE_SINGLE_SELECT -> {
                    uri = data?.data
                    imageBitmap = data?.extras?.get("data") as Bitmap
                    binding.img.setImageBitmap(imageBitmap)
                    // Do stuff with uri
                }

                PHOTO_PICKER_REQUEST_CODE -> {
//                    val currentUri: Uri? = data?.data
                    uri = data?.data
                    binding.img.setImageURI(uri)

                }
            }

        }
    }

        //    private fun addImageToGallery(filePath: String?, context: Context) {
    //        val values = ContentValues()
    //        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
    //        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
    //        values.put(MediaStore.MediaColumns.DATA, filePath)
    //        context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    //    }


    private fun mSaveMediaToStorage(bitmap: Bitmap?) {
        val filename = "${System.currentTimeMillis()}.jpg"
        var fos: OutputStream? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            this.contentResolver?.also { resolver ->
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }
                val imageUri: Uri? =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                fos = imageUri?.let { resolver.openOutputStream(it) }
            }
        }

        else
        {
                val imagesDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            fos = FileOutputStream(image)

        }

        fos?.use {
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, it)
            Toast.makeText(this, "Saved to Gallery", Toast.LENGTH_SHORT).show()
        }


    }


}
package com.example.rurutektrack

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.lang.ref.WeakReference
import android.app.ProgressDialog
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Matrix
import android.hardware.SensorManager.getOrientation
import android.media.ExifInterface
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var currentPhotoPath: String
    private  val CAMERA_REQUEST_CODE = 100
    private lateinit var mod: TextView
    private lateinit var skm: EditText
    private lateinit var simg: ImageView
    private lateinit var opencam: ImageView
    private lateinit var saveButton: Button
    private lateinit var user: TextView
    private lateinit var pdata: TextView
    private lateinit var sharedPreferences: SharedPreferences
    private var selectedImageBitmap: Bitmap? = null

    fun createImageFile(): File? {
        val timeStamp: String =
            SimpleDateFormat("yyyy_MMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fleet)

        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val receivedusername = sharedPreferences.getString("user", null)
        val receivedvehicle = sharedPreferences.getString("model", null)
        val receivedprevious = sharedPreferences.getString("max_end_km", null)
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.blue))
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        val colorBlueLight = ContextCompat.getColor(this, R.color.darkgrey)
        val colorBlueDark = ContextCompat.getColor(this, R.color.blue)
        bottomNavigationView.setBackgroundColor(colorBlueDark)
        bottomNavigationView.itemActiveIndicatorColor = ColorStateList.valueOf(colorBlueLight)
        supportActionBar?.hide()

        mod = findViewById(R.id.model)
        skm = findViewById(R.id.startkm)
        skm.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s != null && s.length > 30) {
                    // If the input exceeds the limit, trim it to 10 characters
                    skm.setText(s.toString().substring(0, 30))
                    skm.setSelection(30) // Move the cursor to the end
                }
            }
        })
        simg = findViewById(R.id.startup)
        user = findViewById(R.id.user)
        pdata = findViewById(R.id.ps)
        mod.text = receivedvehicle
        user.text = receivedusername
        pdata.text = receivedprevious
        saveButton = findViewById(R.id.startsave)
        opencam = findViewById(R.id.opencamera)

        opencam.setOnClickListener {
            openCamera()
        }

        val save = findViewById<Button>(R.id.startsave)
        save.setOnClickListener {
            val model = mod.text.toString()
            val startkm = skm.text.toString()
            val user = user.text.toString()
            val previous = pdata.text.toString()
            val previous1 = previous.toInt()
            if ((previous1 == 0) && (!startkm.isNullOrEmpty())) {
                uploadData(model, startkm, user)
            }
            else if (previous1 != 0){
                try {
                    val startkm1 = startkm.toInt()

                    if (startkm1 == previous1) {
                        uploadData(model, startkm, user)
                    }
                    else if (startkm1 == previous1 + 1) {
                        // Conditions met, proceed to upload data
                        uploadData(model, startkm, user)
                    } else {
                        skm.error = "Start KM must be equal to the previous value"
                    }
                }

                catch (e: NumberFormatException) {
                    // Handle the case where startkm is not a valid integer
                    skm.error = "Invalid Start KM"
                }
            }
            else {
                skm.error = "Start KM is required"

            }
        }
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.dashboard -> {
                    startActivity(Intent(applicationContext, DashboardActivity::class.java))
                    finish()
                    true
                }
                R.id.newtrip -> {
                    startActivity(Intent(applicationContext, Newtrip::class.java))
                    finish()
                    true
                }

                R.id.edit -> {
                    startActivity(Intent(applicationContext, ChangePassword::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun uploadData(model: String, startkm: String, user: String) {
        if (selectedImageBitmap != null) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val compressedByteArray = compressBitmapToByteArray(selectedImageBitmap!!)

                    val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putString("model", model)
                    editor.putString("max_end_km", startkm)
                    editor.putString("user", user)
                    editor.apply()

                    launch(Dispatchers.Main) {
                        BackgroundActivity(this@MainActivity).execute(
                            model,
                            startkm,
                            user,
                            compressedByteArray,
                            saveButton
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    launch(Dispatchers.Main) {
                        Toast.makeText(
                            this@MainActivity,
                            "Error processing image",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        } else {
            Toast.makeText(this, "No Image Selected", Toast.LENGTH_SHORT).show()
        }
    }
    override fun onBackPressed() {

        val intent = Intent(this, DashboardActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
        super.onBackPressed()
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val bitmap = BitmapFactory.decodeFile(currentPhotoPath)
            if (bitmap != null) {
                val orientation = getOrientation(currentPhotoPath!!)
                val rotatedBitmap = rotateImage(bitmap, orientation)

                simg.setImageBitmap(rotatedBitmap)
                selectedImageBitmap = rotatedBitmap
            } else {
                Toast.makeText(applicationContext, "Failed to load the image", Toast.LENGTH_SHORT)
                    .show()
                selectedImageBitmap = null
            }
        } else {
            selectedImageBitmap = null
        }

    }

    private fun rotateImage(bitmap: Bitmap, rotationAngle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(rotationAngle)
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun getOrientation(imagePath: String): Float {
        try {
            val exif = ExifInterface(imagePath)
            return when (exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return 0f
    }

    private fun openCamera() {
        val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY) &&
            captureIntent.resolveActivity(packageManager) != null
        ) {
            val photoFile: File? = createImageFile()
            photoFile?.also {
                val photoURI: Uri = FileProvider.getUriForFile(
                    this@MainActivity,
                    "com.example.rurutektrack.fileprovider",
                    it
                )
                captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(captureIntent, CAMERA_REQUEST_CODE)
            }
        } else {
            Toast.makeText(this, "Camera not available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun compressBitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 20, stream)
        return stream.toByteArray()
    }

}

class BackgroundActivity(private val context: Context) {

    private val MEDIA_TYPE_JPEG = "image/jpeg".toMediaTypeOrNull()
    private val dialog: ProgressDialog = ProgressDialog(context)

    fun execute(model: String, startkm: String, user: String, startimg: ByteArray, saveButton: Button) {
        dialog.setMessage("Uploading :)")
        dialog.setCancelable(false)
        dialog.show()

        val weakContext: WeakReference<Context> = WeakReference(context)

        GlobalScope.launch(Dispatchers.IO) {
            val result = doInBackground(model, startkm, startimg, user)
            launch(Dispatchers.Main) {
                onPostExecute(result, weakContext, startkm, user, model, saveButton )
            }
        }
    }

    private fun doInBackground(
        model: String,
        startkm: String,
        startimg: ByteArray,
        user: String
    ): String {
        val client = OkHttpClient()

        try {
            val requestBody: RequestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("model", model)
                .addFormDataPart("startkm", startkm)
                .addFormDataPart("user", user)
                .addFormDataPart(
                    "startimg",
                    "image.jpeg",
                    RequestBody.create(MEDIA_TYPE_JPEG, startimg)
                )
                .build()

            val request: Request = Request.Builder()
                .url("http://asset.rurutek.com:8080/fleet/trip.php")
                .post(requestBody)
                .build()

            val response: Response = client.newCall(request).execute()
            return if (response.isSuccessful) {

                "Success :) "
            } else {
                "Error: ${response.code}"
            }
        } catch (e: IOException) {
            return "Error: ${e.message}"
        }
    }

    private fun onPostExecute(result: String, weakContext: WeakReference<Context>, startkm: String, user:String, model: String, saveButton: Button) {
        saveButton.isEnabled = false
        dialog.dismiss()
        val context = weakContext.get()
        if (context != null) {
            if (result == "Success :) ") {
                val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                val receivedUsername = sharedPreferences.getString("user", null)
                val receivedVehicle = sharedPreferences.getString("model", null)

                editor.putString("startkm",startkm)
                editor.apply()
                    val intent = Intent(context, EndActivity::class.java)
                intent.putExtra("startkm", startkm)
                    intent.putExtra("user", receivedUsername)
                    intent.putExtra("model", receivedVehicle)

                    context.startActivity(intent)

            }
            val alertDialog = AlertDialog.Builder(context).create()
            alertDialog.setTitle("Status")
            alertDialog.setMessage(result)
            alertDialog.show()

        }
    }
}


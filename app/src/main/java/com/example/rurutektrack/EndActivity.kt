package com.example.rurutektrack

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.hardware.SensorManager.getOrientation
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.CoroutineScope
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
import java.io.File
import java.io.IOException
import java.util.Locale
import java.text.SimpleDateFormat
import java.util.Date

class EndActivity : AppCompatActivity() {

    private lateinit var currentPhotoPath: String
    private val CAMERA_REQUEST_CODE = 100
    private lateinit var plc: EditText
    private lateinit var pdata: TextView
    private lateinit var mod1: TextView
    private lateinit var ekm: EditText
    private lateinit var opencam: ImageView
    private lateinit var passenger: TextView
    private lateinit var username: TextView
    private lateinit var eimg: ImageView
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
        setContentView(R.layout.activity_end)

        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val receiveduser = sharedPreferences.getString("user", null)
        val receivedvehicle = sharedPreferences.getString("model", null)
        val receivedstart = sharedPreferences.getString("startkm", null)
        val intent = intent
        val startkm = intent.getStringExtra("startkm")

        window.setStatusBarColor(ContextCompat.getColor(this, R.color.blue));
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        val colorBlueLight = ContextCompat.getColor(this, R.color.darkgrey)
        val colorBlueDark = ContextCompat.getColor(this, R.color.blue)
        bottomNavigationView.setBackgroundColor(colorBlueDark)
        bottomNavigationView.itemActiveIndicatorColor = ColorStateList.valueOf(colorBlueLight)
        supportActionBar?.hide()

        plc = findViewById(R.id.place)
        mod1 = findViewById(R.id.model1)
        opencam = findViewById(R.id.opencamera)
        plc.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s != null && s.length > 30) {
                    plc.setText(s.toString().substring(0, 30))
                    plc.setSelection(30) // Move the cursor to the end
                }
            }
        })
        ekm = findViewById(R.id.endkm)
        ekm.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s != null && s.length > 7) {
                    // If the input exceeds the limit, trim it to 10 characters
                    ekm.setText(s.toString().substring(0, 7))
                    ekm.setSelection(7) // Move the cursor to the end
                }
            }
        })
        eimg = findViewById(R.id.endup)
        passenger = findViewById(R.id.pasengers)
        pdata = findViewById(R.id.enterdstart)
        mod1 = findViewById(R.id.model1)
        username = findViewById(R.id.user1)
        username.text = receiveduser
        mod1.text = receivedvehicle
        pdata.text = receivedstart
        pdata.text = startkm

        opencam.setOnClickListener {
            openCamera()
        }

        val endsave = findViewById<Button>(R.id.endsave)
        endsave.setOnClickListener {
            isInputValid()
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
    override fun onBackPressed() {
        // Navigate to the main screen when the back button is pressed
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

                eimg.setImageBitmap(rotatedBitmap)
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
                    this@EndActivity,
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

    private suspend fun sendDataToServer(
        place: String,
        endkm: String,
        user: String,
        model: String,
        passengers: String,
        endimg: ByteArray
    ) {
        val MEDIA_TYPE_JPEG = "image/jpeg".toMediaTypeOrNull()
        val dialog: ProgressDialog = ProgressDialog(this)

        dialog.setMessage("uploading :)")
        dialog.setCancelable(false)
        dialog.show()

        CoroutineScope(Dispatchers.IO).launch {
            val client = OkHttpClient()

            try {
                val requestBody: RequestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("place", place)
                    .addFormDataPart("endkm", endkm)
                    .addFormDataPart("model", model)
                    .addFormDataPart("user", user)
                    .addFormDataPart("passengers", passengers)
                    .addFormDataPart(
                        "endimg",
                        "image.jpeg",
                        RequestBody.create(MEDIA_TYPE_JPEG, endimg)
                    )
                    .build()

                val request: Request = Request.Builder()
                    .url("http://asset.rurutek.com:8080/fleet/trip.php")
                    .post(requestBody)
                    .build()

                val response: Response = client.newCall(request).execute()
                val result = if (response.isSuccessful) {
                    response.body?.string() ?: ""
                    "Success :) "
                } else {
                    "Error: ${response.code}"
                }

                launch(Dispatchers.Main) {
                    dialog.dismiss()
                    handlePostExecute(result, endkm, user, model)

                }
            } catch (e: IOException) {
                launch(Dispatchers.Main) {
                    dialog.dismiss()
                    Toast.makeText(this@EndActivity, "No Internet Connection", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun isInputValid() {
        val place = plc.text.toString()
        val endkm = ekm.text.toString()
        val entst = pdata.text.toString()
        val passengers = passenger.text.toString()

        if (place.isEmpty()) {
            plc.error = "Place is required"
            return
        }
        if (passengers.isEmpty()) {
            passenger.error = "Place is required"
            return
        }

        if (endkm.isEmpty()) {
            ekm.error = "End KM is required"
            return
        }
        try {
            val entstValue = entst.toInt()
            val endkmValue = endkm.toInt()
            if (entstValue > endkmValue) {
                ekm.error = "End KM should be greater than Start KM"
                return
            }
            val subtractionResult = endkmValue - entstValue

            AlertDialog.Builder(this)
                .setTitle("Check Total KM")
                .setMessage("Traveled: $subtractionResult KM")
                .setPositiveButton("OK") { _, _ ->
                    // Send data to the server when the user clicks "OK"
                    val model = mod1.text.toString()
                    val user = username.text.toString()

                    if (selectedImageBitmap != null) {
                        GlobalScope.launch(Dispatchers.IO) {
                            try {
                                val compressedByteArray =
                                    compressBitmapToByteArray(selectedImageBitmap!!)
                                launch(Dispatchers.Main) {
                                    sendDataToServer(
                                        place,
                                        endkm,
                                        user,
                                        model,
                                        passengers,
                                        compressedByteArray
                                    )

                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                launch(Dispatchers.Main) {
                                    Toast.makeText(
                                        this@EndActivity,
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
                .setNegativeButton("Cancel") { _, _ ->
// Handle
                }
                .show()
        } catch (e: NumberFormatException) {
            ekm.error = "Invalid Start KM"
        }
    }

    private fun handlePostExecute(result: String, endkm: String, user: String, model: String) {
        val dialog: ProgressDialog = ProgressDialog(this)
        dialog.dismiss()
        if (result == "Success :) ") {
            val intent = Intent(this@EndActivity, DashboardActivity::class.java)
            intent.putExtra("user", user)
            startActivity(intent)
        }

        val alertDialog = AlertDialog.Builder(this@EndActivity).create()
        alertDialog.setTitle("Status")
        alertDialog.setMessage(result)
        alertDialog.show()

        val endsave = findViewById<Button>(R.id.endsave)
        endsave.isEnabled = false
    }

    private fun compressBitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 20, stream)
        return stream.toByteArray()
    }
}

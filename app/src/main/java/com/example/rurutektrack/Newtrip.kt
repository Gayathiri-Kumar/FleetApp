package com.example.rurutektrack

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.lifecycleScope
import com.etebarian.meowbottomnavigation.MeowBottomNavigation
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okio.IOException
import org.json.JSONException
import org.json.JSONObject

class Newtrip : AppCompatActivity() {
    private lateinit var username: AutoCompleteTextView
    private lateinit var scan: AutoCompleteTextView
    private lateinit var scan_btn: Button
    private lateinit var submit: Button
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var bottomNavigation: MeowBottomNavigation
    private lateinit var scannerLayout: RelativeLayout
    private lateinit var imageView1: ImageView
    private lateinit var imageView2: ImageView
    private lateinit var imageView3: ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startQRCodeScanner()
        setContentView(R.layout.activity_newtrip)
        scan_btn = findViewById(R.id.scanbtn)
        submit = findViewById(R.id.sub)
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.blue));

        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val receivedUsername: String? = sharedPreferences.getString("username", null)
        username = findViewById(R.id.username)
        if (receivedUsername != null) {
            val editableText = Editable.Factory.getInstance().newEditable(receivedUsername)
            username.text = editableText
        }
        scan_btn.setOnClickListener {
            startQRCodeScanner()
        }

        submit.setOnClickListener {
            lifecycleScope.launch {
                val vehicle = scan.text.toString()
                val username = username.text.toString()
                val result = login(username,vehicle)
                showToast(result)
                scan.text.clear()
            }
        }

        username = findViewById(R.id.username)
        scan = findViewById(R.id.autocomplete_text)

        bottomNavigation = findViewById(R.id.bottomNavigation)
        bottomNavigation.show(1, true)
        bottomNavigation.add(MeowBottomNavigation.Model(1, R.drawable.baseline_qr_code_scanner_24))
        bottomNavigation.add(MeowBottomNavigation.Model(2, R.drawable.baseline_list_24))
        bottomNavigation.add(MeowBottomNavigation.Model(3, R.drawable.baseline_lock_reset_24))
        meowNavigation()

        scannerLayout = findViewById(R.id.scannerlayout)


        // Schedule the background change every 2 seconds
        val handler = Handler()
        handler.postDelayed(object : Runnable {
            override fun run() {
                // Repeat the task every 2 seconds
                handler.postDelayed(this, 2000)
            }
        }, 2000) // Initial delay of 2 seconds
    }


    fun meowNavigation() {
        bottomNavigation.setOnClickMenuListener { model ->
            when (model.id) {
                1 ->{
                    true
                }
                2 -> {
                    startActivity(Intent(applicationContext, DashboardActivity::class.java))
                    finish()
                    true
                }

                3 -> {
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
    private suspend fun login(username: String, vehicle: String): String {
        return withContext(Dispatchers.IO) {
            val client = OkHttpClient()

            try {
                val requestBody: RequestBody = FormBody.Builder()
                    .add("user", username)
                    .add("vehicle", vehicle)
                    .build()

                val request: Request = Request.Builder()
                    .url("http://asset.rurutek.com:8080/fleet/scanner.php")
                    .post(requestBody)
                    .build()

                val response: Response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val resultJson = response.body?.string()
                    if (resultJson != null) {
                        try {
                            val jsonResponse = JSONObject(resultJson)
                            val status = jsonResponse.getString("status")

                            if (status.trim() == "start") {
                                val returnedUsername = jsonResponse.getString("user")
                                val returnedVehicle = jsonResponse.getString("model")
                                val returnedPrevious = jsonResponse.getString("max_end_km")
                                val editor = sharedPreferences.edit()
                                editor.putString("user", returnedUsername)
                                editor.putString("model", returnedVehicle)
                                editor.putString("max_end_km", returnedPrevious)
                                editor.apply()
                                runOnUiThread {
                                    val intent = Intent(this@Newtrip, MainActivity::class.java)
                                    intent.putExtra("user", returnedUsername)
                                    intent.putExtra("model", returnedVehicle)
                                    intent.putExtra("max_end_km",returnedPrevious)
                                    startActivity(intent)
                                }
                                val result = "New Trip :)"
                                return@withContext result
                            }
                            else if (status.trim() == "end") {
                                val returnedUsername = jsonResponse.getString("user")
                                val returnedVehicle = jsonResponse.getString("model")
                                val returnedstart = jsonResponse.getString("startkm")
                                val editor = sharedPreferences.edit()
                                editor.putString("user", returnedUsername)
                                editor.putString("model", returnedVehicle)
                                editor.putString("startkm", returnedstart)
                                editor.apply()
                                runOnUiThread {
                                    val intent = Intent(this@Newtrip, EndActivity::class.java)
                                    sharedPreferences.getString("user", null) != null
                                    intent.putExtra("user", returnedUsername)
                                    intent.putExtra("model", returnedVehicle)
                                    intent.putExtra("startkm", returnedstart)
                                    startActivity(intent)
                                }
                                val result = "Already IN USE"
                                return@withContext result

                            }
                            else if (status.trim() == "vehiclenotfound") {
                                return@withContext "Invalid QR code"
                            }
                            else {
                                return@withContext ("Unrecognized Status: $status").toString()
                            }
                        } catch (e: JSONException) {
                            Log.d("LoginResponse", resultJson)
                            return@withContext Pair(null, "Error11: ${e.message}").toString()
                        }
                    }
                    else {
                        return@withContext Pair(null, "Login Failed2").toString()
                    }
                } else {
                    return@withContext Pair(null, "Login Failed3: ${response.code}").toString()
                }
            }
            catch (e: IOException) {
                return@withContext "No Internet Connection"
            }
        }
    }

    private fun showToast(text: String) {
        runOnUiThread {
            Toast.makeText(this@Newtrip, text, Toast.LENGTH_LONG).show()
        }
    }
    private fun startQRCodeScanner() {
        val integrator = IntentIntegrator(this)
        integrator.setPrompt(" Scan Vehicle QR Code ")
        integrator.setBeepEnabled(false)
        integrator.setOrientationLocked(true)
        integrator.initiateScan()
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val result: IntentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(this, "Scan canceled", Toast.LENGTH_SHORT).show()
            } else {
                val scannedData = result.contents
                scan.setText(scannedData)
            }
        }
    }
}
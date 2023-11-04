package com.example.rurutektrack

import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import android.widget.ToggleButton
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

class Login : AppCompatActivity() {
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var sharedPreferences: SharedPreferences
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.grey));
        supportActionBar?.hide()
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE) // Initialize sharedPreferences
        etEmail = findViewById(R.id.uname)
        etPassword = findViewById(R.id.etSinInPassword)

        val togglePassword = findViewById<ToggleButton>(R.id.togglePassword)
        togglePassword.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Password is currently hidden, show it
                etPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            } else {
                // Password is currently visible, hide it
                etPassword.inputType =
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }
            // Move the cursor to the end of the text
            etPassword.setSelection( etPassword.text.length)
        }

        val loginButton = findViewById<Button>(R.id.btnSignIn)
        loginButton.setOnClickListener {
            if (isInputValid()) {
                val username = etEmail.text.toString()
                val password = etPassword.text.toString()
                lifecycleScope.launch {
                    val result = login(username, password)
                    showToast(result)
                }
            }
        }
        if (isUserLoggedIn()) {
            // User is already logged in, go to MainActivity
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    override fun onBackPressed() {
        // Navigate to the main screen when the back button is pressed
        val intent = Intent(this, Splash::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
        super.onBackPressed()
    }
    private fun isUserLoggedIn(): Boolean {
        // Check the SharedPreferences to determine if the user is logged in
        return sharedPreferences.getString("username", null) != null
    }
    override fun onResume() {
        super.onResume()
        // Clear the EditText fields when the user returns to the login page
        etEmail.text.clear()
        etPassword.text.clear()
    }
    private fun isInputValid(): Boolean {
        val username = etEmail.text.toString()
        val password = etPassword.text.toString()

        if (username.isEmpty()) {
            etEmail.error = "Username is required"
            return false
        }

        if (password.isEmpty()) {
            etPassword.error = "Password is required"
            return false
        }

        return true
    }

    private suspend fun login(username: String, password: String): String {
        return withContext(Dispatchers.IO) {
            val client = OkHttpClient()

            try {
                val requestBody: RequestBody = FormBody.Builder()
                    .add("emp_id", username)
                    .add("password", password)
                    .build()

                val request: Request = Request.Builder()
                    .url("http://asset.rurutek.com:8080/fleet/login.php") // Replace with your API URL
                    .post(requestBody)
                    .build()

                val response: Response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val resultJson = response.body?.string()

                    if (resultJson != null) {
                        try {
                            val jsonResponse = JSONObject(resultJson)
                            val status = jsonResponse.getString("status")

                            if (status.trim() == "success") {
                                val returnedUsername = jsonResponse.getString("emp_id")
                                val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
                                val editor = sharedPreferences.edit()
                                editor.putString("username", returnedUsername)
                                editor.apply()
                                runOnUiThread {
                                    val intent = Intent(this@Login, DashboardActivity::class.java)
                                    intent.putExtra("emp_id", returnedUsername)
                                    startActivity(intent)
                                }
                                val result = "Login Successful :)"
                                return@withContext result
                            }
                            else if (status.trim() == "incorrect_password") {
                                return@withContext "Incorrect Password".toString()
                            } else if (status.trim() == "notregistered") {
                                return@withContext "No user found! Contact Admin".toString()
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
            Toast.makeText(this@Login, text, Toast.LENGTH_LONG).show()
        }
    }
}
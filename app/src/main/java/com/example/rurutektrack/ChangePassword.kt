package com.example.rurutektrack

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.res.ColorStateList
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.lifecycleScope
import com.etebarian.meowbottomnavigation.MeowBottomNavigation
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import org.json.JSONException
import org.json.JSONObject

class ChangePassword : AppCompatActivity() {
    private lateinit var oldPassword_ET: EditText
    private lateinit var newPassword_ET: EditText
    private lateinit var confirmPassword_ET: EditText
    private lateinit var username: TextView
    private lateinit var resetButton_BTN: Button
    private lateinit var bottomNavigation: MeowBottomNavigation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        supportActionBar?.hide()
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.blue));
        oldPassword_ET = findViewById(R.id.old)
        newPassword_ET = findViewById(R.id.newp)
        confirmPassword_ET = findViewById(R.id.renew)
        resetButton_BTN = findViewById(R.id.changesave)
        username = findViewById(R.id.model)

        val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val receivedUsername: String? = sharedPreferences.getString("username", null)
        username = findViewById(R.id.model)
        if (receivedUsername != null) {
            val editableText = Editable.Factory.getInstance().newEditable(receivedUsername)
            username.text = editableText
        }
        resetButton_BTN.setOnClickListener {
            if (isInputValid()) {

                var newPassword = newPassword_ET.text.toString()
                var username = username.text.toString()
                var oldpswd= oldPassword_ET.text.toString()
                lifecycleScope.launch {
                    val result = change(username, newPassword, oldpswd)
                    showToast(result)
                }
            }
        }
        bottomNavigation = findViewById(R.id.bottomNavigation)
        bottomNavigation.show(3, true)
        bottomNavigation.add(MeowBottomNavigation.Model(1, R.drawable.baseline_qr_code_scanner_24))
        bottomNavigation.add(MeowBottomNavigation.Model(2, R.drawable.baseline_list_24))
        bottomNavigation.add(MeowBottomNavigation.Model(3, R.drawable.baseline_lock_reset_24))
        meowNavigation()

    }
    fun meowNavigation() {
        bottomNavigation.setOnClickMenuListener { model ->
            when (model.id) {
                1 ->{
                    startActivity(Intent(applicationContext, Newtrip::class.java))
                    finish()
                    true
                }
                2 -> {
                    startActivity(Intent(applicationContext, DashboardActivity::class.java))
                    finish()
                    true
                }

                3 -> {
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

    private fun isInputValid(): Boolean {
        var oldPassword = oldPassword_ET.text.toString();
        var newPassword = newPassword_ET.text.toString()
        var confirmPassword = confirmPassword_ET.text.toString();

        if (oldPassword.isEmpty()) {
            oldPassword_ET.error = "Old Password is Required"
            return false
        }
        if (newPassword.isEmpty()) {
            newPassword_ET.error = "New Password is Required"
            return false
        }
        if (confirmPassword.isEmpty()) {
            confirmPassword_ET.error = "Re-enter Password"
            return false
        }
        if (newPassword != confirmPassword) {
            confirmPassword_ET.error = "does not match"
            return false

        }
        return true
    }
    private suspend fun change (username: String, newPassword: String, oldpswd: String): String? {
        return withContext(Dispatchers.IO) {
        val client = OkHttpClient()

        // Replace with your server URL
        val url = "http://asset.rurutek.com:8080/fleet/update_pwd.php"

        // Create a request body with the old and new passwords
        val requestBody = FormBody.Builder()
            .add("emp_id", username)
            .add("new_password", newPassword)
            .add("old_password",oldpswd)
            .build()

        // Create a POST request
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

            try {
                val response = client.newCall(request).execute()
                val responseText = response.body?.string()
                if (response.isSuccessful) {
                    val resultJson = responseText
                    if (resultJson != null) {
                        try {
                            val jsonResponse = JSONObject(resultJson)
                            val status = jsonResponse.getString("status")

                            if (status.trim() == "success") {
                                // Password changed successfully
                               logout()
                                return@withContext "Password updated successfully"

                            } else if (status.trim() == "failed") {
                                // Password change failed, get error message

                                return@withContext "failed: Old Password is Incorrect"
                            }
                        } catch (e: JSONException) {
                            return@withContext "Error: ${e.message}"
                        }
                    }
                }
                return@withContext "Failed to update password. Please try again."
            } catch (e: IOException) {
                return@withContext "No Internet Connection"
            }
        }
    }
    private fun logout() {
        val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove("username") // Clear the username from SharedPreferences
        editor.apply()

        // Redirect to the UserLoginActivity
        val intent = Intent(this, Login::class.java)
        startActivity(intent)
        finish() // Finish the current activity (MainActivity)
    }
    private fun showToast(message: String?) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

}
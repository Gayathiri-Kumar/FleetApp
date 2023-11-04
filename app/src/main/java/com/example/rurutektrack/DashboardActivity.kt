package com.example.rurutektrack

import android.content.Intent
import android.content.res.ColorStateList
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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


class DashboardActivity : AppCompatActivity() {
    lateinit var itemdev: RecyclerView
    lateinit var adapter: TimelineAdapter
    private lateinit var username: TextView
    private lateinit var trip: Button
    private var receivedusername: String? = null

    private fun itemdev() {
        itemdev.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        adapter = TimelineAdapter(ArrayList())
        itemdev.adapter = adapter

        TimelineAdapter.onItemClick = {
            val intent = Intent(this@DashboardActivity,EndActivity::class.java)
            intent.putExtra("username", receivedusername)
            startActivity(intent)
                    finish()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        supportActionBar?.hide()
        trip = findViewById(R.id.data)
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.blue));
        val sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        receivedusername = sharedPreferences.getString("username", null)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        val colorBlueLight = ContextCompat.getColor(this, R.color.darkgrey)
        val colorBlueDark = ContextCompat.getColor(this, R.color.blue)
        bottomNavigationView.setBackgroundColor(colorBlueDark)
        bottomNavigationView.itemActiveIndicatorColor = ColorStateList.valueOf(colorBlueLight)

        trip.setOnClickListener {
            logout()
        }

        username = findViewById(R.id.usernamed)
        receivedusername?.let {
            username.text = it

            // Launch a coroutine to call the suspend function
            lifecycleScope.launch {
                fetchDataFromServer(it) { timelineData ->
                    // Update the UI with the fetched data here
                  adapter.setData(timelineData)
                }
            }
        }


        itemdev = findViewById(R.id.itemdev)
        itemdev()

        val itemDecoration = DividerItemDecoration(itemdev.context, DividerItemDecoration.VERTICAL)
        itemdev.addItemDecoration(itemDecoration)

        bottomNavigationView.selectedItemId = R.id.dashboard
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {

                R.id.dashboard -> true
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
        val intent = Intent(this, Newtrip::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
        super.onBackPressed()
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
    private suspend fun fetchDataFromServer(username: String, callback: (List<TimelineData>) -> Unit) {
        return withContext(Dispatchers.IO) {
            val client = OkHttpClient()
            val url = "http://asset.rurutek.com:8080/fleet/dashboard.php"
            val requestBody = FormBody.Builder()
                .add("user", username)
                .build()
            val request = Request.Builder()
                .url(url) // Replace with your API endpoint URL
                .post(requestBody)
                .build()
            try {
                val response = client.newCall(request).execute()
                val responseText = response.body?.string()

                if (!responseText.isNullOrEmpty()) {
                    // Parse the JSON response as a JSONObject
                    val jsonResponse = JSONObject(responseText)

                    // Check if the "status" field indicates success
                    val status = jsonResponse.getString("status")
                    if (status == "success") {

                        val dataArray = jsonResponse.getJSONArray("data")

                        // Initialize a list to store parsed TimelineData objects
                        val timelineDataList = mutableListOf<TimelineData>()

                        // Loop through the JSON objects in the array and parse them
                        for (i in 0 until dataArray.length()) {
                            val dataObject = dataArray.getJSONObject(i)
                            val user = dataObject.getString("user")
                            val model = dataObject.getString("model")
                            val startkm = dataObject.getString("startkm")
                            val start_time = dataObject.optString("start_time", null)
                            val endkm = dataObject.getString("endkm")
                            val end_time = dataObject.optString("end_time", null)
                            val place = dataObject.getString("place")

                            val timelineData = TimelineData(user,model, startkm, start_time, endkm, end_time, place)
                            timelineDataList.add(timelineData)
                        }

                        // Use runOnUiThread to update the UI on the main thread
                        runOnUiThread {
                            // Callback with the parsed list
                            callback(timelineDataList)
                            // Update the adapter and RecyclerView here
                            adapter.setData(timelineDataList)
                        }

                    } else {
                        showErrorMessage("No Data found! Add New Trip")
                    }
                } else {
                    showErrorMessage("Response text is null or empty.")
                }
            } catch (e: IOException) {
                showErrorMessage("No Internet Connection")
            } catch (e: JSONException) {
                showErrorMessage("JSON parsing error: ${e.message}")
            }
        }
    }

    private fun showErrorMessage(message: String) {
        runOnUiThread {
            Toast.makeText(this@DashboardActivity, message, Toast.LENGTH_SHORT).show()
        }
    }
}


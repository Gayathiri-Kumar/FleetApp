package com.example.rurutektrack

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import org.json.JSONException
import org.json.JSONObject

class Drivers : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DriverAdapter
    private var DriverList: MutableList<Drivermodel> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drivers)

        recyclerView = findViewById(R.id.datare)
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.grey));

        // Initialize the adapter
        adapter = DriverAdapter(this,DriverList)

        // Set the adapter and layout manager for the RecyclerView
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        lifecycleScope.launch {
            fetchDataFromServer() {Drivermodel ->
                adapter.setData(Drivermodel)
            }
        }

    }
    private suspend fun fetchDataFromServer(callback: (List<Drivermodel>) -> Unit) {
        return withContext(Dispatchers.IO) {
            val client = OkHttpClient()
            val url = "http://asset.rurutek.com:8080/fleet/Driver.php"
            val requestBody = FormBody.Builder()
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
                        val DataList = mutableListOf<Datamodel>()

                        // Loop through the JSON objects in the array and parse them
                        for (i in 0 until dataArray.length()) {
                            val dataObject = dataArray.getJSONObject(i)
                            val emp = dataObject.getString("emp_id")
                            val user = dataObject.getString("name")
                            val pswd = dataObject.getString("password")

                            val data = Drivermodel(emp,user,pswd)
                            DriverList.add(data)
                        }

                        // Use runOnUiThread to update the UI on the main thread
                        runOnUiThread {
                            // Callback with the parsed list
                            callback(DriverList)
                            // Update the adapter and RecyclerView here
                            adapter.setData(DriverList)
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
        Log.d("DataActivity", "Data fetching completed.")
    }

    private fun showErrorMessage(message: String) {
        runOnUiThread {
            Toast.makeText(this@Drivers, message, Toast.LENGTH_SHORT).show()
        }
    }
}
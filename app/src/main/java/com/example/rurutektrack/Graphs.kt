package com.example.rurutektrack

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.util.Random

class Graphs : AppCompatActivity() {
    private var vehicleSpinner: Spinner? = null
    private var monthSpinner: Spinner? = null
    private var barChart: BarChart? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.graph)
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.grey));
        vehicleSpinner = findViewById(R.id.vehicleSpinner)
        monthSpinner = findViewById<Spinner>(R.id.monthSpinner)
        barChart = findViewById(R.id.barChart)
        val vehicleAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.vehicle_model,
            android.R.layout.simple_spinner_item
        )
        vehicleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        vehicleSpinner?.setAdapter(vehicleAdapter)
        val monthAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.months,
            android.R.layout.simple_spinner_item
        )
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        monthSpinner?.setAdapter(monthAdapter)

        // Button click listener
        val fetchDataButton = findViewById<Button>(R.id.fetchDataButton)
        fetchDataButton.setOnClickListener {
            // Get selected items from spinners
            var selectedVehicle: String? = vehicleSpinner?.getSelectedItem().toString()
            // Add 1 to convert to month number
            var selectedMonth: String? = monthSpinner?.getSelectedItem().toString()
            try {
                // Encode parameters
                val selectedVehicles = selectedVehicle.toString()
                val selectedMonths = selectedMonth.toString()
                lifecycleScope.launch {
                    val result = RetriveData(selectedVehicles, selectedMonths)
                    showToast(result)
                }
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }
        }
    }
    suspend fun RetriveData(model: String, month: String): String {
        return withContext(Dispatchers.IO) {
            val client = OkHttpClient()
            try {
                val requestBody: RequestBody = FormBody.Builder()
                    .add("model", model)
                    .add("month", month)
                    .build()

                val request = Request.Builder()
                    .url("http://asset.rurutek.com:8080/fleet/chart.php")
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val resultJson = response.body?.string()

                    if (resultJson != null) {
                        try {
                            val jsonResponse = JSONObject(resultJson)
                            val status = jsonResponse.getString("status")

                            if (status.trim() == "success") {
                                val dataArray = jsonResponse.optJSONArray("data") ?: JSONArray()
                                if (dataArray.length() > 0) {
                                    val entries = ArrayList<BarEntry>()
                                    val colors = ArrayList<Int>()
                                    val dataSets = ArrayList<IBarDataSet>()
                                    val xAxisLabels = ArrayList<String>()
                                    val customColors = intArrayOf(
                                        Color.parseColor("#66156A"),
                                        Color.parseColor("#EF7FE3"),
                                        Color.parseColor("#4CD587"),
                                        Color.parseColor("#567F87"),
                                        Color.parseColor("#8D91A4"),
                                        Color.parseColor("#EC355F"),
                                        Color.parseColor("#F8D81C"),
                                        Color.parseColor("#DBA135"),
                                        Color.parseColor("#9188D0"),
                                        Color.parseColor("#D4E462"),
                                        Color.parseColor("#2E94AF"),
                                        Color.parseColor("#EF80D0"),
                                        Color.parseColor("#27E9F1"),
                                        Color.parseColor("#93700D"),
                                        Color.parseColor("#3E0F0F"),
                                        Color.parseColor("#FAAB07"),
                                        Color.parseColor("#1044DB"),
                                        Color.parseColor("#A5842A"),
                                        Color.parseColor("#E2A2C8"),
                                        Color.parseColor("#7B009E"),
                                    )
                                    for (i in 0 until dataArray.length()) {
                                        val jsonObject = dataArray.getJSONObject(i)
                                        val kilometerValue = jsonObject.getInt("Tkm")
                                        val user = jsonObject.getString("user")
                                        entries.add(BarEntry(i.toFloat(),kilometerValue.toFloat()))
                                        colors.add(customColors[i % customColors.size])
                                        val singleEntryDataSet = BarDataSet(listOf(BarEntry(i.toFloat(), kilometerValue.toFloat())), user)
                                        singleEntryDataSet.color = customColors[i % customColors.size] // Set color for this dataset
                                        dataSets.add(singleEntryDataSet)
                                    }
                                    val barData = BarData(dataSets)

                                    val chart = barChart // Smart cast may not work here

                                    chart?.let {
                                        val leftYAxis: YAxis? = chart.axisLeft
                                        val rightYAxis: YAxis? = chart.axisRight
                                        val botXAxis: XAxis? = chart.xAxis

                                        leftYAxis?.setDrawGridLines(false)
                                        rightYAxis?.setDrawGridLines(false)
                                        botXAxis?.setDrawGridLines(false)

                                            withContext(Dispatchers.Main) {
                                                barChart?.data = barData
                                                botXAxis?.valueFormatter = IndexAxisValueFormatter(xAxisLabels)
                                                botXAxis?.position = XAxis.XAxisPosition.BOTTOM
                                                botXAxis?.labelCount = entries.size
                                                botXAxis?.labelRotationAngle = -45f
                                                barChart?.isScaleXEnabled = true
                                                barChart?.description?.text = "Travel Kilometer"
                                                barChart?.xAxis?.setDrawGridLines(false)
                                                barChart?.axisLeft?.setDrawGridLines(false)
                                                barChart?.axisRight?.setDrawGridLines(false)
                                                barChart?.isAutoScaleMinMaxEnabled = true
                                                barChart?.animateY(1000)
                                                barChart?.invalidate()
                                            }
                                        return@withContext "Processing completed"
                                    } ?: run {
                                        return@withContext "No valid context or chart"
                                    }

                                } else {
                                    return@withContext "No data found for the selected Name and vehicle model"
                                }
                            } else {
                                return@withContext "Unrecognized Status: $status"
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            return@withContext "Error parsing JSON: ${e.message}"
                        }
                    } else {
                        return@withContext "No data found in the response"
                    }
                } else {
                    return@withContext "HTTP request failed with code: ${response.code}"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext "An exception occurred: ${e.message}"
            }
        }
    }

    private fun showToast(text: String) {
        runOnUiThread {
            Toast.makeText(this@Graphs, text, Toast.LENGTH_LONG).show()
        }
    }
}




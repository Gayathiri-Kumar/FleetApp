package com.example.rurutektrack
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
class Splash : AppCompatActivity() {
    var image: ImageView? = null
    private lateinit var progressBar: ProgressBar
    private lateinit var sharedPreferences: SharedPreferences

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val progressBar = findViewById<View>(R.id.progressBar) as ProgressBar
        sharedPreferences = getSharedPreferences("your_prefs_name", MODE_PRIVATE)
        progressBar.visibility = View.VISIBLE
        supportActionBar?.hide()
        progressBar.max = 100
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.blue))
        image = findViewById(R.id.splash_logo)
        val handler = Handler()
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

        // Declare progressRunnable outside the postDelayed block
        val progressRunnable = Runnable {
            if (isLoggedIn) {
                // User is already logged in, go to MainActivity
                startActivity(Intent(this, DashboardActivity::class.java))
            } else {
                // User is not logged in, go to UserLoginActivity
                startActivity(Intent(this, Login::class.java))
            }
            finish() // Delay to simulate progress
        }

        // Update the progress every 100 milliseconds (you can adjust this)
        handler.postDelayed({
            val progressIncrement = 10 // Set how much you want the progress to increment
            updateProgressBar(progressIncrement)
            handler.postDelayed(progressRunnable, 2000)
        }, 2000)
    }

    private fun updateProgressBar(increment: Int) {
        if (::progressBar.isInitialized) {
            val currentProgress = progressBar.progress + increment
            progressBar.progress = currentProgress
        }
    }
}

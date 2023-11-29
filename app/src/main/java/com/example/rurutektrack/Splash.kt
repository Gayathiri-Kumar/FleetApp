package com.example.rurutektrack
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
class Splash : AppCompatActivity() {
    var image: ImageView? = null
    private lateinit var imageView: ImageView
    private val SPLASH_TIMEOUT: Long = 3000

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        val animation = AnimationUtils.loadAnimation(this, R.anim.fade)
        imageView = findViewById(R.id.imageView)
        // Apply the animation to your imageView
        imageView?.startAnimation(animation)

        window.setStatusBarColor(ContextCompat.getColor(this, R.color.blue))
        image = findViewById(R.id.splash_logo)
        imageView = findViewById(R.id.imageView)

        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)
        val userType = sharedPreferences.getString("userType", "")

        Handler().postDelayed({
            when {
                isLoggedIn -> {
                    // User already logged in
                    when (userType) {
                        "user" -> startActivity(Intent(this@Splash, DashboardActivity::class.java))
                        "admin" -> startActivity(Intent(this@Splash, Admin::class.java))
                    }
                }
                else -> {
                    // User not logged in, go to login page
                    startActivity(Intent(this@Splash, Login::class.java))
                }
            }
            finish()
        }, SPLASH_TIMEOUT)

    }
}
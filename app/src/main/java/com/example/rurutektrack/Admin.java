package com.example.rurutektrack;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import android.graphics.Color;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.etebarian.meowbottomnavigation.MeowBottomNavigation;

public class Admin extends AppCompatActivity {

    CardView cardView1, cardView2, cardView3, cardView4;
    SharedPreferences sharedPreferences;
    Button logout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);
        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.darkgrey));
        logout=findViewById(R.id.alogout);
        cardView1 = findViewById(R.id.grid_1);
        cardView2 = findViewById(R.id.grid_2);
        cardView3 = findViewById(R.id.grid_3);
        cardView4 = findViewById(R.id.grid_4);
// Update the SharedPreferences with login information
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.putString("userType", "admin"); // Change to "admin" for admin
        editor.apply();

// Check if the user is logged in
        boolean isUserLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        if (!isUserLoggedIn) {
            alogout();
        }
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alogout();
            }
        });

        cardView1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // User touched the card
                        cardView1.setCardBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.blue));
                        return true;
                    case MotionEvent.ACTION_UP:
                        // User released the touch
                        cardView1.setCardBackgroundColor(Color.WHITE); // Change color on touch up
                        Intent intent1 = new Intent(Admin.this, Data.class);
                        startActivity(intent1);
                        return true;
                }
                return false;
            }
        });

        cardView2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // User touched the card
                        cardView2.setCardBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.blue));
                        return true;
                    case MotionEvent.ACTION_UP:
                        // User released the touch
                        cardView2.setCardBackgroundColor(Color.WHITE); // Change color on touch up
                        // Perform other actions here
                        Intent intent2 = new Intent(Admin.this, Drivers.class);
                        startActivity(intent2);
                        return true;
                }
                return false;
            }
        });

        cardView3.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // User touched the card
                        cardView3.setCardBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.blue));
                        return true;
                    case MotionEvent.ACTION_UP:
                        // User released the touch
                        cardView3.setCardBackgroundColor(Color.WHITE); // Change color on touch up
                        // Perform other actions here
                        Intent intent3 = new Intent(Admin.this, Vehicle.class);
                        startActivity(intent3);
                        return true;
                }
                return false;
            }
        });

        cardView4.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // User touched the card
                        cardView4.setCardBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.blue));
                        return true;
                    case MotionEvent.ACTION_UP:
                        // User released the touch
                        cardView4.setCardBackgroundColor(Color.WHITE); // Change color on touch up
                        // Perform other actions here
                        Intent intent4 = new Intent(Admin.this, Graphs.class);
                        startActivity(intent4);
                        return true;
                }
                return false;
            }
        });
    }
    private void alogout() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("isLoggedIn");
        editor.apply();
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
        finish();
    }
    @Override
    public void onBackPressed() {
        // Navigate to the main screen when the back button is pressed
        Intent intent = new Intent(this, Splash.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
        super.onBackPressed();
    }
}
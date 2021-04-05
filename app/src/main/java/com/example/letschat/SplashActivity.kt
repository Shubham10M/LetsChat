package com.example.letschat

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {
    val auth by lazy{
        FirebaseAuth.getInstance()
    }

   // private val SPLASH_TIME_OUT:Long = 3000 // 1 sec
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
           if(auth.currentUser == null){
               startActivity(Intent(this, LoginActivity::class.java))
           }
        else{
               startActivity(Intent(this, MainActivity::class.java))
        }
//        Handler().postDelayed({
//            // This method will be executed once the timer is over
//            // Start your app main activity
//
//            startActivity(Intent(this, LoginActivity::class.java))
//
//            // close this activity
//            finish()
//        }, SPLASH_TIME_OUT)
    }
}
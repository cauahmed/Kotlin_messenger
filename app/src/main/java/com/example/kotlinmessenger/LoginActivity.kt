package com.example.kotlinmessenger

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_main.*

private lateinit var auth: FirebaseAuth

class LoginActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()

        login_button_login.setOnClickListener {

            performsignin()

        }

        back_to_register_activity.setOnClickListener {
            Log.d("LoginActivity", "Try to show register activity")

            //launch the register activity

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun performsignin(){
        val email = email_edittext_login.text.toString()
        val password = password_edittext_login.text.toString()

        Log.d("Login", "Attempt login with email/pw: $email/***")

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful){
                    //Sign in successful
                    Toast.makeText(this, "Successfully Logged In", Toast.LENGTH_LONG).show()
                    Log.d("Sign In", "Sign in with email successful")
                    val intent = Intent(this, MessagesReceivedActivity::class.java)
                    startActivity(intent)
                    finish()
                    //val user = auth.currentUser
                }else{
                    //Sign in unsuccessful
                    Log.w("Sign In", "Unable to sign in user", task.exception)
                    Toast.makeText(baseContext, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
                // ...
            }


    }
}
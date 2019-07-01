package com.example.messenger

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        login_login_button.setOnClickListener {

            performlogin()//FUNCTION TO LOGIN THE USER
        }



        backtoreg_login_textview.setOnClickListener {
            finish()
        }
    }


    private fun performlogin()
    {

        val email = email_login_edittext.text.toString()
        val password = password_logib_edittext.text.toString()

        Log.d("Login","Attempt login with email/pw: $email/***")

        //LOGIN WITH USERNAME AND PASSWORD

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password)
            .addOnCompleteListener {
                Toast.makeText(this,"LOGIN SUCCESSFULL",Toast.LENGTH_SHORT).show()
                //MT ADDITION

                val intent =Intent(this,LatestMessageActivity::class.java)
                startActivity(intent)
            }
            .addOnFailureListener{
                Log.d("Loginact","Login failed: ${it.message}")
                Toast.makeText(this,"Login Failed ${it.message}",Toast.LENGTH_SHORT).show()
            }
    }
}

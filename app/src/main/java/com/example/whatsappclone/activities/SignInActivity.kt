package com.example.whatsappclone.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.whatsappclone.R
import com.google.firebase.FirebaseApp

class SignInActivity : AppCompatActivity() {
    private var mAuth:FirebaseApp?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)


    }
}
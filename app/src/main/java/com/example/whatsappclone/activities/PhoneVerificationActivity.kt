package com.example.whatsappclone.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.whatsappclone.constants.Constants
import com.example.whatsappclone.constants.Constants.INTENT_KEY_FOR_UID
import com.example.whatsappclone.databinding.ActivityPhoneVerificationBinding
import com.google.firebase.auth.FirebaseAuth

class PhoneVerificationActivity : AppCompatActivity() {
    private var binding: ActivityPhoneVerificationBinding? = null
    private var auth: FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhoneVerificationBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        auth = FirebaseAuth.getInstance()

        //Checking if a user has already signed in.
        if (auth!!.currentUser != null) {
            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra(auth!!.uid, INTENT_KEY_FOR_UID)
            startActivity(intent)
            finishAffinity()
        }
        binding!!.etPhoneNumber.requestFocus()
        binding!!.btnContinue.setOnClickListener {
            val intent = Intent(this, OtpActivity::class.java)
            intent.putExtra(
                Constants.INTENT_KEY_PHONE_NUMBER,
                binding!!.etPhoneNumber.text.toString()
            )
            startActivity(intent)
        }
    }
}
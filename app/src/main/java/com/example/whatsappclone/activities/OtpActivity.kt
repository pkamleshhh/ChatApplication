package com.example.whatsappclone.activities

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.whatsappclone.databinding.ActivityOtpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthOptions
import java.util.concurrent.TimeUnit
import android.view.inputmethod.InputMethodManager

import com.google.firebase.auth.PhoneAuthProvider

import androidx.annotation.NonNull

import com.google.firebase.FirebaseException

import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import android.widget.Toast

import android.text.Editable

import android.text.TextWatcher
import android.view.KeyEvent

import android.view.View
import android.content.Intent
import android.widget.ProgressBar
import com.example.whatsappclone.constants.Constants
import com.example.whatsappclone.constants.Constants.MESSAGE_FILL
import com.example.whatsappclone.constants.Constants.MESSAGE_LOGGING_IN
import com.example.whatsappclone.constants.Constants.MESSAGE_LOGIN_FAILED
import com.example.whatsappclone.constants.Constants.MESSAGE_SENDING_OTP

import com.google.firebase.auth.AuthResult

import com.google.android.gms.tasks.Task

import com.google.android.gms.tasks.OnCompleteListener


@Suppress("DEPRECATION")
class OtpActivity : AppCompatActivity() {
    private var binding: ActivityOtpBinding? = null
    private var auth: FirebaseAuth? = null
    private var verificationId: String? = null
    private var phoneNumber: String? = null
    private var dialog: ProgressDialog? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        phoneNumber = intent.getStringExtra(Constants.INTENT_KEY_PHONE_NUMBER)
        binding!!.tvVerifyPhone.text = "Verify $phoneNumber"

        //Showing the progress dialog
        dialog = ProgressDialog(this)
        dialog!!.setMessage(MESSAGE_SENDING_OTP)
        dialog!!.setCancelable(false)
        dialog!!.show()

        //Set focusing for the otp boxes.
        setOtpBoxes()

        //Setting up firebase authentication.
        setUpAuthentication()

        //Check whether the entered code matches.
        binding!!.btnContinue.setOnClickListener {
            var enteredCode =
                binding!!.otpPinView.text.toString()
            if (enteredCode.length != 6) {
                Toast.makeText(this, MESSAGE_FILL, Toast.LENGTH_SHORT).show()
            } else {
                dialog!!.setMessage(MESSAGE_LOGGING_IN)
                dialog!!.setCancelable(false)
                dialog!!.show()
                val credential: PhoneAuthCredential =
                    PhoneAuthProvider.getCredential(verificationId, enteredCode)
                auth!!.signInWithCredential(credential).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val intent = Intent(this, ProfileActivity::class.java)
                        dialog!!.dismiss()
                        startActivity(intent)
                        finishAffinity()
                    } else {
                        Toast.makeText(this, MESSAGE_LOGIN_FAILED, Toast.LENGTH_SHORT).show()
                        dialog!!.dismiss()
                    }
                }
            }
        }
    }


    private fun setUpAuthentication() {
        auth = FirebaseAuth.getInstance()
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {

                }

                override fun onVerificationFailed(e: FirebaseException) {

                }

                override fun onCodeSent(
                    verifyId: String,
                    forceResendingToken: ForceResendingToken
                ) {
                    super.onCodeSent(verifyId, forceResendingToken)
                    verificationId = verifyId
                    dialog!!.dismiss()

                }
            }).build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun setOtpBoxes() {

    }


}
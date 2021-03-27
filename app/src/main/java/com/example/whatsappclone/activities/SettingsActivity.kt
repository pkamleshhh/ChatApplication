package com.example.whatsappclone.activities

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.LiveFolders.INTENT
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.whatsappclone.R
import com.example.whatsappclone.constants.Constants
import com.example.whatsappclone.constants.Constants.INTENT_KEY_FOR_UID
import com.example.whatsappclone.constants.Constants.MSG_SETTING_UP_PROFILE
import com.example.whatsappclone.constants.Constants.NODE_NAME_USERS
import com.example.whatsappclone.constants.Constants.REQUEST_CODE_PROFILE
import com.example.whatsappclone.constants.Constants.REQUEST_CODE_SETTING
import com.example.whatsappclone.databinding.ActivitySettingsBinding
import com.example.whatsappclone.models.UserStatus
import com.example.whatsappclone.models.Users
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

@Suppress("DEPRECATION")
class SettingsActivity : AppCompatActivity() {
    private var binding: ActivitySettingsBinding? = null
    private var userId: String? = null
    private var dataBase: FirebaseDatabase? = null
    private var storage: FirebaseStorage? = null
    private var dialog: ProgressDialog? = null
    private var fileUri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        userId = intent.getStringExtra(INTENT_KEY_FOR_UID)!!

        //Set user's particulars.
        getUserData()

        binding!!.ivBack.setOnClickListener {
            finish()
        }

        binding!!.ivPhotoChange.setOnClickListener {
            var intent: Intent? = Intent()
            intent!!.action = Intent.ACTION_GET_CONTENT
            intent.type = Constants.CONTENT_TYPE
            startActivityForResult(intent, REQUEST_CODE_SETTING)
        }
        binding!!.btnSave.setOnClickListener {

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data!!.data != null) {
            fileUri = data!!.data!!
            binding!!.ivAvatar.setImageURI(fileUri)
        }
    }

    private fun openDialog(msg: String) {
        dialog = ProgressDialog(this)
        dialog!!.setMessage(msg)
        dialog!!.setCancelable(false)
        dialog!!.show()
    }

    private fun setParticulars(userProfilePic: String, userName: String, userStatus: String) {
        Glide.with(this).load(userProfilePic).placeholder(R.drawable.avatar)
            .into(binding!!.ivAvatar)
        binding!!.etUsername.setText(userName)
        binding!!.etAbout.setText(userStatus)
    }

    private fun getUserData() {
        dataBase = FirebaseDatabase.getInstance()
        dataBase!!.reference.child(NODE_NAME_USERS)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (data: DataSnapshot in snapshot.children) {
                            val userProfilePic = data.child("profilePic").value.toString()
                            val userName = data.child("userName").value.toString()
                            val userStatus = data.child("userStatus").value.toString()
                            setParticulars(userProfilePic, userName, userStatus)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun ta(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
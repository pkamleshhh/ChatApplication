package com.example.whatsappclone.activities

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.whatsappclone.R
import com.example.whatsappclone.constants.Constants
import com.example.whatsappclone.constants.Constants.NODE_NAME_PROFILES
import com.example.whatsappclone.constants.Constants.NODE_NAME_STATUS
import com.example.whatsappclone.constants.Constants.NODE_NAME_USERS
import com.example.whatsappclone.constants.Constants.REQUEST_CODE_SETTING
import com.example.whatsappclone.constants.Constants.STATUS_OFFLINE
import com.example.whatsappclone.constants.Constants.STATUS_ONLINE
import com.example.whatsappclone.constants.Constants.UPDATING_PROFILE_MSG
import com.example.whatsappclone.databinding.ActivitySettingsBinding
import com.example.whatsappclone.models.Users
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import java.util.*

@Suppress("DEPRECATION")
class SettingsActivity : AppCompatActivity() {
    private var binding: ActivitySettingsBinding? = null
    private var userId: String? = null
    private var database: FirebaseDatabase? = null
    private var storage: FirebaseStorage? = null
    private var dialog: ProgressDialog? = null
    private var fileUri: Uri? = null
    private var userObject: Users? = null
    private var dataChanged: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        userId = FirebaseAuth.getInstance().uid

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

        binding!!.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            database!!.reference.child(NODE_NAME_USERS)
                .child(userId!!).removeValue()
            database!!.reference.child(NODE_NAME_STATUS)
                .child(userId!!).removeValue()
            val intent = Intent(this, PhoneVerificationActivity::class.java)
            startActivity(intent)
        }
        binding!!.btnSave.setOnClickListener {
            openDialog(UPDATING_PROFILE_MSG)
            userObject!!.userName = binding!!.etUsername.text.toString()
            userObject!!.userStatus = binding!!.etAbout.text.toString()
            storage = FirebaseStorage.getInstance()
            var storageReference =
                storage!!.reference.child(NODE_NAME_PROFILES).child(userId!!)
            storageReference.putFile(fileUri!!)
                .addOnCompleteListener(OnCompleteListener<UploadTask.TaskSnapshot?> { task ->
                    if (task.isSuccessful) {
                        storageReference.downloadUrl
                            .addOnSuccessListener(OnSuccessListener<Uri> {
                                userObject!!.profilePic = it.toString()
                                database!!.reference
                                    .child(NODE_NAME_USERS)
                                    .child(userId!!)
                                    .setValue(userObject)
                                    .addOnSuccessListener {
                                        dialog!!.dismiss()
                                    }
                            })
                    }
                })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data!!.data != null) {
            fileUri = data!!.data!!
            dataChanged = true
            binding!!.ivAvatar.setImageURI(fileUri)
        }
    }

    private fun openDialog(msg: String) {
        dialog = ProgressDialog(this)
        dialog!!.setMessage(msg)
        dialog!!.setCancelable(false)
        dialog!!.show()
    }

    private fun setUserStatus(status: String) {
        database!!.reference.child(Constants.NODE_NAME_STATUS).child(userId!!)
            .setValue(status)
    }

    override fun onResume() {
        super.onResume()
        setUserStatus(STATUS_ONLINE)
    }

    override fun onPause() {
        super.onPause()
        setUserStatus(STATUS_OFFLINE)
    }

    override fun onStop() {
        setUserStatus(STATUS_OFFLINE)
        super.onStop()
    }

    private fun setParticulars(userObject: Users) {
        Glide.with(this).load(userObject.profilePic).placeholder(R.drawable.avatar)
            .into(binding!!.ivAvatar)
        binding!!.etUsername.setText(userObject.userName)
        binding!!.etAbout.setText(userObject.userStatus)
    }

    private fun getUserData() {
        database = FirebaseDatabase.getInstance()
        database!!.reference.child(NODE_NAME_USERS)
            .addValueEventListener(object : ValueEventListener {
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (data: DataSnapshot in snapshot.children) {
                            userObject = data.getValue(Users::class.java)
                            if (userObject!!.userId == userId) {
                                setParticulars(userObject!!)
                                break
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
}
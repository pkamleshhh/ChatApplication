package com.example.whatsappclone.activities

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.whatsappclone.constants.Constants
import com.example.whatsappclone.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.android.gms.tasks.OnSuccessListener

import com.google.firebase.storage.UploadTask

import com.example.whatsappclone.constants.Constants.CONTENT_TYPE
import com.example.whatsappclone.constants.Constants.MSG_SETTING_UP_PROFILE
import com.example.whatsappclone.constants.Constants.NODE_NAME_PROFILES
import com.example.whatsappclone.constants.Constants.NODE_NAME_USERS
import com.example.whatsappclone.constants.Constants.SELECT_NAME
import com.example.whatsappclone.models.Users

import com.google.android.gms.tasks.OnCompleteListener


@Suppress("DEPRECATION")
class ProfileActivity : AppCompatActivity() {
    private var binding: ActivityProfileBinding? = null
    private var auth: FirebaseAuth? = null
    private var database: FirebaseDatabase? = null
    private var storage: FirebaseStorage? = null
    private var selectedImage: Uri? = null
    private var dialog: ProgressDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        binding!!.ivAvatar.setOnClickListener {
            var intent: Intent? = Intent()
            intent!!.action = Intent.ACTION_GET_CONTENT
            intent.type = CONTENT_TYPE
            startActivityForResult(intent, Constants.REQUEST_CODE_PROFILE)
        }
        binding!!.btnContinue.setOnClickListener {
            var name: String = binding!!.etName.text.toString()
            if (name.isEmpty()) {
                binding!!.etName.error = SELECT_NAME
            }
            if (selectedImage != null) {
                openDialog(MSG_SETTING_UP_PROFILE)
                var storageReference: StorageReference =
                    storage!!.reference.child(NODE_NAME_PROFILES).child(auth!!.uid!!)
                storageReference.putFile(selectedImage!!)
                    .addOnCompleteListener(OnCompleteListener<UploadTask.TaskSnapshot?> { task ->
                        if (task.isSuccessful) {
                            storageReference.downloadUrl
                                .addOnSuccessListener(OnSuccessListener<Uri> { uri ->
                                    val imageUrl = uri.toString()
                                    val uid = auth!!.uid
                                    val phone = auth!!.currentUser.phoneNumber
                                    val name: String = binding!!.etName.text.toString()
                                    val user = Users(uid!!, name, phone, imageUrl, "")
                                    database!!.reference
                                        .child(NODE_NAME_USERS)
                                        .child(uid!!)
                                        .setValue(user)
                                        .addOnSuccessListener {
                                            dialog!!.dismiss()
                                            val intent = Intent(
                                                this,
                                                HomeActivity::class.java
                                            )
                                            startActivity(intent)
                                            finish()
                                        }
                                })
                        }
                    })
            }
        }
    }

    private fun openDialog(msg: String) {
        dialog = ProgressDialog(this)
        dialog!!.setMessage(msg)
        dialog!!.setCancelable(false)
        dialog!!.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (data != null) {
            binding!!.ivAvatar.setImageURI(data.data)
            selectedImage = data.data
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
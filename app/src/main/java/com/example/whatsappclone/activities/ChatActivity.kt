package com.example.whatsappclone.activities


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.whatsappclone.constants.Constants
import com.example.whatsappclone.databinding.ActivityChatBinding
import com.example.whatsappclone.models.ChatMessages
import com.google.firebase.auth.FirebaseAuth
import java.util.*
import kotlin.collections.ArrayList
import AdapterRvMessages
import android.annotation.SuppressLint
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.session.MediaSession
import android.media.session.MediaSession.Token
import android.net.Uri
import android.view.View
import com.example.whatsappclone.constants.Constants.CONTENT_TYPE
import com.example.whatsappclone.constants.Constants.ENCRYPTION_ALGORITHM
import com.example.whatsappclone.constants.Constants.ENCRYPTION_KEY
import com.example.whatsappclone.constants.Constants.INTENT_CODE_FOR_ATTACHMENT_MEDIA
import com.example.whatsappclone.constants.Constants.LOADING_MSG
import com.example.whatsappclone.constants.Constants.NODE_NAME_CHATS
import com.example.whatsappclone.constants.Constants.NODE_NAME_MESSAGES
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import java.security.NoSuchAlgorithmException
import javax.crypto.Cipher
import javax.crypto.NoSuchPaddingException
import javax.crypto.spec.SecretKeySpec
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import javax.crypto.BadPaddingException
import javax.crypto.IllegalBlockSizeException
import com.google.android.gms.tasks.OnCompleteListener
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.ActionMode
import android.view.Menu
import android.view.Window
import android.widget.Button
import com.bumptech.glide.Glide
import com.example.whatsappclone.R
import com.example.whatsappclone.constants.Constants.MESSAGE_PHOTO
import com.example.whatsappclone.constants.Constants.NODE_NAME_STATUS
import com.example.whatsappclone.constants.Constants.STATUS_OFFLINE
import com.example.whatsappclone.constants.Constants.STATUS_ONLINE
import com.example.whatsappclone.constants.Constants.STATUS_TYPING
import com.example.whatsappclone.constants.Constants.UPLOADING_MSG
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import java.lang.Exception


@Suppress("DEPRECATION")
class ChatActivity : AppCompatActivity(), AdapterRvMessages.ItemClicked {
    private var binding: ActivityChatBinding? = null
    private var adapterRvMessages: AdapterRvMessages? = null
    private var messagesData = ArrayList<ChatMessages>()
    private var senderRoom: String? = null
    private var receiverRoom: String? = null
    private var dataBase: FirebaseDatabase? = null
    private var dataBaseRef: DatabaseReference? = null
    private var senderUid: String? = null
    private var receiverUid: String? = null
    private var dialog: ProgressDialog? = null
    private var cipher: Cipher? = null
    private var secretKeySpec: SecretKeySpec? = null
    private var storage: FirebaseStorage? = null
    private var profilePic: String? = null
    private var userName: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        setSupportActionBar(binding!!.toolbar)

        //Showing the progress dialog.
        openDialog(LOADING_MSG)

        // Setting the particulars.
        userName = intent.getStringExtra(Constants.INTENT_KEY_FOR_NAME)
        profilePic = intent.getStringExtra(Constants.INTENT_KEY_FOR_PRO_PIC)
        receiverUid = intent.getStringExtra(Constants.INTENT_KEY_FOR_UID)
        senderUid = FirebaseAuth.getInstance().uid!!

        // Getting database reference.
        init()

        //Get user status
        getUserStatus()

        // Getting the data from server.
        getDataFromServer()

        setUserStatus(STATUS_ONLINE)

        binding!!.ivBack.setOnClickListener {
            finish()
        }

        binding!!.ivSend.setOnClickListener {
            // Taking chat input and updating to server.
            val typedMessage = encryptMessage(binding!!.etTextMessage.text.toString())
            updateChat(typedMessage, "")
        }
        binding!!.ivAttachments.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = CONTENT_TYPE
            startActivityForResult(intent, INTENT_CODE_FOR_ATTACHMENT_MEDIA)
        }

        val handler = Handler()
        binding!!.etTextMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
                setUserStatus(STATUS_TYPING)
                handler.removeCallbacksAndMessages(null)
                handler.postDelayed(userStoppedTyping, 1000)
            }

            var userStoppedTyping = Runnable {
                setUserStatus(STATUS_ONLINE)
            }

        })
        supportActionBar!!.setDisplayShowTitleEnabled(false)
//        updateToken(FirebaseInstanceId.getInstance().token!!)
    }

    private fun getUserStatus() {
        dataBase!!.reference.child(NODE_NAME_STATUS).child(receiverUid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val status = snapshot.value.toString()
                        if (status.isNotEmpty()) {
                            if (status == "offline") {
                                binding!!.tvStatus.visibility = View.GONE
                            } else {
                                binding!!.tvStatus.text = status
                                binding!!.tvStatus.visibility = View.VISIBLE
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == INTENT_CODE_FOR_ATTACHMENT_MEDIA) {
            if (data != null) {
                try {
                    val selectedImage: Uri = data.data!!
                    val calender: Calendar = Calendar.getInstance()
                    val storageReference = storage!!.reference.child(NODE_NAME_CHATS)
                        .child(calender.timeInMillis.toString())
                    openDialog(UPLOADING_MSG)
                    storageReference.putFile(selectedImage)
                        .addOnCompleteListener(OnCompleteListener<UploadTask.TaskSnapshot?> { task ->
                            dialog!!.dismiss()
                            if (task.isSuccessful) {
                                storageReference.downloadUrl.addOnSuccessListener { p0 ->
                                    val filePath = p0.toString()
                                    updateChat(MESSAGE_PHOTO, filePath)
                                }
                            }
                        })
                } catch (e: Exception) {

                }
            }
        }
    }

    private fun openDialog(msg: String) {
        dialog = ProgressDialog(this)
        dialog!!.setMessage(msg)
        dialog!!.setCancelable(false)
        dialog!!.show()
    }

//    private fun updateToken(token: String) {
//        val token = com.example.whatsappclone.notification.Token(token!!)
//        dataBaseRef!!.child(FirebaseAuth.getInstance().uid!!).setValue(token)
//    }

    private fun updateChat(msg: String, msgUrl: String) {
        binding!!.etTextMessage.text = null
        val randomKey = dataBase!!.reference.push().key
        val date = Date()
        val messageObject = ChatMessages(randomKey!!, msg, msgUrl, senderUid!!, date.time, 0)
        dataBase!!.reference.child(NODE_NAME_CHATS)
            .child(senderRoom!!)
            .child(NODE_NAME_MESSAGES)
            .child(randomKey!!)
            .setValue(messageObject).addOnSuccessListener {
                dataBase!!.reference.child(NODE_NAME_CHATS)
                    .child(receiverRoom!!)
                    .child(NODE_NAME_MESSAGES)
                    .child(randomKey)
                    .setValue(messageObject).addOnSuccessListener { }
            }
    }

    private fun getDataFromServer() {
        dataBase!!.reference.child(NODE_NAME_CHATS).child(senderRoom!!)
            .child(NODE_NAME_MESSAGES)
            .addValueEventListener(object : ValueEventListener {
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot) {
                    messagesData.clear()
                    for (data: DataSnapshot in snapshot.children) {
                        var msg = data.getValue(ChatMessages::class.java)
                        msg!!.messageId = data.key!!
                        messagesData.add(msg)
                    }
                    adapterRvMessages!!.notifyDataSetChanged()
                    binding!!.rvChatMessages.scrollToPosition(messagesData.size - 1)
                    dialog!!.dismiss()
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }

    private fun init() {
        senderRoom = senderUid + receiverUid
        receiverRoom = receiverUid + senderUid
        dataBase = FirebaseDatabase.getInstance()
        dataBaseRef = FirebaseDatabase.getInstance().getReference("Token")
        storage = FirebaseStorage.getInstance()
        adapterRvMessages =
            AdapterRvMessages(this, messagesData, senderRoom!!, receiverRoom!!, this)
        binding!!.rvChatMessages.adapter = adapterRvMessages
        binding!!.tvPersonName.text = userName
        Glide.with(this).load(profilePic).placeholder(R.drawable.avatar).into(binding!!.ivAvatar)
    }

    private fun encryptMessage(msg: String): String {
        try {
            cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM)
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        } catch (e: NoSuchPaddingException) {
            e.printStackTrace()
        }
        secretKeySpec = SecretKeySpec(ENCRYPTION_KEY, ENCRYPTION_ALGORITHM)
        var returnString: String? = null
        val stringByte: ByteArray = msg.toByteArray()
        var encryptedByte: ByteArray = byteArrayOf(stringByte.size.toByte())
        try {
            cipher!!.init(Cipher.ENCRYPT_MODE, secretKeySpec)
            encryptedByte = cipher!!.doFinal(stringByte)
        } catch (e: BadPaddingException) {
            e.printStackTrace()
        } catch (e: IllegalBlockSizeException) {
            e.printStackTrace()
        }


        val charSet = Charset
            .forName("ISO-8859-1")
        try {
            returnString = String(encryptedByte, charSet)
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }
        return returnString!!
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

    private fun setUserStatus(status: String) {
        dataBase!!.reference.child(NODE_NAME_STATUS).child(senderUid!!)
            .setValue(status)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.chat_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun openDialog(position: Int) {
        val customDialog = Dialog(this)
        customDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        customDialog.setContentView(R.layout.layout_dialog)
        customDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        customDialog.window!!.attributes.windowAnimations =
            R.style.DialogTheme
        val btnDeleteForMe =
            customDialog.findViewById<Button>(R.id.btnDeleteForMe)
        val btnDeleteForAll = customDialog.findViewById<Button>(R.id.btnDeleteForEveryone)
        if (messagesData[position].senderId != FirebaseAuth.getInstance().uid) {
            btnDeleteForAll.visibility = View.GONE
        }
        btnDeleteForMe!!.setOnClickListener {
            dataBase!!.reference.child(NODE_NAME_CHATS)
                .child(senderRoom!!)
                .child(NODE_NAME_MESSAGES)
                .child(messagesData[position].messageId).removeValue()
            customDialog.dismiss()
        }
        btnDeleteForAll!!.setOnClickListener {
            dataBase!!.reference.child(NODE_NAME_CHATS)
                .child(senderRoom!!)
                .child(NODE_NAME_MESSAGES)
                .child(messagesData[position].messageId).removeValue()
            dataBase!!.reference.child(NODE_NAME_CHATS)
                .child(receiverRoom!!)
                .child(NODE_NAME_MESSAGES)
                .child(messagesData[position].messageId).removeValue()
            customDialog.dismiss()
        }
        customDialog.show()
    }

    override fun onItemClicked(position: Int) {
        openDialog(position)
    }

}
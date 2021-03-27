package com.example.whatsappclone.activities

import android.annotation.SuppressLint
import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.whatsappclone.adapters.AdapterRvTopStatus
import com.example.whatsappclone.databinding.ActivityMainBinding
import com.example.whatsappclone.models.UserStatus
import com.example.whatsappclone.models.Users
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DataSnapshot

import com.google.firebase.database.ValueEventListener

import android.content.Intent

import android.R

import androidx.annotation.NonNull

import AdapterRvChatRows
import com.example.whatsappclone.constants.Constants
import com.example.whatsappclone.constants.Constants.CONTENT_TYPE
import com.example.whatsappclone.constants.Constants.INTENT_CODE_FOR_STATUS_MEDIA
import com.example.whatsappclone.constants.Constants.INTENT_KEY_FOR_UID
import com.example.whatsappclone.constants.Constants.LOADING_MSG
import com.example.whatsappclone.constants.Constants.NODE_NAME_USERS
import com.google.firebase.storage.FirebaseStorage


@Suppress("DEPRECATION")
class HomeActivity : AppCompatActivity(), AdapterRvChatRows.ItemClicked {
    private var auth: FirebaseAuth? = null
    private var binding: ActivityMainBinding? = null
    private var dataBase: FirebaseDatabase? = null
    private var usersData = ArrayList<Users>()
    private var usersStatusData = ArrayList<UserStatus>()
    private var adapterRvRvChatRows: AdapterRvChatRows? = null
    private var adapterRvRvUsersStatus: AdapterRvTopStatus? = null
    private var dialog: ProgressDialog? = null
    private var userId:String?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        userId=intent.getStringExtra(INTENT_KEY_FOR_UID)

        //Show the progress dialog.
        openDialog()

        // Setup the servers.
        init()

        //Get server data.
        getServerData()

        //Bottom Navigation Listener.
        binding!!.bnbHome.setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                com.example.whatsappclone.R.id.status -> {
                    val intent = Intent()
                    intent.type = CONTENT_TYPE
                    intent.action = Intent.ACTION_GET_CONTENT
                    startActivityForResult(intent, INTENT_CODE_FOR_STATUS_MEDIA)
                }
            }
            false
        })
    }

    private fun getServerData() {
        dataBase!!.reference.child(NODE_NAME_USERS)
            .addValueEventListener(object : ValueEventListener {
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot) {
                    usersData.clear()
                    if (snapshot.exists()) {
                        for (data: DataSnapshot in snapshot.children) {
                            var user = data.getValue(Users::class.java)
                            if (user!!.userId != auth!!.uid) {
                                usersData.add(user)
                            }
                        }
                    }
                    adapterRvRvChatRows!!.notifyDataSetChanged()
                    dialog!!.dismiss()
                }

                override fun onCancelled(error: DatabaseError) {}
            })

    }

    private fun init() {
        auth = FirebaseAuth.getInstance()
        dataBase = FirebaseDatabase.getInstance()
        adapterRvRvChatRows = AdapterRvChatRows(this, usersData, auth!!.uid!!)
        binding!!.rvChatRows.adapter = adapterRvRvChatRows
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = RecyclerView.HORIZONTAL
        binding!!.rvUsersStatuses.layoutManager = layoutManager
        adapterRvRvUsersStatus = AdapterRvTopStatus(this, usersStatusData)
        binding!!.rvUsersStatuses.adapter = adapterRvRvUsersStatus
    }

    private fun openDialog() {
        dialog = ProgressDialog(this)
        dialog!!.setMessage(LOADING_MSG)
        dialog!!.setCancelable(false)
        dialog!!.show()
    }

    override fun onActivityReenter(resultCode: Int, data: Intent?) {
        if (data!!.data != null) {
            val firebaseStorage = FirebaseStorage.getInstance()


        }
        super.onActivityReenter(resultCode, data)
    }

    override fun onResume() {
        super.onResume()
        val currentId = FirebaseAuth.getInstance().uid
        dataBase!!.reference.child(Constants.NODE_NAME_STATUS).child(currentId!!)
            .setValue(Constants.STATUS_ONLINE)
    }

    override fun onPause() {
        super.onPause()
        val currentId = FirebaseAuth.getInstance().uid
        dataBase!!.reference.child(Constants.NODE_NAME_STATUS).child(currentId!!)
            .setValue(Constants.STATUS_OFFLINE)
    }

    override fun onStop() {
        val currentId = FirebaseAuth.getInstance().uid
        dataBase!!.reference.child(Constants.NODE_NAME_STATUS).child(currentId!!)
            .setValue(Constants.STATUS_OFFLINE)
        super.onStop()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(com.example.whatsappclone.R.menu.top_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            com.example.whatsappclone.R.id.search -> {

            }
            com.example.whatsappclone.R.id.settings -> {
                val intent=Intent(this,SettingsActivity::class.java)
                intent.putExtra(INTENT_KEY_FOR_UID,FirebaseAuth.getInstance().uid )
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onItemClicked(position: Int) {

    }
}
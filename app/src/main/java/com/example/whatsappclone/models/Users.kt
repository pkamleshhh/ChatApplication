package com.example.whatsappclone.models

class Users(
    var userId: String,
    val userName: String,
    val phoneNumber: String,
    val profilePic: String,
    val userStatus: String
) {
    constructor() : this("", "", "", "", "")
}

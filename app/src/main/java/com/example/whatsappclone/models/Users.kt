package com.example.whatsappclone.models

class Users(
    var userId: String,
    var userName: String,
    var phoneNumber: String,
    var profilePic: String,
    var userStatus: String
) {
    constructor() : this("", "", "", "", "")
}

package com.example.whatsappclone.models

class Status {
    var imageUrl: String? = null
    var timeStamp: Long = 0

    constructor() {}
    constructor(imageUrl: String?, timeStamp: Long) {
        this.imageUrl = imageUrl
        this.timeStamp = timeStamp
    }
}

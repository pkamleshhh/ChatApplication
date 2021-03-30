package com.example.whatsappclone.models

class Status {
    private var imageUrl: String? = null
    private var timeStamp: Long = 0

    constructor() {}
    constructor(imageUrl: String?, timeStamp: Long) {
        this.imageUrl = imageUrl
        this.timeStamp = timeStamp
    }
}

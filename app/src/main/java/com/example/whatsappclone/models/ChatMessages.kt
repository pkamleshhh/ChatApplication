package com.example.whatsappclone.models

import java.sql.Timestamp

class ChatMessages(
    var messageId: String,
    var message: String,
    var messageUrl: String,
    var senderId: String,
    var timeStamp: Long,
    var feeling: Int
) {
    constructor() : this("", "", "","", 0, 0)
}
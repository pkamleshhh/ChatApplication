package com.example.whatsappclone.constants

object Constants {
    const val INTENT_KEY_PHONE_NUMBER = "phoneNumber"
    const val REQUEST_CODE_PROFILE = 9
    const val REQUEST_CODE_SETTING = 12
    const val INTENT_KEY_FOR_NAME = "userName"
    const val INTENT_KEY_FOR_UID = "userId"
    const val INTENT_KEY_FOR_PRO_PIC = "profilePic"
    const val ITEM_SENT = 1
    const val ITEM_RECEIVED = 2
    const val NODE_NAME_CHATS = "chats"
    const val NODE_NAME_MESSAGES = "messages"
    const val NODE_NAME_MESSAGE = "message"
    const val NODE_NAME_USERS = "users"
    const val NODE_NAME_PROFILES = "Profiles"
    const val MSG_SETTING_UP_PROFILE = "Setting up profile.."
    const val NODE_NAME_TIME_STAMP = "timeStamp"
    const val NODE_NAME_STATUS = "presence"
    const val STATUS_ONLINE = "online"
    const val STATUS_OFFLINE = "offline"
    const val STATUS_TYPING = "typing.."
    const val SELECT_NAME = "Please select a name."
    const val DATE_PATTERN = "hh:mm a"
    const val CONTENT_TYPE = "image/*"
    const val LOADING_MSG = "Loading..."
    const val UPLOADING_MSG="Uploading Image.."
    const val UPDATING_PROFILE_MSG="Updating Profile.."
    const val INTENT_CODE_FOR_STATUS_MEDIA = 10
    const val ENCRYPTION_ALGORITHM="AES"
    const val INTENT_CODE_FOR_ATTACHMENT_MEDIA=11
    const val INTENT_CODE_FOR_HOME_ACTIVITY=13
    const val MESSAGE_PHOTO="photo"
    const val MESSAGE_FILL="Please fill all the boxes."
    const val MESSAGE_LOGGING_IN="Logging in..."
    const val MESSAGE_LOGIN_FAILED="Logging failed..."
    const val MESSAGE_SENDING_OTP="Sending OTP..."
    val ENCRYPTION_KEY =
        byteArrayOf(9, -11, 19, -21, 29, -31, 39, -41, 49, -51, 59, -61, 69, -71, 79, -81)

}
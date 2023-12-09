package com.example.rescyou

import com.google.firebase.Timestamp

class UserModel {

    lateinit var pinId: String

    lateinit var pinUserId: String
    lateinit var pinName: String

    lateinit var pinRescuerId: String
    lateinit var pinRescuerName: String

    lateinit var createdTimestamp: Timestamp
    lateinit var fcmToken: String

    constructor() // Empty constructor required for Firebase

    constructor(pinId:String?, pinUserId: String?,pinName: String?,pinRescuerId: String?, pinRescuerName: String?, createdTimestamp: Timestamp?, fcmToken: String?) {
        this.pinId = pinId.toString()
        this.pinUserId = pinUserId.toString()
        this.pinName = pinName.toString()
        this.pinRescuerId = pinRescuerId.toString()
        this.pinRescuerName = pinRescuerName.toString()
        this.createdTimestamp = createdTimestamp!!
        this.fcmToken = fcmToken.toString()
    }
}
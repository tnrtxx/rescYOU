package com.example.rescyou

class MyPinModel {

    lateinit var fcmToken: String

    lateinit var pinId: String
    lateinit var pinUserId: String

    lateinit var pinName: String
    lateinit var pinRescuer: String

    lateinit var date: String
    lateinit var time: String

    lateinit var rate: String
    lateinit var disasterType: String
    lateinit var sitio: String
    lateinit var description: String

    lateinit var latitude: String
    lateinit var longitude: String

    lateinit var isResolved: String

    var attachmentList: ArrayList<String> = ArrayList()
}
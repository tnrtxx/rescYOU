package com.example.rescyou

import android.app.Application
import com.google.firebase.database.FirebaseDatabase

class InformationOffline : Application(){
    override fun onCreate() {
        super.onCreate()
        FirebaseDatabase.getInstance("https://rescyou-57570-default-rtdb.asia-southeast1.firebasedatabase.app/").setPersistenceEnabled(true)
    }
}
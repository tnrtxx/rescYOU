package com.example.rescyou.utils

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.text.SimpleDateFormat


object FirebaseUtil {

    fun currentUserId(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    fun isLoggedIn(): Boolean {
        return currentUserId() != null
    }

    fun currentUserDetails(): DatabaseReference {
        return FirebaseDatabase.getInstance("https://rescyou-57570-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Users")

    }

    fun allUserCollectionReference(): CollectionReference {
        return FirebaseFirestore.getInstance().collection("users")
    }

    fun getChatroomReference(chatroomId: String): DocumentReference {
        return FirebaseFirestore.getInstance().collection("chatrooms").document(chatroomId)
    }

    fun getChatroomMessageReference(chatroomId: String): CollectionReference {
        return getChatroomReference(chatroomId).collection("chats")
    }

    fun getChatroomId(userId1: String, userId2: String): String {
        return if (userId1.hashCode() < userId2.hashCode()) {
            userId1 + "_" + userId2
        } else {
            userId2 + "_" + userId1
        }
    }

    fun allChatroomCollectionReference(): CollectionReference {
        return FirebaseFirestore.getInstance().collection("chatrooms")
    }

    fun getOtherUserFromChatroom(userIds: List<String>): DocumentReference {
        return if (userIds[0] == currentUserId()) {
            allUserCollectionReference().document(userIds[1])
        } else {
            allUserCollectionReference().document(userIds[0])
        }
    }

    fun timestampToString(timestamp: Timestamp): String {
        return SimpleDateFormat("HH:MM").format(timestamp.toDate())
    }

    fun logout() {
        FirebaseAuth.getInstance().signOut()
    }

    fun getCurrentProfilePicStorageRef(): StorageReference {
        return FirebaseStorage.getInstance().reference.child("profile_pic")
            .child(currentUserId()!!)
    }

    fun getOtherProfilePicStorageRef(otherUserId: String): StorageReference {
        return FirebaseStorage.getInstance().reference.child("profile_pic")
            .child(otherUserId)
    }

    fun initializeFirebase(context: Context) {
        FirebaseApp.initializeApp(context)
        FirebaseMessaging.getInstance().subscribeToTopic("helpRequests")
    }
}
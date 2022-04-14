package com.agrimetal.yorimichi2.Manager

import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage

class StorageManager {
    constructor()

    private val storage = FirebaseStorage.getInstance()
    private val TAG = "StorageManager"

    public fun profilePictureURL(username: String, callback: (Uri?) -> Unit){
        val ref = storage.reference
        ref.child("${username}/profile_picture.png").downloadUrl
            .addOnSuccessListener {
                callback(it)
            }
            .addOnFailureListener {
                callback(null)
            }
        }

    public fun uploadPost(imageUri: Uri, username: String, id: String, callback: (String) -> Unit){
        val storageRef = storage.reference
        val ref = storageRef.child("${username}/posts/${id}.png")
        var uploadTask = ref.putFile(imageUri)

        val urlTask = uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            ref.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                val myUri = downloadUri.toString()
                Log.d("storagedebug", "${myUri}")
                callback(myUri)
            } else {
            }
        }
    }

    public fun uploadPostFromBytes(bytesData: ByteArray, username: String, id: String, callback: (String) -> Unit){
        val storageRef = storage.reference
        val ref = storageRef.child("${username}/posts/${id}.png")
        //var uploadTask = ref.putFile(imageUri)
        var uploadTask = ref.putBytes(bytesData)

        val urlTask = uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            ref.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                val myUri = downloadUri.toString()
                Log.d("storagedebug", "${myUri}")
                callback(myUri)
            } else {
            }
        }
    }

    public fun deletePost(username: String, id: String, callback: (Boolean) -> Unit){
        val storageRef = storage.reference
        val desertRef = storageRef.child("${username}/posts/${id}.png")

        // Delete the file
        desertRef.delete().addOnSuccessListener {
            // File deleted successfully
            callback(true)
        }.addOnFailureListener {
            // Uh-oh, an error occurred!
            callback(false)
        }
    }

}
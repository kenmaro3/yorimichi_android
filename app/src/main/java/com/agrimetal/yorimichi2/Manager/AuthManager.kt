package com.agrimetal.yorimichi2.Manager

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class AuthManager {
    val TAG = "AuthManager"
    constructor()

//    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var auth: FirebaseAuth

    public fun signIn(email: String, password: String, callback: (Boolean) -> Unit){
        auth = Firebase.auth
        Log.d("auth debug", email)
        Log.d("auth debug", password)
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener{ task ->
                if (task.isSuccessful){
                    Log.d("mydebug", "signin completion handler success")

                    callback(true)
//                    val dbManager = DatabaseManager()
//
//                    val spManager = SharedPreferencesManager()
                }else{
                    Log.d("mydebug", "signin completion handler false")
                    callback(false)
                }
            }
    }

    public fun signOut(callback: (Boolean) -> Unit){
        auth = Firebase.auth
        try{
            auth.signOut()
            callback(true)
        }catch(e: Exception){
            callback(false)
        }
    }


}
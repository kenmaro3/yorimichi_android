package com.agrimetal.yorimichi2.Manager
import androidx.appcompat.app.AppCompatActivity
import android.content.Context

class SharedPreferencesManager : AppCompatActivity{
    constructor()

    val SHARED_PREF = "sharedPreferences"
    val dataStore = getSharedPreferences(SHARED_PREF, Context.MODE_PRIVATE)

    public fun setString(key: String, value: String){
        val editor = dataStore.edit()
        editor.putString(key, value)
        editor.apply()
    }

    public fun getString(key: String) : String? {
        return dataStore.getString(key, "")
    }
}
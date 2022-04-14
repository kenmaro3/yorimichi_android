package com.agrimetal.yorimichi2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment


class MainActivity : AppCompatActivity() {



    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        val dataStore = getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        var currentUsername = dataStore.getString("username", "")
        if (currentUsername == ""){
            Log.e("mydebug", "username at shared preference is empty at MainActivity")
            //currentUsername = "kenmaro"
        }
        else if (currentUsername == null){
            Log.e("mydebug", "username at shared preference is null at MainActivity")

        }
        when (item.itemId) {
            R.id.nav_home -> {
                startActivity(Intent(this@MainActivity, HomeActivity::class.java))
                return@OnNavigationItemSelectedListener true
            }
            R.id.nav_explore -> {
                startActivity(Intent(this@MainActivity, SearchActivity::class.java))
                return@OnNavigationItemSelectedListener true

            }
            R.id.nav_map-> {
                startActivity(Intent(this@MainActivity, MapActivity::class.java))
                return@OnNavigationItemSelectedListener true
            }
            R.id.nav_add_post -> {
                item.isChecked = false
                startActivity(Intent(this@MainActivity, AddPostDetailActivity::class.java))
                return@OnNavigationItemSelectedListener true
            }

            R.id.nav_profile -> {
                val intent = Intent(this@MainActivity, ProfileActivity::class.java)
                intent.putExtra("username", currentUsername)
                startActivity(intent)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        startActivity(Intent(this@MainActivity, HomeActivity::class.java))
    }

    private fun moveToFragment(fragment: Fragment){
        val fragmentTrans = supportFragmentManager.beginTransaction()
        fragmentTrans.replace(R.id.fragment_container, fragment)
        fragmentTrans.commit()

    }
}
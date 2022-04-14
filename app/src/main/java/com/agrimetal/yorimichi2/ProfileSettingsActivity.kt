package com.agrimetal.yorimichi2


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import android.view.View

import android.widget.ListView

import android.content.Intent
import com.agrimetal.yorimichi2.Adaptor.Header
import com.agrimetal.yorimichi2.Adaptor.SettingItemAdapter
import com.agrimetal.yorimichi2.Manager.AuthManager
import kotlinx.android.synthetic.main.activity_profile_settings.*



class ProfileSettingsActivity : AppCompatActivity() {
    private var mAdapter: SettingItemAdapter? = null
    private var listView: ListView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_settings)


        // Create the dataset for the Adapter
        val mList = ArrayList<Any>()
        mList.add(Header("アプリの評価、共有"))
        mList.add("アプリを評価する")
        mList.add("アプリを共有する")
        //mList.add(Footer("Footer 1"))
        mList.add(Header("ヨリミチアプリについて"))
        mList.add("サービス利用規約")
        mList.add("プライバシーポリシー")
        mList.add("ヘルプはこちら")
        //mList.add(Footer("Footer 2"))

        // Create Adapter
        mAdapter = SettingItemAdapter(this, mList)
        listView = findViewById<View>(R.id.list_profile_setting) as ListView
        listView!!.setAdapter(mAdapter)


        logout_button_profile_settings.setOnClickListener{
            didTapLogout()
        }
    }


    private fun didTapLogout(){
        val authManager = AuthManager()
        authManager.signOut { res ->
            if(res){
                val intent = Intent(this@ProfileSettingsActivity, SignInActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
            else{
                val intent = Intent(this@ProfileSettingsActivity, SignInActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()
            }
        }
    }
}
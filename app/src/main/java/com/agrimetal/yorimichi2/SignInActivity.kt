package com.agrimetal.yorimichi2

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.agrimetal.yorimichi2.Manager.AuthManager
import com.agrimetal.yorimichi2.Manager.DatabaseManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_sign_in.*
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignInActivity : AppCompatActivity() {
    val TAG = "SignInActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        signup_link_btn.setOnClickListener{
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        login_btn.setOnClickListener{
            loginUser()
        }
    }

    private fun loginUser(){
        val email = email_login.text.toString()
        val password = password_login.text.toString()

        when{
            TextUtils.isEmpty(email) -> Toast.makeText(this, "Email is required.", Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(password) -> Toast.makeText(this, "Password is required.", Toast.LENGTH_LONG).show()
            else -> {
                val progressDialog = ProgressDialog(this@SignInActivity)
                progressDialog.setTitle("サインイン")
                progressDialog.setMessage("少々お待ちください..")
                progressDialog.setCanceledOnTouchOutside(false)
                progressDialog.show()

                val authManager = AuthManager()
                authManager.signIn(email=email, password=password, callback = { res ->
                    if(res){
                        Log.d("signindebug", "fireauth signIn true")
                        val dbManager = DatabaseManager()
                        dbManager.findUser(email=email, callback={ user ->
                            user?.run {
                                Log.d("signindebug", "here is user ${user}")
//                                val spManager = SharedPreferencesManager()
                                val dataStore = getSharedPreferences("PREFS", Context.MODE_PRIVATE)

                                val editor = dataStore.edit()
                                editor.putString("username", user.username)
                                editor.putString("email", user.email)
                                editor.apply()

                                val intent = Intent(this@SignInActivity, MainActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                                startActivity(intent)

                                finish()

                            } ?: run {
                                Log.d("signindebug", "got it but user is null")
                                Toast.makeText(this, "Error: サインインに失敗しました。メールアドレスとパスワードをご確認ください。", Toast.LENGTH_LONG).show()
                                FirebaseAuth.getInstance().signOut()
                                progressDialog.dismiss()
                                // nullの場合の処理
                            }

                        })

                    }
                    else{
                        Log.d("signindebug", "sign in failed by authmanager")
                        Toast.makeText(this, "Error: サインインに失敗しました。メールアドレスとパスワードをご確認ください。", Toast.LENGTH_LONG).show()
                        FirebaseAuth.getInstance().signOut()
                        progressDialog.dismiss()
                    }

                })

            }

        }
    }

    override fun onStart() {
        super.onStart()
        if(FirebaseAuth.getInstance().currentUser != null){
            val intent = Intent(this@SignInActivity, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }
    }
}
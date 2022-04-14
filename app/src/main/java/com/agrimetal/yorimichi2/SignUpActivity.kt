package com.agrimetal.yorimichi2

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.agrimetal.yorimichi2.Manager.DatabaseManager
import com.agrimetal.yorimichi2.Model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_sign_in.*
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUpActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        signup_btn.setOnClickListener{
            CreateAccount()
        }

        signin_link_btn.setOnClickListener{
            startActivity(Intent(this, SignInActivity::class.java))
        }
    }

    private fun CreateAccount(){
        val userName = username_signup.text.toString()
        val email = email_signup.text.toString()
        val password = password_signup.text.toString()

        when{
            TextUtils.isEmpty(userName) -> Toast.makeText(this, "ユーザーネームは必須項目です。", Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(email) -> Toast.makeText(this, "メールアドレスは必須項目です。", Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(password) -> Toast.makeText(this, "パスワードは必須項目です。", Toast.LENGTH_LONG).show()

            else -> {
                val progressDialog = ProgressDialog(this@SignUpActivity)
                progressDialog.setTitle("アカウントの作成")
                progressDialog.setMessage("アカウントを作成しています...")
                progressDialog.setCanceledOnTouchOutside(false)
                progressDialog.show()

                val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
                mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener{task ->
                    if (task.isSuccessful){
                        saveUserInfo(userName, email, progressDialog)

                    }else{
                        val message = task.exception.toString()
                        Toast.makeText(this, "エラー: アカウントの作成に失敗しました。メールアドレスが既に登録されていないかご確認ください。", Toast.LENGTH_LONG).show()
                        mAuth.signOut()
                        progressDialog.dismiss()
                    }
                }
            }
        }
    }

    private fun saveUserInfo(userName: String, email: String, progressDialog: ProgressDialog){
        val newUser: User = User(
            username = userName.lowercase(),
            email = email,
        )

        val databaseManager = DatabaseManager()
        databaseManager.createUser(user=newUser, callback = { res ->
           if (res) {
               progressDialog.dismiss()
               Toast.makeText(
                   this,
                   "アカウントの作成が完了しました。",
                   Toast.LENGTH_LONG
               ).show()

               val intent = Intent(this@SignUpActivity, MainActivity::class.java)
               intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
               startActivity(intent)
               val dataStore = getSharedPreferences("PREFS", Context.MODE_PRIVATE)

               val editor = dataStore.edit()
               editor.putString("username", newUser.username)
               editor.putString("email", newUser.email)
               editor.apply()
               finish()
           }else{
               Toast.makeText(this, "エラー: アカウントの作成に失敗しました。", Toast.LENGTH_LONG).show()
               FirebaseAuth.getInstance().signOut()
               progressDialog.dismiss()
           }
        })



    }
}





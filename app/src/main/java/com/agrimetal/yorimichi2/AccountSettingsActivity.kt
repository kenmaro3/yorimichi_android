package com.agrimetal.yorimichi2

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.agrimetal.yorimichi2.Manager.DatabaseManager
import com.agrimetal.yorimichi2.Model.UserInfo
import com.google.firebase.auth.FirebaseAuth
import com.canhub.cropper.options
import com.canhub.cropper.CropImageContract
import kotlinx.android.synthetic.main.activity_account_settings.*
import kotlinx.android.synthetic.main.activity_sign_in.*
import android.widget.Toast
import com.agrimetal.yorimichi2.Manager.PicassoManager
import com.canhub.cropper.CropImageView
import com.agrimetal.yorimichi2.Manager.StorageManager
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class AccountSettingsActivity : AppCompatActivity() {
    private lateinit var firebaseUser: FirebaseUser
    private var checker = ""
    private var myUrl = ""
    private var imageUri: Uri? = null
    private var storageProfilePicRef: StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_settings)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        storageProfilePicRef = FirebaseStorage.getInstance().reference.child("profile_pictures")


        fetchInfo()

        change_image_text_btn.setOnClickListener {
            checker = "clicked"
            startCrop()
        }

        save_info_profile_btn.setOnClickListener{
            if (checker == "clicked"){
               didTapUpdateWithProfileImage()
            }else{
                didTapUpdate()
            }
        }



        val dataStore = getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        val currentUsername = dataStore.getString("username", "")
        if (currentUsername == ""){
            return
        }
        currentUsername ?: return // like guard let

        val profile_image_view_profile_setting: CircleImageView = findViewById(R.id.profile_image_view_profile_setting)

        val storageManager = StorageManager()
        storageManager.profilePictureURL(username = currentUsername, callback = {
//            val picassoManager = PicassoManager()
//            picassoManager.picasso.load(it).fit().centerCrop().into(profile_image_view_profile_setting)
            Glide.with(this).asBitmap().load(it).into(profile_image_view_profile_setting)
        })

    }

    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            val uriContent = result.uriContent
        } else {
            val exception = result.error
        }
    }

    private fun startCrop() {
        cropImage.launch(
            options {
                setImageSource(
                    includeGallery = true,
                    includeCamera = true,
                )
                // Normal Settings
                setScaleType(CropImageView.ScaleType.FIT_CENTER)
                setCropShape(CropImageView.CropShape.RECTANGLE)
                setGuidelines(CropImageView.Guidelines.ON_TOUCH)
                setAspectRatio(1, 1)
            }
        )

    }


    private fun updateUserInfoOnly(){
        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Account Settings")
        progressDialog.setMessage("Please wait, profile is being updated...")
        progressDialog.show()
        when {
            //TextUtils.isEmpty(display_name_profile_frag.text.toString()) -> Toast.makeText(this, "Please write display name.", Toast.LENGTH_LONG).show()
            bio_profile_frag.text.toString() == "" -> Toast.makeText(
                this,
                "Please write bio.",
                Toast.LENGTH_LONG
            ).show()

            else -> {
            }
        }

    }

    private fun uploadImageAndUpdateInfo() {

        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Account Settings")
        progressDialog.setMessage("Please wait, profile is being updated...")
        progressDialog.show()
        when {
            //TextUtils.isEmpty(display_name_profile_frag.text.toString()) -> Toast.makeText(this, "Please write display name.", Toast.LENGTH_LONG).show()
            bio_profile_frag.text.toString() == "" -> Toast.makeText(
                this,
                "Please write bio.",
                Toast.LENGTH_LONG
            ).show()

            imageUri == null -> Toast.makeText(this, "Please select image", Toast.LENGTH_LONG)
                .show()

            else -> {
                val fileRef = storageProfilePicRef!!.child(firebaseUser!!.uid + ".jpg")

                var uploadTask: StorageTask<*>
                uploadTask = fileRef.putFile(imageUri!!)
                uploadTask.continueWithTask(com.google.android.gms.tasks.Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                    if (!task.isSuccessful) {
                        task?.exception?.let {
                            throw it
                            progressDialog.dismiss()
                        }

                    }

                    return@Continuation fileRef.downloadUrl

                }).addOnCompleteListener{ OnCompleteListener<Uri> { task ->
                    if(task.isSuccessful){
                        val downloadUril = task.result
                        myUrl = downloadUril.toString()

                        val ref = FirebaseDatabase.getInstance().reference.child("Users")

                        Toast.makeText(this, "Account Information has been updated successfully.", Toast.LENGTH_LONG).show()
                        val intent = Intent(this@AccountSettingsActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()

                    } else {
                        progressDialog.dismiss()
                    }
                }
                }
            }
        }
    }

    private fun didTapUpdate(){
        val progressDialog = ProgressDialog(this@AccountSettingsActivity)
        progressDialog.setTitle("Account Setting")
        progressDialog.setMessage("Updating...")
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.show()
        val bio = bio_profile_frag.text.toString()
        val twitterId = twitter_profile_frag.text.toString()
        val instagramId = instagram_profile_frag.text.toString()


        val newUserInfo = UserInfo(bio=bio, twitterId=twitterId, instagramId=instagramId)
        val dbManager = DatabaseManager()
        dbManager.setUserInfo(context=this, userInfo=newUserInfo, callback = { res ->
            if(res){
                progressDialog.dismiss()
                finish()
            }
            else{
                Log.e("mydebug", "cannot set userInfo")
                progressDialog.dismiss()
                finish()
            }
        })
    }

    private fun didTapUpdateWithProfileImage(){
        val progressDialog = ProgressDialog(this@AccountSettingsActivity)
        progressDialog.setTitle("Account Setting")
        progressDialog.setMessage("Updating...")
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.show()
        val bio = bio_profile_frag.text.toString()


        val newUserInfo = UserInfo(bio=bio)
        val dbManager = DatabaseManager()
        dbManager.setUserInfo(context=this, userInfo=newUserInfo, callback = { res ->
            if(res){
                progressDialog.dismiss()
                finish()
            }
            else{
                Log.e("mydebug", "cannot set userInfo")
                progressDialog.dismiss()
                finish()
            }
        })

        when{
            imageUri == null -> Toast.makeText(this, "Please select image.", Toast.LENGTH_LONG).show()
            else -> {
                val riversRef = storageProfilePicRef?.child("profile_picture.png")
                if (riversRef != null) {
                    var uploadTask = riversRef.putFile(imageUri!!)

                    uploadTask.addOnFailureListener {
                        // Handle unsuccessful uploads
                    }.addOnSuccessListener { taskSnapshot ->
                    }
                }
            }
        }

    }

    private fun fetchInfo(){
        val dataStore = getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        val currentUsername = dataStore.getString("username", "")
        if (currentUsername == ""){
            return
        }
        currentUsername ?: return // like guard let
        val dbManager = DatabaseManager()
        dbManager.getUserInfo(username=currentUsername, callback = { userInfo ->
            bio_profile_frag.setText(userInfo?.getBio())
            twitter_profile_frag.setText(userInfo?.getTwitterId())
            instagram_profile_frag.setText(userInfo?.getInstagramId())

        })
    }
}
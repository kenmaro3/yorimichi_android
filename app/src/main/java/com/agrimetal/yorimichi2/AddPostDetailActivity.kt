package com.agrimetal.yorimichi2

import android.app.ProgressDialog
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageView
import com.canhub.cropper.options
import com.agrimetal.yorimichi2.Manager.StorageManager
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_add_post_detail.*

import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener

import com.google.android.libraries.places.api.net.PlacesClient

import com.google.android.gms.common.api.Status
import java.util.*

import java.time.LocalDateTime
import java.text.SimpleDateFormat
import java.time.ZoneId

import 	android.content.ContentResolver
import android.graphics.ImageDecoder
import java.io.ByteArrayOutputStream

import android.provider.MediaStore;
import com.agrimetal.yorimichi2.Manager.DatabaseManager
import com.agrimetal.yorimichi2.Model.Location
import com.agrimetal.yorimichi2.Model.Post

class AddPostDetailActivity : AppCompatActivity() {
    private var myUri = ""
    private var imageUri: Uri? = null
    private var storageProfilePicRef: StorageReference? = null


    private var placesClient: PlacesClient? = null

    private lateinit var mPlace: Place
    private var croppedUri: Uri? = null

    private var caption: String? = null
    private var locationTitle: String? = null
    private var locationSubTitle: String? = null
    private var location: Location? = null
    private var dateString: String? = null


    private fun Uri.getBitmapOrNull(contentResolver: ContentResolver): Bitmap? {
            val source = ImageDecoder.createSource(contentResolver, this)
            return ImageDecoder.decodeBitmap(source)
    }

    fun getImageUriFromBitmap(context: Context, bitmap: Bitmap): Uri?{
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(context.contentResolver, bitmap, "Title", null)
        if(path != null){
            return Uri.parse(path.toString())
        }
        else{
            return null
        }
    }



    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        Log.d("cameradebug", "true0")
        if (result.isSuccessful) {
            Log.d("cameradebug", "true")
            imageView2.setImageURI(result.uriContent)
            croppedUri = result.uriContent
        } else {
            val exception = result.error
            Log.d("cameradebug", "false")
        }
    }

    private fun dateToString(date: Date): String{
        val df = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.ENGLISH)
        val message = df.format(date)
        return message
    }

    private fun startCrop() {
        cropImage.launch(
            options(uri = imageUri) {
                setOutputCompressFormat(Bitmap.CompressFormat.PNG)
                setGuidelines(CropImageView.Guidelines.ON)
                setImageSource(
                    includeGallery = true,
                    includeCamera = true,
                )
                setScaleType(CropImageView.ScaleType.FIT_CENTER)
                setCropShape(CropImageView.CropShape.RECTANGLE)
                setGuidelines(CropImageView.Guidelines.ON_TOUCH)
                setAspectRatio(1, 1)
            }
        )
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_post_detail)

        imageView2.setOnClickListener{
            startCrop()
        }

        setUpSearchLocation()
        setUpPostButton()




    }

    private fun setUpSearchLocation(){
        val apiKey = getString(R.string.google_maps_key)
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, apiKey)
        }

        // Create a new Places client instance.
        placesClient = Places.createClient(this)

        val autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.place_autocomplete_fragment) as AutocompleteSupportFragment?
        autocompleteFragment!!.setPlaceFields(
            listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.LAT_LNG
            )
        )

        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                val latlong = "${place.latLng?.latitude!!}::${place.latLng?.longitude!!}"

                add_post_detail_location_title.text = place.name.toString()
                add_post_detail_address.text = place.address.toString()

                locationTitle = place.name.toString()
                locationSubTitle = place.name
                location = Location(place.latLng?.latitude!!, place.latLng?.longitude!!)
                mPlace = place

            }

            override fun onError(status: Status) {
                Toast.makeText(applicationContext, status.toString(), Toast.LENGTH_SHORT).show()
            }
        })


    }
    private fun getDateString(): String{
        val ldt: LocalDateTime = LocalDateTime.now()
        val date: Date = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
        val dateString: String = dateToString(date)
        return dateString
    }

    private fun getNewPostID(): String{
        val dataStore = getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        val currentUsername = dataStore?.getString("username", "")
        if (currentUsername == "") {
            Log.e("mydebug", "currentUsername is empty, something is wrong")
        }

        val timeInt = System.currentTimeMillis()/1000
        val randInt = (0..1000).random()

        return "${currentUsername}_${randInt}_${timeInt}"
    }

    private fun setUpPostButton(){
        add_post_detail_post.setOnClickListener{
            uploadImage()

        }
    }


    private fun uploadImage(){
        val dataStore = getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        val currentUsername = dataStore?.getString("username", "")
        if (currentUsername == "") {
            Log.e("mydebug", "currentUsername is empty, something is wrong")
        }

        caption = add_post_detail_caption.text.toString()

        if(croppedUri as Uri == null){
            Toast.makeText(this, "画像が入力されていません。", Toast.LENGTH_SHORT).show()
            return
        }
        if(currentUsername == null){
            Toast.makeText(this, "ユーザーネームが無効です。", Toast.LENGTH_SHORT).show()
            return
        }
        if(caption == null){
            Toast.makeText(this, "キャプションが入力されていません。", Toast.LENGTH_SHORT).show()
            return
        }
        if(locationTitle == null){
            Toast.makeText(this, "場所が入力されていません。", Toast.LENGTH_SHORT).show()
            return
        }

        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("投稿しています。")
        progressDialog.setMessage("アップロードを行っています。お待ちください。")
        progressDialog.show()

        val dateString: String = getDateString()
        val newPostID: String = getNewPostID()




        Log.d("postdebug", "${croppedUri}")
        //val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(c.getContentResolver(), Uri.parse(croppedUri))
        val bitmap: Bitmap? = (croppedUri as Uri).getBitmapOrNull(this.contentResolver)

        bitmap?.let{
            Log.d("postdebug", "bitmap")
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 640, 640, false)
            //val resizedUri = getImageUriFromBitmap(this, resizedBitmap)

            //Log.d("postdebug", "${resizedUri}")

            val storageManager = StorageManager()
            //val data = resizedBitmap.toByteArray()
            val stream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val data = stream.toByteArray()
            storageManager.uploadPostFromBytes(data, currentUsername, newPostID, callback = { downloadUrlString ->
                Log.d("postdebug", downloadUrlString)

                val databaseManager = DatabaseManager()
                databaseManager.findUserByName(currentUsername, callback = { user ->
                    val newPost: Post = Post(
                        id=newPostID,
                        caption=caption!!,
                        locationTitle=locationTitle!!,
                        locationSubTitle=locationSubTitle!!,
                        location=location!!,
                        postedDate=dateString,
                        likers=arrayListOf<String>(),
                        yorimichi = arrayListOf<String>(),
                        postUrlString = downloadUrlString,
                        postThumbnailUrlString = "",
                        genre= hashMapOf<String, Any>("code" to "G000", "type" to hashMapOf<String, Any>("food" to hashMapOf<String, Any>())),
                        user=user!!,
                        isVideo = false
                    )

                    databaseManager.createPost(post=newPost, username=currentUsername, callback = { res ->
                        if(res){
                            Toast.makeText(this, "投稿を完了しました。", Toast.LENGTH_LONG).show()
                        }
                        else{
                            Toast.makeText(this, "投稿に失敗しました。", Toast.LENGTH_LONG).show()
                        }

                        databaseManager.createYorimichiPostAtAll(post=newPost, username=currentUsername, callback = {
                            progressDialog.dismiss()
                            finish()

                        })

                    }
                    )

                })
            })


        }



    }
}
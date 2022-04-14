package com.agrimetal.yorimichi2

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.agrimetal.yorimichi2.Adapter.UserAdapter
import com.agrimetal.yorimichi2.Adaptor.MyImagesAdapter
import com.agrimetal.yorimichi2.Manager.DatabaseManager
import com.agrimetal.yorimichi2.Manager.PicassoManager
import com.agrimetal.yorimichi2.Manager.StorageManager
import com.agrimetal.yorimichi2.Model.Post
import com.agrimetal.yorimichi2.Model.User
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*
import kotlin.collections.ArrayList

class ProfileActivity : AppCompatActivity() {

    private var username: String? = null

    var postList: List<Post>? = null
    var myImagesAdapter: MyImagesAdapter? = null

    var postListLiked: List<Post>? = null
    var myImagesAdapterLiked: MyImagesAdapter? = null

    private var recyclerView: RecyclerView? = null
    private var userAdapter: UserAdapter? = null
    private var mUser: MutableList<User>? = null

    private lateinit var recyclerViewUploaded: RecyclerView
    private lateinit var recyclerViewLiked: RecyclerView


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        username = intent.getStringExtra("username")

        Log.d("mydebug111", "${username}")


        fetchUserCount()
        fetchUserInfo()
        var edit_account_settings_btn: Button = findViewById(R.id.edit_account_settings_btn)
        edit_account_settings_btn.setOnClickListener {
            startActivity(Intent(this, AccountSettingsActivity::class.java))
        }


        recyclerView = findViewById(R.id.recycler_view_search)
        recyclerView?.setHasFixedSize(true)
        recyclerView?.layoutManager = LinearLayoutManager(this)
        mUser = ArrayList()
        userAdapter = UserAdapter(this, mUser as ArrayList<User>, true)
        recyclerView?.adapter = userAdapter

        var profileHeader = findViewById<LinearLayout>(R.id.header_profile_frag)
        var profileMidBar = findViewById<LinearLayout>(R.id.mid_bar)
        var profileSegmented = findViewById<LinearLayout>(R.id.segmented_control_profile_frag)
        var profileUploaded = findViewById<RecyclerView>(R.id.recycler_view_uploaded_grid)
        var profileLiked = findViewById<RecyclerView>(R.id.recycler_view_liked_grid)

        var retrieveUploded: Boolean = false
        var retrieveLiked: Boolean = false

        var search_edit_text_profile_frag =
            findViewById<EditText>(R.id.search_edit_text_profile_frag)

        search_edit_text_profile_frag.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (search_edit_text_profile_frag.text.toString() == "") {
                    recyclerView?.visibility = View.INVISIBLE
                    profileHeader.visibility = View.VISIBLE
                    profileMidBar.visibility = View.VISIBLE
                    profileSegmented.visibility = View.VISIBLE

                    if (retrieveUploded) {
                        profileUploaded.visibility = View.VISIBLE
                        retrieveUploded = false
                    } else if (retrieveLiked) {
                        profileLiked.visibility = View.VISIBLE
                        retrieveLiked = false
                    }

                } else {
                    recyclerView?.visibility = View.VISIBLE
                    profileHeader.visibility = View.INVISIBLE
                    profileMidBar.visibility = View.INVISIBLE
                    profileSegmented.visibility = View.INVISIBLE

                    if (profileUploaded.visibility == View.VISIBLE) {
                        retrieveUploded = true
                        profileUploaded.visibility = View.INVISIBLE
                    } else if (profileLiked.visibility == View.VISIBLE) {
                        retrieveLiked = true
                        profileLiked.visibility = View.INVISIBLE
                    }
                    retrieveUsers()
                }
            }
        })


        // Recycler View for Uploaded Images
        recyclerViewUploaded = findViewById(R.id.recycler_view_uploaded_grid)
        recyclerViewUploaded.setHasFixedSize(true)
        val linearLayoutManager: LinearLayoutManager = GridLayoutManager(this, 3)
        recyclerViewUploaded.layoutManager = linearLayoutManager

        postList = ArrayList()
        myImagesAdapter = MyImagesAdapter(this, postList as ArrayList<Post>)
        recyclerViewUploaded.adapter = myImagesAdapter

        // Recycler View for Liked Images
        recyclerViewLiked = findViewById(R.id.recycler_view_liked_grid)
        recyclerViewLiked.setHasFixedSize(true)
        val linearLayoutManager2: LinearLayoutManager = GridLayoutManager(this, 3)
        recyclerViewLiked.layoutManager = linearLayoutManager2





        setUpView()
        fetchPosts()
        fetchLikedPosts()
        configureTab(recyclerViewLiked, recyclerViewUploaded)


        var settings_button: ImageView
        settings_button = findViewById(R.id.settings_profile_frag)
        settings_button.setOnClickListener {
            val intent = Intent(this, ProfileSettingsActivity::class.java)
            startActivity(intent)
        }

        var twitterLinkButton: ImageButton
        twitterLinkButton = findViewById(R.id.link_to_twitter_profile_frag)
        twitterLinkButton.setOnClickListener {
            val uri = Uri.parse("https://www.twitter.com/");
            val i = Intent(Intent.ACTION_VIEW, uri);
            startActivity(i);

        }

        var instagramLinkButton: ImageButton
        instagramLinkButton = findViewById(R.id.link_to_instagram_profile_frag)
        instagramLinkButton.setOnClickListener {
            val uri = Uri.parse("https://www.instagram.com/");
            val i = Intent(Intent.ACTION_VIEW, uri);
            startActivity(i);
        }


        var notificationButton: ImageView
        notificationButton = findViewById(R.id.notification_profile_frag)
        notificationButton.setOnClickListener {
//            val fragmentTrans = supportFragmentManager?.beginTransaction()
//            fragmentTrans?.replace(R.id.fragment_container, NotificationsFragment())
//            fragmentTrans?.commit()


        }
    }


    private fun retrieveUsers() {
        val db = FirebaseFirestore.getInstance().collection("users")

        var search_edit_text_profile_frag: EditText =
            findViewById(R.id.search_edit_text_profile_frag)

        mUser?.clear()
        db.get()
            .addOnSuccessListener {
                val users = it.toObjects(User::class.java)
                for (user in users) {
                    if (user.username.startsWith(
                            search_edit_text_profile_frag!!.text.toString().lowercase()
                        )
                    ) {
                        mUser?.add(user)
                        userAdapter?.notifyDataSetChanged();
                    }
                }

            }
            .addOnFailureListener { exception ->
            }


    }


    private fun configureTab(recyclerViewLiked: RecyclerView, recyclerViewUploaded: RecyclerView) {
        recyclerViewUploaded.visibility = View.VISIBLE
        recyclerViewLiked.visibility = View.INVISIBLE

        var uploadedImageButton: Button
        uploadedImageButton = findViewById(R.id.post_image_btn_profile_frag)
        uploadedImageButton.setOnClickListener {
            recyclerViewUploaded.visibility = View.VISIBLE
            recyclerViewLiked.visibility = View.INVISIBLE
        }

        var likedImageButton: Button
        likedImageButton = findViewById(R.id.liked_image_btn_profile_frag)
        likedImageButton.setOnClickListener {
            recyclerViewLiked.visibility = View.VISIBLE
            recyclerViewUploaded.visibility = View.INVISIBLE
        }

    }

    private fun fetchPosts() {
        val dbManager = DatabaseManager()
        username ?: return
        postList = ArrayList()
        dbManager.posts(username = username!!, callback = { posts ->
            Collections.reverse(posts)
            postList = posts
            if (posts.count() != 0) {
                myImagesAdapter = MyImagesAdapter(this, postList as ArrayList<Post>)
            } else {
                myImagesAdapter = MyImagesAdapter(this, listOf())
            }

            Log.d("mydebug", "here is the size: ${postList}")
            Log.d("countdebug", "posted: ${postList!!.count()}")
            //myImagesAdapter!!.notifyDataSetChanged()
            recyclerViewUploaded.adapter = myImagesAdapter
            myImagesAdapter?.notifyDataSetChanged()

        })
    }

    private fun fetchLikedPosts() {
        Log.d("countdebug", "like called")
        val dbManager = DatabaseManager()

        if(username != null){
            postListLiked = ArrayList()
            dbManager.postsLiked(username = username!!, callback = { posts ->
                Collections.reverse(posts)
                postListLiked = posts
                if (posts.count() != 0) {
                    myImagesAdapterLiked = MyImagesAdapter(this, postListLiked as ArrayList<Post>)
                } else {
                    myImagesAdapterLiked = MyImagesAdapter(this, listOf())
                }

                Log.d("mydebug", "here is the size of postListLiked: ${postListLiked}")
                Log.d("countdebug", "liked: ${postListLiked!!.count()}")
                recyclerViewLiked.adapter = myImagesAdapterLiked
                myImagesAdapterLiked?.notifyDataSetChanged()

            })
        }
        else{
            val dataStore = getSharedPreferences("PREFS", Context.MODE_PRIVATE)
            val currentUsername = dataStore?.getString("username", "")
            if (currentUsername == "") {
                Log.e("mydebug", "currentUsername is empty, something is wrong")
            }

            postListLiked = ArrayList()
            dbManager.postsLiked(username = currentUsername!!, callback = { posts ->
                Collections.reverse(posts)
                postListLiked = posts
                if (posts.count() != 0) {
                    myImagesAdapterLiked = MyImagesAdapter(this, postListLiked as ArrayList<Post>)
                } else {
                    myImagesAdapterLiked = MyImagesAdapter(this, listOf())
                }

                Log.d("mydebug", "here is the size of postListLiked: ${postListLiked}")
                Log.d("countdebug", "liked: ${postListLiked!!.count()}")
                recyclerViewLiked.adapter = myImagesAdapterLiked
                myImagesAdapterLiked?.notifyDataSetChanged()

            })

        }
    }


    private fun setUpView() {
        val dataStore = getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        val currentUsername = dataStore?.getString("username", "")
        if (currentUsername == "") {
            Log.e("mydebug", "currentUsername is empty, something is wrong")
        }

        if(username != null){
            var profile_fragment_username: TextView = findViewById(R.id.profile_fragment_username)
            profile_fragment_username.setText(username)

            currentUsername ?: return

            val storageManager = StorageManager()
            var pro_image_profile_frag: CircleImageView = findViewById(R.id.pro_image_profile_frag)
            storageManager.profilePictureURL(username=username!!, callback={ url ->
                Log.d("profiledebug", "${url}")
                //PicassoManager().picasso.load(url).fit().centerCrop().into(pro_image_profile_frag)
                Glide.with(this).asBitmap().load(url).into(pro_image_profile_frag)

            })


            var edit_account_settings_btn: Button = findViewById(R.id.edit_account_settings_btn)
            if (currentUsername == username) {
                edit_account_settings_btn.setText("プロフィールを編集する")
                edit_account_settings_btn.setOnClickListener {
                    startActivity(Intent(this, AccountSettingsActivity::class.java))
                }
            } else {

                val dbManager = DatabaseManager()
                username?.let { username ->
                    dbManager.isFollowing(
                        context = this,
                        targetUsername = username,
                        callback = { res ->
                            if (res) {
                                edit_account_settings_btn.setText("フォロー解除")
                                edit_account_settings_btn.setOnClickListener {
                                    val dbManager = DatabaseManager()
                                    Log.d("mydebug", "unfollow button pressed")
                                    dbManager.updateRelationship(
                                        this,
                                        state = DatabaseManager.RelationshipState.UNFOLLOW,
                                        targetUsername = username,
                                        callback = { res ->
                                            Log.d("mydebug", "follow: ${res}")
                                        })
                                    edit_account_settings_btn.setText("フォロー")

                                }
                            } else {
                                edit_account_settings_btn.setText("フォロー")
                                edit_account_settings_btn.setOnClickListener {
                                    val dbManager = DatabaseManager()
                                    Log.d("mydebug", "follow button pressed")
                                    dbManager.updateRelationship(
                                        this,
                                        state = DatabaseManager.RelationshipState.FOLLOW,
                                        targetUsername = username,
                                        callback = { res ->
                                            Log.d("mydebug", "follow: ${res}")
                                        })
                                    edit_account_settings_btn.setText("フォロー解除")

                                }
                            }

                        })

                }
            }

        }
        else{
            currentUsername ?: return
            var profile_fragment_username: TextView = findViewById(R.id.profile_fragment_username)
            profile_fragment_username.setText(currentUsername)


            val storageManager = StorageManager()
            var pro_image_profile_frag: CircleImageView = findViewById(R.id.pro_image_profile_frag)
            storageManager.profilePictureURL(username=currentUsername, callback={ url ->
                Log.d("profiledebug", "${url}")
                //PicassoManager().picasso.load(url).fit().centerCrop().into(pro_image_profile_frag)
                Glide.with(this).asBitmap().load(url).into(pro_image_profile_frag)

            })


            var edit_account_settings_btn: Button = findViewById(R.id.edit_account_settings_btn)
            edit_account_settings_btn.setText("プロフィールを編集する")
            edit_account_settings_btn.setOnClickListener {
                startActivity(Intent(this, AccountSettingsActivity::class.java))
            }
        }


    }


    private fun fetchUserCount() {
        val dbManager = DatabaseManager()
        var total_posts: TextView = findViewById(R.id.total_posts)
        var total_followers: TextView = findViewById(R.id.total_followers)
        var total_following: TextView = findViewById(R.id.total_following)

        if(username != null){
            dbManager.getUserCounts(this, username = username!!, callback = { counts ->
                Log.d("mydebug", "counted")
                Log.d("mydebug", "${counts}")
                total_posts.text = "${counts.first}"
                total_followers.text = "${counts.second}"
                total_following.text = "${counts.third}"


            })

        }
        else{
            val dataStore = getSharedPreferences("PREFS", Context.MODE_PRIVATE)
            val currentUsername = dataStore?.getString("username", "")
            if (currentUsername == "") {
                Log.e("mydebug", "currentUsername is empty, something is wrong")
            }
            dbManager.getUserCounts(this, username = currentUsername!!, callback = { counts ->
                Log.d("mydebug", "counted")
                Log.d("mydebug", "${counts}")
                total_posts.text = "${counts.first}"
                total_followers.text = "${counts.second}"
                total_following.text = "${counts.third}"


            })


        }
    }


    private fun fetchUserInfo() {

        val dbManager = DatabaseManager()
        var bio_profile_frag: TextView = findViewById(R.id.bio_profile_frag)
        if(username == null){
            val dataStore = getSharedPreferences("PREFS", Context.MODE_PRIVATE)
            val currentUsername = dataStore?.getString("username", "")
            if (currentUsername == "") {
                Log.e("mydebug", "currentUsername is empty, something is wrong")
            }
            dbManager.getUserInfo(username = currentUsername!!, callback = { userInfo ->
                //view.display_name_profile_frag.setText(userInfo?.getName())
                bio_profile_frag.setText(userInfo?.getBio())
            })
        }
        else{
            dbManager.getUserInfo(username = username!!, callback = { userInfo ->
                //view.display_name_profile_frag.setText(userInfo?.getName())
                bio_profile_frag.setText(userInfo?.getBio())
            })

        }
    }
}



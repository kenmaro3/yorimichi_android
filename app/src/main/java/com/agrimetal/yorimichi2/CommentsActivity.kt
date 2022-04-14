package com.agrimetal.yorimichi2

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.agrimetal.yorimichi2.Adaptor.CommentAdapter
import com.agrimetal.yorimichi2.Manager.DatabaseManager
import com.agrimetal.yorimichi2.Manager.PicassoManager
import com.agrimetal.yorimichi2.Manager.StorageManager
import com.agrimetal.yorimichi2.Model.PostComment
import com.agrimetal.yorimichi2.Model.PostCommentType
import com.bumptech.glide.Glide
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_account_settings.*
import kotlinx.android.synthetic.main.activity_comments.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class CommentsActivity : AppCompatActivity() {
    private var postId = ""
    private var postPublisher = ""

    private var commentAdapterMenu: CommentAdapter? = null
    private var commentAdapterPrice: CommentAdapter? = null
    private var commentAdapterTime: CommentAdapter? = null

    private var commentListMenu: MutableList<PostComment>? = null
    private var commentListPrice: MutableList<PostComment>? = null
    private var commentListTime: MutableList<PostComment>? = null

    private var segmentedType: HashMap<String, Any>? = null

    private lateinit var recyclerViewMenu: RecyclerView
    private lateinit var recyclerViewPrice: RecyclerView
    private lateinit var recyclerViewTime: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)

        val segmentedButtonMenu: Button = findViewById(R.id.comment_segmented_button_menu)
        val segmentedButtonPrice: Button = findViewById(R.id.comment_segmented_button_price)
        val segmentedButtonTime: Button = findViewById(R.id.comment_segmented_button_time)

        recyclerViewMenu = findViewById(R.id.recycler_view_comments_menu)
        recyclerViewPrice = findViewById(R.id.recycler_view_comments_price)
        recyclerViewTime = findViewById(R.id.recycler_view_comments_time)

        segmentedButtonMenu.setOnClickListener{
            recyclerViewMenu.visibility = View.VISIBLE
            recyclerViewPrice.visibility = View.INVISIBLE
            recyclerViewTime.visibility = View.INVISIBLE

            Log.d("commentdebugseg", "menu: ${commentListMenu}")

            segmentedType = hashMapOf("menu" to emptyMap<String, Any>())

        }
        segmentedButtonPrice.setOnClickListener{
            recyclerViewMenu.visibility = View.INVISIBLE
            recyclerViewPrice.visibility = View.VISIBLE
            recyclerViewTime.visibility = View.INVISIBLE
            Log.d("commentdebugseg", "price: ${commentListPrice}")
            segmentedType = hashMapOf("price" to emptyMap<String, Any>())

        }
        segmentedButtonTime.setOnClickListener{
            recyclerViewMenu.visibility = View.INVISIBLE
            recyclerViewPrice.visibility = View.INVISIBLE
            recyclerViewTime.visibility = View.VISIBLE
            Log.d("commentdebugseg", "time: ${commentListTime}")
            segmentedType = hashMapOf("time" to emptyMap<String, Any>())

        }


        val intent = intent
        postId = intent.getStringExtra("postId").toString()
        postPublisher = intent.getStringExtra("postPublisher").toString()

        setUpCommentsMenu()
        setUpCommentsPrice()
        setUpCommentsTime()

        Log.d("commentdebug", "${commentListMenu}")
        Log.d("commentdebug", "${commentListPrice}")
        Log.d("commentdebug", "${commentListTime}")


        configureImage()

//        dbManager.getUserInfo(username=currentUsername, callback = { userInfo ->
//

        val dataStore = getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        val currentUsername = dataStore.getString("username", "")
        if (currentUsername == ""){
            return
        }
        currentUsername ?: return // like guard let
        val storageManager = StorageManager()
        storageManager.profilePictureURL(username=currentUsername, callback = { uri ->
            //PicassoManager().picasso.load(uri).fit().centerCrop().into(profile_image_comment)
            Glide.with(this).asBitmap().load(uri).into(profile_image_comment)

        })
//        })


        post_comment_btn.setOnClickListener(View.OnClickListener {
            if(add_comment.text.toString() == ""){
                Toast.makeText(this@CommentsActivity, "Please write a comment first.", Toast.LENGTH_LONG).show()
            }
            else{
                addComment()

            }
        })
    }

    private fun addComment(){
        val comment = add_comment.text.toString()

        val dataStore = getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        val currentUsername = dataStore.getString("username", "")
        if (currentUsername == ""){
            return
        }
        currentUsername ?: return // like guard let

        val dbManager = DatabaseManager()

        dbManager.findUserByName(name=currentUsername, callback = { user ->
           user?.let{

               val newComment = PostComment(text=comment, user= user, date=Date().time/1000.0, type= segmentedType)
               dbManager.createComment(postId=postId, owner=postPublisher, comment=newComment, callback = {
                   Log.d("mydebug", "called here: ${add_comment}")
                   add_comment.text.clear()
               })
           }
        })

    }

    private fun setUpCommentsMenu(){
        Log.d("setupdebug", "menu called")
        val dbManager = DatabaseManager()

        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
        recyclerViewMenu.layoutManager = linearLayoutManager

        commentListMenu = ArrayList()

        val dataStore = getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        val currentUsername = dataStore.getString("username", "")
        if (currentUsername == ""){
            return
        }
        currentUsername ?: return // like guard let


        val database = dbManager.getInstance()
        database.collection("users").document(postPublisher).collection("posts").document(postId)
            .collection("comments")
            .addSnapshotListener { snapshot, error ->
                commentListMenu!!.clear()
                if(error!=null){
                    return@addSnapshotListener
                }
                else{
                    val tmpComments = snapshot!!.toObjects(PostComment::class.java)
                    tmpComments.forEach{ comment ->
                        comment.type?.let{ type ->
                            if(type!!.containsKey("menu")){
                                (commentListMenu as ArrayList<PostComment>).add(comment)
                            }
                        }
                    }
                    commentAdapterMenu = CommentAdapter(this, commentListMenu)
                    recyclerViewMenu.adapter = commentAdapterMenu

                }
            }
    }

    private fun setUpCommentsPrice(){
        Log.d("setupdebug", "price called")
        val dbManager = DatabaseManager()

        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
        recyclerViewPrice.layoutManager = linearLayoutManager

        commentListPrice= ArrayList()

        val dataStore = getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        val currentUsername = dataStore.getString("username", "")
        if (currentUsername == ""){
            return
        }
        currentUsername ?: return // like guard let


        val database = dbManager.getInstance()
        database.collection("users").document(postPublisher).collection("posts").document(postId)
            .collection("comments")
            .addSnapshotListener { snapshot, error ->
                commentListPrice!!.clear()
                if(error!=null){
                    return@addSnapshotListener
                }
                else{
                    val tmpComments = snapshot!!.toObjects(PostComment::class.java)
                    tmpComments.forEach{ comment ->
                        comment.type?.let{ type ->
                            if(type!!.containsKey("price")){
                                (commentListPrice as ArrayList<PostComment>).add(comment)
                            }
                        }
                    }
                    commentAdapterPrice = CommentAdapter(this, commentListPrice)
                    recyclerViewPrice.adapter = commentAdapterPrice
//                    commentAdapterPrice!!.notifyDataSetChanged()

                }
            }
    }

    private fun setUpCommentsTime(){
        Log.d("setupdebug", "time called")
        val dbManager = DatabaseManager()

        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
        recyclerViewTime.layoutManager = linearLayoutManager

        commentListTime = ArrayList()

        val dataStore = getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        val currentUsername = dataStore.getString("username", "")
        if (currentUsername == ""){
            return
        }
        currentUsername ?: return // like guard let


        val database = dbManager.getInstance()
        database.collection("users").document(postPublisher).collection("posts").document(postId)
            .collection("comments")
            .addSnapshotListener { snapshot, error ->
                commentListTime!!.clear()
                if(error!=null){
                    return@addSnapshotListener
                }
                else{
                    val tmpComments = snapshot!!.toObjects(PostComment::class.java)
                    Log.d("setupdebugtime", "${tmpComments}")
                    tmpComments.forEach{ comment ->
                        comment.type?.let{ type ->
                            Log.d("setupdebugtime", "${type}")
                            if(type!!.containsKey("time")){
                                (commentListTime as ArrayList<PostComment>).add(comment)
                            }
                        }
                    }
                    commentAdapterTime = CommentAdapter(this, commentListTime)
                    recyclerViewTime.adapter = commentAdapterTime
//                    commentAdapterTime!!.notifyDataSetChanged()

                }
            }
    }

    private fun configureImage(){
        val dbManager = DatabaseManager()
        dbManager.post(postId=postId, owner=postPublisher, callback={ post ->
            //PicassoManager().picasso.load(post?.postUrlString).fit().centerCrop().into(post_image_comment)
            Glide.with(this).asBitmap().load(post?.postUrlString).into(post_image_comment)

        })
    }
}
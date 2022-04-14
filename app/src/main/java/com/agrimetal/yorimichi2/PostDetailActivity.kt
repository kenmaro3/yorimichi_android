package com.agrimetal.yorimichi2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.agrimetal.yorimichi2.Adaptor.PostNewAdapter
import com.agrimetal.yorimichi2.Manager.DatabaseManager
import com.agrimetal.yorimichi2.Model.Post

class PostDetailActivity : AppCompatActivity() {
    private lateinit var postId: String
    private lateinit var owner: String

    private lateinit var postAdapter: PostNewAdapter
    private lateinit var postList: MutableList<Post>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_detail)

        owner = intent.getStringExtra("owner").toString()
        postId = intent.getStringExtra("postId").toString()

        setUpView()
    }

    private fun setUpView(){

        var recyclerView: RecyclerView = findViewById(R.id.post_detail_post)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
        recyclerView.layoutManager = linearLayoutManager

        postList = ArrayList()

        val databaseManager = DatabaseManager()
        databaseManager.post(postId=postId, owner=owner, callback = { post ->
            post?.let{
                (postList as ArrayList<Post>).add(it)
                Log.d("postdetaildebug", "${postList}")
                postAdapter = PostNewAdapter(this, postList as ArrayList<Post>)
                recyclerView.adapter = postAdapter

            }

        })

    }
}
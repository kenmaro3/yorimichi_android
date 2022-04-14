package com.agrimetal.yorimichi2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.agrimetal.yorimichi2.Adaptor.PostExploreAdapter
import com.agrimetal.yorimichi2.Manager.DatabaseManager
import com.agrimetal.yorimichi2.Model.Location
import com.agrimetal.yorimichi2.Model.Post
import kotlinx.android.synthetic.main.activity_explore_more.*

class ExploreMoreActivity : AppCompatActivity() {
    private var title: String? = "テスト"
    private var type: String? = "promotion"
    private var postsList: MutableList<Post>? = null
    private var adapter: PostExploreAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_explore_more)
        title = intent.getStringExtra("explore_more_title")
        type = intent.getStringExtra("explore_more_type")
        explore_more_title.text = title

        setUpPosts()

    }

    private fun setUpInterface(adapter: PostExploreAdapter){
        adapter!!.setOnItemClickListener(object:PostExploreAdapter.OnItemClickListener{
            override fun onItemClickListener(view: View, position: Int, post: Post) {
                val intent = Intent(this@ExploreMoreActivity, MapActivity::class.java)
                intent.putExtra("postId", post.id)
                intent.putExtra("owner", post.user.username)
                startActivity(intent)
            }
        })
    }


    private fun setUpPosts(){
        var recyclerView: RecyclerView? = null
        recyclerView = findViewById(R.id.recycler_view_explore_more)
        val linearLayoutManagerExplore = LinearLayoutManager(this)
        linearLayoutManagerExplore.reverseLayout = true
        linearLayoutManagerExplore.stackFromEnd = true
        recyclerView?.layoutManager = linearLayoutManagerExplore

        postsList = ArrayList()

        val dbManager = DatabaseManager()

        if(type == "promotion"){
            dbManager.explorePromotion(callback = { posts ->
                if(posts.count() != 0){
                    (postsList as ArrayList<Post>).addAll(posts)
                }
                else{
                    (postsList as ArrayList<Post>).addAll(posts)
                }
                adapter = this.let{ PostExploreAdapter(it, postsList as ArrayList<Post>) }
                setUpInterface(adapter=adapter!!)
                recyclerView.adapter = adapter

            })
        }
        else if(type == "recent"){
            dbManager.exploreRecent(callback = { posts ->
                if(posts.count() != 0){
                    (postsList as ArrayList<Post>).addAll(posts)
                }
                else{
                    (postsList as ArrayList<Post>).addAll(posts)
                }
                adapter = this.let{ PostExploreAdapter(it, postsList as ArrayList<Post>) }
                setUpInterface(adapter=adapter!!)
                recyclerView.adapter = adapter

            })

        }
        else if(type == "popular"){
            dbManager.explorePopular(callback = { posts ->
                if(posts.count() != 0){
                    (postsList as ArrayList<Post>).addAll(posts)
                }
                else{
                    (postsList as ArrayList<Post>).addAll(posts)
                }
                adapter = this.let{ PostExploreAdapter(it, postsList as ArrayList<Post>) }
                setUpInterface(adapter=adapter!!)
                recyclerView.adapter = adapter

            })

        }
        else if(type == "nearby"){
            val current_location = Location(0.0, 0.0)
            dbManager.exploreNearby(current_location, callback = { posts ->
                if(posts.count() != 0){
                    (postsList as ArrayList<Post>).addAll(posts)
                }
                else{
                    (postsList as ArrayList<Post>).addAll(posts)
                }
                adapter = this.let{ PostExploreAdapter(it, postsList as ArrayList<Post>) }
                setUpInterface(adapter=adapter!!)
                recyclerView.adapter = adapter

            })

        }

    }


}
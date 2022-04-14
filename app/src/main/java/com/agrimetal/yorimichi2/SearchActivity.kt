package com.agrimetal.yorimichi2

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.agrimetal.yorimichi2.Adapter.UserAdapter
import com.agrimetal.yorimichi2.Adaptor.PostExploreAdapter
import com.agrimetal.yorimichi2.Manager.DatabaseManager
import com.agrimetal.yorimichi2.Model.Location
import com.agrimetal.yorimichi2.Model.Post
import com.agrimetal.yorimichi2.Model.User
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.android.synthetic.main.activity_search.*


import android.location.Location as AndroidLocation
import com.agrimetal.yorimichi2.Manager.toDate


class SearchActivity : AppCompatActivity() {
    private var recyclerView: RecyclerView? = null
    private var userAdapter: UserAdapter? = null
    private var mUser: MutableList<User>? = null


    private var postListPromotionLimit: MutableList<Post>? = null
    private var postListRecentLimit: MutableList<Post>? = null
    private var postListPopularLimit: MutableList<Post>? = null
    private var postListNearbyLimit: MutableList<Post>? = null

    private var postExplorePromotionAdapter: PostExploreAdapter? = null
    private var postExploreRecentAdapter: PostExploreAdapter? = null
    private var postExplorePopularAdapter: PostExploreAdapter? = null
    private var postExploreNearbyAdapter: PostExploreAdapter? = null

    var fusedLocationProviderClient: FusedLocationProviderClient? = null
    var currentLocation: android.location.Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("yodebug", "test0")
        setContentView(R.layout.activity_search)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        fetchLocation()


        recyclerView = findViewById(R.id.recycler_view_search)
        recyclerView?.setHasFixedSize(true)
        recyclerView?.layoutManager = LinearLayoutManager(this)

        var recyclerViewExplore: RecyclerView? = null

        var promotionFooterButton: Button = findViewById(R.id.explore_promotion_footer_button)
        var recentFooterButton: Button = findViewById(R.id.explore_recent_footer_button)
        var popularFooterButton: Button = findViewById(R.id.explore_popular_footer_button)
        var nearbyFooterButton: Button = findViewById(R.id.explore_nearby_footer_button)


        Log.d("yodebug", "test1")


        mUser = ArrayList()
        userAdapter = this.let{ UserAdapter(it, mUser as ArrayList<User>, true)}
        recyclerView?.adapter = userAdapter
        Log.d("yodebug", "test2")

        setUpPromotion()
        setUpRecent()
        setUpPopular()
        setUpNearby()
    }

    private fun fetchLocation() {
        // GPSのパーミッションの確認
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1000)
            return
        }

        val task = fusedLocationProviderClient?.lastLocation
        task?.addOnSuccessListener { location->
            if(location != null ) {
                this.currentLocation = location
                Log.d("locationdebugsearch", "${currentLocation}")
                setUpNearby()

            }

        }

    }

    private fun setUpInterface(adapter: PostExploreAdapter){
        adapter!!.setOnItemClickListener(object:PostExploreAdapter.OnItemClickListener{
            override fun onItemClickListener(view: View, position: Int, post: Post) {
                val intent = Intent(this@SearchActivity, MapActivity::class.java)
                intent.putExtra("postId", post.id)
                intent.putExtra("owner", post.user.username)
                startActivity(intent)
            }
        })
    }

    private fun setUpPromotion(){
        var recyclerViewPromotion: RecyclerView? = null
        recyclerViewPromotion = findViewById(R.id.recycler_view_explore_promotion)
        val linearLayoutManagerExplore = LinearLayoutManager(this)
        linearLayoutManagerExplore.reverseLayout = true
        linearLayoutManagerExplore.stackFromEnd = true
        recyclerViewPromotion?.layoutManager = linearLayoutManagerExplore

        postListPromotionLimit = ArrayList()

        val dbManager = DatabaseManager()

        dbManager.explorePromotion(callback = { posts ->
            if(posts.count() != 0){
                (postListPromotionLimit as ArrayList<Post>).addAll(posts.slice(0..Math.min(
                    2,
                    posts.count() - 1
                )
                ))
            }
            else{
                (postListPromotionLimit as ArrayList<Post>).addAll(posts)
            }
            postExplorePromotionAdapter = this.let{ PostExploreAdapter(it, postListPromotionLimit as ArrayList<Post>) }
            setUpInterface(adapter = postExplorePromotionAdapter!!)
            recyclerViewPromotion.adapter = postExplorePromotionAdapter

        })

        var button: Button
        button = findViewById(R.id.explore_promotion_footer_button)
        button.setOnClickListener{
            val intent = Intent(this, ExploreMoreActivity::class.java)
            intent.putExtra("explore_more_title", "もっとプロモーションをみる")
            intent.putExtra("explore_more_type", "promotion")
            startActivity(intent)
        }
    }


    private fun setUpRecent(){
        var recyclerViewRecent: RecyclerView? = null
        recyclerViewRecent = findViewById(R.id.recycler_view_explore_recent)
        val linearLayoutManagerExplore = LinearLayoutManager(this)
        linearLayoutManagerExplore.reverseLayout = true
        linearLayoutManagerExplore.stackFromEnd = true
        recyclerViewRecent?.layoutManager = linearLayoutManagerExplore

        postListRecentLimit = ArrayList()

        val dbManager = DatabaseManager()

        dbManager.exploreRecent(callback = { posts ->
            if(posts.count() != 0){
                Log.d("mydebug1", "${posts.count()}")
                Log.d("mydebug2", "${posts.slice(0..Math.min(2, posts.count() - 1))}")
                val index = Math.min(2, posts.count()-1)
                (postListRecentLimit as ArrayList<Post>).addAll(posts.slice(posts.count()-1-index..posts.count()-1))
                Log.d("mydebug-1", "${postListRecentLimit}")
            }
            else{
                Log.d("mydebug3", "${posts.count()}")
                (postListRecentLimit as ArrayList<Post>).addAll(posts);
            }
            postExploreRecentAdapter = this.let{ PostExploreAdapter(it, postListRecentLimit as ArrayList<Post>) }
            setUpInterface(adapter = postExploreRecentAdapter!!)
            recyclerViewRecent.adapter = postExploreRecentAdapter

        })

        var button: Button
        button = findViewById(R.id.explore_recent_footer_button)
        button.setOnClickListener{
            val intent = Intent(this, ExploreMoreActivity::class.java)
            intent.putExtra("explore_more_title", "もっと最近の投稿をみる")
            intent.putExtra("explore_more_type", "recent")
            startActivity(intent)

        }
    }

    private fun setUpPopular(){
        var recyclerViewPopular: RecyclerView? = null
        recyclerViewPopular = findViewById(R.id.recycler_view_explore_popular)
        val linearLayoutManagerExplore = LinearLayoutManager(this)
        linearLayoutManagerExplore.reverseLayout = true
        linearLayoutManagerExplore.stackFromEnd = true
        recyclerViewPopular?.layoutManager = linearLayoutManagerExplore

        postListPopularLimit = ArrayList()
        val dbManager = DatabaseManager()

        dbManager.explorePopular(callback = { posts ->
            if(posts.count() != 0){
                val index = Math.min(2, posts.count()-1)
                (postListPopularLimit as ArrayList<Post>).addAll(posts.slice(posts.count()-1-index..posts.count()-1))
            }
            else{
                (postListPopularLimit as ArrayList<Post>).addAll(posts)
            }
            postExplorePopularAdapter = this.let{ PostExploreAdapter(it, postListPopularLimit as ArrayList<Post>) }
            setUpInterface(adapter = postExplorePopularAdapter!!)
            recyclerViewPopular.adapter = postExplorePopularAdapter

        })

        var button: Button
        button = findViewById(R.id.explore_popular_footer_button)
        button.setOnClickListener{
            val intent = Intent(this, ExploreMoreActivity::class.java)
            intent.putExtra("explore_more_title", "もっと人気の投稿をみる")
            intent.putExtra("explore_more_type", "popular")
            startActivity(intent)
        }
    }

    private fun setUpNearby(){
        var recyclerViewNearby: RecyclerView? = null
        recyclerViewNearby = findViewById(R.id.recycler_view_explore_nearby)
        val linearLayoutManagerExplore = LinearLayoutManager(this)
        linearLayoutManagerExplore.reverseLayout = true
        linearLayoutManagerExplore.stackFromEnd = true
        recyclerViewNearby?.layoutManager = linearLayoutManagerExplore

        postListNearbyLimit = ArrayList()
        val dbManager = DatabaseManager()


        if (currentLocation != null){

            val tmpLocation = Location(currentLocation!!.latitude, currentLocation!!.longitude)


            dbManager.exploreNearby(tmpLocation, callback = { posts ->
                if(posts.count() != 0){
                    val index = Math.min(2, posts.count()-1)
                    (postListNearbyLimit as ArrayList<Post>).addAll(posts.slice(posts.count()-1-index..posts.count()-1))
                }
                else{
                    (postListNearbyLimit as ArrayList<Post>).addAll(posts)
                }
                postExploreNearbyAdapter = this.let{ PostExploreAdapter(it, postListNearbyLimit as ArrayList<Post>) }
                setUpInterface(adapter = postExploreNearbyAdapter!!)
                recyclerViewNearby.adapter = postExploreNearbyAdapter

            })

            var button: Button
            button = findViewById(R.id.explore_nearby_footer_button)

            button.setOnClickListener{
                val intent = Intent(this, ExploreMoreActivity::class.java)
                intent.putExtra("explore_more_title", "もっと周辺の投稿をみる")
                intent.putExtra("explore_more_type", "nearby")
                startActivity(intent)
            }
        }
    }
}
package com.agrimetal.yorimichi2

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.viewpager2.widget.ViewPager2
import com.agrimetal.yorimichi2.Adaptor.PostNewAdapter
import com.agrimetal.yorimichi2.Manager.DatabaseManager
import com.agrimetal.yorimichi2.Manager.DispatchGroup
import com.agrimetal.yorimichi2.Model.Post
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.android.material.bottomnavigation.BottomNavigationView

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.location.Location as AndroidLocation
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import com.agrimetal.yorimichi2.Manager.toDate


class HomeActivity : AppCompatActivity() {
    private var postAdapterFollowing: PostNewAdapter? = null
    private var postAdapterRecommended: PostNewAdapter? = null
    private var postListFollowing: MutableList<Post>? = null
    private var postListRecommended: MutableList<Post>? = null
    private var viewPagerFollowing: ViewPager2? = null
    private var viewPagerRecommended: ViewPager2? = null

    private lateinit var mAdView : AdView


    var fusedLocationProviderClient: FusedLocationProviderClient? = null
    var currentLocation: AndroidLocation? = null

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        val dataStore = getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        var currentUsername = dataStore.getString("username", "")
        if (currentUsername == ""){
            Log.e("mydebug", "username at shared preference is empty at MainActivity")
            //currentUsername = "kenmaro"
        }
        else if (currentUsername == null){
            Log.e("mydebug", "username at shared preference is null at MainActivity")

        }
        when (item.itemId) {
            R.id.nav_home -> {
                startActivity(Intent(this@HomeActivity, HomeActivity::class.java))
                return@OnNavigationItemSelectedListener true
            }
            R.id.nav_explore -> {
                startActivity(Intent(this@HomeActivity, SearchActivity::class.java))
                return@OnNavigationItemSelectedListener true

            }
            R.id.nav_map-> {
                startActivity(Intent(this@HomeActivity, MapActivity::class.java))
                return@OnNavigationItemSelectedListener true
            }
            R.id.nav_add_post -> {
                item.isChecked = false
                startActivity(Intent(this@HomeActivity, AddPostDetailActivity::class.java))
                return@OnNavigationItemSelectedListener true
            }

            R.id.nav_profile -> {
                val intent = Intent(this@HomeActivity, ProfileActivity::class.java)
                intent.putExtra("username", currentUsername)
                startActivity(intent)
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    private fun setUpAds(){
//        MobileAds.initialize(this)
//        mAdView = findViewById(R.id.adView_home)
//        mAdView.adUnitId = "ca-app-pub-3940256099942544/6300978111"
//        val adRequest = AdRequest.Builder().build()
//        mAdView.loadAd(adRequest)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

//        setUpAds()

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        fetchLocation()

        val navView: BottomNavigationView = findViewById(R.id.nav_view_home)

        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        viewPagerFollowing = findViewById(R.id.view_pager_2_following)

        Log.d("mydebug", "onCreate")
        val dataStore = getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        val currentUsername = dataStore?.getString("username", "")
        if (currentUsername == "") {
            Log.e("mydebug", "currentUsername is empty, something is wrong")
        }
        postListFollowing = ArrayList()
        postListRecommended = ArrayList()

        val dispatchGroup = DispatchGroup()

        val dbManager = DatabaseManager()
        dispatchGroup.enter()
        dbManager.followingNotBlocking(username=currentUsername!!, callback={ users ->
            Log.d("mydebug", "this is users ${users}")

            for (user in users){
                dispatchGroup.enter()
                Log.d("mydebug", "loop, ${user}")
                dbManager.postsRecent(user, callback={ posts ->
                    Log.d("mydebug", "posts ${posts}")
                    (postListFollowing as ArrayList<Post>).addAll(posts)
                    dispatchGroup.leave()

                })

            }
            dispatchGroup.leave()
        })
        dispatchGroup.notify {
            Log.d("homedebug", "following: ${postListFollowing}")
            Log.d("homedebug", "recommended: ${postListRecommended}")

            (postListFollowing as ArrayList<Post>).sortedBy { it.postedDate.toDate() }
            postAdapterFollowing = PostNewAdapter(this@HomeActivity, postListFollowing as ArrayList<Post>)
            viewPagerFollowing?.adapter = postAdapterFollowing

        }

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
                Log.d("locationdebug", "${currentLocation}")

            }

        }

    }

}
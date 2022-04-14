package com.agrimetal.yorimichi2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources

import com.mapbox.geojson.Point
import com.mapbox.maps.MapView
import com.mapbox.maps.Style

import com.agrimetal.yorimichi2.Manager.LocationPermissionHelper
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.extension.style.expressions.dsl.generated.interpolate

import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.gestures


import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import java.lang.ref.WeakReference

import android.graphics.Bitmap
import android.graphics.Canvas

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.DrawableRes
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.agrimetal.yorimichi2.Adaptor.PostExploreAdapter
import com.agrimetal.yorimichi2.Manager.DatabaseManager
import com.agrimetal.yorimichi2.Model.Location
import com.agrimetal.yorimichi2.Model.Post
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import kotlinx.android.synthetic.main.activity_map.*

import com.google.android.gms.ads.MobileAds


class MapActivity : AppCompatActivity() {
    private var specificPostId: String? = null
    private var specificOwner: String? = null

    private var centerPosition: Boolean = true
    private var updateCurrentLocation: Boolean = true
    private lateinit var mapView: MapView
    private lateinit var locationPermissionHelper: LocationPermissionHelper
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>

    private var userCurrentLocation: Location = Location(0.0, 0.0)
    private lateinit var currentUserPoint: Point

    private lateinit var mAdView : AdView

    private var postList: MutableList<Post>? = null
    private var postListLiked: MutableList<Post>? = null

    private var selectedPost: Post? = null

    private lateinit var postOnMapAdapter: PostExploreAdapter
    private lateinit var postOnMapAdapterLiked: PostExploreAdapter

    private var pointAnnotationManagerForPost: MutableList<PointAnnotationManager> = ArrayList()
    private var pointAnnotationManagerForPostLiked: MutableList<PointAnnotationManager> = ArrayList()

    private val onIndicatorBearingChangedListener = OnIndicatorBearingChangedListener {
        mapView.getMapboxMap().setCamera(CameraOptions.Builder().bearing(it).build())
    }

    private val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener {
        if(centerPosition){
            mapView.getMapboxMap().setCamera(CameraOptions.Builder().center(it).build())
            centerPosition = false
        }

        if(updateCurrentLocation){
            mapView.gestures.focalPoint = mapView.getMapboxMap().pixelForCoordinate(it)
            currentUserPoint = it
            userCurrentLocation.lat = it.latitude()
            userCurrentLocation.lng = it.longitude()
            exploreNearBy()
            updateCurrentLocation = false

        }

    }

    private val onMoveListener = object : OnMoveListener {
        override fun onMoveBegin(detector: MoveGestureDetector) {
            onCameraTrackingDismissed()
        }

        override fun onMove(detector: MoveGestureDetector): Boolean {
            return false
        }

        override fun onMoveEnd(detector: MoveGestureDetector) {}
    }

    //constructor()




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

//        setUpAds()
        setUpTab()

        handleSpecificPoint()

        exploreNearBy()
        exploreLiked()


        // CoordinatorLayout の 直下に配置している View を渡す
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout)

        mapView = findViewById(R.id.mapView)
//        mapView?.getMapboxMap()?.loadStyleUri(Style.MAPBOX_STREETS)

        locationPermissionHelper = LocationPermissionHelper(WeakReference(this))
        locationPermissionHelper.checkPermissions {
            onMapReady()
        }

        setUpRelocationButton()

        setUpQueryButton()

        onmap_go_to_place_container.setOnClickListener{
            if (selectedPost == null){
                Toast.makeText(this, "場所が選択されていません。", Toast.LENGTH_SHORT).show()
            }
            else{
                val geoUriString="geo:"+selectedPost!!.location.lat+","+selectedPost!!.location.lng+"?q=("+selectedPost!!.locationTitle+")@"+selectedPost!!.location.lat+","+selectedPost!!.location.lng;
                val gmmIntentUri = Uri.parse(geoUriString)
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                startActivity(mapIntent)
            }
        }
    }


    private fun exploreNearBy(){
        val dbManager = DatabaseManager()

        var recyclerViewPost : RecyclerView? = null
        recyclerViewPost = findViewById(R.id.recycler_view_map_post)
        val linearLayoutManagerPost = LinearLayoutManager(this)
        linearLayoutManagerPost.reverseLayout = true
        linearLayoutManagerPost.stackFromEnd = true
        recyclerViewPost?.layoutManager = linearLayoutManagerPost

        postList = ArrayList()

        if(userCurrentLocation.lat == 0.0 && userCurrentLocation.lng == 0.0){
            return
        }

        dbManager.exploreNearby(currentLocation=userCurrentLocation){ posts ->
            if(posts.count() > 0){
                (postList as ArrayList<Post>).addAll(posts)
                postOnMapAdapter = this.let{ PostExploreAdapter(it, postList as ArrayList<Post>) }
                postOnMapAdapter.setOnItemClickListener(object:PostExploreAdapter.OnItemClickListener{
                    override fun onItemClickListener(view: View, position: Int, post: Post) {
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED)
                        selectedPost = (postList as ArrayList<Post>)[position]
                        selectedPost?.let{
                            val targetPoint: Point = Point.fromLngLat(it.location.lng, it.location.lat)
                            mapView.getMapboxMap().setCamera(CameraOptions.Builder().center(targetPoint).build())

                        }
                    }
                })
                recyclerViewPost.adapter = postOnMapAdapter

            }

            posts.forEach {
                addAnnotationToMapForPost(it){
                    pointAnnotationManagerForPost.add(it)
                }

            }

        }
    }

    private fun exploreLiked(){
        val dbManager = DatabaseManager()
        val dataStore = getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        val currentUsername = dataStore?.getString("username", "")
        if (currentUsername == "") {
            Log.e("mydebug", "currentUsername is empty, something is wrong")
        }
        currentUsername ?: return

        var recyclerViewPost : RecyclerView? = null
        recyclerViewPost = findViewById(R.id.recycler_view_map_post_liked)
        val linearLayoutManagerPost = LinearLayoutManager(this)
        linearLayoutManagerPost.reverseLayout = true
        linearLayoutManagerPost.stackFromEnd = true
        recyclerViewPost?.layoutManager = linearLayoutManagerPost

        postListLiked = ArrayList()

        dbManager.postsLiked(username = currentUsername){ posts ->
            Log.d("mydebug", "here posts liked ${posts.count()}")
            if(posts.count() > 0){
                (postListLiked as ArrayList<Post>).addAll(posts)
                postOnMapAdapterLiked = this.let{ PostExploreAdapter(it, postListLiked as ArrayList<Post>) }
                recyclerViewPost.adapter = postOnMapAdapterLiked

            }
        }

    }

    private fun setUpRelocationButton(){
        onmap_current_location_button.setOnClickListener{

            mapView.getMapboxMap().setCamera(CameraOptions.Builder().center(currentUserPoint).build())
        }
    }

    private fun setUpTab(){
        val recyclerViewNearBy: RecyclerView = findViewById(R.id.recycler_view_map_post)
        val recyclerViewLiked: RecyclerView = findViewById(R.id.recycler_view_map_post_liked)

        var button_posts_around: Button = findViewById(R.id.onmap_find_posts_around_button)
        button_posts_around.setOnClickListener{
            exploreNearBy()
            recyclerViewNearBy.visibility = View.VISIBLE
            recyclerViewLiked.visibility = View.INVISIBLE
        }
        var button_posts_liked: Button = findViewById(R.id.onmap_find_posts_liked_button)
        button_posts_liked.setOnClickListener{
            exploreLiked()
            recyclerViewNearBy.visibility = View.INVISIBLE
            recyclerViewLiked.visibility = View.VISIBLE
        }
    }

    private fun setUpAds(){
//        MobileAds.initialize(this) {}
//        MobileAds.initialize(this)
//        mAdView = findViewById(R.id.adView_map)
//        mAdView.adUnitId = "ca-app-pub-3940256099942544/6300978111"
//        val adRequest = AdRequest.Builder().build()
//        mAdView.loadAd(adRequest)
    }

    private fun handleSpecificPoint(){
        specificPostId = intent.getStringExtra("postId")
        specificOwner = intent.getStringExtra("owner")

        if(specificPostId != null && specificOwner != null){
            centerPosition = false
            val databaseManager = DatabaseManager()
            databaseManager.post(specificPostId as String, specificOwner as String, callback = { post ->
                post?.let{ it
                    val targetPoint: Point = Point.fromLngLat(it.location.lng, it.location.lat)
                    mapView.getMapboxMap().setCamera(CameraOptions.Builder().center(targetPoint).build())
                    selectedPost = it
                    addAnnotationToMapForPost(it, callbacks = {
                    })
                }
            })
        }
    }

    private fun setUpQueryButton(){
        onmap_query_db_button.setOnClickListener{
            Log.d("buttondebug", "clicked")

            val dbManager = DatabaseManager()

            val center = mapView.getMapboxMap().cameraState.center
            var tmpLocation = Location(center.latitude(), center.longitude())


            var recyclerViewPost : RecyclerView? = null
            recyclerViewPost = findViewById(R.id.recycler_view_map_post)
            val linearLayoutManagerPost = LinearLayoutManager(this)
            linearLayoutManagerPost.reverseLayout = true
            linearLayoutManagerPost.stackFromEnd = true
            recyclerViewPost?.layoutManager = linearLayoutManagerPost

            postList = ArrayList()

            dbManager.exploreNearby(currentLocation=tmpLocation){ posts ->
                Log.d("mydebug", "here posts nearby${posts.count()}")
                if(posts.count() > 0){
                    (postList as ArrayList<Post>).addAll(posts)
                    postOnMapAdapter = this.let{ PostExploreAdapter(it, postList as ArrayList<Post>) }
                    // interface impl
                    postOnMapAdapter.setOnItemClickListener(object:PostExploreAdapter.OnItemClickListener{
                        override fun onItemClickListener(view: View, position: Int, post: Post) {
                            selectedPost = post
                        }
                    })
                    recyclerViewPost.adapter = postOnMapAdapter

                }

                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED)


            }

        }
    }

    private fun onMapReady() {
        mapView.getMapboxMap().setCamera(
            CameraOptions.Builder()
                .zoom(14.0)
                .build()
        )
        val databaseManager = DatabaseManager()
        val uriString = databaseManager.getMapUri(callback = { uriString ->
            if(uriString != null){
                Log.d("mapstyledebug", "not null")
                mapView.getMapboxMap().loadStyleUri(
                    uriString
                ) {
                    initLocationComponent()
                    setupGesturesListener()
                }

            }
            else{
                Log.d("mapstyledebug", "null")
                mapView.getMapboxMap().loadStyleUri(
                    Style.MAPBOX_STREETS
                ) {
                    initLocationComponent()
                    setupGesturesListener()
                }
            }

        })
    }

    private fun setupGesturesListener() {
        mapView.gestures.addOnMoveListener(onMoveListener)
    }

    private fun initLocationComponent() {
        val locationComponentPlugin = mapView.location
        Log.d("locationdebug", "${mapView.location.toString()}")
        //mapView.location.toString()
        locationComponentPlugin.updateSettings {
            this.enabled = true
            this.locationPuck = LocationPuck2D(
                bearingImage = AppCompatResources.getDrawable(
                    this@MapActivity,
                    R.drawable.mapbox_user_puck_icon,
                ),
                shadowImage = AppCompatResources.getDrawable(
                    this@MapActivity,
                    R.drawable.mapbox_user_icon_shadow,
                ),
                scaleExpression = interpolate {
                    linear()
                    zoom()
                    stop {
                        literal(0.0)
                        literal(0.6)
                    }
                    stop {
                        literal(20.0)
                        literal(1.0)
                    }
                }.toJson()
            )
        }
        locationComponentPlugin.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        locationComponentPlugin.addOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
    }

    private fun onCameraTrackingDismissed() {
        mapView.location
            .removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        mapView.location
            .removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
        mapView.gestures.removeOnMoveListener(onMoveListener)
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
        mapView.location
            .removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
        mapView.location
            .removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        mapView.gestures.removeOnMoveListener(onMoveListener)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        locationPermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun addAnnotationToMapForPost(post: Post, callbacks: (PointAnnotationManager) -> (Unit)){
        bitmapFromDrawableRes(
            this@MapActivity,
            R.drawable.red_marker
        )?.let {
            val annotationApi = mapView?.annotations
            val pointAnnotationManager = annotationApi?.createPointAnnotationManager(mapView!!)
            val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
                .withPoint(Point.fromLngLat(post.location.lng, post.location.lat))
                .withIconImage(it)

            pointAnnotationManager?.create(pointAnnotationOptions)

            pointAnnotationManager.addClickListener { clickedAnnotation ->
                Log.d("clickeddebug", "clicked: ${pointAnnotationOptions}")
                clickedAnnotation.isSelected = true

                selectedPost = post

                val targetPoint = Point.fromLngLat(clickedAnnotation.point.longitude(), clickedAnnotation.point.latitude())
                mapView.getMapboxMap().setCamera(CameraOptions.Builder().center(targetPoint).build())

                true
            }

            callbacks(pointAnnotationManager)

        }

    }

    private fun bitmapFromImageView(context: Context, imageView: ImageView) =
        convertDrawableToBitmap(imageView.drawable)

    private fun bitmapFromDrawableRes(context: Context, @DrawableRes resourceId: Int) =
        convertDrawableToBitmap(AppCompatResources.getDrawable(context, resourceId))

    private fun convertDrawableToBitmap(sourceDrawable: Drawable?): Bitmap? {
        if (sourceDrawable == null) {
            return null
        }
        return if (sourceDrawable is BitmapDrawable) {
            sourceDrawable.bitmap
        } else {
            val constantState = sourceDrawable.constantState ?: return null
            val drawable = constantState.newDrawable().mutate()
            val bitmap: Bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth, drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        }
    }


}
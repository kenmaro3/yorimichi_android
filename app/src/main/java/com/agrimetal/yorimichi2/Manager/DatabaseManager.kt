package com.agrimetal.yorimichi2.Manager

import android.content.Context
import android.net.Uri
import android.util.Log
import com.agrimetal.yorimichi2.Model.*
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

import java.text.ParseException
import java.lang.IllegalArgumentException
import java.text.SimpleDateFormat
import java.util.Date



fun String.toDate(pattern: String = "MMM dd, yyyy HH:mm"): Date? {
    val sdFormat = try {
        SimpleDateFormat(pattern, Locale.ENGLISH)
    } catch (e: IllegalArgumentException) {
        null
    }
    val date = sdFormat?.let {
        try {
            it.parse(this)
        } catch (e: ParseException){
            null
        }
    }
    return date
}

fun get_date_with_days_diff(diff_day: Int, pattern: String = "MMM dd, yyyy HH:mm"): Date? {
    val sdFormat = try {
        SimpleDateFormat(pattern, Locale.ENGLISH)
    } catch (e: IllegalArgumentException) {
        null
    }
    val cal = Calendar.getInstance()
    cal.time = Date()

    cal.add(Calendar.DATE, diff_day)
    return cal.time
}

fun get_distance(current_location: Location, target_location: Location): Double {
    val lat_diff = current_location.lat - target_location.lat
    val lng_diff = current_location.lng - target_location.lng

    val diff = Math.pow(lat_diff, 2.0) + Math.pow(lng_diff, 2.0)

    return diff
}

class DatabaseManager {

    constructor()

    private val database = FirebaseFirestore.getInstance()
    private val TAG = "DatabaseManager"

    public fun getInstance(): FirebaseFirestore{
        return database
    }

    public fun getMapUri(callback: (String?) -> Unit){
        val ref = database.collection("customMaps").document("currentMap")
        var uriString: String? = null
        ref.get()
            .addOnSuccessListener { document ->
                Log.d("getmapuridebug", "okay")
                document.data?.let{ data ->
                    uriString = data["styleUrl"] as String?
                    callback(uriString)

                }
                return@addOnSuccessListener

            }
            .addOnFailureListener{
                Log.d("getmapuridebug", "failed")
                callback(null)
            }



    }

    public fun getRequiredUpdateVersion(callback: (String?) -> Unit){
        val ref = database.collection("configure").document("config")

        ref.get()
            .addOnSuccessListener { document ->
                document.data?.let{ data ->
                    val version = data["updateRequiredVersion"] as String?
                    callback(version)

                }
            }
            .addOnFailureListener { error ->
                Log.d("mydebug", error.message!!)
            }
    }

    public fun getMapStyle(callback: (String?) -> Unit){
        val ref = database.collection("customMaps").document("currentMap")
        ref.get()
            .addOnSuccessListener { document ->
                document.data?.let{ data ->
                    val styleUrl = data["styleUrl"] as String?
                    callback(styleUrl)
                }

            }
            .addOnFailureListener { error ->
                Log.d("mydebug", error.message!!)
                callback("mapbox://styles/mapbox/streets-v11")
            }


    }

    public fun findUsers(usernamePrefix: String, callback: (List<User>) -> Unit){
        val ref = database.collection("users")

        ref.get()
            .addOnSuccessListener { documents ->
                val users = documents.map{
                    it.toObject(User::class.java)
                }

                val filteredUsers = users.filter{ user ->
                    user.username.startsWith(usernamePrefix)
                }
                callback(filteredUsers)
            }
            .addOnFailureListener { error ->
                Log.d("mydebug", error.message!!)
                val emptyList: List<User> = listOf()
                callback(emptyList)
            }
    }

    public fun findPlacesSubString(prefix: String, callback: (List<String>) -> Unit){
        val ref = database.collection("yorimichiPost").document("A000").collection("posts")
        ref.get()
            .addOnSuccessListener { documents ->
                val posts = documents.map{
                    it.toObject(Post::class.java)
                }
                val subsets = posts.filter{ post ->
                   post.locationTitle.startsWith(prefix) || post.locationSubTitle.startsWith(prefix)
                }

                val subsetsString = subsets.map{
                    it.locationTitle
                }

                callback(subsetsString.distinct())
            }

            .addOnFailureListener { error ->
                val emptyList: List<String> = listOf()
                callback(emptyList)
            }

    }



    public fun createUser(user: User, callback: (Boolean) -> Unit){
        val defaultUserInfo = hashMapOf(
            "bio" to "プロフィールへようこそ",
            "instagramId" to "",
            "twitterId" to "",
            "name" to ""
        )
        database.collection("users").document(user.username)
            .set(user)
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    database.collection("users").document(user.username).collection("information").document("basic")
                        .set(defaultUserInfo)

                    callback(true)

                }else{
                    callback(false)

                }
            }
    }

    public fun getUserInfo(username: String, callback: (UserInfo?) -> Unit){
        Log.d("mydebug", "userinfo called with: ${username}")

        val ref = database.collection("users").document(username).collection("information").document("basic")
        ref.get()
            .addOnSuccessListener { document ->
                Log.d("mydebug", "userinfo su")
                var tmp = document.toObject(UserInfo::class.java)
                callback(tmp)


            }
            .addOnFailureListener {
                Log.d("mydebug", "userinfo fail")
                callback(null)

            }
    }

    public fun setUserInfo(context: Context, userInfo: UserInfo, callback: (Boolean) -> Unit){
        val dataStore = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        val currentUsername = dataStore.getString("username", "")
        if (currentUsername == ""){
            return
        }
        currentUsername ?: return // like guard let

        val ref = database.collection("users").document(currentUsername).collection("information").document("basic")
        ref.set(userInfo)
            .addOnSuccessListener { document ->
                callback(true)

            }
            .addOnFailureListener {
                callback(false)
            }
    }

    public fun findUser(email: String, callback: (User?) -> Unit){
        Log.d("mydebug", "findUser called with ${email}")
        val collection = "tests"
        database.collection("users")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    Log.d("mydebug", "${document.id} => ${document.data}")
                }
                val users = documents.map{
                    it.toObject(User::class.java)
                }
                val targetUser = users.filter{
                    it.email == email
                }
                Log.d("mydebug", "targetUser: ${targetUser}")
                callback(targetUser.first())
            }
            .addOnFailureListener { exception ->
                Log.w("mydebug", "Error getting documents: ", exception)
                callback(null)
            }
        Log.d("mydebug", "called after")
    }

    public fun findUserByName(name: String, callback: (User?) -> Unit){
        database.collection("users").whereEqualTo("username", name)
            .get()
            .addOnSuccessListener { documents ->
                val users = documents.map{
                   it.toObject(User::class.java)
                }
                callback(users.first())
            }
            .addOnFailureListener {
                callback(null)
            }

    }

    public fun blocks(username: String, callback: (List<String>) -> Unit){
        Log.d("mydebug", "blocks called with ${username}")
        database.collection("users").document(username).collection("blocks")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    Log.d("mydebug", "${document.id} => ${document.data}")
                }
                val users = documents.map{
                    it.id
                }

                callback(users)
            }
            .addOnFailureListener { exception ->
                Log.w("mydebug", "Error getting documents: ", exception)
                callback(emptyList())
            }

    }

    public fun followingNotBlocking(username: String, callback: (List<String>) -> Unit){
        Log.d("mydebug", "followingNotBlocking called with ${username}")
        database.collection("users").document(username).collection("following")
            .get()
            .addOnSuccessListener { documents ->
                val users = documents.map{ followingUsers ->
                    //it.toObject(User::class.java).username
                    followingUsers.id
                }

                blocks(username=username, callback={ blocks ->

                    if (blocks.size == 0){
                        callback(users)
                    }
                    else{
                        val usersFiltered = users.filter{
                            users.contains(it)
                        }
                        callback(usersFiltered)
                    }

                })

            }


    }

    // POSTS
    public fun createPost(post: Post, username: String, callback: (Boolean) -> Unit){
        val ref = database.collection("users").document(username).collection("posts").document(post.id)
        ref.set(post)
            .addOnCompleteListener{ res ->
                if(res.isSuccessful){
                    Log.d("mydebug", "createPost su")
                    callback(true)
                }
                else{
                    Log.d("mydebug", "createPost fail")
                    callback(false)
                }


            }

    }

    public fun createYorimichiPostAtAll(post: Post, username: String, callback: (Boolean) -> Unit){
        val ref = database.collection("yorimichiPost").document("A000").collection("posts").document(post.id)
        ref.set(post)
            .addOnCompleteListener{ res ->
                if(res.isSuccessful){
                    Log.d("mydebug", "createPost su")
                    callback(true)
                }
                else{
                    Log.d("mydebug", "createPost fail")
                    callback(false)
                }


            }

    }

    public fun post(postId: String, owner: String, callback: (Post?) -> Unit){
        val ref = database.collection("users").document(owner).collection("posts").document(postId)
            .get()
            .addOnSuccessListener { document ->
                callback(document.toObject(Post::class.java))

            }
            .addOnFailureListener{
                callback(null)

            }

    }
    public fun posts(username: String, callback: (List<Post>) -> Unit){
        Log.d("mydebug", "posts called with ${username}")
        val ref = database.collection("users").document(username).collection("posts")
            .get()
            .addOnSuccessListener { documents ->
                val posts = documents.map{
                    Log.d("mydebug", "ind post ${it.id}")
                    it.toObject(Post::class.java)
                }
                callback(posts)
            }
            .addOnFailureListener{
                Log.d("mydebug", "failed, please check")
                callback(emptyList())
            }
    }

    public fun postsRecent(username: String, callback: (List<Post>) -> Unit){
        Log.d("mydebug", "posts called with ${username}")
        val ref = database.collection("users").document(username).collection("posts")
            .get()
            .addOnSuccessListener { documents ->
                val posts = documents.map{
                    Log.d("mydebug", "ind post ${it.id}")
                    it.toObject(Post::class.java)
                }
                val date_filter = get_date_with_days_diff(-30)
//                val posts_filtered = posts.filter{
//                    it.postedDate.toDate()!! > date_filter
//                }
                var posts_filtered: MutableList<Post> = mutableListOf()
                posts.forEach { post ->
                    Log.d("postsrecentdebugpostsdate", "${post.postedDate}")
                    Log.d("postsrecentdebugpostsdate2", "${post.postedDate.toDate()}")
                    post.postedDate.toDate()?.let{ dateObj ->
                        Log.d("postsrecentdebugpostscompare", "${dateObj}, ${date_filter}")
                        if(dateObj > date_filter){
                            posts_filtered.add(post)
                        }
                    }

                }

                Log.d("postsrecentdebugpostsoriginal", "${posts}")
                Log.d("postsrecentdebugrecent", "${posts_filtered}")
                posts_filtered.sortBy { it.postedDate.toDate()}

                Log.d("postsrecentdebugsorted", "${posts_filtered}")

                callback(posts_filtered)
            }
            .addOnFailureListener{
                Log.d("mydebug", "failed, please check")
                callback(emptyList())
            }
    }

    public fun explorePromotion(callback: (List<Post>) -> Unit){
        Log.d("exploreRecent", "called")
        val ref = database.collection("yorimichiPost").document("PRO000").collection("posts")
            .get()
            .addOnSuccessListener { documents ->
                val posts = documents.map{
                    Log.d("exploreRecent", "ind post ${it.id}")
                    it.toObject(Post::class.java)
                }

                Log.d("exploreRecent", "here")
                Log.d("exploreRecent", "${posts}")


//                val date_filter = get_date_with_days_diff(-10)
////                val posts_filtered = posts.filter{
////                    Log.d("heredebug", it.postedDate)
////                    //it.postedDate.toDate()!! > date_filter
////                    it.postedDate.toDate()?.let{
////                        it > date_filter
////                    }
////                }
//                var posts_filtered: MutableList<Post> = mutableListOf()
//                posts.forEach { post ->
//                    post.postedDate.toDate()?.let{ dateObj ->
//                        if(dateObj > date_filter){
//                            posts_filtered.add(post)
//                        }
//                    }
//
//                }
//                callback(posts_filtered)
                callback(posts)
            }
            .addOnFailureListener{
                Log.d("mydebug", "failed, please check")
                callback(emptyList())
            }
    }

    public fun exploreRecent(callback: (List<Post>) -> Unit){
        Log.d("exploreRecent", "called")
        val ref = database.collection("yorimichiPost").document("A000").collection("posts")
            .get()
            .addOnSuccessListener { documents ->
                val posts = documents.map{
                    Log.d("exploreRecent", "ind post ${it.id}")
                    it.toObject(Post::class.java)
                }

                Log.d("exploreRecent", "here")
                Log.d("exploreRecent", "${posts}")


                val date_filter = get_date_with_days_diff(-10)
//                val posts_filtered = posts.filter{
//                    it.postedDate.toDate()!! > date_filter
//                }
                var posts_filtered: MutableList<Post> = mutableListOf()
                posts.forEach { post ->
                    post.postedDate.toDate()?.let{ dateObj ->
                        if(dateObj > date_filter){
                            posts_filtered.add(post)
                        }
                    }

                }
                posts_filtered.sortBy { it.postedDate.toDate() }
                callback(posts_filtered)
            }
            .addOnFailureListener{
                Log.d("mydebug", "failed, please check")
                callback(emptyList())
            }
    }

    public fun explorePopular(callback: (List<Post>) -> Unit){
        val ref = database.collection("yorimichiPost").document("A000").collection("posts")
            .get()
            .addOnSuccessListener { documents ->
                val posts = documents.map{
                    Log.d("mydebug", "ind post ${it.id}")
                    it.toObject(Post::class.java)
                }

                val date_filter = get_date_with_days_diff(-10)
//                val posts_recent = posts.filter{
//                    it.postedDate.toDate()!! > date_filter
//                }
                var posts_recent: MutableList<Post> = mutableListOf()
                posts.forEach { post ->
                    post.postedDate.toDate()?.let{ dateObj ->
                        if(dateObj > date_filter){
                            posts_recent.add(post)
                        }
                    }

                }

                val posts_popular = posts_recent.sortedByDescending {
                    it.likers.count()
                }

                callback(posts_popular)
            }
            .addOnFailureListener{
                Log.d("mydebug", "failedsoplease")
                callback(emptyList())
            }
    }

    public fun exploreNearby(currentLocation: Location, callback: (List<Post>) -> Unit){
        var latUpperLimit = currentLocation.lat + 0.015
        var latLowerLimit = currentLocation.lat - 0.015
        Log.d("locationdebug", "${latUpperLimit}")
        Log.d("locationdebug", "${latLowerLimit}")

        val ref = database.collection("yorimichiPost").document("A000").collection("posts")
            .whereGreaterThan("location.lat", latLowerLimit)
            .whereLessThan("location.lat", latUpperLimit)
            .get()
            .addOnSuccessListener { documents ->
                val posts = documents.map {
                    Log.d("mydebug", "ind post ${it.id}")
                    it.toObject(Post::class.java)
                }

//                val date_filter = get_date_with_days_diff(-10)
//                val posts_recent = posts.filter {
//                    it.postedDate.toDate()!! > date_filter
//                }

                var distances = ArrayList<Double>()
                posts.forEach {
                    distances.add(get_distance(currentLocation, it.location))
                }

                val sortedIndices = distances.withIndex()
                    .sortedByDescending { it.value }
                    .map { it.index }

                var posts_nearby = ArrayList<Post>()
                sortedIndices.forEach {
                    posts_nearby.add(posts[it])
                }

                callback(posts_nearby)
            }


            .addOnFailureListener{
                Log.d("mydebug", "failed, please check")
                callback(emptyList())
            }
    }


    // Update Like
    enum class LikeState{
        LIKE, UNLIKE
    }

    public fun addYorimichiLikes(post: Post, username: String, callback: (Boolean) -> Unit){
        val randomNumber = (Math.random()*1000).toInt()
        val dateInt = Date().time/1000
        val newIdentifier = "${username}_${dateInt}_${randomNumber}"
        val ref = database.collection("users").document(username)
            .collection("yorimichiLikes").document(newIdentifier)
        ref.set(post)
            .addOnCompleteListener{ res ->
                if(res.isSuccessful){
                    Log.d("mydebug", "createYorimichiLikes su")
                    callback(true)
                }
                else{
                    Log.d("mydebug", "createYorimichiLikes fail")
                    callback(false)
                }


            }

    }

    public fun removePost(post: Post, callback: (Boolean) -> Unit){
        var flag: Boolean = false
        val ref = database.collection("users").document(post.user.username)
            .collection("posts")
        val matchRef = ref.whereEqualTo("id", post.id)
            .get()
            .addOnSuccessListener { documents ->
                val matchedPostsId = documents.map { it ->
                    it.id
                }
                Log.d("deletedebug", "removePost matchedPostId: ${matchedPostsId}")

                val deleteRef = ref.document(matchedPostsId.first())
                Log.d("deletedebug", "${deleteRef}")

                deleteRef.delete()
                    .addOnSuccessListener {
                        Log.d("deletedebug", "delete okay")
                        flag = true
                        return@addOnSuccessListener
                    }
                    .addOnFailureListener {
                        Log.d("deletedebug", "delete failed")
                        flag = false
                        return@addOnFailureListener

                    }
                    .addOnCanceledListener {
                        Log.d("deletedebug", "delete canceld")
                        flag = false
                        return@addOnCanceledListener

                    }

                callback(flag)
                return@addOnSuccessListener

            }
            .addOnFailureListener {
                flag = false
                callback(flag)
                return@addOnFailureListener

            }

    }

    public fun removePostFromAll(post: Post, callback: (Boolean) -> Unit){
        var flag: Boolean = false
        val ref = database.collection("yorimichiPost").document("A000")
            .collection("posts")
        val matchRef = ref.whereEqualTo("id", post.id)
            .get()
            .addOnSuccessListener { documents ->
                val matchedPostsId = documents.map { it ->
                    it.id
                }

                val deleteRef = ref.document(matchedPostsId.first())
                deleteRef.delete()
                    .addOnSuccessListener {
                        flag = true
                        return@addOnSuccessListener
                    }
                    .addOnFailureListener {
                        flag = false
                        return@addOnFailureListener
                    }

                callback(flag)
                return@addOnSuccessListener
            }
            .addOnFailureListener {
                flag = false
                callback(flag)
                return@addOnFailureListener

            }

    }

    public fun removeYorimichiLikes(post: Post, username: String, callback: (Boolean) -> Unit){
        val ref = database.collection("users").document(username)
            .collection("yorimichiLikes")
        val matchRef = ref.whereEqualTo("id", post.id)
            .get()
            .addOnSuccessListener { documents ->
               val matchedPostsId = documents.map { it ->
                   it.id
               }

                val deleteRef = ref.document(matchedPostsId.first())
                deleteRef.delete()
                    .addOnSuccessListener {
                        callback(true)
                    }
                    .addOnFailureListener {
                        callback(false)
                    }
            }
            .addOnFailureListener {
                callback(true)

            }
    }

    public fun updateLike(currentUsername: String, state: LikeState, postId: String, owner: String, callback: (Boolean) -> Unit){

        val ref = database.collection("users").document(owner).collection("posts").document(postId)
        ref.get()
            .addOnSuccessListener {
                var post = it.toObject(Post::class.java)
                when(state){
                    LikeState.LIKE -> {
                        if(!post!!.likers.contains(currentUsername)){
                            post.likers.add(currentUsername)
                        }
                    }
                    LikeState.UNLIKE -> {
                        post!!.likers.remove(currentUsername)
                    }

                }

                ref.set(post)
                    .addOnSuccessListener {
                        callback(true)
                    }
                    .addOnFailureListener {
                        callback(false)
                    }

            }


    }

    public fun postsLiked(username: String, callback: (List<Post>) -> Unit){
        Log.d("mydebug", "posts called with ${username}")
        val ref = database.collection("users").document(username).collection("yorimichiLikes")
            .get()
            .addOnSuccessListener { documents ->
                val posts = documents.map{
                    Log.d("mydebug", "ind post ${it.id}")
                    it.toObject(Post::class.java)
                }

                Log.d("heredebug", "${posts}")
                if(posts != null){
                    callback(posts)
                }
                else{
                    callback(listOf())
                }
            }
            .addOnFailureListener{
                Log.d("mydebug", "failed, please check")
                callback(listOf())
            }
    }

    // Update follow
    enum class RelationshipState{
        FOLLOW, UNFOLLOW
    }

    public fun updateRelationship(context: Context, state: RelationshipState, targetUsername: String, callback: (Boolean) -> Unit){
        val dataStore = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        val currentUsername = dataStore.getString("username", "")
        if (currentUsername == ""){
            return
        }
        currentUsername ?: return // like guard let

        val currentFollowing = database.collection("users").document(currentUsername).collection("following")
        val targetUserFollowers = database.collection("users").document(targetUsername).collection("followers")

        when (state) {
            RelationshipState.UNFOLLOW -> {
                // Remove follower from requester following list
                currentFollowing.document(targetUsername).delete()
                // Remove follower from targetUser follower list
                targetUserFollowers.document(currentUsername).delete()
                callback(true)

            }
            RelationshipState.FOLLOW -> {
                // Add follower to requester following list
                val tmp = hashMapOf(
                    "valid" to 1
                )
                currentFollowing.document(targetUsername).set(tmp)
                // Add follower to targetUser follower list
                targetUserFollowers.document(currentUsername).set(tmp)
                callback(true)

            }
        }
    }

    public fun isFollowing(context: Context, targetUsername: String, callback: (Boolean) -> Unit){
        val dataSource = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        val currentUsername = dataSource.getString("username", "")

        if(currentUsername == ""){return}
        currentUsername ?: return

        val ref = database.collection("users").document(currentUsername).collection("following").document(targetUsername)
        ref.get()
            .addOnSuccessListener { document ->
                Log.d("mydebug", "get following")
                Log.d("mydebug", "${document.data}")
                if(document.data != null){
                    callback(true)
                }
                else{
                    callback(false)
                }

            }
            .addOnFailureListener {
                Log.d("mydebug", "get following failed")
                Log.d("mydebug", "${it}")
                callback(false)
            }
    }

    public fun getUserCounts(context: Context, username: String, callback: (Triple<Int, Int, Int>) -> Unit){
        val dataSource = context.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        val currentUsername = dataSource.getString("username", "")

        val userRef = database.collection("users").document(username)

        var followers = 0
        var following = 0
        var photoPostCount = 0

        val group = DispatchGroup()
        group.enter()
        group.enter()
        group.enter()


        userRef.collection("posts")
            .get()
            .addOnSuccessListener { documents ->
                photoPostCount = documents.count()
                group.leave()
            }
            .addOnFailureListener {
                group.leave()
            }

        userRef.collection("followers")
            .get()
            .addOnSuccessListener { documents ->
                followers = documents.count()
                group.leave()
            }
            .addOnFailureListener {
                group.leave()
            }

        userRef.collection("following")
            .get()
            .addOnSuccessListener { documents ->
                following = documents.count()
                group.leave()
            }
            .addOnFailureListener {
                group.leave()
            }

        group.notify{
            Log.d("mydebug", "notified")
            Log.d("mydebug", "posts: ${photoPostCount}, followers: ${followers}, following: ${following}")
            val res = Triple<Int, Int, Int>(photoPostCount, followers, following)
            callback(res)
        }
    }

    public fun getComments(postId: String, owner: String, callback: (List<PostComment>) -> Unit){
        val ref = database.collection("users").document(owner).collection("posts").document(postId).collection("comments")
            .get()
            .addOnSuccessListener { document ->
                val comments = document.map{
                    it.toObject(PostComment::class.java)
                }
                callback(comments)
            }
            .addOnFailureListener{
                val emptyList = emptyList<PostComment>()
                callback(emptyList)
            }

    }

    public fun createComment(postId: String, owner: String, comment: PostComment, callback: (Boolean) -> Unit){
        val randomNumber = (Math.random()*1000).toInt()
        val dateInt = Date().time/1000
        val newIdentifier = "${postId}_${comment.user.username}_${dateInt}_${randomNumber}"
        Log.d("mydebug", "newIdentifier: ${newIdentifier}")

        val ref = database.collection("users").document(owner)
            .collection("posts").document(postId).collection("comments").document(newIdentifier)
        ref.set(comment)
            .addOnCompleteListener{ res ->
                if(res.isSuccessful){
                    Log.d("mydebug", "createComment su")
                    callback(true)
                }
                else{
                    Log.d("mydebug", "createComment fail")
                    callback(false)
                }


            }

    }
}
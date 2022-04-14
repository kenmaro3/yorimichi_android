package com.agrimetal.yorimichi2.Adaptor

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.agrimetal.yorimichi2.CommentsActivity
import com.agrimetal.yorimichi2.Manager.*
import com.agrimetal.yorimichi2.MapActivity
import com.agrimetal.yorimichi2.Model.Post
import com.agrimetal.yorimichi2.Model.PostComment
import com.agrimetal.yorimichi2.ProfileActivity
import com.agrimetal.yorimichi2.R
import com.bumptech.glide.Glide
import com.khoiron.actionsheets.ActionSheet
import com.khoiron.actionsheets.callback.ActionSheetCallBack
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class PostNewAdapter
    (private val mContext: Context, private val mPost: List<Post>) :
    RecyclerView.Adapter<PostNewAdapter.ViewHolder>()
{
    lateinit var actionSheetData: ArrayList<String>

    inner class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        //header
        var profileImage: CircleImageView
        var userName: TextView
        var postOption: ImageView

        //image
        var postImage: ImageView

        //button
        var likeButton: ImageView
        var infoButton: ImageView
        var shareButton: ImageView

        //count
        var likeCount: TextView
        var infoCount: TextView

        //text
        var caption: TextView
        var locationTitle: TextView
        var locationSubTitle: TextView
        var datetime: TextView

        var each_post_on_map_button: Button

        init{
            profileImage = itemView.findViewById(R.id.post_user_profile_image)
            userName = itemView.findViewById(R.id.post_user_profile_name)
            postOption = itemView.findViewById(R.id.post_more_button)

            //image
            postImage = itemView.findViewById(R.id.post_image)

            //button
            likeButton = itemView.findViewById(R.id.post_like_button)
            infoButton = itemView.findViewById(R.id.post_info_button)
            shareButton = itemView.findViewById(R.id.post_share_button)

            //count
            likeCount = itemView.findViewById(R.id.post_like_count)
            infoCount = itemView.findViewById(R.id.post_info_count)

            //text
            caption = itemView.findViewById(R.id.post_caption)
            locationTitle = itemView.findViewById(R.id.post_location_title)
            locationSubTitle = itemView.findViewById(R.id.post_location_subtitle)
            datetime = itemView.findViewById(R.id.post_datetime)

            each_post_on_map_button = itemView.findViewById(R.id.each_post_on_map_button)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.d("mydebug", "is this called1 ?")
        val view = LayoutInflater.from(mContext).inflate(R.layout.each_post, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Log.d("mydebug", "is this called2 ?")
        val post = mPost[position]

        val dataStore = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        val currentUsername = dataStore.getString("username", "")
        if (currentUsername == ""){
            return
        }

        holder.postOption.setOnClickListener{
            actionSheetData = arrayListOf()
            if(currentUsername == post.user.username){
                actionSheetData.add("投稿を削除する")


            }
            actionSheetData.add("投稿を通報する")

            ActionSheet(mContext, actionSheetData)
                .setTitle("投稿オプション")
                .setCancelTitle("キャンセル")
                .create(object : ActionSheetCallBack {
                    override fun data(data: String, position: Int) {
                        if ("投稿を削除する".equals(data)){
                            val progressDialog = ProgressDialog(mContext)
                            progressDialog.setTitle("投稿を削除しています。")
                            progressDialog.setMessage("少々お待ちください..")
                            progressDialog.setCanceledOnTouchOutside(false)
                            progressDialog.show()

                            val dispatchGroup = DispatchGroup()

                            val databaseManager = DatabaseManager()
                            val storageManager = StorageManager()

                            dispatchGroup.enter()
                            databaseManager.removePost(post=post, callback = {
                                if(!it){
                                    Log.d("deletedebug", "failed to delete post")

                                }
                                else{
                                    Log.d("deletedebug", "okay removePost")
                                }
                                dispatchGroup.leave()
                            })

                            dispatchGroup.enter()
                            databaseManager.removePostFromAll(post=post, callback = {
                                if(!it){
                                    Log.d("deletedebug", "failed to delete post all")

                                }
                                else{
                                    Log.d("deletedebug", "okay removePostForAll")
                                }
                                dispatchGroup.leave()
                            })

                            dispatchGroup.enter()
                            storageManager.deletePost(username=post.user.username, id=post.id, callback = {
                                if(!it){
                                    Log.d("deletedebug", "failed to delete post from storage")

                                }
                                else{
                                    Log.d("deletedebug", "okay deltePost")
                                }
                                dispatchGroup.leave()

                            })


                            dispatchGroup.notify {
                                progressDialog.dismiss()
                                Log.d("deletedebug", "notified")
                                val intent = Intent(mContext, ProfileActivity::class.java)
                                mContext.startActivity(intent)
                            }


                        }else if("投稿を通報する".equals(data)){
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://yorimichi-privacy-policy.webflow.io/"));
                            mContext.startActivity(intent)
                        }
                    }
                })
        }


        currentUsername ?: return // like guard let

        val storageManager = StorageManager()
        storageManager.profilePictureURL(username = post.user.username, callback = {
            //PicassoManager().picasso.load(it).fit().centerCrop().into(holder.profileImage)
            Glide.with(mContext).asBitmap().load(it).into(holder.profileImage)
        })

        //PicassoManager().picasso.load(post.postUrlString).into(holder.postImage)
        Glide.with(mContext).asBitmap().load(post.postUrlString).into(holder.postImage)
        holder.userName.text = post.user.username
        holder.caption.text = post.caption
        holder.locationTitle.text = post.locationTitle
        holder.locationSubTitle.text = post.locationSubTitle
        holder.datetime.text = post.postedDate

        configureLikes(post, holder.likeButton, holder.likeCount)
        configureComments(post, holder.infoCount)

        holder.postImage.setOnClickListener{
//            val editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
//            editor.putString("postId", post.id)
//            editor.putString("owner", post.user.username)
//            editor.apply()
//            Log.d("mydebug", "postImage tapped")
//            (mContext as FragmentActivity).getSupportFragmentManager()
//                .beginTransaction()
//                .replace(R.id.fragment_container, PostDetailsFragment())
//                .commit()
        }

        holder.userName.setOnClickListener {
//            (mContext as FragmentActivity).getSupportFragmentManager()
//                .beginTransaction()
//                .replace(R.id.fragment_container, ProfileFragment.newInstance(username = post.user.username))
//                .commit()
            val intent = Intent(mContext, ProfileActivity::class.java)
            intent.putExtra("username", post.user.username)
            mContext.startActivity(intent)

        }

        holder.each_post_on_map_button.setOnClickListener{
            val intent = Intent(mContext, MapActivity::class.java)
            intent.putExtra("postId", post.id)
            intent.putExtra("owner", post.user.username)
            mContext.startActivity(intent)

        }

        holder.shareButton.setOnClickListener{
            Log.d("sharedebug", "clicked")
            val shareText: String = "ヨリミチから投稿の共有をします。\n\n${post.locationTitle}:\n${post.caption}\n\n${post.locationSubTitle}"

            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, shareText)
                type = "text/plain"
            }

            val shareIntent = Intent.createChooser(sendIntent, null)
            mContext.startActivity(shareIntent)

//            val shareIntent: Intent = Intent().apply {
//                action = Intent.ACTION_SEND
//                putExtra(Intent.EXTRA_STREAM, post.postUrlString)
//                type = "image/jpeg"
//            }
//            mContext.startActivity(Intent.createChooser(shareIntent, "title"))
        }

        holder.infoButton.setOnClickListener{
            val intentComments = Intent(mContext, CommentsActivity::class.java)
            intentComments.putExtra("postId", post.id)
            intentComments.putExtra("postPublisher", post.user.username)
            mContext.startActivity(intentComments)
        }

        val dbManager = DatabaseManager()
        holder.likeButton.setOnClickListener{
            Log.d("mydebug", "likeButton clicked")
            if(holder.likeButton.tag == "Like"){
                dbManager.updateLike(currentUsername, DatabaseManager.LikeState.LIKE, post.id, post.user.username, callback = { res ->
                    Log.d("mydebug", "likeButton clicked, res: $res")
                    if(res){
                        dbManager.addYorimichiLikes(post, currentUsername, callback = {

                        })
                    }

                })
            }
            else if (holder.likeButton.tag == "Liked"){
                dbManager.updateLike(currentUsername, DatabaseManager.LikeState.UNLIKE, post.id, post.user.username, callback = { res ->
                    Log.d("mydebug", "likeButton released, res: ${res}")
                    if(res){
                        dbManager.removeYorimichiLikes(post, currentUsername, callback = {

                        })
                    }

                })

            }
            else{
                Log.w("mydebug", "likeButton exception")
            }
        }

    }


    private fun configureComments(post: Post, comments: TextView) {

        val dataStore = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        val currentUsername = dataStore.getString("username", "")
        if (currentUsername == ""){
            return
        }
        currentUsername ?: return // like guard let

        val dbManager = DatabaseManager()
        val database = dbManager.getInstance()
        database.collection("users").document(post.user.username).collection("posts").document(post.id)
            .collection("comments")
            .addSnapshotListener { snapshot, error ->
                if(error!=null){
                    return@addSnapshotListener
                }
                else{
                    Log.d("adapterdebug", "${snapshot!!.documents}")
                    val tmpComments = snapshot!!.toObjects(PostComment::class.java)
                    comments.text = "${tmpComments.size} Comments"

                }
            }
//        dbManager.getComments(postId = post.id, owner = post.user.username, callback = { postData ->
//           comments.text = "${postData.size} Comments"
//
//        })
    }

    private fun configureLikes(post: Post, likeButton: ImageView, likes: TextView) {
        val dbManager = DatabaseManager()
        val database = dbManager.getInstance()

        val dataStore = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        val currentUsername = dataStore.getString("username", "")
        if (currentUsername == ""){
            return
        }
        currentUsername ?: return // like guard let

        val ref = database.collection("users").document(post.user.username)
            .collection("posts").document(post.id)
        ref.addSnapshotListener{ postData, error ->
            if(error != null){
                Log.w("mydebug", "Listen failed.", error)
                return@addSnapshotListener
            }

            if(postData != null){
                val postObj = postData.toObject(Post::class.java)
                if(postObj!=null){
                    Log.d("mydebug", "likers: ${postObj!!}")
                    likes.text = "${postObj!!.likers.size} Likes"

                    if(postObj!!.likers.contains(currentUsername)){
                        likeButton.setImageResource(R.drawable.heart_clicked)
                        likeButton.tag = "Liked"
                    }
                    else{
                        likeButton.setImageResource(R.drawable.heart_not_clicked)
                        likeButton.tag = "Like"
                    }

                }
                else{

                }

            }
        }
    }

    override fun getItemCount(): Int {
        return mPost.size
    }

}

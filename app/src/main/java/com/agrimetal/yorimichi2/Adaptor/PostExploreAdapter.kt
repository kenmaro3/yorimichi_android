package com.agrimetal.yorimichi2.Adaptor

import android.content.Context
import android.content.Intent
import android.os.Binder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.agrimetal.yorimichi2.Manager.PicassoManager
import com.agrimetal.yorimichi2.Manager.StorageManager
import com.agrimetal.yorimichi2.Model.Post
import com.agrimetal.yorimichi2.PostDetailActivity
import com.agrimetal.yorimichi2.ProfileActivity
import com.agrimetal.yorimichi2.R
import com.bumptech.glide.Glide
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*


class PostExploreAdapter
    (private val mContext: Context, private val mPost: List<Post>) :
    RecyclerView.Adapter<PostExploreAdapter.ViewHolder>()
{

    // リスナー格納変数
    lateinit var listener: OnItemClickListener

    inner class MyBinder : Binder() {
        private
        fun getTime() = Date().time
    }

    inner class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView)
    //inner class ViewHolder(@NonNull itemView: View, private val binding: MyBinder) : RecyclerView.ViewHolder(binding.root)
    {
        var profileImage: CircleImageView
        var postImage: ImageView
        var userName: TextView
        var caption: TextView
        var locationTitle: TextView
        var user_profile_image_explore: CircleImageView

        init{
            profileImage = itemView.findViewById(R.id.user_profile_image_explore)
            postImage = itemView.findViewById(R.id.post_image_home_explore)
            userName = itemView.findViewById(R.id.user_name_explore)
            caption = itemView.findViewById(R.id.caption)
            locationTitle = itemView.findViewById(R.id.locationTitle)
            user_profile_image_explore = itemView.findViewById(R.id.user_profile_image_explore)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.d("mydebug", "is this called1 ?")
        val view = LayoutInflater.from(mContext).inflate(R.layout.posts_explore_layout, parent, false)
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
        currentUsername ?: return // like guard let

        val storageManager = StorageManager()
        storageManager.profilePictureURL(username = post.user.username, callback = {
            //val picassoManager = PicassoManager()
            //picassoManager.picasso.load(it).fit().centerCrop().into(holder.user_profile_image_explore)
            //Glide.with(activity=).asBitmap().load(urlString).into(iv_show_image)
            Glide.with(mContext).asBitmap().load(it).into(holder.user_profile_image_explore)
        })


        //PicassoManager().picasso.load(post.postUrlString).fit().centerCrop().into(holder.postImage)
        Glide.with(mContext).asBitmap().load(post.postUrlString).into(holder.postImage)
        holder.userName.text = post.user.username
        holder.caption.text = post.caption
        holder.locationTitle.text = post.locationTitle

        holder.itemView.setOnClickListener{
            // タップしたとき
            listener.onItemClickListener(it, position, post)

        }

        holder.postImage.setOnClickListener{
            Log.d("mydebugyo", "clicked")
            val intent = Intent(mContext, PostDetailActivity::class.java)
            intent.putExtra("owner", post.user.username)
            intent.putExtra("postId", post.id)
            mContext.startActivity(intent)

        }

        holder.userName.setOnClickListener{
            Log.d("mydebugyo", "clicked")
//            (mContext as FragmentActivity).getSupportFragmentManager()
//                .beginTransaction()
//                .replace(R.id.fragment_container, ProfileFragment.newInstance(username = post.user.username))
//                .commit()
            val intent = Intent(mContext, ProfileActivity::class.java)
            intent.putExtra("username", post.user.username)
            mContext.startActivity(intent)
        }

    }

    //インターフェースの作成
    interface OnItemClickListener{
        fun onItemClickListener(view: View, position: Int, post: Post)
    }

    // リスナー
    fun setOnItemClickListener(listener: OnItemClickListener){
        this.listener = listener
    }




    override fun getItemCount(): Int {
        return mPost.size
    }

}
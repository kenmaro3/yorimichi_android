package com.agrimetal.yorimichi2.Adaptor

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.agrimetal.yorimichi2.Manager.PicassoManager
import com.agrimetal.yorimichi2.Model.Post
import com.agrimetal.yorimichi2.R
import com.bumptech.glide.Glide
import com.squareup.picasso.Picasso

class PostOnMapAdapter
    (private val mContext: Context, private val mPost: List<Post>) :
    RecyclerView.Adapter<PostOnMapAdapter.ViewHolder>()
{
    inner class ViewHolder(@NonNull itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        var postImage: ImageView
        var caption: TextView
        var locationTitle: TextView

        init{
            postImage = itemView.findViewById(R.id.post_image_home_explore)
            caption = itemView.findViewById(R.id.caption)
            locationTitle = itemView.findViewById(R.id.locationTitle)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.d("mydebug", "is this called1 ?")
        val view = LayoutInflater.from(mContext).inflate(R.layout.posts_onmap_layout, parent, false)
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

//        val picassoManager = PicassoManager()
//        picassoManager.picasso.load(post.postUrlString).fit().centerCrop().into(holder.postImage)
        Glide.with(mContext).asBitmap().load(post.postUrlString).into(holder.postImage)
        holder.caption.text = post.caption
        holder.locationTitle.text = post.locationTitle

    }




    override fun getItemCount(): Int {
        return mPost.size
    }

}

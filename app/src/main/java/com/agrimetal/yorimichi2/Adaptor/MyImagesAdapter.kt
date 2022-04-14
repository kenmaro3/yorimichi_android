package com.agrimetal.yorimichi2.Adaptor

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.agrimetal.yorimichi2.Manager.PicassoManager
import com.agrimetal.yorimichi2.Model.Post
import com.agrimetal.yorimichi2.PostDetailActivity
import com.agrimetal.yorimichi2.R
import com.bumptech.glide.Glide
import com.squareup.picasso.Picasso

class MyImagesAdapter(private val mContext: Context, mPost: List<Post>)
    :RecyclerView.Adapter<MyImagesAdapter.ViewHolder?>()
{
    private var mPost: List<Post>? = null

    init {
        this.mPost = mPost
    }

        inner class ViewHolder(@NonNull itemView: View): RecyclerView.ViewHolder(itemView){
            var postImage: ImageView

            init {
                postImage = itemView.findViewById(R.id.post_image)
            }


        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.images_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val post: Post = mPost!![position]
        Log.d("pathdebug", "${post.postUrlString}")
        if (post.postUrlString.isEmpty()) {
        } else{
            //val picassoManager = PicassoManager()
            //picassoManager.picasso.load(post.postUrlString).into(holder.postImage)
            Glide.with(mContext).asBitmap().load(post.postUrlString).into(holder.postImage)
        }
        holder.postImage.setOnClickListener{
//            val editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit()
//            editor.putString("postId", post.id)
//            editor.putString("owner", post.user.username)
//            editor.apply()
//            (mContext as FragmentActivity).getSupportFragmentManager()
//                .beginTransaction()
//                .replace(R.id.fragment_container, PostDetailsFragment.newInstance(postId = post.id, owner=post.user.username))
//                .commit()

            val intent = Intent(mContext, PostDetailActivity::class.java)
            intent.putExtra("postId", post.id)
            intent.putExtra("owner", post.user.username)
            mContext.startActivity(intent)

        }
    }

    override fun getItemCount(): Int {
        return mPost!!.size
    }
}
package com.agrimetal.yorimichi2.Adaptor

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.agrimetal.yorimichi2.Manager.PicassoManager
import com.agrimetal.yorimichi2.Manager.StorageManager
import com.agrimetal.yorimichi2.Model.PostComment
import com.agrimetal.yorimichi2.R
import com.bumptech.glide.Glide
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class CommentAdapter(private val mContext: Context,
                     private val mComment: MutableList<PostComment>?
): RecyclerView.Adapter<CommentAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentAdapter.ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.comments_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentAdapter.ViewHolder, position: Int) {
        val comment = mComment!![position]
        holder.commentTV.text = comment.text
        holder.userNameTV.text = comment.user.username

        val storageManager = StorageManager()
        storageManager.profilePictureURL(username = comment.user.username, callback = { uri ->
//            val picassoManager = PicassoManager()
//            picassoManager.picasso.load(uri).fit().centerCrop().into(holder.imageProfile)
            Glide.with(mContext).asBitmap().load(uri).into(holder.imageProfile)

        })

    }

    override fun getItemCount(): Int {
        return mComment!!.size
    }


    inner class ViewHolder(@NonNull itemView: View): RecyclerView.ViewHolder(itemView){
        var imageProfile: CircleImageView
        var userNameTV: TextView
        var commentTV: TextView

        init{
            imageProfile = itemView.findViewById(R.id.user_profile_image_comment)
            userNameTV = itemView.findViewById(R.id.user_name_comment)
            commentTV = itemView.findViewById(R.id.comment_comment)
        }
    }

}
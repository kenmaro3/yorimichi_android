package com.agrimetal.yorimichi2.Adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import com.agrimetal.yorimichi2.Manager.DatabaseManager
import com.agrimetal.yorimichi2.Manager.PicassoManager
import com.agrimetal.yorimichi2.Manager.StorageManager
import com.agrimetal.yorimichi2.Model.User
import com.agrimetal.yorimichi2.ProfileActivity
import com.agrimetal.yorimichi2.R
import com.bumptech.glide.Glide
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class UserAdapter(private var mContext: Context,
                  private var mUser: List<User>,
                  private var isFragment: Boolean = false

                  ) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserAdapter.ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.user_item_layout, parent, false)
        return UserAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserAdapter.ViewHolder, position: Int) {
        val user = mUser[position]
        holder.userNameTextView.text = user.username

        holder.itemView.setOnClickListener(View.OnClickListener {
            val intent = Intent(mContext, ProfileActivity::class.java)
            intent.putExtra("username", user.username)
            mContext.startActivity(intent)

        })

        val storageManager = StorageManager()
        storageManager.profilePictureURL(username=user.username, callback = { uri ->
//           val picassoManager = PicassoManager()
//           picassoManager.picasso.load(uri).fit().centerCrop().into(holder.userProfileImage)
            Glide.with(mContext).asBitmap().load(uri).into(holder.userProfileImage)


        })

        val dbManager = DatabaseManager()
        dbManager.isFollowing(context = mContext, targetUsername = user.username, callback = { isFollowing ->
            if(isFollowing){
                holder.followButton.setText("フォロー解除")
            }
            else{
                holder.followButton.setText("フォロー")
            }

            holder.followButton.setOnClickListener {
                val dbManager = DatabaseManager()
                if (holder.followButton.text.toString() == "Follow"){
                    Log.d("mydebug", "follow button pressed")
                    dbManager.updateRelationship(mContext, state = DatabaseManager.RelationshipState.FOLLOW, targetUsername = user.username, callback = { res ->
                        Log.d("mydebug", "follow: ${res}")
                    })
                    holder.followButton.setText("UnFollow")

                }else{
                    Log.d("mydebug", "unfollow button pressed")
                    dbManager.updateRelationship(mContext, state = DatabaseManager.RelationshipState.UNFOLLOW, targetUsername = user.username, callback = { res ->
                        Log.d("mydebug", "unfollow: ${res}")
                    })
                    holder.followButton.setText("Fllow")

                }
            }

        })
        //holder.userFullnameTextView.text = user.getFullname()
        //Picasso.get().load(user.getImage()).placeholoder(R.drawable.profile).into(holder.userProfileImage)
        //Picasso.get().load(user.getImage()).into(holder.userProfileImage)
    }

    override fun getItemCount(): Int {
        return mUser.size
    }


    class ViewHolder (@NonNull itemView: View) : RecyclerView.ViewHolder(itemView){
        var userNameTextView: TextView = itemView.findViewById(R.id.user_name_search)
        var userProfileImage: CircleImageView = itemView.findViewById(R.id.user_profile_image_search)
        var followButton: Button = itemView.findViewById(R.id.follow_button_search)

    }

}
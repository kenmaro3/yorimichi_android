package com.agrimetal.yorimichi2.Model

data class UserInfo(
    private var bio: String = "",
    private var twitterId: String? = null,
    private var instagramId: String? = null


) {

    fun getBio(): String{
        return bio
    }
    fun getTwitterId(): String?{
        return twitterId
    }

    fun getInstagramId(): String?{
        return instagramId
    }

    fun setBio(bio: String){
        this.bio = bio
    }

    fun setTwitterId(twitterId: String?){
        this.twitterId = twitterId

    }

    fun setInstagramId(instagramId: String?){
        this.instagramId = instagramId

    }


}
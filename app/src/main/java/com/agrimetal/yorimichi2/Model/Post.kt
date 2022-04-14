package com.agrimetal.yorimichi2.Model

import com.google.firebase.firestore.PropertyName

data class Post(
    public val id: String = "",
    public var caption: String = "",
    public var locationTitle: String = "",
    public var locationSubTitle: String = "",
    public var location: Location = Location(lat=0.0, lng=0.0),
    public var postedDate: String = "",
    public var likers: ArrayList<String> = arrayListOf<String>(),
    public var yorimichi: ArrayList<String> = arrayListOf<String>(),
    public var postUrlString: String = "",
    public var postThumbnailUrlString: String = "",
    //public var genre: GenreInfo = GenreInfo(code="", type=GenreType.food),
    public var genre: HashMap<String, Any> = hashMapOf<String, Any>("food" to ""),
    public var user: User = User(username="", email=""),
    @set:PropertyName("isVideo")
    @get:PropertyName("isVideo")
    public var isVideo: Boolean = false
) {

}
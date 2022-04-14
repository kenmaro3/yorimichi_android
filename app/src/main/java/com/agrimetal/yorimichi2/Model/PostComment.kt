package com.agrimetal.yorimichi2.Model

//data class PostCommentType(
//    public val type: String = ""
//){
//
//}

//data class PostCommentType(
//    public val menu: Map<Any, Any>?=null,
//    public val price: Map<Any, Any>?=null,
//    public val time: Map<Any, Any>?=null
//)

//enum class PostCommentType{
//    menu, price, time
//}

data class PostCommentType(
//    public val type: HashMap<String, Any>? = hashMapOf("price" to "")
    public val type: HashMap<String, Any>? = null
){}


data class PostComment(
    public val text: String = "",
    public val user: User = User(username = "", email = ""),
    public val date: Double = 0.0,
    public val type: HashMap<String, Any>?=null,

) {
}
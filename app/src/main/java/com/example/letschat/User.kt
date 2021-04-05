package com.example.letschat

data class User(
    val name: String,
    val imageUrl: String,
    val thumbImage: String,
    val uid: String,
    val deviceToken: String,
    val status: String,
    val onlineStatus: String

) {

    /** Empty  [Constructor]  for Firebase **/
    constructor() : this("",  " " , " ", " ", " " , " " , "  ")

    constructor(name : String, imageUrl: String, thumbImage: String, uid: String) : this(
        name,  imageUrl,  thumbImage, uid, " " , "Hey there I am using whatsapp " , " ")
}
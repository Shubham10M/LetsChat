package com.example.letschat

import android.content.Context
import java.util.*

interface ChatEvent{
    val sendAt : Date
}
data class Message(
    val msg : String,
    val senderId : String,
    val msgId : String,
    val type : String = "Text",
    val status  : Int = 1,
    val liked : Boolean = false,
    override val sendAt : Date = Date()
) : ChatEvent{
    constructor():this("", "", "","", 1, false, Date())
}

data class DateHeader(
    override val sendAt : Date = Date(),
    val context : Context
) : ChatEvent{
    val date : String = sendAt.formatAsHeader(context)
}
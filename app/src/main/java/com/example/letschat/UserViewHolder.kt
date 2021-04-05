package com.example.letschat

import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso


class UserViewHolder(itemView : View):RecyclerView.ViewHolder(itemView){
    lateinit var countTv : TextView
    lateinit var timeTv : TextView
    lateinit var titleTv : TextView
    lateinit var subtitleTv : TextView
    lateinit var userImgView : com.google.android.material.imageview.ShapeableImageView


    fun bind(user: User , onClick:(name: String, photo : String, id : String) -> Unit) = with(itemView) {
        countTv = findViewById(R.id.countTv)
        timeTv = findViewById(R.id.timeTv)
        titleTv = findViewById(R.id.titleTv)
        subtitleTv = findViewById(R.id.subtitleTv)
        userImgView = findViewById(R.id.userImgView)

            countTv.isVisible = false
            timeTv.isVisible = false

            titleTv.text = user.name
            subtitleTv.text = user.status
            Picasso.get()
                .load(user.thumbImage)
                .placeholder(R.drawable.profile_pic)
                .error(R.drawable.profile_pic)
                .into(userImgView)

            setOnClickListener {
                onClick.invoke(user.name, user.thumbImage, user.uid)
            }
    }
}

//class EmptyViewHolder(view: View) : RecyclerView.ViewHolder(view)
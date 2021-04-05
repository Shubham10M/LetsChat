package com.example.letschat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.list_item_chat_recv_message.view.*
import kotlinx.android.synthetic.main.list_item_chat_sent_message.view.*
import kotlinx.android.synthetic.main.list_item_chat_sent_message.view.highFiveImg
import kotlinx.android.synthetic.main.list_item_date_header.view.*


class ChatAdapter (private val list : MutableList<ChatEvent>, private val nCurrenUid : String) :
      RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var highFiveClick: ((id: String, status: Boolean) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflate = { layout: Int ->
            LayoutInflater.from(parent.context)
                    .inflate(layout, parent, false)
        }
        return when (viewType) {
            TEXT_MESSAGE_RECEIVED -> {
                MessageViewHolder(
                        inflate(R.layout.list_item_chat_recv_message)
                )
            }
            TEXT_MESSAGE_SENT -> {
                MessageViewHolder(
                        inflate(R.layout.list_item_chat_sent_message)
                )
            }
            DATE_HEADER -> {
                DateViewHolder(
                        inflate(R.layout.list_item_date_header)
                )
            }
            else -> {
                MessageViewHolder(
                        inflate(R.layout.list_item_chat_recv_message)
                )
            }
        }
    }

    override fun getItemViewType(position: Int): Int {

        return when (val event = list[position]) {
            is Message -> {
                if (event.senderId == nCurrenUid) {
                    TEXT_MESSAGE_SENT
                } else {
                    TEXT_MESSAGE_RECEIVED
                }
            }
            is DateHeader -> DATE_HEADER
            else -> UNSUPPORTED
        }
    }
    override fun getItemCount(): Int = list.size
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {


        when (val item = list[position]) {
            is DateHeader -> {

                holder.itemView.textView.text = item.date
            }
            is Message -> {
                holder.itemView.apply {
                   // holder.itemView.content = item.msg
                    holder.itemView.time.text = item.sendAt.formatAsTime()
                    when (getItemViewType(position)) {
                        TEXT_MESSAGE_RECEIVED -> {
   //                          var messageCardView : com.google.android.material.card.MaterialCardView
//                            lateinit var highFiveImg : ImageView
                            holder.itemView.messageCardView.setOnClickListener(object :
                                    DoubleClickListener() {
                                override fun onDoubleClick(v: View?) {
                                    highFiveClick?.invoke(item.msgId, !item.liked)
                                }
                            })
                            holder.itemView.highFiveImg.apply {
                                isVisible = position == itemCount - 1 || item.liked
                                isSelected = item.liked
                                setOnClickListener {
                                    highFiveClick?.invoke(item.msgId, !isSelected)
                                }
                            }
                        }
                        TEXT_MESSAGE_SENT -> {
                            holder.itemView.highFiveImg.apply {
                                isVisible = item.liked
                            }
                        }
                    }
                }
            }
        }
    }





    class DateViewHolder(view : View):RecyclerView.ViewHolder(view)
    class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view)
    // companion oblects are ther static variables of this class
    companion object {
        private const val UNSUPPORTED = -1
        private const val TEXT_MESSAGE_RECEIVED = 0
        private const val TEXT_MESSAGE_SENT = 1
        private const val DATE_HEADER = 2
    }
}

abstract class DoubleClickListener : View.OnClickListener {
    var lastClickTime: Long = 0
    override fun onClick(v: View?) {
        val clickTime = System.currentTimeMillis()
        if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELTA) {
            onDoubleClick(v)
            lastClickTime = 0
        }
        lastClickTime = clickTime
    }

    abstract fun onDoubleClick(v: View?)
    companion object {
        private const val DOUBLE_CLICK_TIME_DELTA: Long = 300 //milliseconds
    }
}

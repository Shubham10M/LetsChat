package com.example.letschat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.EmojiPopup
import com.vanniktech.emoji.google.GoogleEmojiProvider
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

const val UID = "uid"
const val NAME = "name"
const val IMAGE = "photo"

class ChatActivity : AppCompatActivity() {
    private val friendId by lazy {
         intent.getStringExtra(UID)
    }
    private val name by lazy {
        intent.getStringExtra(NAME)
    }

    private val image by lazy {
        intent.getStringExtra(IMAGE)
    }
    private val nCurrentUid by lazy{
        FirebaseAuth.getInstance().uid!!
    }
    private val db by lazy{
     FirebaseDatabase.getInstance()
    }
//    lateinit var nameTv : TextView
//    lateinit var userImgView  : ImageView
//    lateinit var sendBtn : ImageView
//    lateinit var msgEdtv : EditText
//    lateinit var msgRv : RecyclerView
   // lateinit var textView : TextView

//       lateinit var currentUser: User
      private var messages = mutableListOf<ChatEvent>()
    lateinit var chatAdapter: ChatAdapter
    private lateinit var keyboardVisibilityHelper: KeyboardVisibilityUtils
    private val mutableItems: MutableList<ChatEvent> = mutableListOf()
    private val mLinearLayout: LinearLayoutManager by lazy { LinearLayoutManager(this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EmojiManager.install(GoogleEmojiProvider())
        setContentView(R.layout.activity_chat)
        keyboardVisibilityHelper = KeyboardVisibilityUtils(rootView) {
            msgRv.scrollToPosition(mutableItems.size - 1)
        }
//        FirebaseFirestore.getInstance().collection("users").document(nCurrentUid).get()
//            .addOnSuccessListener {
//                currentUser == it.toObject(User::class.java)!!
//            }
//        //currentUser = findViewById(R.id.currentUser)
//        nameTv = findViewById(R.id.nameTv)
//        userImgView = findViewById(R.id.userImgView)
//        sendBtn = findViewById(R.id.sendBtn)
//        msgEdtv = findViewById(R.id.msgEdtv)
//       // textView = findViewById(R.id.textView)
//        msgRv = findViewById(R.id.msgRv)

        chatAdapter = ChatAdapter(messages, nCurrentUid)
        msgRv.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity)
            adapter = chatAdapter
        }
        nameTv.text = name
        Picasso.get().load(image).into(userImgView)
        val emojiPopup = EmojiPopup.Builder.fromRootView(rootView).build(msgEdtv)
        smileBtn.setOnClickListener {
            emojiPopup.toggle()
        }
        swipeToLoad.setOnRefreshListener{
            val workerScope = CoroutineScope(Dispatchers.Main)
             workerScope.launch{
                 delay(2000)
                 swipeToLoad.isRefreshing = false
             }
        }
        sendBtn.setOnClickListener {
            msgEdtv.text?.let {
                if(it.isNotEmpty()){
                    sendMessage(it.toString())
                    it.clear()
                }
            }
        }
        ListenToMessage(){ msg, update ->
            if (update as Boolean) {
                updateMessage(msg as Message)
            } else {
                addMessage(msg as Message, true)
            }

        }
        chatAdapter.highFiveClick = { id, status ->
            updateHighFive(id, status)
        }
    }

    private fun updateHighFive(id: String, status: Boolean) {
        friendId?.let { getMessages(it).child(id).updateChildren(mapOf("liked" to status)) }
    }

    private fun addMessage(event: Message, b: Boolean) {
        val eventBefore = messages.lastOrNull()
        // Add date header if it's a different day
        if((eventBefore != null && !eventBefore.sendAt.isSameDayAs(event.sendAt)) || eventBefore == null){
            messages.add(
                DateHeader(
                    event.sendAt, context = this
                )
            )
        }

        messages.add(event)
        chatAdapter.notifyItemInserted(messages.size)
        msgRv.scrollToPosition(messages.size + 1)

    }

    private fun updateMessage(msg: Message){
        val position = mutableItems.indexOfFirst {
            when (it) {
                is Message -> it.msgId == msg.msgId
                else -> false
            }
        }
        mutableItems[position] = msg

        chatAdapter.notifyItemChanged(position)
    }

    private fun ListenToMessage(param: (Any, Any) -> Unit) {
           friendId?.let {
               getMessages(it)
                       .orderByKey().addChildEventListener(object : ChildEventListener{
                           override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                               val msg = snapshot.getValue(Message::class.java)!!
                               addMessage(msg, true)
                           }

                           override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

                           }

                           override fun onChildRemoved(snapshot: DataSnapshot) {
                               TODO("Not yet implemented")
                           }

                           override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                               TODO("Not yet implemented")
                           }

                           override fun onCancelled(error: DatabaseError) {
                               TODO("Not yet implemented")
                           }

                       })
           }
       }

    private fun sendMessage(msg: String) {
    val id = friendId?.let { getMessages(it).push().key }
        checkNotNull(id){"cannot be null"}
        val msgMap =  Message(msg, nCurrentUid, id)
        friendId?.let {
            getMessages(it).child(id).setValue(msgMap).addOnSuccessListener {
                Log.i("CHATS", "completed")
            }.addOnFailureListener {
                Log.i("CHATS", it.localizedMessage)
            }
        }
        updateLastMessage(msgMap)
    }

    private fun updateLastMessage(message: Message) {
        val inboxMap = Inbox(
                message.msg,
                friendId,
                name,
                image,
                count = 0
        )
        getInox(nCurrentUid , friendId).setValue(inboxMap).addOnSuccessListener {
            getInox(friendId, nCurrentUid).addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                     val value = snapshot.getValue(Inbox::class.java)
                    inboxMap.apply {
                        from = message.senderId
//                        name = currentUser.name
 //                       image = currentUser.thumbImage
                        count = 1
                    }
                    value?.let {
                        if(it.from == message.senderId){
                            inboxMap.count =  value.count+1
                        }
                    }
                    getInox(friendId, nCurrentUid).setValue(inboxMap)
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }


            })
        }

    }

    private fun markAsread(){
        getInox(friendId, nCurrentUid).child("count").setValue(0)
    }

    // this function will return a refernce of inbox
    private fun getInox(toUser: String?, fromUser: String?) =
        db.reference.child("chats/$toUser/$fromUser")

    private fun getMessages(friendId:String) =
        db.reference.child("messages/${getId(friendId)}")
    // ID for the messages
    private fun getId(friendId: String):String{
      return if(friendId  > nCurrentUid){
          nCurrentUid + friendId
      } else
      {
          friendId  + nCurrentUid
      }
    }
    override fun onResume() {
        super.onResume()
        rootView.viewTreeObserver
            .addOnGlobalLayoutListener(keyboardVisibilityHelper.visibilityListener)
    }
    override fun onPause() {
        super.onPause()
        rootView.viewTreeObserver
            .removeOnGlobalLayoutListener(keyboardVisibilityHelper.visibilityListener)
    }
    companion object {
        fun createChatActivity(context: Context, id: String, name: String, image: String): Intent {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra(UID, id)
            intent.putExtra(NAME, name)
            intent.putExtra(IMAGE, image)
            return intent
        }
    }
}
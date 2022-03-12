package com.example.messenger.messages

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.messenger.R
import com.example.messenger.databinding.ActivityChatLogBinding
import com.example.messenger.models.ChatMessage
import com.example.messenger.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import de.hdodenhof.circleimageview.CircleImageView

class ChatLogActivity : AppCompatActivity() {
    private val TAG = "ChatLogActivity"
    private lateinit var binding: ActivityChatLogBinding
    private lateinit var adapter: GroupieAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private var toUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatLogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        toUser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        supportActionBar?.title = toUser?.username

        listenForMessages()

        binding.sendButtonChatLog.setOnClickListener {
            Log.d(TAG, "Attempt to send message")
            performSendMessage()
        }
    }

    private fun performSendMessage() {
        val text = binding.edittextChatLog.text.toString()

        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val toId = user?.uid

        val reference = FirebaseDatabase.getInstance()
            .getReference("/user-messages/$fromId/$toId").push()
        if (reference.key == null || fromId == null || toId == null) return

        val toReference = FirebaseDatabase.getInstance()
            .getReference("/user-messages/$toId/$fromId").push()
        val chatMessage = ChatMessage(reference.key!!, text,
            fromId, toId, System.currentTimeMillis() / 1000)
        reference.setValue(chatMessage).addOnSuccessListener {
            Log.d(TAG, "Saved our chat message: ${reference.key}")
            binding.edittextChatLog.text.clear()
            binding.recyclerviewChatLog.scrollToPosition(adapter.itemCount-1)
        }
        toReference.setValue(chatMessage)

        val latestMessageRef = FirebaseDatabase.getInstance()
            .getReference("/latest-messages/$fromId/$toId")
        latestMessageRef.setValue(chatMessage)

        val latestMessageToRef = FirebaseDatabase.getInstance()
            .getReference("/latest-messages/$toId/$fromId")
        latestMessageToRef.setValue(chatMessage)
    }

    private fun listenForMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser?.uid
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")

        layoutManager = LinearLayoutManager(applicationContext).apply {  }
        adapter = GroupieAdapter().apply {
//            setOnItemClickListener(onItemClickListener)
//            setOnItemLongClickListener(onItemLongClickListener)
//            spanCount = 12
        }


        ref.addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java)
                if (chatMessage != null) {
                    Log.d(TAG, chatMessage.text)

                    if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
                        val currentUser = LatestMessagesActivity.currentUser ?: return
                        adapter.add(ChatFromItem(chatMessage.text, currentUser))
                    } else {
                        if (toUser != null) {
                            adapter.add(ChatToItem(chatMessage.text, toUser!!))
                        }
                    }
                }

            }

            override fun onCancelled(error: DatabaseError) { }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) { }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) { }

            override fun onChildRemoved(snapshot: DataSnapshot) { }
        })

        binding.recyclerviewChatLog.also {
            it.layoutManager = layoutManager
            it.adapter = adapter
        }
    }
}

class ChatFromItem(val text: String, private val user: User): Item<GroupieViewHolder>() {

    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.textview_from_row).text = text

        val uri = user.profileImageUrl
        Picasso.get().load(uri).into(viewHolder.itemView.findViewById<CircleImageView>(
            R.id.imageview_from_row))

    }
}

class ChatToItem(val text: String, private val user: User): Item<GroupieViewHolder>() {

    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.textview_to_row).text = text

        // load out user image into the star
        val uri = user.profileImageUrl
        val targetImageView = viewHolder.itemView
            .findViewById<CircleImageView>(R.id.imageview_to_row)
        Picasso.get().load(uri).into(targetImageView)

    }
}
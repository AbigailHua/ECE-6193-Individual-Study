package com.example.messenger.views

import android.widget.ImageView
import android.widget.TextView
import com.example.messenger.R
import com.example.messenger.models.ChatMessage
import com.example.messenger.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item

class LatestMessageRow(private val chatMessage: ChatMessage): Item<GroupieViewHolder>() {
    var chatPartnerUser: User? = null
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.message_textview_latest_message)
            .text = chatMessage.text
        val chatPartnerId: String = if (chatMessage.fromId == FirebaseAuth.getInstance().uid) {
            chatMessage.toId
        } else {
            chatMessage.fromId
        }

        val ref = FirebaseDatabase.getInstance().getReference("/users/$chatPartnerId")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                chatPartnerUser = snapshot.getValue(User::class.java)
                viewHolder.itemView.findViewById<TextView>(R.id.username_textview_latest_message)
                    .text = chatPartnerUser?.username
                Picasso.get().load(chatPartnerUser?.profileImageUrl).into(
                    viewHolder.itemView.findViewById<ImageView>(R.id.imageview_latest_message))
            }

            override fun onCancelled(error: DatabaseError) { }
        })
    }

    override fun getLayout(): Int {
        return R.layout.latest_message_row
    }

}

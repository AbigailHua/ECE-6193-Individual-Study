package com.example.messenger.messages

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.messenger.R
import com.example.messenger.models.User
import com.example.messenger.databinding.ActivityNewMessageBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import com.xwray.groupie.OnItemClickListener
import de.hdodenhof.circleimageview.CircleImageView


class NewMessageActivity : AppCompatActivity() {
    private val TAG = "NewMessageActivity"
    private lateinit var binding: ActivityNewMessageBinding
    private lateinit var adapter: GroupieAdapter
    private lateinit var layoutManager: LinearLayoutManager

    companion object {
        val USER_KEY = "USER_KEY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.title = "Select User"

        fetchUsers()
    }

    private fun fetchUsers() {
        val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                layoutManager = LinearLayoutManager(applicationContext).apply {  }
                adapter = GroupieAdapter().apply {
                    setOnItemClickListener(onItemClickListener)
//            setOnItemLongClickListener(onItemLongClickListener)
//            spanCount = 12
                }
                snapshot.children.forEach{
                    Log.d(TAG, it.toString())
                    val user = it.getValue(User::class.java)
                    if (user != null) {
                        adapter.add(UserItem(user))

                    }
                }

                binding.recyclerviewNewMessage.also {
                    it.layoutManager = layoutManager
                    it.adapter = adapter
                }
            }

            override fun onCancelled(error: DatabaseError) { }

        })
    }

    private val onItemClickListener = OnItemClickListener { item, view ->
        val userItem = item as UserItem
        val intent = Intent(view.context, ChatLogActivity::class.java)
        intent.putExtra(USER_KEY, userItem.user)
        startActivity(intent)
        finish()
    }
}

class UserItem(val user: User): Item<GroupieViewHolder>() {
    override fun getLayout(): Int {
        return R.layout.user_row_new_message
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.findViewById<TextView>(R.id.username_textview_new_message)
            .text = user.username
        Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView
            .findViewById<CircleImageView>(R.id.imageview_new_message))

    }
}
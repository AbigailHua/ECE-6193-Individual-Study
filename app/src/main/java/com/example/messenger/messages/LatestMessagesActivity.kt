package com.example.messenger.messages

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.messenger.R
import com.example.messenger.databinding.ActivityLatestMessagesBinding
import com.example.messenger.models.ChatMessage
import com.example.messenger.models.User
import com.example.messenger.registerlogin.RegisterActivity
import com.example.messenger.views.LatestMessageRow
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupieAdapter
import com.xwray.groupie.OnItemClickListener

class LatestMessagesActivity : AppCompatActivity() {
    private val TAG = "LastestMessagesActivity"
    private lateinit var binding: ActivityLatestMessagesBinding
    private lateinit var adapter: GroupieAdapter
    private lateinit var layoutManager: LinearLayoutManager
    val latestMessagesMap = HashMap<String, ChatMessage>()

    companion object {
        var currentUser: User? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLatestMessagesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.recyclerviewLatestMessage
            .addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        listenForLatestMessages()
        fetchCurrentUser()
        verifyUserIsLoggedIn()
    }

    private fun refreshRecyclerViewMessages() {
        adapter.clear()
        latestMessagesMap.values.forEach {
            adapter.add(LatestMessageRow(it))
        }
    }

    private fun listenForLatestMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId")
        ref.addChildEventListener(object: ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java) ?: return
                latestMessagesMap[snapshot.key!!] = chatMessage
                refreshRecyclerViewMessages()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java) ?: return
                latestMessagesMap[snapshot.key!!] = chatMessage
                refreshRecyclerViewMessages()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) { }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) { }

            override fun onCancelled(error: DatabaseError) { }

        })

        layoutManager = LinearLayoutManager(applicationContext).apply {  }
        adapter = GroupieAdapter().apply {
            setOnItemClickListener(onItemClickListener)
//            setOnItemLongClickListener(onItemLongClickListener)
//            spanCount = 12
        }

        binding.recyclerviewLatestMessage.also {
            it.layoutManager = layoutManager
            it.adapter = adapter
        }

        binding.recyclerviewLatestMessage.scrollToPosition(adapter.itemCount - 1)
    }

    private val onItemClickListener = OnItemClickListener { item, view ->
        val row = item as LatestMessageRow
        val intent = Intent(view.context, ChatLogActivity::class.java)
        intent.putExtra(NewMessageActivity.USER_KEY, row.chatPartnerUser)
        startActivity(intent)
    }

    private fun fetchCurrentUser() {
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) { }

            override fun onDataChange(snapshot: DataSnapshot) {
                currentUser = snapshot.getValue(User::class.java)
                Log.d(TAG, "Current user ${currentUser?.username}")
            }
        })
    }

    private fun verifyUserIsLoggedIn() {
        val uid = FirebaseAuth.getInstance().uid
        if (uid == null) {
            val intent = Intent(this, RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_new_message -> {
                val intent = Intent(this, NewMessageActivity::class.java)
                startActivity(intent)
            }
            R.id.menu_sign_out -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, RegisterActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }
}
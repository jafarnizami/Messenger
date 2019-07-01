package com.example.messenger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_latest_message.*
import kotlinx.android.synthetic.main.latest_message_row.view.*

class LatestMessageActivity : AppCompatActivity() {

    companion object{
        var currentUser:User?=null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_message)
        recycerview_latest_messages.adapter=adapter
        recycerview_latest_messages.addItemDecoration(DividerItemDecoration(this,DividerItemDecoration.VERTICAL))

        //adding item click listener on the adapter
        adapter.setOnItemClickListener { item, view ->
            val intent=Intent(this,ChatLogActivity::class.java)
            val row = item as LatestMessageRow
            intent.putExtra(NewMessageActivity.USER_KEY,row.chatPartnerUser)
            startActivity(intent)
        }

      //  setupDummyrows()
        listenforlatestmessages()
        verifyUserIsLoggedin()
        fetchCurrentUser()// to fetch the data of the current user basically to display the image of the user
    }

    class LatestMessageRow(val chatMessage:ChatLogActivity.ChatMessage):Item<ViewHolder>()
    {
        var chatPartnerUser:User?=null
        override fun bind(viewHolder: ViewHolder, position: Int) {
            viewHolder.itemView.textview_latestmsg_row.text=chatMessage.text

            val chatPartnerId:String
            if (chatMessage.fromId==FirebaseAuth.getInstance().uid)
            {
                chatPartnerId=chatMessage.toId
            }
            else{
                chatPartnerId=chatMessage.fromId
            }
            val ref =FirebaseDatabase.getInstance().getReference("/users/$chatPartnerId")
            ref.addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onDataChange(p0: DataSnapshot) {
                    chatPartnerUser= p0.getValue(User::class.java)
                    viewHolder.itemView.textview_username_latestmessage_row.text=chatPartnerUser?.username

                    val targetImageView=viewHolder.itemView.imageview_latest_msg_row
                    Picasso.get().load(chatPartnerUser?.profileimageurl).into(targetImageView)

                }

                override fun onCancelled(p0: DatabaseError) {

                }
            })

        }

        override fun getLayout(): Int {
            return R.layout.latest_message_row
        }
    }

    val latestMessagesMap=HashMap<String,ChatLogActivity.ChatMessage>()

    private fun refreshRecyclerview()
    {
        adapter.clear()
        latestMessagesMap.values.forEach{
            adapter.add(LatestMessageRow(it))
        }
    }

    private fun listenforlatestmessages()
    {
        val fromId =FirebaseAuth.getInstance().uid
        val ref =FirebaseDatabase.getInstance().getReference("latest-messages/$fromId")
        ref.addChildEventListener(object :ChildEventListener{
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage=p0.getValue(ChatLogActivity.ChatMessage::class.java) ?:return

                latestMessagesMap[p0.key!!]= chatMessage
                refreshRecyclerview()// functio refreshes our recyler view

            }

            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val chatMessage=p0.getValue(ChatLogActivity.ChatMessage::class.java) ?:return

                latestMessagesMap[p0.key!!]= chatMessage
                refreshRecyclerview()// functio refreshes our recyler view
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }

            override fun onChildRemoved(p0: DataSnapshot) {

            }
        })
    }

    val adapter=GroupAdapter<ViewHolder>()



    private fun fetchCurrentUser()

    {
        val uid =FirebaseAuth.getInstance().uid
        val ref=FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                currentUser=p0.getValue(User::class.java)
            }

            override fun onCancelled(p0: DatabaseError) {
            }
        })
    }

    private fun verifyUserIsLoggedin(){
        val uid = FirebaseAuth.getInstance().uid

        if (uid==null)
        {
            val intent = Intent(this,MainActivity::class.java)
            intent.flags=Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }


    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){

            R.id.menu_sign_out->{
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this,MainActivity::class.java)
                intent.flags=Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                Toast.makeText(this,"SIGN OUT SUCCESSFUL",Toast.LENGTH_SHORT).show()

            }

            R.id.menu_new_message->
            {
                val intent=Intent(this,NewMessageActivity::class.java)
                startActivity(intent)

            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

}

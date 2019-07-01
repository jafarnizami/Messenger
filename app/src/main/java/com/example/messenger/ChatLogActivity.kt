package com.example.messenger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*

class ChatLogActivity : AppCompatActivity() {

    companion object{
        val TAG ="ChatLog"
    }
    val adapter=GroupAdapter<ViewHolder>()
    var toUser:User?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

       // supportActionBar?.title="Chat Log"

      //  val username =intent.getStringExtra(NewMessageActivity.USER_KEY) // USING THE KEY TO FETCH DATA FROM THE  NewMessageActivity.

        recyclerview_chat_log.adapter=adapter

        toUser= intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY) // GERTING DATA FROM THE MAIN NEW MESSAGE ACTIVITY (ALL DATA A-Z)

        supportActionBar?.title=toUser?.username

   //  setupDummyData()
        listenForMessages()

        send_button_chatlog.setOnClickListener {

            Log.d(TAG,"Attempt to send a message...")

            performSendMessage()
        }



    }
    class ChatMessage(val id:String,val text: String,val fromId:String,val toId:String,val timestamp:Long)// to tell the form of our data
    {
        constructor(): this("","","","",-1)
    }
    private fun listenForMessages()
    {
        val fromId =FirebaseAuth.getInstance().uid
        val toId =toUser?.uid
        val ref= FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")
            .addChildEventListener(object : ChildEventListener{
                override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                  val chatMessage=  p0.getValue(ChatMessage::class.java) // p0 has all the data which is in the form of class ChatMessage

                    if (chatMessage!=null) {
                        Log.d(TAG, chatMessage.text)

                        if (chatMessage.fromId==FirebaseAuth.getInstance().uid)
                        {
                            val currentUser=LatestMessageActivity.currentUser?:return
                            adapter.add(ChatFromItem(chatMessage.text,currentUser))
                        }
                        else{
                            adapter.add(ChatToItem(chatMessage.text,toUser!!))

                        }
                    }
                    recyclerview_chat_log.scrollToPosition(adapter.itemCount-1)

                }

                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onChildChanged(p0: DataSnapshot, p1: String?) {

                }

                override fun onChildMoved(p0: DataSnapshot, p1: String?) {

                }

                override fun onChildRemoved(p0: DataSnapshot) {

                }
            })
    }

    private fun performSendMessage(){
        //saving message inside the firebase

        val text=edittext_chat_log.text.toString()

        val fromId =FirebaseAuth.getInstance().uid
        val user= intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val toId=user.uid
        if (fromId==null)return

       // val reference =FirebaseDatabase.getInstance().getReference("/messages").push() // push will create a nide automatically
        val reference =FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()

        val toReference =FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()


        val  chatMessage=ChatMessage(reference.key!!,text,fromId,toId,System.currentTimeMillis()/1000) //ref.key-> new id generated from the push method
        reference.setValue(chatMessage)
            .addOnSuccessListener {
                Log.d(TAG,"saved our chat message ${reference.key}")
                edittext_chat_log.text.clear()
                recyclerview_chat_log.scrollToPosition(adapter.itemCount-1)
            }

        toReference.setValue(chatMessage)

        val latestMessageRef=FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")

        latestMessageRef.setValue(chatMessage)

        val latestMessageToRef=FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId")

        latestMessageToRef.setValue(chatMessage)


    }


}

//ITEM FOR CHAT ROW (FROM)
class ChatFromItem(val text:String, val user: User) :Item<ViewHolder>()
{
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.textview_from_row.text=text

        val uri = user.profileimageurl
        val targetimageview=viewHolder.itemView.imageview_from_row
        Picasso.get().load(uri).into(targetimageview)


    }
    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }
}

//ITEM FOR CHAT ROW(TO)
class ChatToItem (val text: String, val user:User):Item<ViewHolder>()
{
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.textview_to_row.text=text

        //load our userimage into the star
        val uri = user.profileimageurl
        val targetimageview=viewHolder.itemView.imageview_to_row
        Picasso.get().load(uri).into(targetimageview)

    }
    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }
}

package com.example.messenger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.user_row_new_message.view.*
import java.util.*

class NewMessageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)

        //TO ADD A BUTTON ON TH ACTIVIVITY PASTE THIS BETWEEN ACTIVITY <meta-data android:name="android.support.PARENT_ACTIVITY"
        //                                                               android:value=".LatestMessageActivity"/>

        supportActionBar?.title = "Select User" //TO ADD TITLE TO THE ACTION BAR

        //linearlayoutmanager defined in the xml file
        //dependeies of groupie and picasso

       // val adapter=GroupAdapter<ViewHolder>()

        //adapter.add(UserItem())
        //adapter.add(UserItem())
        //adapter.add(UserItem())


        //recyclerview_newmessage.adapter=adapter

        fetchusers()//fetching user data entered from our firebase

    }

    companion object{
        val USER_KEY ="USER_KEY"
    }
    private fun fetchusers()
    {
        val ref= FirebaseDatabase.getInstance().getReference("/users")
        ref.addListenerForSingleValueEvent(object :ValueEventListener{

            override fun onDataChange(p0: DataSnapshot) { //p0 has the data form the firebase
                val adapter=GroupAdapter<ViewHolder>()
                //will get called everytime we retrieve data from our database ,p0 contains all the data
                p0.children.forEach {
                    //for each loop runs for each children
                    Log.d("NewMessage",it.toString())

                    val user =it.getValue(User::class.java)//converting it to some user object so we can use it|||| loop is running for eah data snapshhot
                    // we are converting these it values in the form of the data stored in the User class , it fetvhes all the data that is present in the firebase
                    if (user!=null)
                    {
                        adapter.add(UserItem(user)) // here Useritem()class is being called!!!!!!
                    }
                }

                adapter.setOnItemClickListener{ item, view ->  //WHEN USER CLICKS ON EACH ELEMENT OF THE LIST THEN IT WILL TAKE USER TO Chatlog ACTIVITY

                    val userItem = item as UserItem //casting item into correct objects (basically making item in the form of data present in the UserItem object),BASICALLY WE WANT TO USE THE OBJECT OF THE CLASS Classitem

                    val intent=Intent(view.context,ChatLogActivity::class.java)


                    //intent.putExtra(USER_KEY,item.user.username) this line is only user to pass username
                    // the item we click on  from the list basically its data is stored in the item variable so we can put this item in the putextra and extract the data in our chatlog activity
                    //WE CAN PASS DATA IN THE FORM OF KEY VALUES PAIRS AND HERE WE ARE CREATING A KEY


                    intent.putExtra(USER_KEY,userItem.user) // make the user class present in the main activity to Parcelable to use this
                    //we can only pass parcelable objects in putextra so we make th class User parcelable by making some changes in the build.gradle
                    //parcelable is used to serialize(converting state of object into byte stream)the class
                    startActivity(intent)
                    finish() // we use finish so that when user press back button  it will take user to  New message activity instead of showilg the lists of the user
                }
                recyclerview_newmessage.adapter=adapter
            }
            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }
}
//class used for reclerview adapter
class UserItem (val user:User):Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
        viewHolder.itemView.username_textview_newmessage.text=user.username
        Picasso.get().load(user.profileimageurl).into(viewHolder.itemView.imageview_newmessage) // picasso allows you to load images
    }


    override fun getLayout(): Int {
        return R.layout.user_row_new_message
    }
}

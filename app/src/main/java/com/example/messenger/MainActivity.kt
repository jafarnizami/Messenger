package com.example.messenger

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)




        register_button_register.setOnClickListener {
            performregister() //FUNCTION TO REGISTER THE USER USING FIREBASE
        }



        already_accoubnt_textiew.setOnClickListener {

            val intent=Intent(this,LoginActivity::class.java)
            startActivity(intent)
        }

        // CODE TO SELECT AN IMAGE FROM PHONE AND DISPLAY
        select_photo_button_register.setOnClickListener {
            Log.d("Main Activity","Try to show the photo selected")

            val intent=Intent(Intent.ACTION_PICK)  // THIS CODE TELLS THE COMPILER THAT WHEN IMAGE BUTTON IS CLICKED THEN TO START AN ACTIVITY TO PICK IMAGE FROM THE PHONE
            intent.type="image/*"                    // 1)
            startActivityForResult(intent,0)
        }

    }

    var selectedPhotouri: Uri?=null // we habe to use this againa nd agian so we have to define it ourside our method.

    //TELLS WHICH PHOTO THE USER HAS SELECTED FROM THE SELECTOR

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode==0 && resultCode == Activity.RESULT_OK && data!=null)
        {
            Log.d("RegisterActivity","Photo was selected") //2)
            selectedPhotouri = data.data   //selectedphoturi TELLS WHERE THE IMAGE IS STORED ON THE DEVICE ,, data.data gives us the uri ,,data is the image that the user has selected
            //3))
            val bitmap=MediaStore.Images.Media.getBitmap(contentResolver,selectedPhotouri) //We have access to the bitmap of the photo that we selected ,, basicalyy converting the image in bitmap format
            //bitmap is a file format that is used to create and display computer graphics


            //CIRCLE IMAGE VIEW WAY
            //  FOR CIRCLE VIEW WE CAN DIRECTLY SUSE BITMAP WHILE FOR ADDING IMAGE ON BUTTON WE HAVE TO CONVERT TO DRAWABLE
          select_imageview_register.setImageBitmap(bitmap)
            select_photo_button_register.alpha=0f // this line will disappear the button when the image is displayed
            //4))
            //SETTING THE IMAGE ON THE select_photo_button_register
            //  val bitmapDrawable=BitmapDrawable(bitmap)  // -->this basically creates a drawable of the image (which is in the form of bitmap)
          //  select_photo_button_register.setBackgroundDrawable(bitmapDrawable)--->//takes drawable and sets on the button , our drawable is the image that we have seleced from our phone

                // SO BASICALLY OUR GOAL IS TO MAKE AN DRAWABLE OF THE IMAGE THAT WE ARE IMPORTING FROM OUR GALLERY SO THAT WE CAN DISPLAY IT ANYWHERE IN OUR APP.

            //SUMMARY
            /* 1) MAKE AN INTENT THAT TAKES THE USESR TO GALLERY
            2) GET LOCATION OF THE SELECTED IMAGE(DATA) WHICH IS GIVEN BY URI
            3)MAKE THE SELECTED IMAGE AS BITMAP FILE FORMAT
            4)CONVERT BTMAP TO BITMAPDRAWABLE AND PUT THAT DRAWABLE ANYWHERE YOU WANT IN THE APP
             */
        }
    }

    private fun performregister()
    {
        val email = email_edittext_register.text.toString()
        val password=password_edittext_register.text.toString()
        Log.d("MainActivity","email:"+email)
        Log.d("MainActivity","password: $password")


        if (email.isEmpty()||password.isEmpty())
        {
            Toast.makeText(this,"EMPTY FIELD",Toast.LENGTH_SHORT).show()
            return
        }

        //Firebase Authentication to create username and password
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener {
                if (!it.isSuccessful) return@addOnCompleteListener
                else {
                    Log.d("MainActivity", "Successfully created user with uid:${it.result?.user?.uid}")
                    uploadImagetoFirebaseStrage() //FUNCTIOIN TO UPLOAD THE IMAGE TO THE FIREBASE STORAGE
                }

            }
            .addOnFailureListener{
                Log.d("MainActivity","Failed to create user:${it.message}")
                Toast.makeText(this,"Failed to enter data:${it.message}",Toast.LENGTH_SHORT).show()

            }
    }

    private fun uploadImagetoFirebaseStrage() //USING FIREBASE STORAGE
    {
        if (selectedPhotouri==null) {
            return
        }
        else {
            val filename = UUID.randomUUID().toString() //unique user id of the image just for checking purpose
            val ref = FirebaseStorage.getInstance().getReference("/images/$filename")// LOCATION WHERE OUR IMAGE WILL BE SAVED IN FIREBASE

            ref.putFile(selectedPhotouri!!)
                .addOnSuccessListener {
                    Log.d("MainActivity", "Successfully uploaded image:${it.metadata?.path}")


                    ref.downloadUrl.addOnSuccessListener {

                        Log.d("MainActivity", "File Location: $it") // gives url of the image in the logcat

                        saveUsertoFirebaseDatabase(it.toString())
                    }
                }.addOnFailureListener{
                    Log.d("Main Activity","Failed to upload the image${it.message}")
                }

        }

    }
    private fun saveUsertoFirebaseDatabase(profileimageurl: String)
    {
        //ALWAYS KEEP RUES IN FIREBASE DATABASE TO true,true otherwise there will be an exception thats says permission denied
        val uid= FirebaseAuth.getInstance().uid ?:""//UNIQUE USER IF FOR EVERY USER

        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")//OUR DATA WILL BE MADE IN A DATA TREE UNDER USERS

        val user =User(uid,userame_edittext_register.text.toString(),profileimageurl)
        ref.setValue(user) //SENDING THE VALUES IN FORM OF DATA PRESENT IN THE User Class.
            .addOnSuccessListener {
                Log.d("MainActivity","Successfully saved data to firebase")
                Toast.makeText(this,"REGISTRATION SUCCSESSFULL",Toast.LENGTH_SHORT).show()

                val intent=Intent(this,LatestMessageActivity::class.java)
                intent.flags=Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK) // when we click on back button it does not take us to the register actiivty instead it closes the app
                startActivity(intent)
            }
            .addOnFailureListener{
                Log.d("MainActivity","values not set:${it.message}")

                Toast.makeText(this,"Values not set${it.message}",Toast.LENGTH_SHORT).show()
            }
    }
}

@Parcelize
class User(val uid:String,val username:String,val profileimageurl:String):Parcelable{
    constructor():this("","","")
}
//add this to build.gradle to use parcelize
//androidExtensions{
//    experimental=true
//}
package com.example.kotlinmessenger

import android.Manifest
import android.app.Activity
import android.app.PendingIntent.getActivity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlinx.android.parcel.Parcelize


private lateinit var auth: FirebaseAuth

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        auth = FirebaseAuth.getInstance()


        circleprofilepic.setOnClickListener {
            //check runtime permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_DENIED){
                    //permission denied
                    val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                    //show popup to request runtime permission
                    requestPermissions(permissions, PERMISSION_CODE)
                }
                else {
                    //permission already granted
                    pickfromgallery()
                }
            }
            else {
                //system OS is < Marshmallow
                pickfromgallery()
            }

        }


        register_button_register.setOnClickListener {
            performregister()
        }

        already_have_account_textview.setOnClickListener {
            Log.d("MainActivity", "Try to show login activity")

                //launch the login activity

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            }

        }

    private fun pickfromgallery() {
        //Intent to pick image
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    companion object {
            //image pick code
            private val IMAGE_PICK_CODE = 1000;
            //Permission code
            private val PERMISSION_CODE = 1001;
        }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
       when(requestCode){
           PERMISSION_CODE -> {
               if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                   //permission from popup granted
                   pickfromgallery()
               }
               else {
                   //permission from pop-up denied
                   Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
               }
           }
       }
    }

    var selectedPhotoUri: Uri? = null

    //handle image pick result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE){
            Log.d("MainActivity", "photo was selected")
            selectedPhotoUri = data?.data

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver,selectedPhotoUri)


            //val bitmapDrawable = BitmapDrawable(this.resources,bitmap)

            circleprofilepic.setImageURI(selectedPhotoUri)


        }
    }


        private fun performregister(){
        val email = email_edittext_register.text.toString()
        val password = password_edittext_register.text.toString()

        Log.d("MainActivity", "Email is " + email)
        Log.d("MainActivity", "Password: $password")

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter text in email/password", Toast.LENGTH_SHORT).show()
            return
        }

        //firebase authentication
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->

                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("MainActivity", "Successfully created new user")
                    //val user = auth.currentUser
                    uploadImageToFirebaseStorage()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("MainActivity", "createUserWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Failed to create new user",
                        Toast.LENGTH_SHORT).show()
                }

                // ...
            }
            //saveUserToFirebaseDatabase(selectedPhotoUri.toString())
}


    private fun uploadImageToFirebaseStorage(){
        if(selectedPhotoUri == null) return
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d("MainActivity", "Successfully uploaded image: ${it.metadata?.path}")

                ref.downloadUrl.addOnSuccessListener {
                    it.toString()
                    Log.d("MainActivity", "File Location: $it")

                    saveUserToFirebaseDatabase(it.toString())

                }
            }
    }

    private fun saveUserToFirebaseDatabase(profileImageUrl: String){
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user = User(uid, username_edittext_register.text.toString(), profileImageUrl )
        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("MainActivity", "User has been saved to firebase database")

                val intent = Intent(this, MessagesReceivedActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
    }


    }

@Parcelize
class User(val uid: String, val username:String, val profileImageUrl: String ): Parcelable {
    constructor() : this("", "", "")
}


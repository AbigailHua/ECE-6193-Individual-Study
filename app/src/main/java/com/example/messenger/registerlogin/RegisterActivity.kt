package com.example.messenger.registerlogin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.example.messenger.databinding.ActivityRegisterBinding
import com.example.messenger.messages.LatestMessagesActivity
import com.example.messenger.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.util.*

class RegisterActivity : AppCompatActivity() {
    private val TAG = "RegisterActivity"
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var database: FirebaseDatabase
    private var selectedPhotoUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // Initialize Firebase Auth
        auth = Firebase.auth

        binding.selectPhotoButtonRegister.setOnClickListener { uploadPhoto() }
        binding.registerButtonRegister.setOnClickListener { performRegister() }
        binding.alreadyHaveAccountTextview.setOnClickListener {
            Log.d(TAG, "Try to show login activity")

            // launch the login activity somehow
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun uploadPhoto() {
        Log.d(TAG, "Try to show photo selector")

        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            // proceed and check what the selected image was ...
            Log.d(TAG, "Photo was selected")
            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

            binding.selectPhotoImageviewRegister.setImageBitmap(bitmap)
            binding.selectPhotoButtonRegister.alpha = 0f
        }
    }

    private fun performRegister() {
        val email = binding.emailEdittextRegister.text.toString()
        val password = binding.passwordEdittextRegister.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter text in email/pw", Toast.LENGTH_SHORT).show()
        }

        Log.d(TAG, "Email is: $email")
        Log.d(TAG, "Password: $password")

        // Firebase Authentication to create a user with email and password
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = auth.currentUser
                    Log.d(TAG, "${user.toString()} logged in")
                    uploadImageToFirebaseStorage()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener{ exception ->
                Log.d(TAG, "Failed to create user: ${exception.message}")
            }
    }

    private fun uploadImageToFirebaseStorage() {
        if (selectedPhotoUri == null) return
        val filename = UUID.randomUUID().toString()
        storage = Firebase.storage
        val imgRef = storage.reference.child("/images/$filename")
        imgRef.putFile(selectedPhotoUri!!)
            .addOnSuccessListener { task ->
                Log.d(TAG, "Successfully uploaded image: ${task.metadata?.path}")
                imgRef.downloadUrl.addOnSuccessListener { uri ->
                    Log.d(TAG, "File location: $uri")
                    saveUserToFirebaseDatabase(uri.toString())
                }
            }
            .addOnFailureListener {
                Log.w(TAG, "Fail to upload image: ${it.message}")
            }
    }

    private fun saveUserToFirebaseDatabase(profileImageUrl: String) {
        database = Firebase.database
        val uid = auth.uid ?: ""
        val dbRef = database.getReference("/users/$uid")
        val user = User(uid, binding.usernameEdittextRegister.text.toString(), profileImageUrl)
        dbRef.setValue(user)
            .addOnSuccessListener {
                Log.d(TAG, "Finally we saved the user to Firebase Database")

                val intent = Intent(this, LatestMessagesActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .addOnFailureListener {
                Log.w(TAG, "Failed to set value to database: ${it.message}")
            }
    }
}
package com.example.messenger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.messenger.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {
    private val TAG = "RegisterActivity"
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth

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
//                        updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
//                        updateUI(null)
                }
            }
            .addOnFailureListener{ exception ->
                Log.d(TAG, "Failed to create user: ${exception.message}")
            }
    }

//    public override fun onStart() {
//        super.onStart()
//        // Check if user is signed in (non-null) and update UI accordingly.
//        val currentUser = auth.currentUser
//        if(currentUser != null){
//            reload();
//        }
//    }
}
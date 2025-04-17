package com.example.financialtracker

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AccountActivity : AppCompatActivity() {

    // Declare the UI elements
    private lateinit var accountTitle: TextView
    private lateinit var profilePicture: ImageView
    private lateinit var changeProfilePictureButton: Button
    private lateinit var usernameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var saveChangesButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.account) // Replace with your actual XML layout file name

        // Initialize UI elements
        accountTitle = findViewById(R.id.account_control_title)
        profilePicture = findViewById(R.id.profile_picture)
        changeProfilePictureButton = findViewById(R.id.change_profile_picture_button)
        usernameEditText = findViewById(R.id.username_input)
        emailEditText = findViewById(R.id.email_input)
        passwordEditText = findViewById(R.id.password_input)
        saveChangesButton = findViewById(R.id.save_changes_button)

        // Set click listeners for buttons
        changeProfilePictureButton.setOnClickListener {
            // Open a gallery or camera to change the profile picture (implement this logic)
        }

        saveChangesButton.setOnClickListener {
            onSaveChangesClicked()
        }
    }

    // Handle the Save Changes button click
    private fun onSaveChangesClicked() {
        // Get input values from EditText fields
        val username = usernameEditText.text.toString()
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()

        // Validate the inputs (basic validation)
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email address", Toast.LENGTH_SHORT).show()
        } else if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
        } else {
            // Save the account details (replace this with your actual logic)
            // For example, you can update the account in the database or call an API

            Toast.makeText(this, "Account details updated successfully", Toast.LENGTH_SHORT).show()

            // Optionally, navigate to another activity (e.g., home or profile page)
            // startActivity(Intent(this, HomeActivity::class.java))
        }
    }
}
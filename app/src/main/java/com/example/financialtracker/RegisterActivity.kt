package com.example.financialtracker

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity() {

    // Declare the UI elements
    private lateinit var nameEditText: EditText
    private lateinit var usernameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var registerButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register) // Replace with your actual XML layout file name

        // Initialize UI elements
        nameEditText = findViewById(R.id.editTextText2)
        usernameEditText = findViewById(R.id.editTextText3)
        emailEditText = findViewById(R.id.editTextTextEmailAddress)
        passwordEditText = findViewById(R.id.editTextNumberPassword)
        registerButton = findViewById(R.id.button3)

        // Set click listener for the register button
        registerButton.setOnClickListener {
            onRegisterClicked()
        }
    }

    // Handle the Register button click
    private fun onRegisterClicked() {
        // Get input values from EditText fields
        val name = nameEditText.text.toString()
        val username = usernameEditText.text.toString()
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()

        // Validate the inputs (basic validation)
        if (name.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email address", Toast.LENGTH_SHORT).show()
        } else if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
        } else {
            // Registration logic (replace this with your actual logic)
            // For example, you can store the data in a database or send it to a server

            Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()

            // Optionally, navigate to another activity (e.g., login screen)
            // startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}
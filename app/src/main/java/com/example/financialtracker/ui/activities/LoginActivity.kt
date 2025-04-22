package com.example.financialtracker.ui.activities

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.financialtracker.R

class LoginActivity : AppCompatActivity() {

    // Declare the UI elements
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login) // Replace with your actual XML layout file name

        // Initialize the UI elements
        usernameEditText = findViewById(R.id.editTextText)
        passwordEditText = findViewById(R.id.editTextTextPassword)
        loginButton = findViewById(R.id.button)

        // Set click listener for the login button
        loginButton.setOnClickListener {
            onLoginClicked()
        }
    }

    // Handle the Login button click
    private fun onLoginClicked() {
        // Get input values from EditText fields
        val username = usernameEditText.text.toString()
        val password = passwordEditText.text.toString()

        // Validate the inputs (basic validation)
        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Both fields are required", Toast.LENGTH_SHORT).show()
        } else {
            // Check credentials (replace this with your actual validation logic)
            if (isValidCredentials(username, password)) {
                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()

                // Optionally, navigate to another activity (e.g., home screen)
                // startActivity(Intent(this, HomeActivity::class.java))
                finish()  // Close the login screen
            } else {
                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // A placeholder for actual credential validation logic
    private fun isValidCredentials(username: String, password: String): Boolean {
        // This should be replaced with your actual backend/API logic
        // For now, we just check if the username is "user" and password is "password"
        return username == "user" && password == "password"
    }
}
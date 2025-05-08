package com.example.financialtracker.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.financialtracker.R
import com.example.financialtracker.ui.viewmodels.RegisterViewModel

class RegisterActivity : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var usernameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var registerButton: Button
    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register)

        nameEditText = findViewById(R.id.editTextText2)
        usernameEditText = findViewById(R.id.editTextText3)
        emailEditText = findViewById(R.id.editTextTextEmailAddress)
        passwordEditText = findViewById(R.id.editTextPassword)
        registerButton = findViewById(R.id.button3)

        registerButton.setOnClickListener {
            onRegisterClicked()
        }

        viewModel.registrationSuccess.observe(this, Observer { success ->
            if (success) {
                Toast.makeText(this, "Registration successful. Please verify your email.", Toast.LENGTH_LONG).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        })

        viewModel.errorMessage.observe(this, Observer { error ->
            Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
        })
    }

    private fun onRegisterClicked() {

        val name = nameEditText.text.toString()
        val username = usernameEditText.text.toString()
        val email = emailEditText.text.toString()
        val password = passwordEditText.text.toString()

        if (name.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email address", Toast.LENGTH_SHORT).show()
        } else if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.registerUser(name, username, email, password)
        }
    }
}
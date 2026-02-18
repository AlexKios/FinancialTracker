package com.example.financialtracker.ui.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.financialtracker.R
import com.example.financialtracker.ui.viewmodels.AccountViewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set

class AccountActivity : AppCompatActivity() {

    private lateinit var viewModel: AccountViewModel

    private lateinit var accountTitle: TextView
    private lateinit var profilePicture: ImageView
    private lateinit var changeProfilePictureButton: Button
    private lateinit var usernameEditText: EditText
    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var saveChangesButton: Button
    private lateinit var showQrCode: Button
    private lateinit var qrCodeImageView: ImageView
    private lateinit var toggleQrCodeButton: Button

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            it.data?.data?.let {
                uri -> viewModel.uploadProfilePicture(uri)
            }
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                launchImagePicker()
            } else {
                Toast.makeText(this, "Permission denied to read photos.", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.account)

        accountTitle = findViewById(R.id.account_control_title)
        profilePicture = findViewById(R.id.profile_picture)
        changeProfilePictureButton = findViewById(R.id.change_profile_picture_button)
        usernameEditText = findViewById(R.id.username_input)
        nameEditText = findViewById(R.id.name_input)
        emailEditText = findViewById(R.id.email_input)
        passwordEditText = findViewById(R.id.password_input)
        saveChangesButton = findViewById(R.id.save_changes_button)
        showQrCode = findViewById(R.id.show_qr_code_button)
        qrCodeImageView = findViewById(R.id.qr_code_image_view)
        toggleQrCodeButton = findViewById(R.id.toggle_qr_code_button)

        changeProfilePictureButton.setOnClickListener {
            requestStoragePermission()
        }

        viewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[AccountViewModel::class.java]

        // Observe user data to populate fields
        viewModel.currentUser.observe(this) { user ->
            usernameEditText.setText(user.username)
            emailEditText.setText(user.email)
            nameEditText.setText(user.name)
            passwordEditText.setText("") // Do not display passwords
            if (user.profileImageUrl.isNotEmpty()) {
                Glide.with(this)
                    .load(user.profileImageUrl.toUri())
                    .placeholder(R.drawable.user)
                    .error(R.drawable.user)
                    .into(profilePicture)
            } else {
                profilePicture.setImageResource(R.drawable.user)
            }
        }

        viewModel.uploadResult.observe(this) { result ->
            result.onSuccess {
                Toast.makeText(this, "Profile picture updated", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_LONG).show()
            }
        }

        // Observe update results
        viewModel.updateResult.observe(this) { result ->
            result.onSuccess {
                Toast.makeText(this, "Changes saved successfully", Toast.LENGTH_SHORT).show()
            }.onFailure { error ->
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_LONG).show()
            }
        }

        // Load user data
        viewModel.loadCurrentUser()

        // Handle save button click
        saveChangesButton.setOnClickListener {
            val newUsername = usernameEditText.text?.toString()?.trim().takeIf { !it.isNullOrEmpty() }
            val newEmail = emailEditText.text?.toString()?.trim().takeIf { !it.isNullOrEmpty() }
            val newPassword = passwordEditText.text?.toString()?.trim().takeIf { !it.isNullOrEmpty() }
            val newName = nameEditText.text?.toString()?.trim().takeIf { !it.isNullOrEmpty() }

            viewModel.updateProfile(
                newUsername = newUsername,
                newEmail = newEmail,
                newPassword = newPassword,
                newName = newName
            )
        }

        showQrCode.setOnClickListener {
            viewModel.currentUser.value?.let { user ->
                val userId = user.uid // Assuming `user.id` is the unique identifier
                Log.d("QR Code", "User ID: $userId")
                generateQrCode(userId)
            }?: run {
                Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
            }
        }

        toggleQrCodeButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                if (qrCodeImageView.isVisible) {
                    qrCodeImageView.visibility = View.GONE
                    toggleQrCodeButton.visibility = View.GONE
                }
            }
        })
    }

    private fun requestStoragePermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_GRANTED -> {
                launchImagePicker()
            }
            shouldShowRequestPermissionRationale(permission) -> {
                Toast.makeText(this, "Permission needed to select a profile picture.", Toast.LENGTH_LONG).show()
                requestPermissionLauncher.launch(permission)
            }
            else -> {
                requestPermissionLauncher.launch(permission)
            }
        }
    }

    private fun launchImagePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        pickImageLauncher.launch(intent)
    }

    private fun generateQrCode(userId: String) {
        val qrCodeWriter = QRCodeWriter()
        try {
            // Create a BitMatrix based on user ID
            val bitMatrix: BitMatrix = qrCodeWriter.encode(userId, BarcodeFormat.QR_CODE, 512, 512)

            // Convert BitMatrix to Bitmap
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bmp = createBitmap(width, height, Bitmap.Config.RGB_565)

            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp[x, y] = if (bitMatrix.get(
                            x,
                            y
                        )
                    ) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                }
            }

            toggleQrCodeButton.visibility = View.VISIBLE

            qrCodeImageView.setImageBitmap(bmp)
        } catch (e: Exception) {
            Toast.makeText(this, "Error generating QR Code", Toast.LENGTH_SHORT).show()
        }
    }
}
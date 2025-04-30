package com.example.financialtracker.ui.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ListView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import com.example.financialtracker.R
import com.example.financialtracker.ui.viewmodels.FriendsViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

class FriendsActivity : BaseActivity() {

    override val navMenuItemId = R.id.nav_friends
    private lateinit var friendsListView: ListView
    private val friendsList = mutableListOf<String>()
    private lateinit var friendsViewModel: FriendsViewModel
    private lateinit var scanButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutInflater.inflate(R.layout.activity_friends, findViewById(R.id.content_container), true)
        scanButton = findViewById(R.id.scanButton)


        friendsListView = findViewById(R.id.friendsListView)

        friendsListView.setOnItemClickListener { _, _, position, _ ->
            val selectedFriend = friendsList[position]
            startChat(selectedFriend)
        }

        scanButton.setOnClickListener {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            scanQrLauncher.launch(cameraIntent)
        }

        friendsViewModel = ViewModelProvider(this)[FriendsViewModel::class.java]

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, friendsList)
        friendsListView.adapter = adapter

        friendsViewModel.friendsList.observe(this) { updatedList ->
            friendsList.clear()
            friendsList.addAll(updatedList)
            adapter.notifyDataSetChanged()
        }

        friendsViewModel.loadFriends()
    }

    private fun startChat(friendName: String) {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("friend_name", friendName)
        startActivity(intent)
    }

    private val scanQrLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val photo = result.data?.extras?.get("data") as? Bitmap
            if (photo != null) {
                scanQrCode(photo)
            } else {
                Toast.makeText(this, "No image captured", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun scanQrCode(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)
        val scanner = BarcodeScanning.getClient()

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    barcode.rawValue?.let { uid ->
                        friendsViewModel.addFriendFromQrCode(uid,
                            onSuccess = {
                                Toast.makeText(this, "Friend added!", Toast.LENGTH_SHORT).show()
                            },
                            onFailure = { e ->
                                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to scan QR code", Toast.LENGTH_SHORT).show()
            }
    }
}
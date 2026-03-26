package com.example.financialtracker.ui.activities

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ListView
import android.widget.Button
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.financialtracker.R
import com.example.financialtracker.data.adapter.FriendsAdapter
import com.example.financialtracker.data.model.Friend
import com.example.financialtracker.data.repositories.UserRepository
import com.example.financialtracker.ui.viewmodels.FriendsViewModel
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class FriendsActivity : BaseActivity() {

    override val navMenuItemId = R.id.nav_friends
    private lateinit var friendsListView: ListView
    private val friendsList = mutableListOf<Friend>()
    private lateinit var friendsViewModel: FriendsViewModel
    private lateinit var scanButton: Button
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var previewView: PreviewView
    private var isScanning = false
    private var cameraProvider: ProcessCameraProvider? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutInflater.inflate(R.layout.activity_friends, findViewById(R.id.content_container), true)
        scanButton = findViewById(R.id.scanButton)
        friendsListView = findViewById(R.id.friendsListView)
        previewView = findViewById(R.id.previewView)
        cameraExecutor = Executors.newSingleThreadExecutor()

        scanButton.setOnClickListener {
            if (isScanning) {
                stopCamera()
            } else {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    openCameraPreview()
                } else {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 101)
                }
            }
        }

        friendsViewModel = ViewModelProvider(this)[FriendsViewModel::class.java]

        friendsViewModel.loadFriends()
        friendsViewModel.startListeningToFriendStatuses()

        val adapter = FriendsAdapter(
            this,
            friendsList,
            onRemoveClick = { friendName ->
                AlertDialog.Builder(this)
                    .setTitle("Remove Friend")
                    .setMessage("Are you sure you want to remove $friendName?")
                    .setPositiveButton("Yes") { _, _ ->
                        friendsViewModel.removeFriend(friendName,
                            onSuccess = {
                                Toast.makeText(this, "$friendName removed", Toast.LENGTH_SHORT).show()
                                friendsViewModel.loadFriends()
                            },
                            onFailure = { e ->
                                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            },
            onFriendClick = { friendName ->
                val friend = friendsList.find { it.username == friendName }
                startChat(friendName, friend?.profileImageUrl ?: "")
            }
        )

        friendsListView.adapter = adapter

        friendsViewModel.friendsList.observe(this) { updatedList ->
            friendsList.clear()
            friendsList.addAll(updatedList)
            adapter.notifyDataSetChanged()
        }
    }

    private fun startChat(friendName: String, profileImageUrl: String) {
        val userRepository = UserRepository()

        userRepository.getUserIdByUsername(friendName, { friendUid ->
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("friend_name", friendName)
            intent.putExtra("friend_uid", friendUid)
            intent.putExtra("friend_profile_image_url", profileImageUrl)
            startActivity(intent)
        }, { error ->
            Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
        })
    }

    @OptIn(ExperimentalGetImage::class)
    private fun openCameraPreview() {
        previewView.visibility = View.VISIBLE
        scanButton.text = getString(R.string.stop_scan)
        isScanning = true

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val scanner = BarcodeScanning.getClient()
            val analysisUseCase = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            analysisUseCase.setAnalyzer(cameraExecutor) { imageProxy ->
                val mediaImage = imageProxy.image
                if (mediaImage != null) {
                    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                    scanner.process(image)
                        .addOnSuccessListener { barcodes ->
                            for (barcode in barcodes) {
                                val rawValue = barcode.rawValue
                                if (!rawValue.isNullOrEmpty()) {
                                    runOnUiThread {
                                        previewView.visibility = View.GONE
                                        cameraProvider.unbindAll()
                                        friendsViewModel.addFriendFromQrCode(rawValue,
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
                        }
                        .addOnFailureListener {
                            Log.e("CameraX", "Barcode scanning failed", it)
                        }
                        .addOnCompleteListener {
                            imageProxy.close()
                        }
                } else {
                    imageProxy.close()
                }
            }

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, analysisUseCase)
            } catch (e: Exception) {
                Log.e("CameraX", "Use case binding failed", e)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    private fun stopCamera() {
        cameraProvider?.unbindAll()
        previewView.visibility = View.GONE
        scanButton.text = getString(R.string.scan)
        isScanning = false
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("isScanning", isScanning)
        outState.putInt("scrollPosition", friendsListView.firstVisiblePosition)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val scrollPosition = savedInstanceState.getInt("scrollPosition", 0)

        isScanning = savedInstanceState.getBoolean("isScanning", false)
        if (isScanning) {
            openCameraPreview()
        }
        friendsViewModel.friendsList.observe(this) { updatedList ->
            friendsList.clear()
            friendsList.addAll(updatedList)
            (friendsListView.adapter as? FriendsAdapter)?.notifyDataSetChanged()
            friendsListView.setSelection(scrollPosition)
        }
    }
}
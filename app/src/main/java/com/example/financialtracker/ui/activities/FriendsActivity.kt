package com.example.financialtracker.ui.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ListView
import android.widget.ArrayAdapter
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
import com.example.financialtracker.ui.viewmodels.FriendsViewModel
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class FriendsActivity : BaseActivity() {

    override val navMenuItemId = R.id.nav_friends
    private lateinit var friendsListView: ListView
    private val friendsList = mutableListOf<String>()
    private lateinit var friendsViewModel: FriendsViewModel
    private lateinit var scanButton: Button
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var previewView: PreviewView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        layoutInflater.inflate(R.layout.activity_friends, findViewById(R.id.content_container), true)
        scanButton = findViewById(R.id.scanButton)

        friendsListView = findViewById(R.id.friendsListView)

        friendsListView.setOnItemClickListener { _, _, position, _ ->
            val selectedFriend = friendsList[position]
            startChat(selectedFriend)
        }

        previewView = findViewById(R.id.previewView)
        cameraExecutor = Executors.newSingleThreadExecutor()

        scanButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                openCameraPreview()
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 101)
            }
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

    @OptIn(ExperimentalGetImage::class)
    private fun openCameraPreview() {
        previewView.visibility = View.VISIBLE

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
}
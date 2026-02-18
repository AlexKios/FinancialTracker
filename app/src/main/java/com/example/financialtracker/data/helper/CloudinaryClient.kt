package com.example.financialtracker.data.helper

import android.content.Context
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback

object CloudinaryClient {

    fun init(context: Context) {
        val config = mapOf(
            "cloud_name" to "dyhhkatcj",
            "secure" to true
        )
        MediaManager.init(context, config)
    }

    fun uploadImage(
        uri: Uri,
        uploadPreset: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        MediaManager.get().upload(uri)
            .unsigned(uploadPreset)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {
                    // your code here
                }

                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                    // your code here
                }

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val secureUrl = resultData["secure_url"] as? String
                    if (secureUrl != null) {
                        onSuccess(secureUrl)
                    } else {
                        onError("Failed to get secure URL from Cloudinary")
                    }
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    onError("Cloudinary upload error: ${'$'}{error.description}")
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {
                    // your code here
                }
            })
            .dispatch()
    }
}
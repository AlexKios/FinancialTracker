package com.example.financialtracker.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.graphics.Bitmap
import android.graphics.Color
import android.widget.ImageView
import com.example.financialtracker.R
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.util.UUID
// import com.google.firebase.auth.FirebaseAuth

class HomeFragment : Fragment() {
    private lateinit var qrImageView: ImageView
    private val userId = /* FirebaseAuth.getInstance().uid ?: */ UUID.randomUUID().toString()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        qrImageView = view.findViewById(R.id.qrImageView)
        generateQRCode(userId)
        return view
    }

    private fun generateQRCode(text: String) {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 512, 512)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

        for (x in 0 until width) {
            for (y in 0 until height) {
                bmp.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
            }
        }

        qrImageView.setImageBitmap(bmp)
    }
}
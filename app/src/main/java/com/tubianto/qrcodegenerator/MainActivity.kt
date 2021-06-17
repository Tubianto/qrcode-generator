package com.tubianto.qrcodegenerator

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.WriterException
import net.glxn.qrgen.android.QRCode

/**
 * Created by Tubianto on 15/06/2021.
 */
class MainActivity : AppCompatActivity() {
    private var WRITE_EXTERNAL_STORAGE_PERMISSION_CODE: Int = 1
    private lateinit var etText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        init()
        checkPermission()
    }

    private fun init(){
        etText = findViewById(R.id.et_text)
    }

    private fun checkPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                requestPermissions(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    WRITE_EXTERNAL_STORAGE_PERMISSION_CODE
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            WRITE_EXTERNAL_STORAGE_PERMISSION_CODE -> if (grantResults.isNotEmpty()) {
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(this, "Anda perlu memberikan semua izin untuk menggunakan aplikasi ini.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    fun clickGenerate(view: View) {
        val myText = etText.text.toString().trim()
        if (myText.isEmpty() || myText == ""){
            etText.error = "Bidang ini tidak boleh kosong"
            etText.requestFocus()
        } else {
            buildQrCode(myText)
        }
    }

    private fun buildQrCode(content: String){
        var bm: Bitmap? = null
        val builder = AlertDialog.Builder(this)
        val factory = LayoutInflater.from(this)
        val myView = factory.inflate(R.layout.dialog_qr_code, null)
        val ivQrcode = myView.findViewById<ImageView>(R.id.iv_qrcode)
        val pbQrcode = myView.findViewById<ProgressBar>(R.id.pb_qrcode)

        builder.setView(myView)
        builder.setIcon(R.drawable.ic_qr_code)
        builder.setTitle("QR Code")
        builder.setNegativeButton("Close"){ dialog, _ ->
            dialog.dismiss()
        }
        builder.setPositiveButton("Share"){ _, _ ->
            if (bm != null){
                share(bm!!, "QR Code")
            }
        }
        builder.show().withCenteredButtons()

        try {
            bm = QRCode.from(content).bitmap()
            if (bm != null) {
                ivQrcode.visibility = View.VISIBLE
                pbQrcode.visibility = View.GONE
                ivQrcode.setImageBitmap(bm)
            }
        }
        catch (e: WriterException) {}
    }

    private fun AlertDialog.withCenteredButtons() {
        val positive = getButton(AlertDialog.BUTTON_POSITIVE)
        val negative = getButton(AlertDialog.BUTTON_NEGATIVE)

        //Disable the material spacer view in case there is one
        val parent = positive.parent as? LinearLayout
        parent?.gravity = Gravity.CENTER_HORIZONTAL
        val leftSpacer = parent?.getChildAt(1)
        leftSpacer?.visibility = View.GONE

        //Force the default buttons to center
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        layoutParams.weight = 1f
        layoutParams.gravity = Gravity.CENTER

        positive.layoutParams = layoutParams
        negative.layoutParams = layoutParams
    }

    private fun share(source: Bitmap, title: String){
        val bitmapPath = MediaStore.Images.Media.insertImage(contentResolver, source, title, null)
        Log.e("BITMAP PATH", bitmapPath)
        val bitmapUri: Uri = Uri.parse(bitmapPath)

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "image/png"
        intent.putExtra(Intent.EXTRA_STREAM, bitmapUri)
        startActivity(Intent.createChooser(intent, "Bagikan QR Code melalui"))
    }
}
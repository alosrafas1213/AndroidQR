package com.tallercmovil.myapplication

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.webkit.URLUtil
import android.widget.Toast
import android.content.ClipboardManager
import android.graphics.BitmapFactory
import android.os.Environment
import android.os.Environment.getExternalStorageDirectory
import android.provider.MediaStore
import android.util.Base64
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.Result
import com.google.zxing.common.HybridBinarizer
import me.dm7.barcodescanner.zxing.ZXingScannerView
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.net.MalformedURLException
import java.net.URL


class QR : AppCompatActivity(), ZXingScannerView.ResultHandler {

    private val PERMISO_CAMARA = 1
    private val PERMISO_STORAGE = 1
    private val PERMISO_WRITE = 1
    private var scannerView: ZXingScannerView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        scannerView = ZXingScannerView(this)
        setContentView(scannerView)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(checarPermiso()){
                //el permiso se concedió
            }else{
                solicitarPermiso()
            }
            if(checarPermiso2()){
                //el permiso se concedió
            }else{
                solicitarPermiso2()
            }
            if(checarPermiso3()){
                //el permiso se concedió
            }else{
                solicitarPermiso3()
            }
        }

        scannerView?.setResultHandler(this)
        scannerView?.startCamera()

    }

    private fun checarPermiso(): Boolean {
        return(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }

    private fun checarPermiso2(): Boolean {
        return(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
    }

    private fun checarPermiso3(): Boolean {
        return(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
    }

    private fun solicitarPermiso(){
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA),PERMISO_CAMARA)
    }
    private fun solicitarPermiso2(){
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),PERMISO_STORAGE)
    }

    private fun solicitarPermiso3(){
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),PERMISO_WRITE)
    }

    override fun handleResult(p0: Result?) {
        val scanResult = p0?.text

        Log.d("QR_LEIDO", scanResult!!)
        //Log.d("Tipo_de_variable", "${scanResult::class.simpleName}")
        var cabecera = scanResult.lines ()
        Log.d("Cabecera", cabecera[0])
        try{
            if (URLUtil.isValidUrl(scanResult)) {
                Log.d("Mensaje","El texto pertenece a una URL")
                val url = URL(scanResult)
                val i = Intent(Intent.ACTION_VIEW)

                i.setData(Uri.parse(scanResult))

                startActivity(i)
                finish()
            }
            else if (cabecera[0]=="BEGIN:VCARD"){
                Log.d("Mensaje","El texto pertenece a una VCARD")

                val outputDir: File = this.getExternalFilesDir(null)!! // context being the Activity pointer
                //val outputDir: File = File("file///")
                val outputFile = File.createTempFile("archivo", ".vcf", outputDir)
                var archivo=outputFile.absolutePath
                Log.d("Ubicacion archivo",archivo)
                //val fileOutputStream: FileOutputStream = openFileOutput("archivo.vcf", Context.MODE_PRIVATE)
                val fileOutputStream: FileOutputStream = FileOutputStream(outputFile)
                val outputWriter = OutputStreamWriter(fileOutputStream)
                outputWriter.write(scanResult)
                outputWriter.close()

                val i = Intent(Intent.ACTION_VIEW)
                i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                //i.setDataAndType(Uri.fromFile(outputFile),"text/x-vcard");
                i.setDataAndType(Uri.parse(archivo),"text/vcard");
                //i.setDataAndType(Uri.parse("data:text/x-vcard;base64," + Base64.encodeToString(theStringContainingYourVcard.getBytes()),"text/x-vcard");
                //i.setDataAndType(Uri.parse(Base64.encodeToString(scanResult.toByteArray(), Base64.DEFAULT)),"text/vcard")
                startActivity(i);
                finish()
            }
            else if(scanResult.split(":")[0]=="SMSTO"){
                Log.d("Mensaje","El texto pertenece a un SMS")
                var cadena1=scanResult.split(":")[0].lowercase()+":"+scanResult.split(":")[1]

                var enviar=Uri.parse(cadena1)
                val i = Intent(Intent.ACTION_SENDTO,enviar)
                var cadena = scanResult.split("SMSTO:")
                var cadenas=cadena[1].substringAfter(":")

                i.putExtra("sms_body", cadenas)
                startActivity(i)
            }
            else if(scanResult.split(":")[0]=="MATMSG"){
                Log.d("Mensaje","El texto pertenece a un e-mail")
                val arreg = scanResult.split("MATMSG:TO:")
                val correos = arreg[1].substringBefore(":").substringBefore(";")
                val asunto = arreg[1].substringAfter(":").substringBefore(";")
                val bodyy = arreg[1].substringAfter(":").substringAfter("BODY:").substringBefore(";;")
                val i = Intent(Intent.ACTION_SEND)
                /*To send an email you need to specify mailto: as URI using setData() method
                and data type will be to text/plain using setType() method*/
                i.data = Uri.parse("mailto:")
                i.type = "text/plain"
                // put recipient email in intent
                /* recipient is put as array because you may wanna send email to multiple emails
                   so enter comma(,) separated emails, it will be stored in array*/
                i.putExtra(Intent.EXTRA_EMAIL, arrayOf(correos))
                //put the Subject in the intent
                i.putExtra(Intent.EXTRA_SUBJECT, asunto)
                //put the message in the intent
                i.putExtra(Intent.EXTRA_TEXT, bodyy)
                startActivity(i)
            }
            else{
                Log.d("Mensaje","El texto pertenece a un texto sipmple")

                AlertDialog.Builder(this)
                    .setTitle("Mensaje")
                    .setMessage("Texto: $scanResult")
                    .setPositiveButton("Aceptar", DialogInterface.OnClickListener { dialog, which ->
                        dialog.dismiss()
                        finish()
                    })
                    .create()
                    .show()

            }
        }catch(e: MalformedURLException){
            AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("El código QR no es válido para la aplicación")
                .setPositiveButton("Aceptar", DialogInterface.OnClickListener { dialog, which ->
                    dialog.dismiss()
                    finish()
                })
                .create()
                .show()

        }

    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode){
            PERMISO_CAMARA -> {
                if(grantResults.isNotEmpty() && grantResults[0]!=PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                    if(shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)){
                        AlertDialog.Builder(this@QR)
                            .setTitle("Permiso requerido")
                            .setMessage("Se necesita la cámara para leer los códigos QR")
                            .setPositiveButton("Aceptar", DialogInterface.OnClickListener { dialog, which ->
                                requestPermissions(arrayOf(Manifest.permission.CAMERA), PERMISO_CAMARA)
                            })
                            .setNegativeButton("Cancelar", DialogInterface.OnClickListener { dialog, which ->
                                dialog.dismiss()
                                finish()
                            })
                            .create()
                            .show()
                    }else{
                        Toast.makeText(this@QR, "El permiso de la cámara no se ha concedido", Toast.LENGTH_LONG).show()
                        finish()
                    }
                }
            }
        }

    }


    override fun onResume() {
        super.onResume()
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(checarPermiso()){
                if(scannerView == null){
                    scannerView = ZXingScannerView(this)
                    setContentView(scannerView)
                }

                scannerView?.setResultHandler(this)
                scannerView?.startCamera()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scannerView?.stopCamera()
    }
}
package com.barryzea.nilopartner.promo

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.barryzea.nilopartner.EventPost
import com.barryzea.nilopartner.R
import com.barryzea.nilopartner.commons.Constants
import com.barryzea.nilopartner.commons.Constants.COLLECTION_PRODUCT
import com.barryzea.nilopartner.commons.Constants.PRODUCT_IMAGE
import com.barryzea.nilopartner.databinding.FragmentDialogAddBinding
import com.barryzea.nilopartner.databinding.FragmentPromoBinding
import com.barryzea.nilopartner.fcm.NotificationRS
import com.barryzea.nilopartner.interfaces.MainAux
import com.barryzea.nilopartner.pojo.Product
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.security.AllPermission

class PromoFragment:DialogFragment(), DialogInterface.OnShowListener {

    private var bind: FragmentPromoBinding?=null
    private var positiveButton:Button?=null
    private var negativeButton:Button?=null
    private var product:Product?=null
    private var photoSelectedUri:Uri?=null
    private val resultLauncher=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if(it.resultCode==Activity.RESULT_OK){
            photoSelectedUri=it.data?.data
            //bind?.imgProductPreview?.setImageURI(photoSelectedUri)
            bind?.let{bind->
                Glide.with(this)
                    .load(photoSelectedUri)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.ic_image_search)
                    .error(R.drawable.ic_broken_image)
                    .centerCrop()
                    .into(bind.imgProductPreview)
            }
        }
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        activity?.let{activity->
            bind= FragmentPromoBinding.inflate(LayoutInflater.from(activity))
            bind?.let{
                val builder=AlertDialog.Builder(activity)
                    .setTitle("Agregar producto")
                    .setPositiveButton("Agregar",null)
                    .setNegativeButton("Cancelar", null)
                    .setView(it.root)
                val dialog=builder.create()
                dialog.setOnShowListener(this)
                return dialog
            }

        }
        return super.onCreateDialog(savedInstanceState)
    }
    override fun onShow(dialogInterface: DialogInterface?) {

        configButtons()
        val dialog = dialog as? AlertDialog
        dialog?.let{
            positiveButton=it.getButton(Dialog.BUTTON_POSITIVE)
            negativeButton=it.getButton(Dialog.BUTTON_NEGATIVE)

            positiveButton?.setOnClickListener {
                enableUI(false)

                        uploadReducedImage()

            }

            negativeButton?.setOnClickListener { dismiss() }
        }
    }

    private fun configButtons(){
        bind?.let{
            it.ibProduct.setOnClickListener {
                openGallery()
            }
        }
    }

    private fun openGallery() {
        val intent=Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        resultLauncher.launch(intent)
    }

    private fun uploadReducedImage(){

        //creamos una carpeta para cada usuario que agrege productos con su id como nombre de directorio uid/product_images/imagen


            photoSelectedUri?.let{uri->
                bind?.let{bind->
                    getBitmapFromUri(uri)?.let{bitmap->
                        bind.pbUpload.visibility= View.VISIBLE
                        //comprimimos el tamaño de la imagen
                        val baos=ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG,90, baos)
                        val promoRef=FirebaseStorage.getInstance().reference.child("promos")
                            .child(bind.etTopic.text.toString().trim())

                          promoRef.putBytes(baos.toByteArray())
                            .addOnProgressListener {
                                val progress=(100 * it.bytesTransferred / it.totalByteCount ).toInt()
                                it.run {
                                    bind.pbUpload.progress=progress
                                    bind.tvProgress.text=String.format( "%s%%",progress)
                                }
                            }
                            .addOnSuccessListener {
                                it.storage.downloadUrl.addOnSuccessListener {downloadUrl->
                                    val notificationRS=NotificationRS()
                                    notificationRS.sendNotificationByTopic(
                                        bind.etTitle.text.toString().trim(),
                                        bind.etDescription.text.toString().trim(),
                                        bind.etTopic.text.toString().trim(),
                                        downloadUrl.toString()
                                    ){
                                        if(it){
                                            Toast.makeText(context, "Promoción enviada", Toast.LENGTH_SHORT).show()
                                            dismiss()
                                        }
                                        else{
                                            Toast.makeText(context, "Error intente más tarde", Toast.LENGTH_SHORT).show()
                                        }
                                        enableUI(true)

                                    }

                                }
                            }
                            .addOnFailureListener{

                                enableUI(true)
                                Toast.makeText(requireActivity(), "Error al subir imagen", Toast.LENGTH_SHORT).show()
                                bind?.pbUpload?.visibility=View.GONE

                            }
                    }


                }

        }


    }
    //retornar el mapa de bits desde la uri
    private fun getBitmapFromUri(uri: Uri): Bitmap?{
        //ya que getBitmap está obsoleto en versiones recientes de android, usamos esta sentencia if
        activity?.let{
            val bitmap=if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.P){
                val source=ImageDecoder.createSource(it.contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            }
            else{
                MediaStore.Images.Media.getBitmap(it.contentResolver, uri)
            }
            return getResizedImage(bitmap, 320)
        }
        return null
    }
    //retornamos el bitmap redimensionado
    private fun getResizedImage(image:Bitmap, maxSize:Int):Bitmap{
        var width=image.width
        var height=image.height
        if(width<=maxSize && height<=maxSize) return image
        val bitmapRatio=width.toFloat() / height.toFloat()
        if(bitmapRatio>1){
            width=maxSize
            height=(width/bitmapRatio).toInt()
        }
        else{
            height=maxSize
            width=(height/bitmapRatio).toInt()
        }
        return Bitmap.createScaledBitmap(image, width,height,true)
    }

    private fun enableUI(enable:Boolean){
        positiveButton?.isEnabled=enable
        negativeButton?.isEnabled=enable
        bind?.apply {

            etTitle.isEnabled=enable
            etDescription.isEnabled=enable
            etTopic.isEnabled=enable

        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        bind=null
    }
}
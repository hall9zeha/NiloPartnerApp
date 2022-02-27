package com.barryzea.nilopartner.dialogs

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

class AddDialogFragment:DialogFragment(), DialogInterface.OnShowListener {

    private var bind:FragmentDialogAddBinding?=null
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
            bind= FragmentDialogAddBinding.inflate(LayoutInflater.from(activity))
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

        initProduct()
        configButtons()
        val dialog = dialog as? AlertDialog
        dialog?.let{

            positiveButton=it.getButton(Dialog.BUTTON_POSITIVE)
            negativeButton=it.getButton(Dialog.BUTTON_NEGATIVE)

            product?.let{positiveButton?.text = "Actualizar"}
            positiveButton?.setOnClickListener {
                enableUI(false)
               // if (photoSelectedUri != null) {
                    //uploadImage(product?.id) { eventPost ->
                        uploadReducedImage(product?.id , product?.imgUrl){eventPost->
                        if (eventPost.isSuccess == true) {

                            bind?.let {
                                if (product == null) {
                                    val product = Product(
                                        name = it.etName.text.toString().trim(),
                                        description = it.etDescription.text.toString(),
                                        quantity = it.etQuantity.text.toString().toInt(),
                                        price = it.etPrice.text.toString().toDouble(),
                                        imgUrl = eventPost.photoUrl,
                                        sellerId = eventPost.sellerId
                                    )
                                    saveProduct(product, eventPost.documentId!!)
                                } else {
                                    product?.apply {
                                        name = it.etName.text.toString().trim()
                                        description = it.etDescription.text.toString()
                                        quantity = it.etQuantity.text.toString().toInt()
                                        price = it.etPrice.text.toString().toDouble()
                                        imgUrl = eventPost.photoUrl
                                        updateProduct(this)
                                    }
                                }
                            }
                        }

                    }


             //   }
               /* else if (product != null) {
                    product?.apply {
                        name = bind?.etName?.text.toString().trim()
                        description = bind?.etDescription?.text.toString()
                        quantity = bind?.etQuantity?.text.toString().toInt()
                        price = bind?.etPrice?.text?.toString()!!.toDouble()
                        imgUrl = product?.imgUrl
                        updateProduct(this)
                    }
                }*/
            }

            negativeButton?.setOnClickListener { dismiss() }
        }
    }

    private fun initProduct() {
       product=(activity as? MainAux)?.getProductSelected()
        product?.let{product->
            dialog?.setTitle("Actualizar producto")
            bind?.etName?.setText(product.name)
            bind?.etDescription?.setText(product.description)
            bind?.etPrice?.setText(product.price.toString())
            bind?.etQuantity?.setText(product.quantity.toString())
            Glide.with(this)
                .load(product.imgUrl)
                .apply(RequestOptions()
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(bind?.imgProductPreview!!)
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

    private fun uploadImage(productId:String?, callback:(EventPost)->Unit){

        val eventPost=EventPost()
        eventPost.documentId=productId ?: FirebaseFirestore.getInstance().collection(Constants.COLLECTION_PRODUCT)
            .document().id
        val storageRef=FirebaseStorage.getInstance().reference.child(Constants.PRODUCT_IMAGE)
        photoSelectedUri?.let{uri->
            bind?.let{bind->
                bind.pbUpload.visibility= View.VISIBLE
                val photoRef=storageRef.child(eventPost.documentId!!)
                photoRef.putFile(uri)
                    .addOnProgressListener {
                        val progress=(100 * it.bytesTransferred / it.totalByteCount ).toInt()
                        it.run {
                            bind.pbUpload.progress=progress
                            bind.tvProgress.text=String.format( "%s%%",progress)
                        }
                    }
                    .addOnSuccessListener {
                        it.storage.downloadUrl.addOnSuccessListener {downloadUrl->

                            eventPost.isSuccess=true
                            eventPost.photoUrl=downloadUrl.toString()
                            callback(eventPost)

                        }
                    }
                    .addOnFailureListener{
                        eventPost.isSuccess=false
                        enableUI(true)
                        Toast.makeText(requireActivity(), "Error al subir imagen", Toast.LENGTH_SHORT).show()
                        bind?.pbUpload?.visibility=View.GONE
                        callback(eventPost)
                    }

            }
        }
    }
    private fun uploadReducedImage(productId:String?,  imageUrl:String?,callback:(EventPost)->Unit){

        val eventPost=EventPost()
        imageUrl?.let{
            eventPost.photoUrl=it
        }
        eventPost.documentId=productId ?: FirebaseFirestore.getInstance().collection(Constants.COLLECTION_PRODUCT)
            .document().id

        //creamos una carpeta para cada usuario que agrege productos con su id como nombre de directorio uid/product_images/imagen
        FirebaseAuth.getInstance().currentUser?.let{user->

            eventPost.sellerId=user.uid
            val imageRef=FirebaseStorage.getInstance().reference.child(user.uid)
                .child(Constants.PRODUCT_IMAGE)
            //nos dará posibilidades de agregar más imágenes a un mismo producto

            val photoRef=imageRef.child(eventPost.documentId!!).child("image0")
            if(photoSelectedUri == null){
                eventPost.isSuccess=true
                callback(eventPost)
            }
            else{
            //photoSelectedUri?.let{uri->
                bind?.let{bind->
                    getBitmapFromUri(photoSelectedUri!!)?.let{bitmap->
                        bind.pbUpload.visibility= View.VISIBLE
                        //comprimimos el tamaño de la imagen
                        val baos=ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG,90, baos)


                        //photoRef=imageRef.child(eventPost.documentId!!)
                        //en lugar de putFile
                        //photoRef.putFile(uri)
                        //le pasamos en byteArray
                          photoRef.putBytes(baos.toByteArray())
                            .addOnProgressListener {
                                val progress=(100 * it.bytesTransferred / it.totalByteCount ).toInt()
                                it.run {
                                    bind.pbUpload.progress=progress
                                    bind.tvProgress.text=String.format( "%s%%",progress)
                                }
                            }
                            .addOnSuccessListener {
                                it.storage.downloadUrl.addOnSuccessListener {downloadUrl->

                                    eventPost.isSuccess=true
                                    eventPost.photoUrl=downloadUrl.toString()
                                    callback(eventPost)

                                }
                            }
                            .addOnFailureListener{
                                eventPost.isSuccess=false
                                enableUI(true)
                                Toast.makeText(requireActivity(), "Error al subir imagen", Toast.LENGTH_SHORT).show()
                                bind?.pbUpload?.visibility=View.GONE
                                callback(eventPost)
                            }
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
    private fun saveProduct(product:Product, documentId:String){
        val db=FirebaseFirestore.getInstance()
        //le setearemos manualmente el id del documento ya que debemos esperar a que suba la imagen
        db.collection(COLLECTION_PRODUCT)
            //.add(product)
            .document(documentId)
            .set(product)
            .addOnSuccessListener {
                Toast.makeText(context, "producto agregado", Toast.LENGTH_SHORT).show()
                dismiss()
            }
            .addOnCompleteListener {
                enableUI(true)
                bind?.pbUpload?.visibility=View.INVISIBLE

            }
            .addOnFailureListener {
                enableUI(true)
                Toast.makeText(context, "error al insertar", Toast.LENGTH_SHORT).show()
            }
    }
    private fun updateProduct(product:Product){
        val db=FirebaseFirestore.getInstance()
        product.id?.let{id->
            db.collection(COLLECTION_PRODUCT)
                .document(id)
                .set(product)
                .addOnSuccessListener {
                    Toast.makeText(activity, "Producto modificado", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(activity, "Error al actualizar producto", Toast.LENGTH_SHORT).show()
                }
                .addOnCompleteListener {
                    enableUI(true)
                    bind?.pbUpload?.visibility=View.INVISIBLE
                    dismiss()
                }
        }

    }
    private fun enableUI(enable:Boolean){
        positiveButton?.isEnabled=enable
        negativeButton?.isEnabled=enable
        bind?.apply {

            etName.isEnabled=enable
            etDescription.isEnabled=enable
            etPrice.isEnabled=enable
            etQuantity.isEnabled=enable
            tvProgress.visibility=if(enable) View.INVISIBLE else View.VISIBLE
            pbUpload.visibility=if(enable)View.INVISIBLE else View.VISIBLE
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        bind=null
    }
}
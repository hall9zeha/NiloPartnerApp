package com.barryzea.nilopartner.dialogs

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
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

            positiveButton?.setOnClickListener {
                enableUI(false)
                uploadImage(product?.id) { eventPost ->
                    if (eventPost.isSuccess == true) {
                        bind?.let {
                            if (product == null) {
                                val product = Product(
                                    name = it.etName.text.toString().trim(),
                                    description = it.etDescription.text.toString(),
                                    quantity = it.etQuantity.text.toString().toInt(),
                                    price = it.etPrice.text.toString().toDouble(),
                                    imgUrl = eventPost.photoUrl
                                )
                                saveProduct(product,eventPost.documentId!!)
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

            }
            negativeButton?.setOnClickListener { dismiss() }
        }
    }

    private fun initProduct() {
       product=(activity as? MainAux)?.getProductSelected()
        product?.let{product->
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
    private fun saveProduct(product:Product, documentId:String){
        val db=FirebaseFirestore.getInstance()
        //le setearemos manualmente el id del documento ya que debemos esperar a que suba la imagen
        db.collection(COLLECTION_PRODUCT)
            //.add(product)
            .document(documentId)
            .set(product)
            .addOnSuccessListener {
                Toast.makeText(context, "producto agregado", Toast.LENGTH_SHORT).show()
            }
            .addOnCompleteListener {
                enableUI(true)
                bind?.pbUpload?.visibility=View.INVISIBLE
                dismiss()
            }
            .addOnFailureListener {
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
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        bind=null
    }
}
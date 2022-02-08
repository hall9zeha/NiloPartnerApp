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
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.barryzea.nilopartner.EventPost
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
            bind?.imgProductPreview?.setImageURI(photoSelectedUri)
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
                uploadImage() { eventPost ->
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
                                    imgUrl = product?.imgUrl
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
            Glide.with(requireActivity())
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

    private fun uploadImage(callback:(EventPost)->Unit){

        val eventPost=EventPost()
        eventPost.documentId=FirebaseFirestore.getInstance().collection(Constants.COLLECTION_PRODUCT)
            .document().id
        val storageRef=FirebaseStorage.getInstance().reference.child(Constants.PRODUCT_IMAGE)
        photoSelectedUri?.let{uri->
            bind?.let{bind->
                val photoRef=storageRef.child(eventPost.documentId!!)
                photoRef.putFile(uri)
                    .addOnSuccessListener {
                        it.storage.downloadUrl.addOnSuccessListener {downloadUrl->

                            eventPost.isSuccess=true
                            eventPost.photoUrl=downloadUrl.toString()
                            callback(eventPost)

                        }
                    }
                    .addOnFailureListener{
                        eventPost.isSuccess=false
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
            .addOnFailureListener {
                Toast.makeText(context, "error al insertar", Toast.LENGTH_SHORT).show()
            }
            .addOnCompleteListener {
                enableUI(true)
                dismiss()
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
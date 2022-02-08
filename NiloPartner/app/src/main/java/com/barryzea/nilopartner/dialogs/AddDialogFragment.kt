package com.barryzea.nilopartner.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.barryzea.nilopartner.commons.Constants.COLLECTION_PRODUCT
import com.barryzea.nilopartner.databinding.FragmentDialogAddBinding
import com.barryzea.nilopartner.interfaces.MainAux
import com.barryzea.nilopartner.pojo.Product
import com.google.firebase.firestore.FirebaseFirestore

class AddDialogFragment:DialogFragment(), DialogInterface.OnShowListener {

    private var bind:FragmentDialogAddBinding?=null
    private var positiveButton:Button?=null
    private var negativeButton:Button?=null
    private var product:Product?=null
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
        val dialog = dialog as? AlertDialog
        dialog?.let{
            positiveButton=it.getButton(Dialog.BUTTON_POSITIVE)
            negativeButton=it.getButton(Dialog.BUTTON_NEGATIVE)

            positiveButton?.setOnClickListener {
                enableUI(false)
                bind?.let {
                    if (product == null) {
                        val product = Product(
                            name = it.etName.text.toString().trim(),
                            description = it.etDescription.text.toString(),
                            quantity = it.etQuantity.text.toString().toInt(),
                            price = it.etPrice.text.toString().toDouble()
                        )
                        saveProduct(product)
                    }
                    else{
                        product?.apply{
                            name =it.etName.text.toString().trim()
                            description=it.etDescription.text.toString()
                            quantity=it.etQuantity.text.toString().toInt()
                            price=it.etPrice.text.toString().toDouble()

                            updateProduct(this)
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
        }
    }

    private fun saveProduct(product:Product){
        val db=FirebaseFirestore.getInstance()
        db.collection(COLLECTION_PRODUCT)
            .add(product)
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
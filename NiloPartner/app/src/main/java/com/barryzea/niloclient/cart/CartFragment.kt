package com.barryzea.niloclient.cart

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.barryzea.niloclient.R
import com.barryzea.niloclient.adapters.ProductCartAdapter
import com.barryzea.niloclient.commons.Constants
import com.barryzea.niloclient.databinding.FragmentCartBinding
import com.barryzea.niloclient.interfaces.MainAux
import com.barryzea.niloclient.interfaces.OnCartListener
import com.barryzea.niloclient.order.OrderActivity
import com.barryzea.niloclient.pojo.Order
import com.barryzea.niloclient.pojo.Product
import com.barryzea.niloclient.pojo.ProductOrder
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class CartFragment : BottomSheetDialogFragment(),  OnCartListener {
    private var bind: FragmentCartBinding?=null
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<*>
    private lateinit var adapter:ProductCartAdapter
    private var totalPrice=0.0
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        bind= FragmentCartBinding.inflate(LayoutInflater.from(activity))
        bind?.let{
            val bottomSheetDialog=super.onCreateDialog(savedInstanceState) as BottomSheetDialog
            bottomSheetDialog.setContentView(it.root)

            bottomSheetBehavior= BottomSheetBehavior.from(it.root.parent as View)
            bottomSheetBehavior.state=BottomSheetBehavior.STATE_EXPANDED
            setupRecyclerView()
            setupButtons()
            getProducts()
            setupAnalytics()

            return bottomSheetDialog
        }
        return super.onCreateDialog(savedInstanceState)
    }
    private fun setupRecyclerView(){
        bind?.let{
            adapter= ProductCartAdapter(arrayListOf(), this)
            it.recyclerView.apply{
                layoutManager=LinearLayoutManager(context)
                adapter=this@CartFragment.adapter
            }
        }
    }
    private fun setupButtons(){
        bind?.let{
            it.ibCancel.setOnClickListener {
                bottomSheetBehavior.state=BottomSheetBehavior.STATE_HIDDEN
            }
            it.efab.setOnClickListener {
                //requestOrder()
                /*
                * implementaremos la consulta con una transacción para reducir el inventario si hacemos una compra
                * */
                requestOrderTransaction()
            }
        }
    }

    private fun requestOrderTransaction() {
        val user=FirebaseAuth.getInstance().currentUser
        user?.let{myUser->
            enableUI(false)
            val products= hashMapOf<String, ProductOrder>()
            adapter.getProducts().forEach { product ->
                products.put(product.id!!,
                    ProductOrder(product.id!!,product.name!!, product.newQuantity!!)
                )
            }
            val order= Order(clientId = myUser.uid,products=products, totalPrice = totalPrice, status = 1)

            val db=FirebaseFirestore.getInstance()

            //con transacciones
            val requestDoc =db.collection(Constants.COLLECTION_REQUESTS).document()
            val productsRef=db.collection(Constants.COLLECTION_PRODUCT)
            //transaccion por lotes
            db.runBatch { batch->
                batch.set(requestDoc, order)
                order.products.forEach {
                    batch.update(productsRef.document(it.key),Constants.QUANTITY,
                        FieldValue.increment(-it.value.quantity.toLong()))
                }
            }
            //con transacciones ahora continuará desde el batch que es la transacción por lotes
          /*  db.collection(Constants.COLLECTION_REQUESTS)
                .add(order)*/
                .addOnSuccessListener {
                    dismiss()
                    (activity as MainAux)?.clearCart()
                    startActivity(Intent(context, OrderActivity::class.java))
                    Toast.makeText(activity, "Compra realizada", Toast.LENGTH_SHORT).show()

                    //Analytics
                    firebaseAnalytics.logEvent(FirebaseAnalytics.Event.ADD_PAYMENT_INFO){
                        val products= mutableListOf<Bundle>()
                        order.products.forEach {
                            if(it.value.quantity>5) {
                                val bundle = Bundle()
                                bundle.putString("id_product", it.key)
                                products.add(bundle)
                            }
                        }
                        param(FirebaseAnalytics.Param.QUANTITY, products.toTypedArray())

                    }
                    firebaseAnalytics.setUserProperty(Constants.USER_PROP_QUANTITY,
                    if(products.size>0)"con_mayoreo" else "sin_mayoreo")
                }
                .addOnFailureListener {
                    Toast.makeText(activity, "Error al realizar la orden", Toast.LENGTH_SHORT).show()
                }
                .addOnCompleteListener {
                    enableUI(true)
                }
        }

     
    }

    private fun requestOrder() {
        val user=FirebaseAuth.getInstance().currentUser
        user?.let{myUser->
            enableUI(false)
            val products= hashMapOf<String, ProductOrder>()
            adapter.getProducts().forEach { product ->
                products.put(product.id!!,
                    ProductOrder(product.id!!,product.name!!, product.newQuantity!!)
                )
            }
            val order= Order(clientId = myUser.uid,products=products, totalPrice = totalPrice, status = 1)

            val db=FirebaseFirestore.getInstance()
            db.collection(Constants.COLLECTION_REQUESTS)
                .add(order)
                .addOnSuccessListener {
                    dismiss()
                    (activity as MainAux)?.clearCart()
                    startActivity(Intent(context, OrderActivity::class.java))
                    Toast.makeText(activity, "Compra realizada", Toast.LENGTH_SHORT).show()

                    //Analytics
                    firebaseAnalytics.logEvent(FirebaseAnalytics.Event.ADD_PAYMENT_INFO){
                        val products= mutableListOf<Bundle>()
                        order.products.forEach {
                            if(it.value.quantity>5) {
                                val bundle = Bundle()
                                bundle.putString("id_product", it.key)
                                products.add(bundle)
                            }
                        }
                        param(FirebaseAnalytics.Param.QUANTITY, products.toTypedArray())

                    }
                    firebaseAnalytics.setUserProperty(Constants.USER_PROP_QUANTITY,
                        if(products.size>0)"con_mayoreo" else "sin_mayoreo")
                }
                .addOnFailureListener {
                    Toast.makeText(activity, "Error al realizar la orden", Toast.LENGTH_SHORT).show()
                }
                .addOnCompleteListener {
                    enableUI(true)
                }
        }


    }
    private fun enableUI(enable:Boolean){
        bind?.let{
            it.ibCancel.isEnabled=enable
            it.efab.isEnabled=enable
        }
    }
    private fun getProducts(){
        (activity as MainAux)?.getProductCart()?.forEach {
            adapter.add(it)
        }
    }
    private fun setupAnalytics(){
        firebaseAnalytics= Firebase.analytics
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW){
            param(FirebaseAnalytics.Param.METHOD,"check_track")
        }
    }
    override fun onDestroyView() {
        (activity as MainAux)?.updateTotal()
        super.onDestroyView()
        bind=null
    }

    override fun setQuantity(product: Product) {
        adapter.update(product)
    }

    override fun showTotal(total: Double) {
       totalPrice=total
        bind?.let{
            it.tvTotal.text=getString(R.string.car_full, total)
        }
    }

}
package com.barryzea.nilopartner.order

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.barryzea.nilopartner.R

import com.barryzea.nilopartner.adapters.OrderAdapter
import com.barryzea.nilopartner.chat.ChatFragment
import com.barryzea.nilopartner.commons.Constants
import com.barryzea.nilopartner.databinding.ActivityOrderBinding
import com.barryzea.nilopartner.fcm.NotificationRS
import com.barryzea.nilopartner.interfaces.OnOrderListener
import com.barryzea.nilopartner.interfaces.OrderAux
import com.barryzea.nilopartner.pojo.Order
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase

class OrderActivity : AppCompatActivity(), OnOrderListener, OrderAux {
    private lateinit var bind:ActivityOrderBinding
    private lateinit var adapter:OrderAdapter
    private lateinit var orderSelected:Order
    private lateinit var firebaseAnalytics:FirebaseAnalytics
    private val aValues:Array<String> by lazy {
        resources.getStringArray(R.array.status_value)
    }
    private val aKeys:Array<Int> by lazy{
       resources.getIntArray(R.array.status_key).toTypedArray()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind= ActivityOrderBinding.inflate(layoutInflater)
        setContentView(bind.root)

        setupRecyclerView()
        setupFirebase()
        setupAnalytics()
    }


    private fun setupRecyclerView() {
        adapter= OrderAdapter(mutableListOf(), this)
        bind.recyclerView.apply {
            layoutManager=LinearLayoutManager(this@OrderActivity)
            adapter=this@OrderActivity.adapter
        }
    }
    private fun setupFirebase() {
        //Agregando oedenamiento don orderby esto ya lo habías implementado antes
       val db=FirebaseFirestore.getInstance()
        db.collection(Constants.COLLECTION_REQUESTS)
            .orderBy(Constants.TIMESTAMP, Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener {
                for(document in it){
                    val order=document.toObject(Order::class.java)
                    order.id=document.id
                    adapter.add(order)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al consultar datos", Toast.LENGTH_SHORT).show()
            }
    }
    private fun setupAnalytics(){
        firebaseAnalytics=Firebase.analytics
    }
    private fun notifyClient(order: Order){
        val db = FirebaseFirestore.getInstance()
        db.collection(Constants.COLLECTION_USERS)
            .document(order.clientId)
            .collection(Constants.COLLECTION_TOKENS)
            .get()
            .addOnSuccessListener {
                    var tokensStr=""
                for(document in it){
                    val tokenMap=document.data
                    tokensStr +="${tokenMap.getValue(Constants.PROPERTY_TOKEN)},"

                }
                if(tokensStr.length>0)tokensStr=tokensStr.dropLast(1)
                var names=""
                order.products.forEach{
                    names+="${it.value.name}, "
                }
                names=names.dropLast(2)
                val index=aKeys.indexOf(order.status)

                val notificationRS=NotificationRS()
                notificationRS.sendNotification("Tu pedido ha sido ${aValues[index]}",names,tokensStr,order.id,order.status)
            }
            .addOnFailureListener {

            }
    }

    override fun onStatusChange(order: Order) {
        val db=FirebaseFirestore.getInstance()
        db.collection(Constants.COLLECTION_REQUESTS)
            .document(order.id)
            .update(Constants.PROPERTY_STATUS, order.status)
            .addOnSuccessListener {
                Toast.makeText(this, "Orden Actualizada", Toast.LENGTH_SHORT).show()
                notifyClient(order)

                //Analytics enviando nuevos parámetros cuando se hace un envío que ya ha sido procesado
                //usamos un bundle porque es el formato requerido por analytics
                firebaseAnalytics.logEvent(FirebaseAnalytics.Event.ADD_SHIPPING_INFO){
                    val products= mutableListOf<Bundle>()
                    order.products.forEach{
                        val bundle=Bundle()
                        bundle.putString("id_product", it.key)
                        products.add(bundle)
                    }
                    param(FirebaseAnalytics.Param.SHIPPING, products.toTypedArray())
                    param(FirebaseAnalytics.Param.PRICE, order.totalPrice)


                }

            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al actualizar orden", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onStartChat(order: Order) {
        orderSelected=order
       val fragment=ChatFragment()
        supportFragmentManager
            .beginTransaction()
            .add(R.id.containerMainOrder,fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun getOrderSelected(): Order =orderSelected
}
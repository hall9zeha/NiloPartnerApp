package com.barryzea.niloclient.order

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.barryzea.niloclient.R
import com.barryzea.niloclient.adapters.OrderAdapter
import com.barryzea.niloclient.chat.ChatFragment
import com.barryzea.niloclient.commons.Constants

import com.barryzea.niloclient.interfaces.OnOrderListener
import com.barryzea.niloclient.interfaces.OrderAux
import com.barryzea.niloclient.pojo.Order
import com.barryzea.niloclient.track.TrackFragment
import com.barryzea.niloclient.databinding.ActivityOrderBinding
import com.google.firebase.auth.FirebaseAuth

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class OrderActivity : AppCompatActivity(), OnOrderListener, OrderAux {
        private lateinit var bind: ActivityOrderBinding
        private lateinit var adapter:OrderAdapter
        private lateinit var orderSelected:Order
        private var clientId:String?=null
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        bind= ActivityOrderBinding.inflate(layoutInflater)
        setContentView(bind.root)

        setupRecyclerView()
        setupFirestore()
        checkIntent(intent)
    }

    private fun checkIntent(intent: Intent?) {
        intent?.let{
            val id=it.getStringExtra("id")?:""
            val status=intent.getIntExtra("status", 0)
            orderSelected= Order(id=id, status=status)
            val fragment=TrackFragment()

            supportFragmentManager
                .beginTransaction()
                .add(R.id.containerMainOrder, fragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun setupRecyclerView() {
        adapter= OrderAdapter(mutableListOf(), this)

        bind.recyclerView.apply {
            FirebaseAuth.getInstance().currentUser?.let{
                clientId=it.uid

                layoutManager=LinearLayoutManager(this@OrderActivity)
                adapter=this@OrderActivity.adapter

            }
        }
    }
    private fun setupFirestore(){
        val db=FirebaseFirestore.getInstance()
        db.collection(Constants.COLLECTION_REQUESTS)
             //mostramos solamente las compras de cada cliente como en cobra
            //.whereEqualTo(Constants.CLIENT_ID,clientId.toString())
            //.orderBy(Constants.TIMESTAMP, Query.Direction.DESCENDING)
            //consultamos el campo que tenga los dos parámetros siguientes como si fuera un or
            //.whereIn(Constants.STATUS, listOf(3,2))
            //ahora al revés no traerá los que designemos
            //.whereNotIn(Constants.STATUS, listOf(3,2))
            //filtrar campos si el elemento es mayor que
            //.whereGreaterThan(Constants.STATUS, 3)
            //ahora menor que
            //.whereLessThan(Constants.STATUS, 3)
            //mayor o igual que
            //.whereGreaterThanOrEqualTo(Constants.STATUS,3)
            //menor o igual que
            //.whereLessThanOrEqualTo(Constants.STATUS,3)
            //índice más complejo con cuatro filtros, creo que no es nada nuevo
            .whereEqualTo(Constants.CLIENT_ID,clientId)
            .orderBy(Constants.STATUS, Query.Direction.ASCENDING)
            .whereLessThan(Constants.STATUS, 4)
            .orderBy(Constants.TIMESTAMP,Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener {
                for(document in it){
                    val order=document.toObject(Order::class.java)
                    order.id=document.id

                    adapter.add(order)
                }
            }
    }

    override fun onTrack(order: Order) {

        orderSelected=order
       val fragment=TrackFragment()

        supportFragmentManager
            .beginTransaction()
            .add(R.id.containerMainOrder, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onStartChat(order: Order) {
        orderSelected=order
        val fragment=ChatFragment()
        supportFragmentManager
            .beginTransaction()
            .add(R.id.containerMainOrder, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun getOrderSelected(): Order=orderSelected
}
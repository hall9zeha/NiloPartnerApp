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
import com.barryzea.nilopartner.interfaces.OnOrderListener
import com.barryzea.nilopartner.interfaces.OrderAux
import com.barryzea.nilopartner.pojo.Order
import com.google.firebase.firestore.FirebaseFirestore

class OrderActivity : AppCompatActivity(), OnOrderListener, OrderAux {
    private lateinit var bind:ActivityOrderBinding
    private lateinit var adapter:OrderAdapter
    private lateinit var orderSelected:Order

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind= ActivityOrderBinding.inflate(layoutInflater)
        setContentView(bind.root)

        setupRecyclerView()
        setupFirebase()
    }


    private fun setupRecyclerView() {
        adapter= OrderAdapter(mutableListOf(), this)
        bind.recyclerView.apply {
            layoutManager=LinearLayoutManager(this@OrderActivity)
            adapter=this@OrderActivity.adapter
        }
    }
    private fun setupFirebase() {
       val db=FirebaseFirestore.getInstance()
        db.collection(Constants.COLLECTION_REQUESTS)
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

    override fun onStatusChange(order: Order) {
        val db=FirebaseFirestore.getInstance()
        db.collection(Constants.COLLECTION_REQUESTS)
            .document(order.id)
            .update(Constants.PROPERTY_STATUS, order.status)
            .addOnSuccessListener {
                Toast.makeText(this, "Orden Actualizada", Toast.LENGTH_SHORT).show()
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
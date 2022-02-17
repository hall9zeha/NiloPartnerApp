package com.barryzea.niloclient.order

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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

import com.google.firebase.firestore.FirebaseFirestore

class OrderActivity : AppCompatActivity(), OnOrderListener, OrderAux {
        private lateinit var bind: ActivityOrderBinding
        private lateinit var adapter:OrderAdapter
        private lateinit var orderSelected:Order
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        bind= ActivityOrderBinding.inflate(layoutInflater)
        setContentView(bind.root)

        setupRecyclerView()
        setupFirestore()
    }

    private fun setupRecyclerView() {
        adapter= OrderAdapter(mutableListOf(), this)

        bind.recyclerView.apply {
            layoutManager=LinearLayoutManager(this@OrderActivity)
            adapter=this@OrderActivity.adapter
        }
    }
    private fun setupFirestore(){
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
package com.barryzea.nilopartner.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.RecyclerView

import com.barryzea.nilopartner.R
import com.barryzea.nilopartner.databinding.ItemOrderBinding
import com.barryzea.nilopartner.interfaces.OnOrderListener

import com.barryzea.nilopartner.pojo.Order

class OrderAdapter(private val orderList:MutableList<Order>, private val listener: OnOrderListener):
    RecyclerView.Adapter<OrderAdapter.ViewHolder>() {
    private lateinit var context: Context
    private val aValues:Array<String> by lazy {
        context.resources.getStringArray(R.array.status_value)
    }
    private val aKeys:Array<Int> by lazy{
        context.resources.getIntArray(R.array.status_key).toTypedArray()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context=parent.context
        val view= LayoutInflater.from(context).inflate(R.layout.item_order, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val order=orderList[position]
        holder.setListener(order)
        holder.bind.tvId.text=context.getString(R.string.order_id,order.id)
        var names=""
        order.products.forEach {
            names+= "${it.value.name}, "
        }

        val index=aKeys.indexOf(order.status)

        //Ahora usamos dropLast para eliminar los dos últimos caracteres de la cadena
        holder.bind.tvProductNames.text=names.dropLast(2)
        holder.bind.tvTotalPrice.text=context.getString(R.string.car_full,order.totalPrice)

        //alimentando el spinner
        val arrayAdapter=ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line, aValues)
        holder.bind.actvStatus.setAdapter(arrayAdapter)
        if(index !=-1){
            holder.bind.actvStatus.setText(aValues[index], false)
        }
        else{
            holder.bind.actvStatus.setText(context.getString(R.string.order_status_unknown), false)
        }
    }
    fun add(order:Order){
        orderList.add(order)
        notifyItemInserted(orderList.size -1)
    }
    override fun getItemCount(): Int=orderList.size

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val bind= ItemOrderBinding.bind(view)

        fun setListener(order:Order){
            bind.actvStatus.setOnItemClickListener { adapterView, view, position, id ->
                order.status=aKeys[position]
                listener.onStatusChange(order)
            }
            bind.chipChat.setOnClickListener{listener.onStartChat(order)}
        }
    }
}
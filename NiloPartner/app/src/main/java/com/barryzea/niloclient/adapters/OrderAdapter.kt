package com.barryzea.niloclient.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.barryzea.niloclient.R
import com.barryzea.niloclient.databinding.ItemOrderBinding
import com.barryzea.niloclient.interfaces.OnOrderListener
import com.barryzea.niloclient.pojo.Order

class OrderAdapter(private val orderList:MutableList<Order>,private val listener:OnOrderListener ):RecyclerView.Adapter<OrderAdapter.ViewHolder>() {
    private lateinit var context:Context
    private val aValues:Array<String> by lazy {
        context.resources.getStringArray(R.array.status_value)
    }
    private val aKeys:Array<Int> by lazy{
        context.resources.getIntArray(R.array.status_key).toTypedArray()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
       context=parent.context
        val view=LayoutInflater.from(context).inflate(R.layout.item_order, parent, false)
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
        val statusStr=if(index != -1)aValues[index] else context.getString(R.string.order_status_unknown)
        //Ahora usamos dropLast para eliminar los dos últimos caracteres de la cadena
        holder.bind.tvProductNames.text=names.dropLast(2)
        holder.bind.tvTotalPrice.text=context.getString(R.string.car_full,order.totalPrice)
        holder.bind.tvStatus.text=context.getString(R.string.order_status,statusStr)
    }
    fun add(order:Order){
        orderList.add(order)
        notifyItemInserted(orderList.size -1)
    }
    override fun getItemCount(): Int=orderList.size

    inner class ViewHolder(view: View):RecyclerView.ViewHolder(view){
        val bind=ItemOrderBinding.bind(view)

        fun setListener(order:Order){
            bind.btnTrack.setOnClickListener { listener.onTrack(order) }
            bind.chipChat.setOnClickListener{listener.onStartChat(order)}
        }
    }
}
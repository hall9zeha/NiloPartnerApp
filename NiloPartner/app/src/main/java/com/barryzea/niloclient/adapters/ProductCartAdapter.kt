package com.barryzea.niloclient.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.barryzea.niloclient.R
import com.barryzea.niloclient.databinding.ItemProductCartBinding
import com.barryzea.niloclient.interfaces.OnCartListener
import com.barryzea.niloclient.pojo.Product
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions

class ProductCartAdapter(private val productList:MutableList<Product>, private val listener:OnCartListener):RecyclerView.Adapter<ProductCartAdapter.ViewHolder>() {
    private lateinit var context:Context

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
       context=parent.context
        val view=LayoutInflater.from(context).inflate(R.layout.item_product_cart,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product=productList[position]
        holder.setListener(product)
        holder.bind.tvName.text=product.name
        holder.bind.tvQuantity.text=product.quantity.toString()

        val requestOptions = RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .centerCrop()

        Glide.with(context)
            .load(product.imgUrl)
            .apply(requestOptions)
            .circleCrop()
            .into(holder.bind.imgProduct)
    }
    fun add(product:Product){
        if(!productList.contains(product)){
            productList.add(product)
            notifyItemInserted(productList.size-1)
        }
        else{
            update(product)
        }
    }
    fun update(product:Product){
        val index=productList.indexOf(product)
        if(index !=-1){
            productList[index] = product
            notifyItemChanged(index)
        }
    }
    fun delete(product:Product){
        val index=productList.indexOf(product)
        if(index !=-1){
            productList.removeAt(index)
            notifyItemRemoved(index)
        }
    }
    override fun getItemCount(): Int=  productList?.size

    inner class ViewHolder(view:View):RecyclerView.ViewHolder(view){
        val bind=ItemProductCartBinding.bind(view)

        fun setListener(product:Product){
            bind.ibSum.setOnClickListener {
                listener.setQuantity(product)
            }
            bind.ibSub.setOnClickListener {
                listener.setQuantity(product)
            }
        }
    }
}
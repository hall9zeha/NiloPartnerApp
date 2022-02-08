package com.barryzea.nilopartner.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.barryzea.nilopartner.R
import com.barryzea.nilopartner.databinding.ItemProductBinding
import com.barryzea.nilopartner.interfaces.OnProductListener
import com.barryzea.nilopartner.pojo.Product
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import java.security.AllPermission

class ProductAdapter (private val products:MutableList<Product>, private val listener:OnProductListener):RecyclerView.Adapter<ProductAdapter.ViewHolder>(){
    private lateinit var context: Context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context=parent.context
        val view= LayoutInflater.from(context).inflate(R.layout.item_product, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product= products[position]
        holder.setListener(product)
        holder.bind.tvName.text=product.name
        holder.bind.tvPrice.text=product.price.toString()
        holder.bind.tvQuantity.text=product.quantity.toString()

        val requestOptions =RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .centerCrop()

        Glide.with(context)
            .load(product.imgUrl)
            .apply(requestOptions)
            .into(holder.bind.imgProduct)

    }

    fun add(product:Product){
        if(!products.contains(product)){
            products.add(product)
            notifyItemInserted(products.size-1)
        }
        else{
            update(product)
        }
    }
    fun update(product:Product){
        val index=products.indexOf(product)
        if(index !=-1){
            products[index] = product
            notifyItemChanged(index)
        }
    }
    fun delete(product:Product){
        val index=products.indexOf(product)
        if(index !=-1){
            products.removeAt(index)
            notifyItemRemoved(index)
        }
    }
    override fun getItemCount(): Int = products.let{products.size}

    /*
        * al poner inner a una clase anidada esta podrá acceder a los elementos de la clase externa, la que la contiene
        * a diferencia de java que no debemos poner inner
        * */
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val bind=ItemProductBinding.bind(itemView)

        fun setListener(product:Product){
            bind.root.setOnClickListener { listener.onClick(product) }
            bind.root.setOnLongClickListener { listener.onLongClick(product); true }
        }

    }
}
package com.barryzea.niloclient.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.barryzea.niloclient.R
import com.barryzea.niloclient.databinding.FragmentDetailBinding
import com.barryzea.niloclient.interfaces.MainAux
import com.barryzea.niloclient.pojo.Product
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class DetailFragment : Fragment() {

    private var bind:FragmentDetailBinding?=null
    private var product: Product?=null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        bind= FragmentDetailBinding.inflate(inflater, container, false)
        bind?.let{
            return it.root
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getProduct()
        setupButtons()
    }

    private fun getProduct() {
       product=(activity as MainAux)?.getProductSelected()
        product?.let{product->
            bind?.let{
                it.tvName.text=product.name
                it.tvDescription.text=product.description
                it.tvQuantity.text=getString(R.string.detail_quantity, product.quantity)

                setNewQuantity(product)

                Glide.with(this)
                    .load(product.imgUrl)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .placeholder(R.drawable.ic_image_search)
                    .error(R.drawable.ic_broken_image)
                    .into(it.imgProduct)
            }
        }
    }

    private fun setNewQuantity(product: Product) {
            bind?.let{
                it.edtNewQuantity.setText(product.newQuantity.toString())
                it.tvTotalPrice.text=getString(R.string.total_price,product.totalPrice(),
                    product.newQuantity, product.price)
            }
    }
    private fun setupButtons(){
        product?.let{product->
            bind?.let{bind->
                bind.ibSub.setOnClickListener {
                    if (product.newQuantity > 1) {
                        product.newQuantity -=1
                        setNewQuantity(product)
                    }
                }
                bind.ibSum.setOnClickListener {
                    if(product.newQuantity < product.quantity){
                        product.newQuantity +=1
                        setNewQuantity(product)
                    }
                }
                bind.efabAddCart.setOnClickListener{view->
                    product.newQuantity=bind.edtNewQuantity.text.toString().toInt()

                    addProductToCart(product)
                }

            }
        }
    }

    private fun addProductToCart(product: Product) {
        (activity as MainAux)?.let{
            it.addProductToCart(product)
            activity?.onBackPressed()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bind=null
        (activity as MainAux)?.showButton(true)

    }
}
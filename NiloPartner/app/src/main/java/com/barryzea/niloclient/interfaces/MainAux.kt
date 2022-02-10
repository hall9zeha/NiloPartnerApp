package com.barryzea.niloclient.interfaces

import com.barryzea.niloclient.pojo.Product

interface MainAux {
   fun getProductCart(): MutableList<Product>
   fun getProductSelected():Product?
   fun showButton(isVisible:Boolean)
   fun addProductToCart(product:Product)
   fun updateTotal()
}
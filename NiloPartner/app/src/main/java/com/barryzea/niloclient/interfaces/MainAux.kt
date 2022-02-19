package com.barryzea.niloclient.interfaces

import com.barryzea.niloclient.pojo.Product
import com.google.firebase.auth.FirebaseUser

interface MainAux {
   fun getProductCart(): MutableList<Product>
   fun getProductSelected():Product?
   fun showButton(isVisible:Boolean)
   fun addProductToCart(product:Product)
   fun updateTotal()
   fun clearCart()
   fun updateTitle(user:FirebaseUser)
}
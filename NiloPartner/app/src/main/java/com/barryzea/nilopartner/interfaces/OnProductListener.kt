package com.barryzea.nilopartner.interfaces

import com.barryzea.nilopartner.pojo.Product

interface OnProductListener {
    fun onClick(product: Product)
    fun onLongClick(product:Product)
}
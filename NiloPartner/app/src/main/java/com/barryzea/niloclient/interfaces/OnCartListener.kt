package com.barryzea.niloclient.interfaces

import com.barryzea.niloclient.pojo.Product

interface OnCartListener {
    fun setQuantity(product:Product)
    fun showTotal(total:Double)
}
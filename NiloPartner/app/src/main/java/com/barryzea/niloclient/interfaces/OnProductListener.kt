package com.barryzea.niloclient.interfaces

import com.barryzea.niloclient.pojo.Product

interface OnProductListener {
    fun onClick(product: Product)

}
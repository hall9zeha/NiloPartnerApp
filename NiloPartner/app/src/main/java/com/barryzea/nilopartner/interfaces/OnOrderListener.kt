package com.barryzea.nilopartner.interfaces

import com.barryzea.nilopartner.pojo.Order

interface OnOrderListener {
        fun onStatusChange(order:Order)
        fun onStartChat(order: Order)


}
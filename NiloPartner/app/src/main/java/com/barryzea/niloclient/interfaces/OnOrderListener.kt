package com.barryzea.niloclient.interfaces

import com.barryzea.niloclient.pojo.Order

interface OnOrderListener {
   fun onTrack(order: Order)
   fun onStartChat(order:Order)

}
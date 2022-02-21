package com.barryzea.niloclient.pojo

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp

data class Order(@get:Exclude var id:String="",
                var clientId:String="",
                var products:Map<String , ProductOrder> = hashMapOf(),
                var totalPrice:Double=0.0,
                var status: Int=0,
                 //con esta anotación firestore insertará automáticamente la hora del servidor en nuestro registro
                 @ServerTimestamp var timestamp: Timestamp?=null){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Order

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}

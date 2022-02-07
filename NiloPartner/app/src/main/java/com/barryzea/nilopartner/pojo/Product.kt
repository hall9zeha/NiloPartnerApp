package com.barryzea.nilopartner.pojo

data class Product(
    var id:String?="",
    var name:String?="",
    var description:String?="",
    var imgUrl:String?="",
    var quantity:Int=0,
    var price:Double=0.0

) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Product

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}

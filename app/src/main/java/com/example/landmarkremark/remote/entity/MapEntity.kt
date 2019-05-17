package com.example.landmarkremark.remote.entity

import java.io.Serializable

data class MapEntity(
    var userName : String,
    var notes : String?,
    var latitude : Double,
    var longitude : Double) : Serializable {
    constructor(): this("","",0.0, 0.0 )
}
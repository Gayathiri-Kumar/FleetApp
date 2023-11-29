package com.example.rurutektrack


data class TimelineData(val user: String,
                        val model: String,
                        val startkm: String,
                        val start_time: String?,
                        val endkm: String,
                        val end_time: String?,
                        val place: String)

class Datamodel(
    var name: String,
    var si: String,
    var simg: String,
    var eImg: String,
    var plc: String
)

class Drivermodel( var un: String,
                   var emp: String,
                   var pwd: String
)

class Vehiclemodel( var vehicle: String)
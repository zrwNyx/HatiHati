package com.dicoding.picodiploma.ha.Model

data class Report(
    var Reporter : String = " ",
    var Category : String = " ",
    var Dates : String = " ",
    var Hours : Int = 0,
    var Label : Int = 0,
    var X : Double= 0.00 ,
    var Y : Double= 0.00
)

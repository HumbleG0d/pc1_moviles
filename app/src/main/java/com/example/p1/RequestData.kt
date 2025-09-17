package com.example.p1


data class RequestData(
    val data: List<Double>,
    val data2: List<Int>
)

data class ResponseData(
    val prediction: List<Int>
)
package com.example.bottomnavigation.data

data class AddressResponse(
    val results: Results
)

data class Results(
    val common: Common,
    val juso: List<Juso>
)

data class Common(
    val totalCount: String,
    val currentPage: String,
    val countPerPage: String,
    val errorCode: String,
    val errorMessage: String
)

data class Juso(
    val roadAddr: String,
    val roadAddrPart1: String,
    val roadAddrPart2: String,
    val jibunAddr: String,
    val engAddr: String,
    val zipNo: String,
    val admCd: String,
    val siNm: String,
    val sggNm: String,
    val emdNm: String
)


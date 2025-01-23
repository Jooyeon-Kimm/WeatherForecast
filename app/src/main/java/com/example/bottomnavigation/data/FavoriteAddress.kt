package com.example.bottomnavigation.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class FavoriteAddress(
    val title : String,
    val descr : String,
    val isBookmarked: Boolean,
    var isChecked: Boolean = false, //기본값 false
){
    @PrimaryKey(autoGenerate = true) var id: Int = 0 // DB 인덱스, 자동생성
}
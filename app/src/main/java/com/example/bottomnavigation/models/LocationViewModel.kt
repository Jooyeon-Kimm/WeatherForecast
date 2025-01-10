package com.example.bottomnavigation.models

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class LocationViewModel : ViewModel() {

    private val _location = MutableStateFlow<Location?>(null)
    val location: StateFlow<Location?> get() = _location

    fun fetchLocation(context: Context, fusedLocationProviderClient: FusedLocationProviderClient) {
        viewModelScope.launch {
            val result = getLocation(context, fusedLocationProviderClient)
            _location.value = result
        }
    }

    // 현재 프래그먼트에서 위치정보 계속 가져오기
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun getLocation(context: Context, fusedLocationProviderClient: FusedLocationProviderClient): Location? {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(context, "위치 권한이 필요합니다.", Toast.LENGTH_LONG).show()
            return null
        }

        return suspendCancellableCoroutine { continuation ->
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    continuation.resume(location)
                } else {
                    Toast.makeText(context, "위치 정보를 가져올 수 없습니다.", Toast.LENGTH_LONG).show()
                    continuation.resume(null)
                }
            }.addOnFailureListener {
                Toast.makeText(context, "위치 정보를 가져오는데 실패했습니다.", Toast.LENGTH_LONG).show()
                continuation.resume(null)
            }
        }
    }

}

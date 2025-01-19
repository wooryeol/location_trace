package kr.co.example.tracer

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.naver.maps.geometry.LatLng
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class LocationViewModel(context: Context):ViewModel() {
    private val _result = MutableLiveData<LatLng>()
    val result: LiveData<LatLng> get() = _result

    private var fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    private var locationJob: Job? = null

    @SuppressLint("MissingPermission")
    fun getLocation() {
        locationJob?.cancel()
        locationJob = null
        locationJob = viewModelScope.launch {
            while (isActive) {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location: Location? ->
                        if (location != null) {
                            // _result.value = LatLng(location.latitude, location.longitude)
                            _result.postValue(LatLng(location.latitude, location.longitude))
                        } else {
                            Log.d("wooryeol", "location이 null입니다.")
                        }
                    }
                delay(1000)
            }
        }
    }
}
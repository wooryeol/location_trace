package kr.co.example.tracer

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.naver.maps.geometry.LatLng
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.example.tracer.databinding.ActivityMainBinding
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var mBinding : ActivityMainBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var executor: ScheduledExecutorService
    private var scheduledFuture: ScheduledFuture<*>? = null

    private var address: List<String> = listOf("서울특별시", "중구", "명동")
    private var locationInfo = mutableListOf<LatLng>()
    private lateinit var adapter: ArrayAdapter<String>
    private var data: ArrayList<String> = arrayListOf()




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, data)
        mBinding.list.adapter = adapter

        executor = Executors.newSingleThreadScheduledExecutor()
        val task = Runnable {
            checkLocationPermission()
        }

        checkLocationPermission()

        mBinding.recording.setOnClickListener {
            if (scheduledFuture == null || scheduledFuture!!.isCancelled){
                scheduledFuture = executor.scheduleAtFixedRate(task, 0, 1, TimeUnit.SECONDS)
                mBinding.recording.text = getString(R.string.recording_stop)
                mBinding.buttonShowMap.visibility = View.VISIBLE
            } else {
                mBinding.recording.text = getString(R.string.recording)
                scheduledFuture!!.cancel(true)
                scheduledFuture = null
                mBinding.buttonShowMap.visibility = View.GONE
                data.clear()
                adapter.notifyDataSetChanged()
            }
        }

        mBinding.buttonShowMap.setOnClickListener {
            showMap()
        }

    }

    private fun showMap(){
        val manager = supportFragmentManager
        val transaction = manager.beginTransaction()
        val fragment = MapsFragment()
        val bundle = Bundle()
        bundle.putParcelableArrayList("LATLNG", ArrayList(locationInfo))
        fragment.arguments = bundle
        transaction.add(R.id.maps_fragment, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("MissingPermission", "SetTextI18n")
    private fun checkLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        val geocoder = Geocoder(this, Locale.KOREA)
                        GlobalScope.launch(Dispatchers.IO) {
                            val addressList = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                            withContext(Dispatchers.Main) {
                                if (!addressList.isNullOrEmpty()) {
                                    for (addressInfo in addressList) {
                                        val splitAddress = addressInfo.getAddressLine(0).split(" ")
                                        address = splitAddress
                                    }
                                    Log.d("test log", "address >>> $address")
                                    Log.d("test log", "위도: ${location.latitude} / 경도: ${location.longitude}")
                                    createView("위도: ${location.latitude} / 경도: ${location.longitude}")
                                    locationInfo.add(LatLng(location))
                                }
                            }
                        }
                    } else {
                        Log.w("checkLocationPermission", "location이 null입니다.")
                    }
                }
        } else {
            Toast.makeText(this, "위치권한이 없습니다.", Toast.LENGTH_SHORT).show()
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),1000
            )
        }
    }


    private fun createView(location: String) {
        data.add(location)
        adapter.notifyDataSetChanged()
    }

    private val multiplePermissionsCode = 100
    private var gpsGranted = false

    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            multiplePermissionsCode -> {
                if (grantResults.isNotEmpty()) {
                    for ((i, permission) in permissions.withIndex()) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            //권한 획득 실패시 동작
                            Toast.makeText(
                                this,
                                "The user has denied to $permission",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.i("TAG", "I can't work for you anymore then. ByeBye!")
                        } else {
                            gpsGranted = true
                        }
                    }
                }
            }
        }
    }
}
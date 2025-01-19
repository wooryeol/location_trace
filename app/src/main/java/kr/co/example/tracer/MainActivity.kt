package kr.co.example.tracer

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.PathOverlay
import com.naver.maps.map.util.FusedLocationSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kr.co.example.tracer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var mBinding : ActivityMainBinding
    private lateinit var adapter: ArrayAdapter<String>
    private val locationViewModel: LocationViewModel by viewModels {
        LocationViewModelFactory(this)
    }

    private var locationInfo: ArrayList<LatLng> = arrayListOf()
    private var adapterDataList: ArrayList<String> = arrayListOf()
    private lateinit var locationSource: FusedLocationSource

    private var isGranted = false
    private var isClicked = false

    private val permission = object : PermissionListener {
        override fun onPermissionGranted() {
            Toast.makeText(this@MainActivity, "권한 허가", Toast.LENGTH_SHORT).show()
            isGranted = true
        }

        override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
            Toast.makeText(this@MainActivity, "권한 거부", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mBinding.lifecycleOwner = this
        mBinding.viewModel = locationViewModel
        locationSource = FusedLocationSource(this, 1000)

        locationViewModel.result.observe(this, Observer { location ->
            if (isClicked) {
                addLocation(location)
                locationInfo.add(location)
            }
        })

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, adapterDataList)
        mBinding.list.adapter = adapter

        mBinding.recording.setOnClickListener {
            if (isGranted) {
                isClicked = !isClicked
                if (isClicked) {
                    mBinding.recording.text = getString(R.string.recording_stop)
                    adapterDataList.clear()
                    adapter.notifyDataSetChanged()
                    locationViewModel.getLocation()
                    mBinding.buttonShowMap.visibility = View.VISIBLE
                } else {
                    mBinding.recording.text = getString(R.string.recording)
                    mBinding.buttonShowMap.visibility = View.GONE
                }
            } else {
                requestPermission()
            }
        }

        mBinding.buttonShowMap.setOnClickListener {
            showMap()
        }

        requestPermission()
    }

    private fun requestPermission() {
        TedPermission.create()
            .setPermissionListener(permission)
            .setDeniedMessage("권한이 거부되었습니다. 설정 > 권한에서 허용해주세요.")
            .setPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            .check()
    }

    private fun addLocation(location: LatLng) {
        adapterDataList.add("${adapterDataList.size}. 위도: ${location.latitude} / 경도: ${location.longitude}")
        adapter.notifyDataSetChanged()
    }

    private fun showMap(){
        val manager = supportFragmentManager
        val transaction = manager.beginTransaction()
        val fragment = MapsFragment()
        fragment.locationSource = locationSource
        transaction.add(R.id.maps_fragment, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}
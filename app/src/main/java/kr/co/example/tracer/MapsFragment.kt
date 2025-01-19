package kr.co.example.tracer

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.LocationSource
import com.naver.maps.map.LocationTrackingMode
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.PathOverlay
import kr.co.example.tracer.databinding.FragmentMapsBinding

class MapsFragment : Fragment(), OnMapReadyCallback {
    private lateinit var naverMap: NaverMap
    lateinit var locationSource: LocationSource
    private var _mBinding: FragmentMapsBinding? = null
    private val mBinding get() = _mBinding!!
    private var locationInfo: ArrayList<LatLng> = arrayListOf()
    private val locationViewModel: LocationViewModel by activityViewModels()


    // 프래그먼트와 레이아웃을 연결시켜주는 부분
    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _mBinding = FragmentMapsBinding.inflate(inflater, container, false)

        // 닫기 버튼
        mBinding.closeButton.setOnClickListener {
            fragmentManager?.popBackStack()
        }

        //지도 생성
        initMapView()

        return mBinding.root
    }



    private fun initMapView() {
        val fm = childFragmentManager
        val mapFragment = fm.findFragmentById(R.id.maps_fragment) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.maps_fragment, it).commit()
            }

        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(naverMap: NaverMap) {

        this.naverMap = naverMap

        locationViewModel.result.observe(viewLifecycleOwner, Observer { location ->
            locationInfo.add(location)
            naverMap.locationSource = locationSource
            naverMap.uiSettings.isLocationButtonEnabled = true
            naverMap.locationTrackingMode = LocationTrackingMode.Follow
            /*val cameraPosition = CameraPosition(
                location, // 대상 지점
                16.0, // 줌 레벨
            )
            naverMap.cameraPosition = cameraPosition*/
            location.let {
                val cameraUpdate = CameraUpdate.scrollTo(
                    location
                )
                naverMap.moveCamera(cameraUpdate)
            }

            if (locationInfo.size > 2) {
                val path = PathOverlay()
                path.coords = locationInfo
                path.map = naverMap
            }
        })


    }
}
package kr.co.example.tracer

import android.annotation.SuppressLint
import android.graphics.Color
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.PathOverlay
import kr.co.example.tracer.databinding.FragmentMapsBinding

class MapsFragment : Fragment(), OnMapReadyCallback {

    companion object {
        fun newInstance() : MapsFragment {
            return MapsFragment()
        }
    }

    private var _mBinding: FragmentMapsBinding? = null
    private val mBinding get() = _mBinding!!
    private lateinit var naverMap: NaverMap
    private var latLng: ArrayList<LatLng> = arrayListOf()

    // 프래그먼트와 레이아웃을 연결시켜주는 부분
    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _mBinding = FragmentMapsBinding.inflate(inflater, container, false)
        latLng = arguments?.getParcelableArrayList("LATLNG") ?: arrayListOf()
        /*latLng.add(LatLng(37.5670135, 126.9783740))
        latLng.add(LatLng(37.5670136, 126.9783741))*/

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
        val center = latLng.size/2
        val cameraPosition = CameraPosition(
            latLng[center], // 대상 지점
            16.0, // 줌 레벨
        )
        naverMap.cameraPosition = cameraPosition

        val path = PathOverlay()
        path.color = Color.RED
        path.width = 20
        path.coords = latLng
        path.map = naverMap

        /*val boundsBuilder = LatLngBounds.Builder()
        for(i in latLng){
            boundsBuilder.include(i)
        }
        val bounds = boundsBuilder.build()

        naverMap.moveCamera(CameraUpdate.fitBounds(bounds, 1000))*/
    }
}
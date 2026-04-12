package ru.netology.nework.fragments.item

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentMapsBinding

@AndroidEntryPoint
class MapsFragment : Fragment(R.layout.fragment_maps) {

    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!

    private lateinit var mapView: MapView
    private lateinit var placemarkCollection: MapObjectCollection
    private var selectedPoint: Point = Point(55.751244, 37.618423)
    private val inputListener = object : InputListener {
        override fun onMapTap(map: Map, point: Point) {
            handlePointSelected(point, map)
        }

        override fun onMapLongTap(map: Map, point: Point) {
            handlePointSelected(point, map)
        }
    }

    companion object {
        const val RESULT_KEY = "maps_result"
        private const val KEY_LAT = "lat"
        private const val KEY_LNG = "long"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMapsBinding.bind(view)

        mapView = binding.map
        MapKitFactory.initialize(requireContext())
        mapView.onStart()
        MapKitFactory.getInstance().onStart()

        val map = mapView.map
        placemarkCollection = map.mapObjects.addCollection()

        setupToolbar()
        setupMap(map)
        setupListeners(map)
    }

    private fun setupToolbar() {
        binding.topAppBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            if (menuItem.itemId == R.id.save) {
                returnResult()
                true
            } else {
                false
            }
        }
    }

    private fun setupMap(map: Map) {
        map.move(CameraPosition(selectedPoint, 15f, 0f, 0f))
        addPlacemark(selectedPoint)
    }

    private fun setupListeners(map: Map) {
        map.isRotateGesturesEnabled = true
        map.isZoomGesturesEnabled = true
        map.isScrollGesturesEnabled = true

        map.addInputListener(inputListener)
    }

    private fun handlePointSelected(point: Point, map: Map) {
        selectedPoint = point
        updatePlacemark(point)
        map.move(
            CameraPosition(point, map.cameraPosition.zoom, 0f, 0f),
            Animation(Animation.Type.SMOOTH, 0.3f),
            null
        )
    }

    private fun addPlacemark(point: Point) {
        placemarkCollection.addPlacemark().apply {
            geometry = point
            setIcon(ImageProvider.fromResource(requireContext(), R.drawable.ic_location_pin_24))
        }
    }

    private fun updatePlacemark(point: Point) {
        placemarkCollection.clear()
        addPlacemark(point)
    }

    private fun returnResult() {
        val result = Bundle().apply {
            putDouble(KEY_LAT, selectedPoint.latitude)
            putDouble(KEY_LNG, selectedPoint.longitude)
        }
        setFragmentResult(RESULT_KEY, result)
        findNavController().navigateUp()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
        MapKitFactory.getInstance().onStart()
    }

    override fun onStop() {
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
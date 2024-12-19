package com.example.storyapp.ui.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.storyapp.R
import com.example.storyapp.data.StoryRepository
import com.example.storyapp.data.remote.response.Story
import com.example.storyapp.data.remote.retrofit.RetrofitInstance
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.launch

class MapsFragment : Fragment(), OnMapReadyCallback {

    private lateinit var storyRepository: StoryRepository
    private var stories: List<Story> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val token = "Bearer ${requireContext().getSharedPreferences("UserPrefs", 0).getString("token", "")}"
        storyRepository = StoryRepository(RetrofitInstance.api, token)

        lifecycleScope.launch {
            try {
                Toast.makeText(requireContext(), "Fetching stories with location", Toast.LENGTH_SHORT).show()
                storyRepository.getStoriesWithLocation().observe(viewLifecycleOwner) { stories ->
                    Toast.makeText(requireContext(), "Stories fetched: ${stories.size}", Toast.LENGTH_SHORT).show()
                    this@MapsFragment.stories = stories

                    val mapFragment = childFragmentManager.findFragmentById(R.id.mapsFragment) as SupportMapFragment
                    mapFragment?.getMapAsync(this@MapsFragment) ?: Toast.makeText(requireContext(), "MapFragment is null", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error fetching stories", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        if (stories.isEmpty()) {
            Toast.makeText(requireContext(), "No stories available for markers", Toast.LENGTH_SHORT).show()
            return
        }

        var hasValidLocation = false
        var minLat = Double.MAX_VALUE
        var maxLat = Double.MIN_VALUE
        var minLon = Double.MAX_VALUE
        var maxLon = Double.MIN_VALUE

        stories.forEach { story ->
            val lat = story.lat
            val lon = story.lon

            if (lat == null || lon == null) {
            } else {
                val position = LatLng(lat, lon)
                googleMap.addMarker(
                    MarkerOptions()
                        .position(position)
                        .title(story.name)
                        .snippet(story.description)
                )

                minLat = minOf(minLat, lat)
                maxLat = maxOf(maxLat, lat)
                minLon = minOf(minLon, lon)
                maxLon = maxOf(maxLon, lon)

                hasValidLocation = true
            }
        }

        if (!hasValidLocation) {
            Toast.makeText(requireContext(), "No valid locations in stories", Toast.LENGTH_SHORT).show()
            return
        }

        if (maxLon - minLon > 180) {
            val adjustedMinLon = if (minLon < 0) minLon + 360 else minLon
            val adjustedMaxLon = if (maxLon < 0) maxLon + 360 else maxLon

            val bounds = LatLngBounds(
                LatLng(minLat, adjustedMinLon),
                LatLng(maxLat, adjustedMaxLon)
            )

            try {
                val padding = 100
                googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))
            } catch (e: Exception) {
            }
        } else {
            val bounds = LatLngBounds(
                LatLng(minLat, minLon),
                LatLng(maxLat, maxLon)
            )

            try {
                val padding = 100
                googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding))
            } catch (e: Exception) {
            }
        }
    }
}
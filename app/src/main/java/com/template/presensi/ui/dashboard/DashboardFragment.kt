package com.template.presensi.ui.dashboard

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import org.osmdroid.util.GeoPoint

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.template.presensi.R
import com.template.presensi.network.RetrofitBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.content.SharedPreferences
import android.widget.TextView
import com.template.presensi.model.AbsensiRequest
import org.json.JSONException
import org.json.JSONObject
import org.osmdroid.config.Configuration
import org.osmdroid.library.BuildConfig
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

class DashboardFragment : Fragment() {

    private lateinit var mapView: MapView
    private lateinit var locationText: TextView
    private lateinit var findLocationButton: Button
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var kirimAbsensiButton: Button
    private lateinit var sharedPreferences: SharedPreferences
    // Properti kelas untuk menyimpan overlay lokasi
    private lateinit var locationOverlay: MyLocationNewOverlay

    // Properti kelas untuk menyimpan lokasi saat tombol kirim absensi diklik
    private var lastClickedLocation: GeoPoint? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_dashboard, container, false)

        mapView = rootView.findViewById(R.id.mapView)
        locationText = rootView.findViewById(R.id.locationText)
        findLocationButton = rootView.findViewById(R.id.findLocationButton)
        kirimAbsensiButton = rootView.findViewById(R.id.kirimAbsensiButton)


        // Inisialisasi osmdroid
        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
        mapView.setMultiTouchControls(true)


        // Inisialisasi SharedPreferences
        sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        // Inisialisasi FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Menambahkan overlay lokasi
        locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(requireContext()), mapView)
        locationOverlay.enableMyLocation()
        mapView.overlays.add(locationOverlay)


        // Set listener untuk pembaruan lokasi
        locationOverlay.runOnFirstFix {
            requireActivity().runOnUiThread {
                val latitude = locationOverlay.myLocation?.latitude
                val longitude = locationOverlay.myLocation?.longitude
                if (latitude != null && longitude != null) {
                    locationText.text = "Latitude: $latitude\nLongitude: $longitude"
                    zoomToLocation(latitude, longitude, 19.0)
                } else {
                    Toast.makeText(requireContext(), "Lokasi belum aktif", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Set listener untuk tombol kirimAbsensiButton
        kirimAbsensiButton.setOnClickListener {
            onKirimAbsensiButtonClick()
        }

        // Set listener untuk tombol findLocationButton
        findLocationButton.setOnClickListener {
            onFindLocationButtonClick()
        }

        // Menambahkan judul "ATTENDANCE"
        val attendanceTitle = rootView.findViewById<TextView>(R.id.attendanceTitle)
        attendanceTitle.text = "ATTENDANCE"

        return rootView
    }

    private fun onKirimAbsensiButtonClick() {
        // Menggunakan lokasi yang terakhir kali diupdate oleh overlay
        val geoPoint = lastClickedLocation
        if (geoPoint != null) {
            // TODO: Gantilah dengan token yang sesuai dari SharedPreferences
            val token = sharedPreferences.getString("TOKEN_KEY", "")

            // Kirim absensi ke API
            kirimAbsensiKeAPI(token, geoPoint.latitude, geoPoint.longitude)
        } else {
            Toast.makeText(requireContext(), "Lokasi tidak tersedia", Toast.LENGTH_SHORT).show()
        }
    }

    private fun zoomToLocation(latitude: Double, longitude: Double, zoomLevel: Double) {
        mapView.controller.setZoom(zoomLevel)
        mapView.controller.setCenter(GeoPoint(latitude, longitude))
    }


    override fun onResume() {
        super.onResume()

        // Menggunakan listener untuk menyimpan lokasi saat overlay di klik
        locationOverlay.runOnFirstFix {
            lastClickedLocation = GeoPoint(locationOverlay.myLocation)
        }
    }


    private fun kirimAbsensiKeAPI(token: String?, latitude: Double, longitude: Double) {
        // TODO: Gantilah dengan URL API yang sesuai
        val apiUrl = RetrofitBuilder.BASE_URL

        // Buat instance dari ApiService
        val apiService = RetrofitBuilder.apiService

        // Buat objek AbsensiRequest
        val absensiRequest = AbsensiRequest(latitude, longitude)

        // Kirim data absensi ke API
        val absensiCall = apiService.kirimAbsensi("Bearer $token", absensiRequest)
        absensiCall.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Absensi berhasil dikirim", Toast.LENGTH_SHORT).show()
                } else {
                    val errorBody = response.errorBody()?.string()
                    try {
                        // Parsing JSON untuk mendapatkan nilai "message"
                        val json = JSONObject(errorBody)
                        val errorMessage = json.getString("message")
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                    } catch (e: JSONException) {
                        // Jika parsing JSON gagal atau properti "message" tidak ada
                        Toast.makeText(requireContext(), "Gagal mengirim absensi", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    @SuppressLint("MissingPermission")
    private fun onFindLocationButtonClick() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        val latitude = location.latitude
                        val longitude = location.longitude
                        locationText.text = "Latitude: $latitude\nLongitude: $longitude"
                        // Anda dapat menambahkan logika lain di sini, seperti mengirim data ke server, dsb.

                        // Zoom ke lokasi dengan level zoom tertentu (misalnya, 15.0)
                        zoomToLocation(latitude, longitude, 19.0)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Lokasi tidak tersedia",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        } else {
            // Izin lokasi tidak diberikan, mungkin perlu meminta izin lagi atau memberikan pesan kepada pengguna.
            Toast.makeText(
                requireContext(),
                "Izin lokasi tidak diberikan",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}


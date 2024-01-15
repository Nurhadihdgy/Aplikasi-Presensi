package com.template.presensi.ui.home

import android.content.Context
import android.content.SharedPreferences
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.template.presensi.PresensiAdapter
import com.template.presensi.R
import com.template.presensi.databinding.FragmentHomeBinding
import com.template.presensi.model.Presensi
import com.template.presensi.model.PresensiResponse
import com.template.presensi.model.User
import com.template.presensi.model.UserResponse
import com.template.presensi.network.RetrofitBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {


    private lateinit var binding: FragmentHomeBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var presensiAdapter: PresensiAdapter
    private lateinit var recyclerView: RecyclerView

    // Handler untuk update detik setiap detik
    private lateinit var handler: Handler
    private lateinit var dateFormat: SimpleDateFormat
    private lateinit var timeFormat: SimpleDateFormat

    private lateinit var tvTotalAbsensi: TextView
    private lateinit var tvTepatWaktu: TextView
    private lateinit var tvTerlambat: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        // Inisialisasi SharedPreferences
        sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        // Inisialisasi RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        presensiAdapter = PresensiAdapter(mutableListOf())
        recyclerView.adapter = presensiAdapter

        tvTotalAbsensi = view.findViewById(R.id.tv_total_absensi)
        tvTepatWaktu = view.findViewById(R.id.tv_tepat_waktu)
        tvTerlambat = view.findViewById(R.id.tv_terlambat)


        // Inisialisasi handler untuk memperbarui waktu setiap detik
        handler = Handler(Looper.getMainLooper())
        // Inisialisasi format tanggal dan waktu
        dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        // Ambil data user dari API
        fetchUserData()

        // Ambil data presensi dari API
        fetchPresensiData()

        // Perbarui waktu setiap detik
        handler.post(updateTimeRunnable)

        return view
    }

    // Runnable untuk memperbarui waktu setiap detik
    private val updateTimeRunnable: Runnable = object : Runnable {
        override fun run() {
            // Perbarui TextView dengan waktu saat ini
            updateDateTime()

            // Jadwalkan pemanggilan ulang setiap detik
            handler.postDelayed(this, 1000)
        }
    }

    // Method untuk memperbarui TextView dengan tanggal dan waktu saat ini
    private fun updateDateTime() {
        val currentDate = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(Date())
        val currentTime = timeFormat.format(Date())

        binding.tanggalTextView.text = "Hari ini : $currentDate"
        binding.waktuTextView.text = "$currentTime"
    }


    override fun onDestroyView() {
        // Hentikan pembaruan waktu saat Fragment dihancurkan
        handler.removeCallbacks(updateTimeRunnable)
        super.onDestroyView()
    }

    private fun fetchUserData() {
        // TODO: Gantilah dengan token yang sesuai dari SharedPreferences
        val token = sharedPreferences.getString("TOKEN_KEY", "")

        // Buat instance dari ApiService
        val apiService = RetrofitBuilder.apiService

        // Melakukan request data user ke API
        val userCall = apiService.getUserData("Bearer $token")
        userCall.enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    val user = response.body()?.user
                    if (user != null) {
                        // Update UI dengan data user
                        updateUIWithUserData(user)
                    } else {
                        Log.e("HomeFragment", "User data is null")
                    }
                } else {
                    Log.e("HomeFragment", "Failed to fetch user data, HTTP status code: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
                Log.e("HomeFragment", "Error fetching user data: ${t.message}")
            }
        })
    }

    private fun updateUIWithUserData(user: User) {
        // Memperbarui TextView dengan data pengguna
        binding.namaTextView.text = "Nama: ${user.nama}"
        binding.kelasTextView.text = "Kelas: ${user.kelas}"
        binding.noAbsenTextView.text = "No Absen: ${user.noAbsen}"
        binding.nisnTextView.text = "NISN: ${user.nisn}"
    }


    private fun fetchPresensiData() {
        // TODO: Gantilah dengan token yang sesuai dari SharedPreferences
        val token = sharedPreferences.getString("TOKEN_KEY", "")

        // TODO: Gantilah dengan NISN yang sesuai dari SharedPreferences
        val nisn = sharedPreferences.getString("NISN_KEY", "")

        // Buat instance dari ApiService
        val apiService = RetrofitBuilder.apiService

        val nonNullToken: String = token ?: ""
        val nonNullNisn: String = nisn ?: ""

        val presensiCall = apiService.getPresensiData("Bearer $nonNullToken", nonNullNisn)

        presensiCall.enqueue(object : Callback<PresensiResponse> {
            override fun onResponse(call: Call<PresensiResponse>, response: Response<PresensiResponse>) {
                if (response.isSuccessful) {
                    val presensiList = response.body()?.presensi
                    if (presensiList != null) {
                        // Update UI dengan data presensi
                        updateUIWithPresensiData(presensiList)
                        updateUIWithPresensi(presensiList)
                    } else {
                        Log.e("HomeFragment", "Presensi data is null")
                    }
                } else {
                    Log.e("HomeFragment", "Failed to fetch presensi data, HTTP status code: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<PresensiResponse>, t: Throwable) {
                Log.e("HomeFragment", "Error fetching presensi data: ${t.message}")
            }
        })
    }

    private fun updateUIWithPresensiData(presensiList: List<Presensi>) {
        // Update RecyclerView dengan data presensi
        presensiAdapter.setData(presensiList)
    }



    // Di dalam updateUIWithPresensi()
    // Fungsi untuk memperbarui UI
    private fun updateUIWithPresensi(presensiList: List<Presensi>) {
        // Update RecyclerView dengan data presensi
        presensiAdapter.setData(presensiList)


        // Hitung jumlah terlambat dan tepat waktu
        var terlambatCount = 0
        var tepatWaktuCount = 0

        for (presensi in presensiList) {
            if (presensi.telat == 1) {
                terlambatCount++
            } else if (presensi.telat == 0) {
                tepatWaktuCount++
            }
        }

        // Hitung persentase
        val totalAbsensi = presensiList.size
        val persentaseTepatWaktu = (tepatWaktuCount.toDouble() / totalAbsensi) * 100

        // Update TextView untuk rekapitulasi
        tvTotalAbsensi.text = totalAbsensi.toString()
        tvTepatWaktu.text = tepatWaktuCount.toString()
        tvTerlambat.text = terlambatCount.toString()

        // Check if any of the views are null before using them
        val progressBar: ProgressBar? = binding.progressBar
        val tvProgressPercentage: TextView? = binding.tvProgressPercentage
        val tvAttendanceTitle: TextView? = binding.tvAttedanceTitle

        if (progressBar != null && tvProgressPercentage != null && tvAttendanceTitle != null) {
            // Update TextView untuk rekapitulasi persentase
            tvProgressPercentage.text = String.format("%.2f%%", persentaseTepatWaktu)
            tvAttendanceTitle.text = "Persentase Kehadiran"

            // Update ProgressBar
            progressBar.progress = persentaseTepatWaktu.toInt()
        }
    }



}

package com.template.presensi.ui.notifications

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.template.presensi.ui.login.LoginActivity
import com.template.presensi.R
import com.template.presensi.model.LogoutResponse
import com.template.presensi.network.RetrofitBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NotificationsFragment : Fragment() {

    private lateinit var logoutButton: Button
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_notifications, container, false)

        logoutButton = rootView.findViewById(R.id.logoutButton)

        // Inisialisasi SharedPreferences
        sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        logoutButton.setOnClickListener {
            performLogout()
        }

        return rootView
    }

    private fun performLogout() {
        // TODO: Gantilah dengan token yang sesuai dari SharedPreferences
        val token = sharedPreferences.getString("TOKEN_KEY", "")

        // Buat instance dari ApiService
        val apiService = RetrofitBuilder.apiService

        // Melakukan request logout ke API
        val logoutCall = apiService.logout("Bearer $token")
        logoutCall.enqueue(object : Callback<LogoutResponse> {
            override fun onResponse(call: Call<LogoutResponse>, response: Response<LogoutResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), response.body()?.message, Toast.LENGTH_SHORT).show()
                    // Redirect ke halaman login setelah logout berhasil
                    redirectToLoginPage()
                } else {
                    Toast.makeText(requireContext(), "Failed to logout", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<LogoutResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Error during logout: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun redirectToLoginPage() {
        // Implementasi untuk kembali ke halaman login
        // Contoh:
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }
}


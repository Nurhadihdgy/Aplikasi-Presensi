package com.template.presensi.ui.login

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.template.presensi.MainActivity
import com.template.presensi.R
import com.template.presensi.model.LoginRequest
import com.template.presensi.model.LoginResponse
import com.template.presensi.network.RetrofitBuilder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var nisnEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var loginButton: Button
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        nisnEditText = findViewById(R.id.nisnEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)

        // Inisialisasi SharedPreferences
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        loginButton.setOnClickListener {
            val nisn = nisnEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            // Validasi sederhana
            if (nisn.isEmpty() || password.isEmpty()) {
                // Menampilkan pesan jika NISN atau password kosong
                nisnEditText.error = "NISN harus diisi"
                passwordEditText.error = "Password harus diisi"
                return@setOnClickListener
            }

            // Membuat instance dari ApiService
            val apiService = RetrofitBuilder.apiService

            // Membuat objek LoginRequest
            val loginRequest = LoginRequest(nisn, password)

            // Melakukan request login ke API
            val loginCall = apiService.loginUser(loginRequest)
            loginCall.enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.isSuccessful) {
                        val loginResponse = response.body()
                        if (loginResponse != null) {
                            val token = loginResponse.token
                            val nama = loginResponse.user.nama
                            val kelas = loginResponse.user.kelas
                            val noAbsen = loginResponse.user.noAbsen

                            Log.d("Login Success", "Nama: $nama")
                            // Menampilkan alert login berhasil
                            showLoginSuccessAlert(nama, kelas, noAbsen, token)

                        } else {
                            showLoginErrorAlert()
                            Log.e("Login Error", "Response body is null")
                        }
                    } else {
                        // Tangani kasus respons yang tidak berhasil lainnya
                        showLoginErrorAlert()
                        Log.e("Login Error", "HTTP status code: ${response.code()}")
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    if (t.message != null) {
                        showLoginErrorAlert()
                        Log.e("Login Failure", "Error message: ${t.message}")
                    }
                }
            })
        }
    }

    private fun saveTokenToSharedPreferences(token: String) {
        // Menyimpan token ke SharedPreferences
        val editor = sharedPreferences.edit()
        editor.putString("TOKEN_KEY", token)
        editor.apply()
    }

    private fun saveUserInfoToSharedPreferences(nama: String?, kelas: String?, noAbsen: String?) {
        val editor = sharedPreferences.edit()
        editor.putString("NAMA_KEY", nama)
        editor.putString("KELAS_KEY", kelas)
        editor.putString("NO_ABSEN_KEY", noAbsen)
        editor.apply()
    }

    private fun showLoginSuccessAlert(nama: String?, kelas: String?, noAbsen: String?, token: String?) {
        val alert = AlertDialog.Builder(this)
        alert.setTitle("Login Berhasil")

        val namaToShow = nama ?: "Nama tidak tersedia"
        val kelasToShow = kelas ?: "Kelas tidak tersedia"
        val noAbsenToShow = noAbsen ?: "No Absen tidak tersedia"
        val tokenToShow = token ?: "Token tidak tersedia"

        alert.setMessage("$namaToShow berhasil login ")
        alert.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()

            // Simpan informasi ke SharedPreferences atau sesi pengguna sesuai kebutuhan
            saveUserInfoToSharedPreferences(nama, kelas, noAbsen)
            if (token != null) {
                saveTokenToSharedPreferences(token)
            }

            // Lanjutkan dengan pindah ke MainActivity
            val intent = Intent(this@LoginActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        alert.show()
    }


    private fun showLoginErrorAlert() {
        val alert = AlertDialog.Builder(this)
        alert.setTitle("Login Gagal")
        alert.setMessage("Terjadi kesalahan saat login.")
        alert.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        alert.show()
    }
}

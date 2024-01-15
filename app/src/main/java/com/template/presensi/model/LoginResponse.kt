package com.template.presensi.model

import com.google.gson.annotations.SerializedName

data class User(
    val id: Int,
    val nisn: String,
    val nama: String,
    val kelas: String,
    @SerializedName("no_absen")
    val noAbsen: String,
    val created_at: String,
    val updated_at: String
)

data class LoginResponse(
    val message: String,
    val token: String,
    val user: User
)



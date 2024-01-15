package com.template.presensi.model

// PresensiResponse.kt

import com.google.gson.annotations.SerializedName

data class PresensiResponse(
    @SerializedName("user_info")
    val userInfo: UserInfo,
    val presensi: List<Presensi>
)

data class UserInfo(
    val nisn: String,
    val nama: String,
    val kelas: String,
    @SerializedName("no_absen")
    val noAbsen: String
)

data class Presensi(
    val id: Int,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("tanggal_absen")
    val tanggalAbsen: String,
    @SerializedName("waktu_absen")
    val waktuAbsen: String,
    val telat: Int,
    @SerializedName("menit_telat")
    val menitTelat: Int,
    val lokasi: String,
    val latitude: Double,
    val longitude: Double,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String
)

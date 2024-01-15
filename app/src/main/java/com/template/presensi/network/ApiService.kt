import com.template.presensi.model.AbsensiRequest
import com.template.presensi.model.LoginRequest
import com.template.presensi.model.LoginResponse
import com.template.presensi.model.LogoutResponse
import com.template.presensi.model.PresensiResponse
import com.template.presensi.model.UserResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @POST("login")
    fun loginUser(@Body requestBody: LoginRequest): Call<LoginResponse>

    @GET("user")
    fun getUserData(@Header("Authorization") token: String): Call<UserResponse>

    @GET("data_presensi_user")
    fun getPresensiData(@Header("Authorization") token: String, @Query("nisn") nisn: String): Call<PresensiResponse>
    // Fungsi untuk mengirim absensi
    @POST("presensi")
    fun kirimAbsensi(
        @Header("Authorization") token: String,
        @Body request: AbsensiRequest
    ): Call<Void>
    @POST("logout")
    fun logout(@Header("Authorization") token: String): Call<LogoutResponse>
}

package additionalfunc
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST


interface ApiInterface {
    @GET("addmobile")
    fun getData(): retrofit2.Call<List<MyDataItem>>

    @FormUrlEncoded
    @POST("addmobile")
    fun getCity(@Field("mes")mes:String): Call<City>
}
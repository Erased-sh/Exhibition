package additionalfunc

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiClient {

    var BaseUrl="http://192.168.1.3:3000/"
    var apiInterface:ApiInterface
    init {
        var retrofit=
            Retrofit.Builder().baseUrl(BaseUrl).addConverterFactory(GsonConverterFactory.create()).build()
        apiInterface=retrofit.create(ApiInterface::class.java)
    }
    fun addCity(mes:String): Call<City> {
        return apiInterface.getCity(mes)
    }
}

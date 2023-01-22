package com.example.exhibition

import additionalfunc.ApiClient
import additionalfunc.ApiInterface
import additionalfunc.City
import additionalfunc.MyDataItem
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class Writing : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_writing)
        val Ge:Button=findViewById<Button>(R.id.Get)
        val Se:Button=findViewById<Button>(R.id.Send)
        val GR=findViewById<TextView>(R.id.GetResponse)
        val Edi=findViewById<EditText>(R.id.edi)
        Ge.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                GlobalScope.launch{
                getMyData(GR)}
            }
        })
        Se.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                val MD=Edi.getText().toString()
                GlobalScope.launch {
                PushData(MD,GR)}

            }
        })


    }

    private fun getMyData(GR:TextView) {

        val retrofitBuilder = Retrofit.Builder().addConverterFactory(GsonConverterFactory.create())
            .baseUrl("http://192.168.1.3:3000/").build().create(ApiInterface::class.java)

        val retrofitData = retrofitBuilder.getData()
        retrofitData.enqueue(object : Callback<List<MyDataItem>?> {
            override fun onResponse(
                call: Call<List<MyDataItem>?>,
                response: Response<List<MyDataItem>?>
            ) {
                val myStringBuilder=StringBuilder()
                val responseBody=response.body()!!
                for (MyData in responseBody){
                    myStringBuilder.append(MyData.id)
            }
                GR.setText(myStringBuilder)
            }

            override fun onFailure(call: Call<List<MyDataItem>?>, t: Throwable) {
                Log.d("Writing","OnFail: "+t.message)
                GR.setText(t.message)
            }
        })
    }
    private fun PushData(Querry:String,GR: TextView) {
        var apiClient= ApiClient()
        var call=apiClient.addCity(Querry)
        call.enqueue(object :Callback<City>{
            override fun onFailure(call: Call<City>, t: Throwable) {
                GR.setText(t.toString())
            }

            @SuppressLint("SuspiciousIndentation")
            override fun onResponse(call: Call<City>, response: Response<City>) {
             val Res:String =response.body()!!.Responce.message
                GR.setText(Res)
            }
    })

}
}
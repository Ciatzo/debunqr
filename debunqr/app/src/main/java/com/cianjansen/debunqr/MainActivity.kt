package com.cianjansen.debunqr

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.JsonObject
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableOnSubscribe
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.lang.Exception


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        testButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                println("click")
                getNewSandboxApiKey()
            }
        })

    }

    /**
     *
     */
    private fun getNewSandboxApiKey() {

        object : Thread() {
            override fun run() {
                val client = OkHttpClient()
                val request: Request = Request.Builder()
                    .url("https://public-api.sandbox.bunq.com/v1/sandbox-user-person")
                    .post(RequestBody.create(null, ByteArray(0)))
                    .addHeader("Content-Type", "application/json")
                    .addHeader("user-agent", "curl-request")
                    .addHeader("x-bunq-client-request-id", "12121212")
                    .addHeader("cache-control", "none")
                    .addHeader("x-bunq-geolocation", "0 0 0 0 000")
                    .addHeader("x-bunq-language", "nl_NL")
                    .addHeader("x-bunq-region", "nl_NL")
                    .build()
                try {
                    val response: Response = client.newCall(request).execute()
                    println(response.code())
                    val responseString: String = response.body().string()
                    print("Response:")
                    println(responseString)
                    val jsonObject: JsonObject = Gson().fromJson(responseString, JsonObject::class.java)
                    val apiKey: String =
                        jsonObject.getAsJsonArray("Response").get(0).getAsJsonObject()
                            .get("ApiKey").getAsJsonObject().get("api_key").toString()
                    println("Key: $apiKey")

                }catch (e : Exception){
                    e.printStackTrace()
                }

            }
        }.start()




    }



}
package com.cianjansen.debunqr

import android.os.Bundle
import android.util.JsonReader
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.StringReader


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        testButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                println("click")
                testHttpReq()
            }
        })

    }

    private fun testHttpReq() {
        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url("https://public-api.sandbox.bunq.com/v1/sandbox-user")
            .post(RequestBody.create(null, ByteArray(0)))
            .addHeader("x-bunq-client-request-id", "1234")
            .addHeader("cache-control", "no-cache")
            .addHeader("x-bunq-geolocation", "0 0 0 0 NL")
            .addHeader("x-bunq-language", "en_US")
            .addHeader("x-bunq-region", "en_US")
            .build()
        val response: Response = client.newCall(request).execute()
        val responseString: String = response.body().string()
        println("Response:")
        println(responseString)
        val jsonObject: JsonObject = Gson().fromJson(responseString, JsonObject::class.java)
        val apiKEy: JsonObject =
            jsonObject.getAsJsonArray("Response").get(0).getAsJsonObject()
                .get("Apikey").getAsJsonObject()
        println("Key: $apiKEy")


    }


}
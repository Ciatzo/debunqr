package com.cianjansen.debunqr

import android.content.Intent
import android.os.Bundle
import android.provider.AlarmClock.EXTRA_MESSAGE
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bunq.sdk.context.ApiContext
import com.bunq.sdk.context.ApiEnvironmentType
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.JsonObject
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_login.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.File


class LoginActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        newUserButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                generateApiKey()
            }
        })

        useExistingUserButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                useCurrentUser()
            }
        })

        showCurrentUser()
    }


    /**
     * Send HTTP post request to retrieve new API key from bunq sandbox server
     */
    private fun generateApiKey() {
        Single.fromCallable {
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
                val responseString: String = response.body()!!.string()
                val jsonObject: JsonObject =
                    Gson().fromJson(responseString, JsonObject::class.java)
                val apiKey: String =
                    jsonObject.getAsJsonArray("Response").get(0).getAsJsonObject()
                        .get("ApiKey").getAsJsonObject().get("api_key").toString()

                return@fromCallable apiKey.substring(1, apiKey.length - 1)
            } catch (e: Exception) {
                e.printStackTrace()
            }


        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ apiKey ->
                val key: String = apiKey as String
                val snackbar = Snackbar
                    .make(
                        findViewById(R.id.outerConstraintLayout),
                        "Api key ${key} registered",
                        Snackbar.LENGTH_INDEFINITE
                    )
                snackbar.show()
                Log.i("debug", "Api key ${key} registered")
                setupApiContext(key)
            }, {
                it.printStackTrace()
            })
    }

    /**
     * Set up and save a new ApiContext
     */
    private fun setupApiContext(apiKey: String) {
        Single.fromCallable {
            try {
                Log.i("debug", "Using apikey ${apiKey}")
                val apiContext = ApiContext.create(
                    ApiEnvironmentType.SANDBOX,
                    apiKey,
                    "DEVICE_DESCRIPTION"
                )
                apiContext.save(
                    applicationContext.getExternalFilesDir("conf").toString() + "bunq.conf"
                )
                return@fromCallable ApiContext.restore(
                    applicationContext.getExternalFilesDir("conf").toString() + "bunq.conf"
                ).apiKey
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ apiKey ->
                val snackbar = Snackbar
                    .make(
                        findViewById(R.id.outerConstraintLayout),
                        "Api context created with key ${apiKey}",
                        Snackbar.LENGTH_LONG
                    )
                snackbar.show()
                showCurrentUser()

            }, {
                it.printStackTrace()
            })
    }

    /**
     * Start main activity if there is a current registered user
     */
    private fun useCurrentUser(){
        try {
            val apiContext = ApiContext.restore(applicationContext.getExternalFilesDir("conf").toString() + "bunq.conf")
            val intent = Intent(this@LoginActivity, MainActivity::class.java).apply {
            }
            startActivity(intent)

        }catch (e : java.lang.Exception){
            Log.i("debug","No current user")
            val snackbar = Snackbar
                .make(
                    findViewById(R.id.outerConstraintLayout),
                    "No current user, please create new user",
                    Snackbar.LENGTH_LONG
                )
            snackbar.show()
        }
    }

    /**
     * Shows current user
     */
    private fun showCurrentUser(){
        try {
            val apiContext = ApiContext.restore(applicationContext.getExternalFilesDir("conf").toString() + "bunq.conf")
            val currentUserView : TextView = findViewById(R.id.currentUserView)
            currentUserView.text = "Current User:\n${apiContext.apiKey}"
        }catch (e : java.lang.Exception){
            Log.i("debug","No current user")
            currentUserView.text = "No current user registered"
        }
    }

}
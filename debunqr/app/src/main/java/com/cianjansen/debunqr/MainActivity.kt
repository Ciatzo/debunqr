package com.cianjansen.debunqr

import android.graphics.Color
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.RelativeLayout.*
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.bunq.sdk.context.ApiContext
import com.bunq.sdk.context.ApiEnvironmentType
import com.bunq.sdk.context.BunqContext
import com.bunq.sdk.model.generated.endpoint.Payment
import com.google.gson.Gson
import com.google.gson.JsonObject
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableSource
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.functions.Supplier
import io.reactivex.rxjava3.observers.DisposableObserver
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.net.URL
import java.nio.charset.Charset
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class MainActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        testButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                setupApiContext()
            }
        })
        addButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                setup2()

            }
        })


    }

    private fun listPayments() {

        val apiContext = ApiContext.restore(applicationContext.getExternalFilesDir("conf").toString() + "bunq.conf")
        BunqContext.loadApiContext(apiContext)

        var listOfPayments: List<Payment>

        try {
            listOfPayments = Payment.list().value

        }catch (e : java.lang.Exception){
            e.printStackTrace()
            listOfPayments = ArrayList<Payment>()
        }
//        val listOfPayments: ArrayList<Payment> =
//        listOfPayments.add(Payment(Amount("5")))

        val scrollContainer: LinearLayout = findViewById(R.id.scrollLayout)


        listOfPayments.forEach {

            val parent = LinearLayout(this)
            val parentParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            parentParams.setMargins(10, 10, 10, 10)

            parent.layoutParams = parentParams
            parent.orientation = LinearLayout.HORIZONTAL


            //children of parent linearlayout
            val amountTextView = TextView(this)
//            val amount = it.amount.value
            val amount = "8.70"
            amountTextView.text = amount
            if (amount.toDouble() >= 0) {
                amountTextView.setTextColor(Color.parseColor("#00ff80"))
            } else {
                amountTextView.setTextColor(Color.parseColor("#ff3300"))
            }


            val textViewParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )

            amountTextView.layoutParams = textViewParams

            val descriptionLayout = LinearLayout(this)
            descriptionLayout.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            descriptionLayout.orientation = LinearLayout.VERTICAL

            parent.addView(descriptionLayout)
            parent.addView(amountTextView)

            //children of descriptionLayout (left hand side) LinearLayout
            val dateTextView = TextView(this)
            dateTextView.setTextColor(Color.parseColor("#F4F4F4"))
            val aliasTextView = TextView(this)
            aliasTextView.setTextColor(Color.parseColor("#FFFFFF"))
            val paymentTypeTextView = TextView(this)
            paymentTypeTextView.setTextColor(Color.parseColor("#C9C9C9"))

//            dateTextView.text = it.created
//            aliasTextView.text = it.counterpartyAlias.displayName
//            paymentTypeTextView.text = it.type

            dateTextView.text = "21-09-2020"
            aliasTextView.text = "Counter Alias"
            paymentTypeTextView.text = "Card"

            descriptionLayout.addView(dateTextView)
            descriptionLayout.addView(aliasTextView)
            descriptionLayout.addView(paymentTypeTextView)


            parent.setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View?) {
                    println("clicking view")
//                    val intent = Intent(this, PaymentDetailView::class.java).apply {
//                        putExtra("EXTRA_MESSAGE", "message")
//                    }
//                    startActivity(intent)


                }
            })
            scrollContainer.addView(parent)
        }


    }


    /**
     * Generates a new sandbox user and retrieves user api key
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
                    val responseString: String = response.body()!!.string()
                    print("Response:")
                    println(responseString)
                    val jsonObject: JsonObject =
                        Gson().fromJson(responseString, JsonObject::class.java)
                    val apiKey: String =
                        jsonObject.getAsJsonArray("Response").get(0).getAsJsonObject()
                            .get("ApiKey").getAsJsonObject().get("api_key").toString()
                    println("Key: $apiKey")

                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }.start()


    }

    /**
     * Create new sandbox API context and saves details
     */
    private fun setupApiContext() {
        println(applicationContext.getExternalFilesDir("conf"))
        object : Thread() {
            override fun run() {

                try {
                    val apiContext = ApiContext.create(
                        ApiEnvironmentType.SANDBOX,
                        "sandbox_c518ee084f55f57a8fa850d941b9d77a6523137ad155a3a9839d4fb8",
                        "DEVICE_DESCRIPTION"
                    )
                    apiContext.save(applicationContext.getExternalFilesDir("conf").toString() + "bunq.conf")
                    if (apiContext.ensureSessionActive()) println("active") else println("not active")
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }.start()
    }

    private fun setup2(){
        Single.fromCallable {
            // Here we load a simple string from the web service.
            val apiContext = ApiContext.restore(applicationContext.getExternalFilesDir("conf").toString() + "bunq.conf")
            BunqContext.loadApiContext(apiContext)


            return@fromCallable Payment.list().value
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ listOfPayments ->
                val scrollContainer: LinearLayout = findViewById(R.id.scrollLayout)
                scrollContainer.removeAllViews()

                println("List of payments size: " + listOfPayments.size)
                listOfPayments.forEach {

                    val parent = LinearLayout(this)
                    val parentParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    parentParams.setMargins(10, 10, 10, 10)

                    parent.layoutParams = parentParams
                    parent.orientation = LinearLayout.HORIZONTAL


                    //children of parent linearlayout
                    val amountTextView = TextView(this)
                    val amount = it.amount.value
//                    val amount = "8.70"
                    amountTextView.text = amount
                    if (amount.toDouble() >= 0) {
                        amountTextView.setTextColor(Color.parseColor("#00ff80"))
                    } else {
                        amountTextView.setTextColor(Color.parseColor("#ff3300"))
                    }


                    val textViewParams = RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                    )

                    amountTextView.layoutParams = textViewParams

                    val descriptionLayout = LinearLayout(this)
                    descriptionLayout.layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    descriptionLayout.orientation = LinearLayout.VERTICAL

                    parent.addView(descriptionLayout)
                    parent.addView(amountTextView)

                    //children of descriptionLayout (left hand side) LinearLayout
                    val dateTextView = TextView(this)
                    dateTextView.setTextColor(Color.parseColor("#F4F4F4"))
                    val aliasTextView = TextView(this)
                    aliasTextView.setTextColor(Color.parseColor("#FFFFFF"))
                    val paymentTypeTextView = TextView(this)
                    paymentTypeTextView.setTextColor(Color.parseColor("#C9C9C9"))

                    val l = LocalDate.parse(it.created, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.nnnnnn"))

                    dateTextView.text = "${l.dayOfMonth} ${l.month} ${l.year}"
                    aliasTextView.text = it.counterpartyAlias.displayName
                    paymentTypeTextView.text = it.type


                    descriptionLayout.addView(dateTextView)
                    descriptionLayout.addView(aliasTextView)
                    descriptionLayout.addView(paymentTypeTextView)


                    parent.setOnClickListener(object : View.OnClickListener {
                        override fun onClick(v: View?) {
                            println("clicking payment " + it.description)
//                    val intent = Intent(this, PaymentDetailView::class.java).apply {
//                        putExtra("EXTRA_MESSAGE", "message")
//                    }
//                    startActivity(intent)


                        }
                    })
                    scrollContainer.addView(parent)
                }
            }, {
                it.printStackTrace()
            })
    }



}
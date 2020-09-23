package com.cianjansen.debunqr

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.bunq.sdk.context.ApiContext
import com.bunq.sdk.context.BunqContext
import com.bunq.sdk.http.BunqResponse
import com.bunq.sdk.model.generated.`object`.Amount
import com.bunq.sdk.model.generated.`object`.Pointer
import com.bunq.sdk.model.generated.endpoint.Payment
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_create_payment.*

class CreatePaymentActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_payment)

        sendPaymentButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val amount: String = findViewById<EditText>(R.id.amountEditText).text.toString()
                val recipient: String = findViewById<EditText>(R.id.recipientEditText).text.toString()
                val description: String = findViewById<EditText>(R.id.descriptionEditText).text.toString()
                sendPayment(amount, recipient, description)

            }
        })
    }

    private fun sendPayment(amount: String, recipient: String, description: String) {
        Single.fromCallable {
            val apiContext = ApiContext.restore(
                applicationContext.getExternalFilesDir("conf").toString() + "bunq.conf"
            )
            BunqContext.loadApiContext(apiContext)
            val payResponse: BunqResponse<Int>? = Payment.create(
                Amount(amount, "EUR"),
                Pointer("EMAIL", recipient, recipient),
                description
            )

            return@fromCallable payResponse
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ payResponse ->
                val pr: BunqResponse<Int>? = payResponse
                Log.i("debug", pr.toString())

            }, {
                it.printStackTrace()
            })
    }
}
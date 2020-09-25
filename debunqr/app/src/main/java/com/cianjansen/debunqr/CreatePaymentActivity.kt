package com.cianjansen.debunqr

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.bunq.sdk.context.ApiContext
import com.bunq.sdk.context.BunqContext
import com.bunq.sdk.exception.BadRequestException
import com.bunq.sdk.http.BunqResponse
import com.bunq.sdk.model.generated.`object`.Amount
import com.bunq.sdk.model.generated.`object`.Pointer
import com.bunq.sdk.model.generated.endpoint.Payment
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_create_payment.*


class CreatePaymentActivity : AppCompatActivity() {

    private var isClickable : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_payment)
        isClickable = true
        sendPaymentButton.setOnClickListener {
            if(isClickable){
                val amount: String = findViewById<EditText>(R.id.amountEditText).text.toString()
                val recipient: String = findViewById<EditText>(R.id.recipientEditText).text.toString()
                val description: String = findViewById<EditText>(R.id.descriptionEditText).text.toString()
                sendPayment(amount, recipient, description)
            }else{
                Log.i("debug","button bounced")
            }
        }
    }

    /**
     * Create new payment with user filled details and send to bunq servers
     */
    private fun sendPayment(amount: String, recipient: String, description: String) {
        isClickable = false
        Single.fromCallable {
            val apiContext = ApiContext.restore(
                ConfigDirectory.getDirectory(applicationContext)
            )
            BunqContext.loadApiContext(apiContext)

            return@fromCallable Payment.create(
                Amount(amount, "EUR"),
                Pointer("EMAIL", recipient, recipient),
                description
            )
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ payResponse ->
                val pr: BunqResponse<Int>? = payResponse
                if (pr != null) {
                    Log.i("debug", pr.value.toString())
                    isClickable = true
                    finish()
                }

            }, {
//                it.printStackTrace()
//                Log.i("debug", "$it.message")
                if (it is BadRequestException){
                    val BRE = it as BadRequestException
                    val message = BRE.message
                    val subMessage : String = BRE.message?.substringAfter("Error message:")!!
                    Log.i("debug", subMessage)
//                    val snackbar = Snackbar
//                        .make(
//                            findViewById(R.id.createPaymentOuterConstraint),
//                            subMessage,
//                            Snackbar.LENGTH_INDEFINITE
//                        )
//                    snackbar.show()
                    if (message != null) {
                        createPopup(subMessage)
                    }
                }
            })
    }

    private fun createPopup(message : String){
        AlertDialog.Builder(this)
            .setTitle("Payment incorrect")
            .setMessage(message)
            .setPositiveButton(
                android.R.string.yes
            ) { dialog, which ->
                isClickable = true
            }
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }
}
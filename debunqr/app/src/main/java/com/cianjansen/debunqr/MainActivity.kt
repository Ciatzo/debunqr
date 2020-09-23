package com.cianjansen.debunqr

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.RelativeLayout.*
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bunq.sdk.context.ApiContext
import com.bunq.sdk.context.BunqContext
import com.bunq.sdk.model.generated.endpoint.MonetaryAccount
import com.bunq.sdk.model.generated.endpoint.Payment
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        updateButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                showPaymentList()
                updateBalance()

            }
        })
        newPaymentButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val intent = Intent(this@MainActivity, CreatePaymentActivity::class.java).apply {
                }
                startActivity(intent)
            }
        })
        showPaymentList()

    }

    /**
     * When resuming activity, update payment list
     */
    override fun onResume() {
        super.onResume()
        showPaymentList()
        updateBalance()
    }

    /**
     * Update list of payments by retrieving all completed payments by current user from bunq server
     */
    private fun showPaymentList() {
        Single.fromCallable {
            val apiContext = ApiContext.restore(
                applicationContext.getExternalFilesDir("conf").toString() + "bunq.conf"
            )
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
                    dateTextView.textSize = 10F
                    val aliasTextView = TextView(this)
                    aliasTextView.setTextColor(Color.parseColor("#FFFFFF"))
                    val paymentTypeTextView = TextView(this)
                    paymentTypeTextView.setTextColor(Color.parseColor("#C9C9C9"))

                    val l = LocalDate.parse(
                        it.created,
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.nnnnnn")
                    )

                    dateTextView.text = "${l.dayOfMonth} ${l.month} ${l.year}"
                    aliasTextView.text = "${it.description} - ${it.counterpartyAlias.displayName}"
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

    private fun updateBalance() {

        Single.fromCallable {
            val apiContext = ApiContext.restore(
                applicationContext.getExternalFilesDir("conf").toString() + "bunq.conf"
            )
            val list = MonetaryAccount.list().value


            return@fromCallable list
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ list ->
                val accountList: List<MonetaryAccount> = list
                if (accountList.size == 1){
                    val balance = accountList[0].monetaryAccountBank.balance.value
                    val balanceTextView : TextView = findViewById(R.id.balanceValueTextView)
                    balanceTextView.text = "€$balance"
                }



            }, {
                it.printStackTrace()
            })

    }


}
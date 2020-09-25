package com.cianjansen.debunqr

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
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

        updateButton.setOnClickListener {
            showPaymentList()
            updateBalance()
        }

        newPaymentButton.setOnClickListener {
            val intent = Intent(this@MainActivity, CreatePaymentActivity::class.java).apply {
            }
            startActivity(intent)
        }

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
        // Load api context and request payments in separate thread
        Single.fromCallable {
            val apiContext = ApiContext.restore(
                ConfigDirectory.getDirectory(applicationContext)
            )
            BunqContext.loadApiContext(apiContext)


            return@fromCallable Payment.list().value
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ listOfPayments ->

                val scrollContainer: LinearLayout = findViewById(R.id.scrollLayout)
                scrollContainer.removeAllViews()

                listOfPayments.forEach {

                    val parent = ConstraintLayout(this)
                    val parentParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )


                    parentParams.setMargins(15, 15, 15, 15)

                    parent.layoutParams = parentParams


                    val amountTextView = TextView(this)
                    val amount = it.amount.value
                    amountTextView.text = amount
                    if (amount.toDouble() >= 0) {
                        amountTextView.setTextColor(Color.parseColor("#00ff80"))
                    } else {
                        amountTextView.setTextColor(Color.parseColor("#ff3300"))
                    }
                    amountTextView.textSize = 20F


                    val descriptionLayout = LinearLayout(this)
                    descriptionLayout.layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    descriptionLayout.orientation = LinearLayout.VERTICAL


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

                    dateTextView.text =
                        "${l.dayOfMonth} ${l.month.toString().toLowerCase().capitalize()} ${l.year}"
                    var desc: String = it.description
                    if (desc.length > 25) {
                        desc = desc.substring(0, 24) + "..."
                    }
                    aliasTextView.text = "${desc}  |  ${it.counterpartyAlias.displayName}"
                    paymentTypeTextView.text = it.type


                    descriptionLayout.addView(dateTextView)
                    descriptionLayout.addView(aliasTextView)
                    descriptionLayout.addView(paymentTypeTextView)

                    descriptionLayout.id = View.generateViewId()
                    amountTextView.id = View.generateViewId()
                    parent.id = View.generateViewId()
                    parent.addView(descriptionLayout)
                    parent.addView(amountTextView)
                    val set = ConstraintSet()
                    set.clone(parent)
                    set.connect(
                        descriptionLayout.id,
                        ConstraintSet.START,
                        parent.id,
                        ConstraintSet.START
                    )
                    set.connect(
                        descriptionLayout.id,
                        ConstraintSet.TOP,
                        parent.id,
                        ConstraintSet.TOP
                    )
                    set.connect(
                        descriptionLayout.id,
                        ConstraintSet.BOTTOM,
                        parent.id,
                        ConstraintSet.BOTTOM
                    )


                    set.connect(amountTextView.id, ConstraintSet.END, parent.id, ConstraintSet.END)
                    set.connect(amountTextView.id, ConstraintSet.TOP, parent.id, ConstraintSet.TOP)
                    set.connect(
                        amountTextView.id,
                        ConstraintSet.BOTTOM,
                        parent.id,
                        ConstraintSet.BOTTOM
                    )
                    set.applyTo(parent)


                    parent.setOnClickListener(object : View.OnClickListener {
                        /**
                         * Open payment detail view activity with details of selected payment
                         */
                        override fun onClick(v: View?) {
                            val intent =
                                Intent(this@MainActivity, PaymentDetailView::class.java).apply {
                                    putExtra("DESCRIPTION", it.description)
                                    putExtra("CREATED", it.created)
                                    putExtra("ALIASNAME", it.alias.displayName)
                                    putExtra("ALIASIBAN", it.alias.iban)
                                    putExtra("AMOUNT", it.amount.value)
                                    putExtra("COUNTERALIASNAME", it.counterpartyAlias.displayName)
                                    putExtra("COUNTERALIASIBAN", it.counterpartyAlias.iban)

                                }
                            startActivity(intent)


                        }
                    })
                    scrollContainer.addView(parent)
                }
            }, {
                it.printStackTrace()
            })
    }

    /**
     * Update balance by retrieving current balance from bunq server
     */
    private fun updateBalance() {

        Single.fromCallable {
            val apiContext = ApiContext.restore(
                ConfigDirectory.getDirectory(applicationContext)
            )
            val list = MonetaryAccount.list().value


            return@fromCallable list
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ list ->
                val accountList: List<MonetaryAccount> = list
                if (accountList.size == 1) {
                    val balance = accountList[0].monetaryAccountBank.balance.value
                    val balanceTextView: TextView = findViewById(R.id.balanceValueTextView)
                    balanceTextView.text = "€$balance"
                }


            }, {
                it.printStackTrace()
            })

    }


}
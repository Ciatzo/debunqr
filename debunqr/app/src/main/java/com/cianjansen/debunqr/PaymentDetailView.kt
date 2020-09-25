package com.cianjansen.debunqr

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class PaymentDetailView : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_detail_view)



        fillFields(savedInstanceState)
    }


    /**
     * Populate all designated text views with the data passed in Intent extras
     */
    private fun fillFields(savedInstanceState: Bundle?){
        val description: String?
        val created: String?
        val aliasName: String?
        val aliasIban: String?
        val amount: String?
        val counterAliasIban: String?
        val counterAliasName: String?

        if (savedInstanceState == null) {
            val extras = intent.extras
            description = extras?.getString("DESCRIPTION")
            created = extras?.getString("CREATED")
            aliasName = extras?.getString("ALIASNAME")
            aliasIban = extras?.getString("ALIASIBAN")
            amount = extras?.getString("AMOUNT")
            counterAliasIban = extras?.getString("COUNTERALIASIBAN")
            counterAliasName = extras?.getString("COUNTERALIASNAME")

        } else {
            description = savedInstanceState.getSerializable("DESCRIPTION") as String?
            created = savedInstanceState.getSerializable("CREATED") as String?
            aliasName = savedInstanceState.getSerializable("ALIASNAME") as String?
            aliasIban = savedInstanceState.getSerializable("ALIASIBAN") as String?
            amount = savedInstanceState.getSerializable("AMOUNT") as String?
            counterAliasIban = savedInstanceState.getSerializable("COUNTERALIASIBAN") as String?
            counterAliasName = savedInstanceState.getSerializable("ACOUNTERALIASNAME") as String?


        }
        var viewAmount : String? = amount
        if (amount != null) {
            if(amount.substring(0,1).equals("-") ){
                viewAmount = amount.removePrefix("-")
                findViewById<TextView>(R.id.arrowTextView).text = "→"
                findViewById<TextView>(R.id.amountTextView).setTextColor(Color.parseColor("#FFFF5722"))

            }else{
                findViewById<TextView>(R.id.arrowTextView).text = "←"
                findViewById<TextView>(R.id.amountTextView).setTextColor(Color.parseColor("#8BC34A"))

            }
        }

        findViewById<TextView>(R.id.descriptionTextView).text = "Description: $description"
        val l = LocalDate.parse(
            created,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.nnnnnn")
        )

        findViewById<TextView>(R.id.dateTextView).text = "${l.dayOfMonth} ${l.month.toString().toLowerCase().capitalize()} ${l.year}"
        findViewById<TextView>(R.id.thisPartyTextView).text = "$aliasName\n$aliasIban"
        findViewById<TextView>(R.id.counterPartyTextView).text = "$counterAliasName\n$counterAliasIban"
        findViewById<TextView>(R.id.amountTextView).text = "€$viewAmount"


    }
}
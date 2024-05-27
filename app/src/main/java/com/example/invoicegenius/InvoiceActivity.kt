package com.example.invoicegenius

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.gson.Gson

class InvoiceActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invoice)

        val data = intent.getSerializableExtra("invoiceData") as String
        val invoiceData = Gson().fromJson(data, InvoiceData::class.java)

        // Invoice template fields
        val invoiceNumberTemp: TextView = findViewById(R.id.invoiceNumberTemp)
        val issueDateTemp: TextView = findViewById(R.id.issueDateTemp)
        val sellDateTemp: TextView = findViewById(R.id.sellDateTemp)

        // Seller fields
        val companyNameSellerTemp: TextView = findViewById(R.id.companyNameSellerTemp)
        val addressSellerTemp: TextView = findViewById(R.id.addressSellerTemp)
        val nipSellerTemp: TextView = findViewById(R.id.nipSellerTemp)
        val phoneNumberSellerTemp: TextView = findViewById(R.id.phoneNumberSellerTemp)
        val bankAccountNumberTemp: TextView = findViewById(R.id.bankAccountNumberTemp)

        // Buyer fields
        val companyNameBuyerTemp: TextView = findViewById(R.id.companyNameBuyerTemp)
        val addressBuyerTemp: TextView = findViewById(R.id.addressBuyerTemp)
        val emailBuyerTemp: TextView = findViewById(R.id.emailBuyerTemp)
        val phoneNumberBuyerTemp: TextView = findViewById(R.id.phoneNumberBuyerTemp)

        // Summary fields
        val priceNettoTemp: TextView = findViewById(R.id.priceNettoTemp)
        val priceVatTemp: TextView = findViewById(R.id.priceVatTemp)
        val priceBruttoTemp: TextView = findViewById(R.id.priceBruttoTemp)
        val paymentMethodTemp: TextView = findViewById(R.id.paymentMethodTemp)
        val paymentTargetDateTemp: TextView = findViewById(R.id.paymentTargetDayTemp)
        val wholePriceTemp: TextView = findViewById(R.id.wholePriceTemp)
        val signatureSellerTemp: TextView = findViewById(R.id.signatureSellerTemp)

        //header data
        invoiceNumberTemp.text = "Faktura nr: ${invoiceData.invoiceNumber}"
        issueDateTemp.text = "Data wystawienia: ${invoiceData.issueDate}"
        sellDateTemp.text = "Data sprzedaży: ${invoiceData.sellDate}"

        //seller data
        companyNameSellerTemp.text = invoiceData.seller.companyName
        addressSellerTemp.text = invoiceData.seller.address
        nipSellerTemp.text = "NIP: ${invoiceData.seller.nip}"
        phoneNumberSellerTemp.text = "Telefon: ${invoiceData.seller.phoneNumber}"
        bankAccountNumberTemp.text = "Numer konta: ${invoiceData.seller.bankAccountNumber}"

        companyNameBuyerTemp.text = invoiceData.buyer.companyName
        addressBuyerTemp.text = invoiceData.buyer.address
        emailBuyerTemp.text = invoiceData.buyer.email
        phoneNumberBuyerTemp.text = "Telefon: ${invoiceData.buyer.phoneNumber}"

        paymentMethodTemp.text = invoiceData.paymentMethod
        paymentTargetDateTemp.text = invoiceData.paymentTargetDate

        signatureSellerTemp.text = invoiceData.seller.companyName
    }

}
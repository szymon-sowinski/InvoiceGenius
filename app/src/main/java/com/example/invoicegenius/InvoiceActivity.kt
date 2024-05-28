package com.example.invoicegenius

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson

class InvoiceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invoice)

        val data = intent.getSerializableExtra("invoiceData") as String
        val invoiceData = Gson().fromJson(data, InvoiceData::class.java)

        val invoiceNumberTemp: TextView = findViewById(R.id.invoiceNumberTemp)
        val issueDateTemp: TextView = findViewById(R.id.issueDateTemp)
        val sellDateTemp: TextView = findViewById(R.id.sellDateTemp)

        val companyNameSellerTemp: TextView = findViewById(R.id.companyNameSellerTemp)
        val addressSellerTemp: TextView = findViewById(R.id.addressSellerTemp)
        val nipSellerTemp: TextView = findViewById(R.id.nipSellerTemp)
        val phoneNumberSellerTemp: TextView = findViewById(R.id.phoneNumberSellerTemp)
        val bankAccountNumberTemp: TextView = findViewById(R.id.bankAccountNumberTemp)

        val companyNameBuyerTemp: TextView = findViewById(R.id.companyNameBuyerTemp)
        val addressBuyerTemp: TextView = findViewById(R.id.addressBuyerTemp)
        val emailBuyerTemp: TextView = findViewById(R.id.emailBuyerTemp)
        val phoneNumberBuyerTemp: TextView = findViewById(R.id.phoneNumberBuyerTemp)

        val priceNettoTemp: TextView = findViewById(R.id.priceNettoTemp)
        val priceVatTemp: TextView = findViewById(R.id.priceVatTemp)
        val priceBruttoTemp: TextView = findViewById(R.id.priceBruttoTemp)
        val paymentMethodTemp: TextView = findViewById(R.id.paymentMethodTemp)
        val paymentTargetDateTemp: TextView = findViewById(R.id.paymentTargetDayTemp)
        val wholePriceTemp: TextView = findViewById(R.id.wholePriceTemp)
        val signatureSellerTemp: TextView = findViewById(R.id.signatureSellerTemp)

        invoiceNumberTemp.text = "Faktura nr: ${invoiceData.invoiceNumber}"
        issueDateTemp.text = "Data wystawienia: ${invoiceData.issueDate}"
        sellDateTemp.text = "Data sprzedaży: ${invoiceData.sellDate}"

        companyNameSellerTemp.text = invoiceData.seller.companyName
        addressSellerTemp.text = invoiceData.seller.address
        nipSellerTemp.text = "NIP: ${invoiceData.seller.nip}"
        phoneNumberSellerTemp.text = "Telefon: ${invoiceData.seller.phoneNumber}"
        bankAccountNumberTemp.text = "Numer konta: ${invoiceData.seller.bankAccountNumber}"

        companyNameBuyerTemp.text = invoiceData.buyer.companyName
        addressBuyerTemp.text = invoiceData.buyer.address
        emailBuyerTemp.text = invoiceData.buyer.email
        phoneNumberBuyerTemp.text = "Telefon: ${invoiceData.buyer.phoneNumber}"

        val productsContainer = findViewById<LinearLayout>(R.id.productsContainer)
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        var totalNetto = 0.0
        var totalVat = 0.0
        var totalBrutto = 0.0

        for ((index, product) in invoiceData.products.withIndex()) {
            val productItemView = inflater.inflate(R.layout.product_item_template, null)

            val vatAmount = product.productPriceNetto * product.vatRate / 100
            productItemView.findViewById<TextView>(R.id.positionNumber).text = (index + 1).toString()
            productItemView.findViewById<TextView>(R.id.productName).text = product.productName
            productItemView.findViewById<TextView>(R.id.productAmount).text = product.productAmount.toString()
            productItemView.findViewById<TextView>(R.id.productMeasure).text = product.productMeasure
            productItemView.findViewById<TextView>(R.id.productPriceNetto).text = "${product.productPriceNetto} PLN"
            productItemView.findViewById<TextView>(R.id.productVatRate).text = "${product.vatRate}%"
            productItemView.findViewById<TextView>(R.id.productVatAmount).text = "${vatAmount} PLN"
            productItemView.findViewById<TextView>(R.id.productPriceBrutto).text = "${product.productPriceNetto + vatAmount} PLN"

            totalVat += vatAmount
            totalNetto += product.productPriceNetto
            totalBrutto += product.productPriceNetto + vatAmount
            productsContainer.addView(productItemView)
        }
        val formattedNetto = String.format("%.2f", totalNetto)
        val formattedVat = String.format("%.2f", totalVat)
        val formattedBrutto = String.format("%.2f", totalBrutto)

        priceNettoTemp.text = "${formattedNetto} PLN"
        priceVatTemp.text = "${formattedVat} PLN"
        priceBruttoTemp.text = "${formattedBrutto} PLN"
        wholePriceTemp.text = "${formattedBrutto} PLN"
        paymentMethodTemp.text = invoiceData.paymentMethod
        paymentTargetDateTemp.text = invoiceData.paymentTargetDate

        signatureSellerTemp.text = invoiceData.seller.companyName
    }
}

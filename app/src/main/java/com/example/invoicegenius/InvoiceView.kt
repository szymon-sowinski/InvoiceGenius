package com.example.invoicegenius

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.example.invoicegenius.ui.form.FormFragment

class InvoiceView(context: Context) : FormFragment() {


    private val invoiceView: View = LayoutInflater.from(context).inflate(R.layout.activity_invoice, null)
    private val dataContainer: LinearLayout = invoiceView.findViewById(R.id.invoiceDataContainer)

    // Invoice template fields
    val invoiceNumberTemp: TextView = invoiceView.findViewById(R.id.invoiceNumberTemp)
    val issueDateTemp: TextView = invoiceView.findViewById(R.id.issueDateTemp)
    val sellDateTemp: TextView = invoiceView.findViewById(R.id.sellDateTemp)

    // Seller fields
    val companyNameSellerTemp: TextView = invoiceView.findViewById(R.id.companyNameSellerTemp)
    val addressSellerTemp: TextView = invoiceView.findViewById(R.id.addressSellerTemp)
    val nipSellerTemp: TextView = invoiceView.findViewById(R.id.nipSellerTemp)
    val phoneNumberSellerTemp: TextView = invoiceView.findViewById(R.id.phoneNumberSellerTemp)
    val bankAccountNumberTemp: TextView = invoiceView.findViewById(R.id.bankAccountNumberTemp)

    // Buyer fields
    val companyNameBuyerTemp: TextView = invoiceView.findViewById(R.id.companyNameBuyerTemp)
    val addressBuyerTemp: TextView = invoiceView.findViewById(R.id.addressBuyerTemp)
    val emailBuyerTemp: TextView = invoiceView.findViewById(R.id.emailBuyerTemp)
    val phoneNumberBuyerTemp: TextView = invoiceView.findViewById(R.id.phoneNumberBuyerTemp)

    // Summary fields
    val priceNettoTemp: TextView = invoiceView.findViewById(R.id.priceNettoTemp)
    val priceVatTemp: TextView = invoiceView.findViewById(R.id.priceVatTemp)
    val priceBruttoTemp: TextView = invoiceView.findViewById(R.id.priceBruttoTemp)
    val paymentMethodTemp: TextView = invoiceView.findViewById(R.id.paymentMethodTemp)
    val paymentTargetDateTemp: TextView = invoiceView.findViewById(R.id.paymentTargetDayTemp)
    val wholePriceTemp: TextView = invoiceView.findViewById(R.id.wholePriceTemp)
    val signatureSellerTemp: TextView = invoiceView.findViewById(R.id.signatureSellerTemp)

    fun fillInvoiceFields(invoiceData: InvoiceData) {
        invoiceNumberTemp.text = invoiceData.invoiceNumber
        issueDateTemp.text = invoiceData.issueDate
        sellDateTemp.text = invoiceData.sellDate

        companyNameSellerTemp.text = invoiceData.seller.companyName
        addressSellerTemp.text = invoiceData.seller.address
        nipSellerTemp.text = invoiceData.seller.nip
        phoneNumberSellerTemp.text = invoiceData.seller.phoneNumber
        bankAccountNumberTemp.text = invoiceData.seller.bankAccountNumber

        companyNameBuyerTemp.text = invoiceData.buyer.companyName
        addressBuyerTemp.text = invoiceData.buyer.address
        emailBuyerTemp.text = invoiceData.buyer.email
        phoneNumberBuyerTemp.text = invoiceData.buyer.phoneNumber

        paymentMethodTemp.text = invoiceData.paymentMethod
        paymentTargetDateTemp.text = invoiceData.paymentTargetDate

        signatureSellerTemp.text = invoiceData.seller.companyName

    }

    fun addProducts(products: List<Map<String, String>>) {
        val inflater = LayoutInflater.from(invoiceView.context)
        products.forEachIndexed { index, product ->
            val productView = inflater.inflate(R.layout.product_item_template, dataContainer, false)
            productView.findViewById<TextView>(R.id.positionNumber).text = (index + 1).toString()
            productView.findViewById<TextView>(R.id.productName).text = product["name"]
            productView.findViewById<TextView>(R.id.productAmount).text = product["amount"]
            productView.findViewById<TextView>(R.id.productMeasure).text = product["measure"]
            productView.findViewById<TextView>(R.id.productPriceNetto).text = product["price"]
            productView.findViewById<TextView>(R.id.productVatRate).text = product["vat"]

            invoiceNumberTemp.text = invoiceNumber.text.toString().trim()

            issueDateTemp.text = issueDate.text.toString().trim()
            sellDateTemp.text = sellDate.text.toString().trim()

            companyNameSellerTemp.text = companyNameSeller.text.toString().trim()
            addressSellerTemp.text = addressSeller.text.toString().trim()
            nipSellerTemp.text = nipSeller.text.toString().trim()
            phoneNumberSellerTemp.text = phoneNumberSeller.text.toString().trim()
            bankAccountNumberTemp.text = bankAccountNumber.text.toString().trim()

            companyNameBuyerTemp.text = companyNameBuyer.text.toString().trim()
            addressBuyerTemp.text = addressBuyer.text.toString().trim()
            emailBuyerTemp.text = emailBuyer.text.toString().trim()
            phoneNumberBuyerTemp.text = phoneNumberBuyer.text.toString().trim()

            val priceNetto = product["price"]?.toDoubleOrNull() ?: 0.0
            val vatRate = product["vat"]?.toDoubleOrNull() ?: 0.0
            val vatAmount = priceNetto * vatRate / 100
            val priceBrutto = priceNetto + vatAmount

            productView.findViewById<TextView>(R.id.productVatAmount).text =
                String.format("%.2f PLN", vatAmount)
            productView.findViewById<TextView>(R.id.productPriceBrutto).text =
                String.format("%.2f PLN", priceBrutto)

            dataContainer.addView(productView)
        }
    }
}

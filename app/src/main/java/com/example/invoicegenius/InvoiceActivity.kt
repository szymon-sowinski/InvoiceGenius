package com.example.invoicegenius

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.gson.Gson
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import java.io.File
import java.io.FileOutputStream

class InvoiceActivity : AppCompatActivity() {

    private val REQUEST_WRITE_PERMISSION = 786

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invoice)

        val data = intent.getStringExtra("invoiceData") as String
        val invoiceData = Gson().fromJson(data, InvoiceData::class.java)

        setupInvoiceView(invoiceData)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_WRITE_PERMISSION)
        } else {
            val pdfFile = generatePdf(invoiceData)
            openPdf(pdfFile)
        }
    }

    private fun setupInvoiceView(invoiceData: InvoiceData) {
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

    private fun generatePdf(invoiceData: InvoiceData): File {
        val file = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "invoice.pdf")
        val pdfWriter = PdfWriter(FileOutputStream(file))
        val pdfDocument = com.itextpdf.kernel.pdf.PdfDocument(pdfWriter)
        val document = Document(pdfDocument)

        document.add(Paragraph("Faktura nr: ${invoiceData.invoiceNumber}"))
        document.add(Paragraph("Data wystawienia: ${invoiceData.issueDate}"))
        document.add(Paragraph("Data sprzedaży: ${invoiceData.sellDate}"))
        document.add(Paragraph("Sprzedawca: ${invoiceData.seller.companyName}"))
        document.add(Paragraph("Adres: ${invoiceData.seller.address}"))
        document.add(Paragraph("NIP: ${invoiceData.seller.nip}"))
        document.add(Paragraph("Telefon: ${invoiceData.seller.phoneNumber}"))
        document.add(Paragraph("Numer konta: ${invoiceData.seller.bankAccountNumber}"))

        document.add(Paragraph("Nabywca: ${invoiceData.buyer.companyName}"))
        document.add(Paragraph("Adres: ${invoiceData.buyer.address}"))
        document.add(Paragraph("Email: ${invoiceData.buyer.email}"))
        document.add(Paragraph("Telefon: ${invoiceData.buyer.phoneNumber}"))

        var totalNetto = 0.0
        var totalVat = 0.0
        var totalBrutto = 0.0

        for ((index, product) in invoiceData.products.withIndex()) {
            val vatAmount = product.productPriceNetto * product.vatRate / 100
            document.add(Paragraph("Produkt ${index + 1}: ${product.productName}"))
            document.add(Paragraph("Ilość: ${product.productAmount}"))
            document.add(Paragraph("Jednostka: ${product.productMeasure}"))
            document.add(Paragraph("Cena netto: ${product.productPriceNetto} PLN"))
            document.add(Paragraph("Stawka VAT: ${product.vatRate}%"))
            document.add(Paragraph("Kwota VAT: ${vatAmount} PLN"))
            document.add(Paragraph("Cena brutto: ${product.productPriceNetto + vatAmount} PLN"))

            totalNetto += product.productPriceNetto
            totalVat += vatAmount
            totalBrutto += product.productPriceNetto + vatAmount
        }

        val formattedNetto = String.format("%.2f", totalNetto)
        val formattedVat = String.format("%.2f", totalVat)
        val formattedBrutto = String.format("%.2f", totalBrutto)

        document.add(Paragraph("Cena netto: $formattedNetto PLN"))
        document.add(Paragraph("VAT: $formattedVat PLN"))
        document.add(Paragraph("Cena brutto: $formattedBrutto PLN"))

        document.add(Paragraph("Metoda płatności: ${invoiceData.paymentMethod}"))
        document.add(Paragraph("Termin płatności: ${invoiceData.paymentTargetDate}"))

        document.close()
        return file
    }

    private fun openPdf(pdfFile: File) {
        val intent = Intent(Intent.ACTION_VIEW)
        val uri = FileProvider.getUriForFile(this, "${applicationContext.packageName}.fileprovider", pdfFile)
        intent.setDataAndType(uri, "application/pdf")
        intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        val chooser = Intent.createChooser(intent, "Open PDF")
        startActivity(chooser)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_WRITE_PERMISSION) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                val data = intent.getStringExtra("invoiceData") as String
                val invoiceData = Gson().fromJson(data, InvoiceData::class.java)
                val pdfFile = generatePdf(invoiceData)
                openPdf(pdfFile)
            }
        }
    }
}

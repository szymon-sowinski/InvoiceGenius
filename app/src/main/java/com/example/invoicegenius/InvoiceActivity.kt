package com.example.invoicegenius

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.gson.Gson;
import com.itextpdf.io.font.PdfEncodings
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;

import java.io.File;
import java.io.FileOutputStream;

class InvoiceActivity : AppCompatActivity() {

    private val REQUEST_WRITE_PERMISSION = 786

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invoice)

        val data = intent.getStringExtra("invoiceData") as String
        val invoiceData = Gson().fromJson(data, InvoiceData::class.java)

        setupInvoiceView(invoiceData)

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_WRITE_PERMISSION
            )
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
            productItemView.findViewById<TextView>(R.id.positionNumber).text =
                (index + 1).toString()
            productItemView.findViewById<TextView>(R.id.productName).text = product.productName
            productItemView.findViewById<TextView>(R.id.productAmount).text =
                product.productAmount.toString()
            productItemView.findViewById<TextView>(R.id.productMeasure).text =
                product.productMeasure
            productItemView.findViewById<TextView>(R.id.productPriceNetto).text =
                "${product.productPriceNetto} PLN"
            productItemView.findViewById<TextView>(R.id.productVatRate).text = "${product.vatRate}%"
            productItemView.findViewById<TextView>(R.id.productVatAmount).text = "${vatAmount} PLN"
            productItemView.findViewById<TextView>(R.id.productPriceBrutto).text =
                "${product.productPriceNetto + vatAmount} PLN"

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
        val file = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "faktura.pdf")
        val pdfWriter = PdfWriter(FileOutputStream(file))
        val pdfDocument = PdfDocument(pdfWriter)
        val document = Document(pdfDocument)

        val fontStream = assets.open("lato_regular.ttf")
        val fontBytes = fontStream.readBytes()
        val font = PdfFontFactory.createFont(fontBytes, PdfEncodings.IDENTITY_H)

        val headerTable = Table(floatArrayOf(1f, 1f))
        headerTable.setWidth(UnitValue.createPercentValue(100f))
        headerTable.addCell(
            Cell().add(
                Paragraph("Faktura nr: ${invoiceData.invoiceNumber}").setFont(
                    font
                )
            )
        )
        headerTable.addCell(
            Cell().add(
                Paragraph("Data wystawienia: ${invoiceData.issueDate}\nData sprzedaży: ${invoiceData.sellDate}").setFont(
                    font
                )
            )
        )
        document.add(headerTable)

        val infoTable = Table(floatArrayOf(1f, 1f))
        infoTable.setWidth(UnitValue.createPercentValue(100f))
        infoTable.addCell(
            Cell().add(
                Paragraph("Sprzedawca: ${invoiceData.seller.companyName}\n${invoiceData.seller.address}\nNIP: ${invoiceData.seller.nip}\nTelefon: ${invoiceData.seller.phoneNumber}\nNumer konta: ${invoiceData.seller.bankAccountNumber}").setFont(
                    font
                )
            )
        )
        infoTable.addCell(
            Cell().add(
                Paragraph("Nabywca: ${invoiceData.buyer.companyName}\n${invoiceData.buyer.address}\nEmail: ${invoiceData.buyer.email}\nTelefon: ${invoiceData.buyer.phoneNumber}").setFont(
                    font
                )
            )
        )
        document.add(infoTable)

        val productTable = Table(floatArrayOf(1f, 3f, 1f, 1f, 1f, 1f, 1f))
        productTable.setWidth(UnitValue.createPercentValue(100f))
        productTable.addHeaderCell(Cell().add(Paragraph("Lp.").setFont(font)))
        productTable.addHeaderCell(Cell().add(Paragraph("Nazwa towaru/usługi").setFont(font)))
        productTable.addHeaderCell(Cell().add(Paragraph("Ilość").setFont(font)))
        productTable.addHeaderCell(Cell().add(Paragraph("Jed. miary").setFont(font)))
        productTable.addHeaderCell(Cell().add(Paragraph("Cena netto").setFont(font)))
        productTable.addHeaderCell(Cell().add(Paragraph("VAT").setFont(font)))
        productTable.addHeaderCell(Cell().add(Paragraph("Cena brutto").setFont(font)))

        for ((index, product) in invoiceData.products.withIndex()) {
            val vatAmount = product.productPriceNetto * product.vatRate / 100
            val productPriceBrutto = product.productPriceNetto + vatAmount
            productTable.addCell(Cell().add(Paragraph((index + 1).toString()).setFont(font)))
            productTable.addCell(Cell().add(Paragraph(product.productName).setFont(font)))
            productTable.addCell(Cell().add(Paragraph(product.productAmount.toString()).setFont(font)))
            productTable.addCell(Cell().add(Paragraph(product.productMeasure).setFont(font)))
            productTable.addCell(
                Cell().add(
                    Paragraph(
                        String.format(
                            "%.2f PLN",
                            product.productPriceNetto
                        )
                    ).setFont(font)
                )
            )
            productTable.addCell(
                Cell().add(
                    Paragraph(
                        String.format(
                            "%.2f%%",
                            product.vatRate
                        )
                    ).setFont(font)
                )
            )
            productTable.addCell(
                Cell().add(
                    Paragraph(
                        String.format(
                            "%.2f PLN",
                            productPriceBrutto
                        )
                    ).setFont(font)
                )
            )
        }
        document.add(productTable)

        // Add summary
        val summaryTable = Table(floatArrayOf(1f, 1f, 1f))
        summaryTable.setWidth(UnitValue.createPercentValue(100f))
        summaryTable.addCell(Cell(1, 3).add(Paragraph("Podsumowanie:").setFont(font)))
        summaryTable.addCell(Cell().add(Paragraph("Netto").setFont(font)))
        summaryTable.addCell(Cell().add(Paragraph("VAT").setFont(font)))
        summaryTable.addCell(Cell().add(Paragraph("Brutto").setFont(font)))
        summaryTable.addCell(
            Cell().add(
                Paragraph(
                    String.format(
                        "%.2f PLN",
                        invoiceData.products.sumByDouble { it.productPriceNetto.toDouble() })
                )
                    .setFont(font)
            )
        )

        summaryTable.addCell(
            Cell().add(
                Paragraph(
                    String.format(
                        "%.2f PLN",
                        invoiceData.products.sumByDouble { (it.productPriceNetto * it.vatRate / 100).toDouble() })
                )
                    .setFont(font)
            )
        );

        summaryTable.addCell(
            Cell().add(
                Paragraph(
                    String.format(
                        "%.2f PLN",
                        invoiceData.products.sumByDouble { (it.productPriceNetto * (1 + it.vatRate / 100)).toDouble() })
                )
                    .setFont(font)
            )
        )

        document.add(summaryTable)

        // Add payment info
        val paymentTable = Table(floatArrayOf(1f, 1f, 1f))
        paymentTable.setWidth(UnitValue.createPercentValue(100f))
        paymentTable.addCell(Cell(1, 3).add(Paragraph("Do zapłaty:").setFont(font)))
        paymentTable.addCell(Cell().add(Paragraph("Sposób zapłaty").setFont(font)))
        paymentTable.addCell(Cell().add(Paragraph("Termin płatności").setFont(font)))
        paymentTable.addCell(Cell().add(Paragraph("Kwota do zapłaty").setFont(font)))
        paymentTable.addCell(Cell().add(Paragraph(invoiceData.paymentMethod).setFont(font)))
        paymentTable.addCell(Cell().add(Paragraph(invoiceData.paymentTargetDate).setFont(font)))
        paymentTable.addCell(
            Cell().add(
                Paragraph(
                    String.format(
                        "%.2f PLN",
                        invoiceData.products.sumByDouble { (it.productPriceNetto * (1 + it.vatRate / 100)).toDouble() })
                ).setFont(font)
            )
        )

        document.add(paymentTable)

        val footerTable = Table(floatArrayOf(1f, 1f))
        footerTable.setWidth(UnitValue.createPercentValue(100f))
        footerTable.addCell(Cell().add(Paragraph("Wystawił:").setFont(font)))
        footerTable.addCell(Cell().add(Paragraph("Otrzymał:").setFont(font)))
        footerTable.addCell(Cell().add(Paragraph(invoiceData.seller.companyName).setFont(font)))
        footerTable.addCell(Cell())
        document.add(footerTable)

        document.add(
            Paragraph("Faktura wygenerowana przez: InvoiceGenius").setFont(font).setFontSize(10f)
                .setTextAlignment(
                    TextAlignment.CENTER
                )
        )

        document.close()
        return file
    }


    private fun openPdf(pdfFile: File) {
        val intent = Intent(Intent.ACTION_VIEW)
        val uri = FileProvider.getUriForFile(
            this,
            "${applicationContext.packageName}.fileprovider",
            pdfFile
        )
        intent.setDataAndType(uri, "application/pdf")
        intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        val chooser = Intent.createChooser(intent, "Open PDF")
        startActivity(chooser)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
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

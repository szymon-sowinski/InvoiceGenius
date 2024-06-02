package com.example.invoicegenius.ui.file

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.invoicegenius.Buyer
import com.example.invoicegenius.InvoiceData
import com.example.invoicegenius.Product
import com.example.invoicegenius.Seller
import com.example.invoicegenius.databinding.ActivityGeneratePdfBinding
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.property.TextAlignment
import com.itextpdf.layout.property.UnitValue
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader


class GeneratePdfActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGeneratePdfBinding
    private var fileUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGeneratePdfBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fileUri = intent.getParcelableExtra("file_uri")
        val fileName = intent.getStringExtra("file_name")
        if (fileUri != null && fileName != null) {
            readFileContent(fileUri!!, fileName)
        } else {
            Toast.makeText(this, "Nie udało się załadować pliku", Toast.LENGTH_SHORT).show()
        }
    }

    private fun readFileContent(uri: Uri, fileName: String) {
        val inputStream = contentResolver.openInputStream(uri)
        inputStream?.use {
            when {
                fileName.endsWith(".json", true) -> {
                    val invoiceData = readInvoiceDataFromJson(it)
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
                        generatePdf(invoiceData)
                    }
                }

                fileName.endsWith(".xml", true) -> {
                    val invoiceData = readInvoiceDataFromXml(it)
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
                        generatePdf(invoiceData)
                    }
                }
            }
        }
    }

    private fun readInvoiceDataFromXml(inputStream: InputStream): InvoiceData {
        val parserFactory = XmlPullParserFactory.newInstance()
        val parser = parserFactory.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(inputStream, null)

        var eventType = parser.eventType
        var invoiceNumber = ""
        var seller: Seller? = null
        var buyer: Buyer? = null
        var sellDate = ""
        var issueDate = ""
        var targetPaymentDate = ""
        var paymentMethod = ""
        var paymentDate = ""
        val products = mutableListOf<Product>()

        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "numer_faktury" -> invoiceNumber = parser.nextText()
                        "sprzedawca" -> seller = readSeller(parser)
                        "nabywca" -> buyer = readBuyer(parser)
                        "data_sprzedazy" -> sellDate = parser.nextText()
                        "data_wystawienia_faktury" -> issueDate = parser.nextText()
                        "termin_platnosci" -> targetPaymentDate = parser.nextText()
                        "metoda_platnosci" -> paymentMethod = parser.nextText()
                        "data_platnosci" -> paymentDate = parser.nextText()
                        "produkty" -> products.addAll(readProducts(parser))
                    }
                }
            }
            eventType = parser.next()
        }

        return InvoiceData(
            invoiceNumber = invoiceNumber,
            seller = seller ?: Seller("", "", "", "", ""),
            buyer = buyer ?: Buyer("", "", "", ""),
            sellDate = sellDate,
            issueDate = issueDate,
            paymentTargetDate = targetPaymentDate,
            paymentMethod = paymentMethod,
            paymentDate = paymentDate,
            products = products
        )
    }

    private fun readSeller(parser: XmlPullParser): Seller {
        var companyName = ""
        var address = ""
        var nip = ""
        var bankAccountNumber = ""
        var phoneNumber = ""

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "nazwa_firmy" -> companyName = parser.nextText()
                "adres" -> address = parser.nextText()
                "nip" -> nip = parser.nextText()
                "numer_konta_bankowego" -> bankAccountNumber = parser.nextText()
                "numer_telefonu" -> phoneNumber = parser.nextText()
            }
        }

        return Seller(companyName, address, nip, bankAccountNumber, phoneNumber)
    }

    private fun readBuyer(parser: XmlPullParser): Buyer {
        var companyName = ""
        var address = ""
        var email = ""
        var phoneNumber = ""

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "nazwa_firmy" -> companyName = parser.nextText()
                "adres" -> address = parser.nextText()
                "email" -> email = parser.nextText()
                "telefon" -> phoneNumber = parser.nextText()
            }
        }

        return Buyer(companyName, address, email, phoneNumber)
    }

    private fun readProducts(parser: XmlPullParser): List<Product> {
        val products = mutableListOf<Product>()
        var eventType = parser.eventType

        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG && parser.name == "produkt") {
                products.add(readProduct(parser))
            }

            if (eventType == XmlPullParser.END_TAG && parser.name == "produkty") {
                break
            }

            eventType = parser.next()
        }

        return products
    }

    private fun readProduct(parser: XmlPullParser): Product {
        var productName = ""
        var productAmount = 0.0f
        var productMeasure = ""
        var productPriceNetto = 0.0f
        var vatRate = 0.0f

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "nazwa_produktu" -> productName = parser.nextText()
                "ilosc_produktu" -> productAmount = parser.nextText().toFloat()
                "jednostka_miary" -> productMeasure = parser.nextText()
                "cena_netto" -> productPriceNetto = parser.nextText().toFloat()
                "stawka_podatku_vat" -> vatRate = parser.nextText().toFloat()
            }
        }

        return Product(productName, productAmount, productMeasure, productPriceNetto, vatRate)
    }

    private fun readInvoiceDataFromJson(inputStream: InputStream): InvoiceData {
        val reader = InputStreamReader(inputStream)
        val gson = Gson()
        val jsonObject = gson.fromJson(reader, JsonObject::class.java)

        val invoiceNumber = jsonObject.get("numer_faktury").asString

        val sellerObject = jsonObject.getAsJsonObject("sprzedawca")
        val sellerCompanyName = sellerObject.get("nazwa_firmy").asString
        val sellerAddress = sellerObject.get("adres").asString
        val sellerNip = sellerObject.get("nip").asString
        val sellerBankAccountNumber = sellerObject.get("numer_konta_bankowego").asString
        val sellerPhoneNumber = sellerObject.get("numer_telefonu").asString

        val buyerObject = jsonObject.getAsJsonObject("nabywca")
        val buyerCompanyName = buyerObject.get("nazwa_firmy").asString
        val buyerAddress = buyerObject.get("adres").asString
        val buyerEmail = buyerObject.get("email").asString
        val buyerPhoneNumber = buyerObject.get("telefon").asString

        val sellDate = jsonObject.get("data_sprzedazy").asString
        val issueDate = jsonObject.get("data_wystawienia_faktury").asString
        val targetPaymentDate = jsonObject.get("termin_platnosci").asString
        val paymentMethod = jsonObject.get("metoda_platnosci").asString
        val paymentDate = jsonObject.get("data_platnosci").asString

        val productsArray = jsonObject.getAsJsonArray("produkty")
        val products = mutableListOf<Product>()

        for (productElement in productsArray) {
            val productObject = productElement.asJsonObject
            val productName = productObject.get("nazwa_produktu").asString
            val productAmount = productObject.get("ilosc_produktu").asFloat
            val productMeasure = productObject.get("jednostka_miary").asString
            val productPriceNetto = productObject.get("cena_netto").asFloat
            val vatRate = productObject.get("stawka_podatku_vat").asFloat

            val product = Product(
                productName = productName,
                productAmount = productAmount,
                productMeasure = productMeasure,
                productPriceNetto = productPriceNetto,
                vatRate = vatRate
            )
            products.add(product)
        }

        return InvoiceData(
            invoiceNumber = invoiceNumber,
            seller = Seller(
                companyName = sellerCompanyName,
                address = sellerAddress,
                nip = sellerNip,
                bankAccountNumber = sellerBankAccountNumber,
                phoneNumber = sellerPhoneNumber
            ),
            buyer = Buyer(
                companyName = buyerCompanyName,
                address = buyerAddress,
                email = buyerEmail,
                phoneNumber = buyerPhoneNumber
            ),
            sellDate = sellDate,
            issueDate = issueDate,
            paymentTargetDate = targetPaymentDate,
            paymentMethod = paymentMethod,
            paymentDate = paymentDate,
            products = products
        )
    }


    private fun generatePdf(invoiceData: InvoiceData) {
        val file = File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "faktura.pdf")

        val pdfWriter = com.itextpdf.kernel.pdf.PdfWriter(file)
        val pdfDocument = PdfDocument(pdfWriter)
        val document = com.itextpdf.layout.Document(pdfDocument)

        val fontStream = assets.open("lato_regular.ttf")
        val fontBytes = fontStream.readBytes()
        val font = PdfFontFactory.createFont(fontBytes, "Cp1250")

        document.setFont(font)

        val headerTable = Table(floatArrayOf(1f, 1f))
        headerTable.setWidth(UnitValue.createPercentValue(100f))
        headerTable.addCell(
            Cell().add(
                Paragraph("Faktura nr: ${invoiceData.invoiceNumber}")
            )
        )
        headerTable.addCell(
            Cell().add(
                Paragraph("Data wystawienia: ${invoiceData.issueDate}\nData sprzedaży: ${invoiceData.sellDate}")
            )
        )
        document.add(headerTable)

        val infoTable = Table(floatArrayOf(1f, 1f))
        infoTable.setWidth(UnitValue.createPercentValue(100f))
        infoTable.addCell(
            Cell().add(
                Paragraph("Sprzedawca: ${invoiceData.seller.companyName}\n${invoiceData.seller.address}\nNIP: ${invoiceData.seller.nip}\nTelefon: ${invoiceData.seller.phoneNumber}\nNumer konta: ${invoiceData.seller.bankAccountNumber}")
            )
        )
        infoTable.addCell(
            Cell().add(
                Paragraph("Nabywca: ${invoiceData.buyer.companyName}\n${invoiceData.buyer.address}\nEmail: ${invoiceData.buyer.email}\nTelefon: ${invoiceData.buyer.phoneNumber}")
            )
        )
        document.add(infoTable)

        val productTable = Table(floatArrayOf(1f, 3f, 1f, 1f, 1f, 1f, 1f))
        productTable.setWidth(UnitValue.createPercentValue(100f))
        productTable.addHeaderCell(Cell().add(Paragraph("Lp.")))
        productTable.addHeaderCell(Cell().add(Paragraph("Nazwa towaru/usługi")))
        productTable.addHeaderCell(Cell().add(Paragraph("Ilość")))
        productTable.addHeaderCell(Cell().add(Paragraph("Jed. miary")))
        productTable.addHeaderCell(Cell().add(Paragraph("Cena netto")))
        productTable.addHeaderCell(Cell().add(Paragraph("VAT")))
        productTable.addHeaderCell(Cell().add(Paragraph("Cena brutto")))

        for ((index, product) in invoiceData.products.withIndex()) {
            val vatAmount = product.productPriceNetto * product.vatRate / 100
            val productPriceBrutto = product.productPriceNetto + vatAmount
            productTable.addCell(Cell().add(Paragraph((index + 1).toString())))
            productTable.addCell(Cell().add(Paragraph(product.productName)))
            productTable.addCell(Cell().add(Paragraph(product.productAmount.toString())))
            productTable.addCell(Cell().add(Paragraph(product.productMeasure)))
            productTable.addCell(
                Cell().add(
                    Paragraph(
                        String.format(
                            "%.2f PLN",
                            product.productPriceNetto
                        )
                    )
                )
            )
            productTable.addCell(Cell().add(Paragraph(String.format("%.2f%%", product.vatRate))))
            productTable.addCell(
                Cell().add(
                    Paragraph(
                        String.format(
                            "%.2f PLN",
                            productPriceBrutto
                        )
                    )
                )
            )
        }
        document.add(productTable)
        val summaryTable = Table(floatArrayOf(1f, 1f, 1f))
        summaryTable.setWidth(UnitValue.createPercentValue(100f))
        summaryTable.addCell(Cell(1, 3).add(Paragraph("Podsumowanie:")))
        summaryTable.addCell(Cell().add(Paragraph("Netto")))
        summaryTable.addCell(Cell().add(Paragraph("VAT")))
        summaryTable.addCell(Cell().add(Paragraph("Brutto")))
        summaryTable.addCell(
            Cell().add(
                Paragraph(
                    String.format(
                        "%.2f PLN",
                        invoiceData.products.sumByDouble { it.productPriceNetto.toDouble() })
                )
            )
        )
        summaryTable.addCell(
            Cell().add(
                Paragraph(
                    String.format(
                        "%.2f PLN",
                        invoiceData.products.sumByDouble { (it.productPriceNetto * it.vatRate / 100).toDouble() })
                )
            )
        )
        summaryTable.addCell(
            Cell().add(
                Paragraph(
                    String.format(
                        "%.2f PLN",
                        invoiceData.products.sumByDouble { ((it.productPriceNetto * (1 + it.vatRate / 100)).toDouble()) })
                )
            )
        )
        document.add(summaryTable)

        val paymentTable = Table(floatArrayOf(1f, 1f, 1f))
        paymentTable.setWidth(UnitValue.createPercentValue(100f))
        paymentTable.addCell(Cell(1, 3).add(Paragraph("Do zapłaty:")))
        paymentTable.addCell(Cell().add(Paragraph("Metoda płatności")))
        paymentTable.addCell(Cell().add(Paragraph("Termin płatności")))
        paymentTable.addCell(Cell().add(Paragraph("Kwota do zapłaty")))
        paymentTable.addCell(Cell().add(Paragraph(invoiceData.paymentMethod)))
        paymentTable.addCell(Cell().add(Paragraph(invoiceData.paymentTargetDate)))
        paymentTable.addCell(
            Cell().add(
                Paragraph(
                    String.format(
                        "%.2f PLN",
                        invoiceData.products.sumByDouble { ((it.productPriceNetto * (1 + it.vatRate / 100)).toDouble()) })
                )
            )
        )
        document.add(paymentTable)

        val footerTable = Table(floatArrayOf(1f, 1f))
        footerTable.setWidth(UnitValue.createPercentValue(100f))
        footerTable.addCell(Cell().add(Paragraph("Wystawił:")))
        footerTable.addCell(Cell().add(Paragraph("Otrzymał:")))
        footerTable.addCell(Cell().add(Paragraph(invoiceData.seller.companyName)))
        footerTable.addCell(Cell())
        document.add(footerTable)

        document.add(
            Paragraph("Faktura wygenerowana przez: InvoiceGenius").setFontSize(10f)
                .setTextAlignment(TextAlignment.CENTER)
        )

        document.close()

        openPdf(file)
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

    companion object {
        private const val REQUEST_WRITE_PERMISSION = 100
    }
}

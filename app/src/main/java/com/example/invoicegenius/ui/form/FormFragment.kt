package com.example.invoicegenius.ui.form

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModelProvider
import com.example.invoicegenius.Buyer
import com.example.invoicegenius.InvoiceActivity
import com.example.invoicegenius.InvoiceData
import com.example.invoicegenius.InvoiceView
import com.example.invoicegenius.MainActivity
import com.example.invoicegenius.Product
import com.example.invoicegenius.R
import com.example.invoicegenius.Seller
import com.example.invoicegenius.databinding.FragmentFormBinding
import com.google.gson.Gson

open class FormFragment : Fragment() {

    // Lazy initialization for views
    val invoiceNumber by lazy { binding.invoiceNumber }

    // Seller data
    val companyNameSeller by lazy { binding.companyNameSeller }
    val addressSeller by lazy { binding.addressSeller }
    val nipSeller by lazy { binding.nipSeller }
    val bankAccountNumber by lazy { binding.bankAccountNumber }
    val phoneNumberSeller by lazy { binding.phoneNumberSeller }

    // Buyer data
    val companyNameBuyer by lazy { binding.companyNameBuyer }
    val addressBuyer by lazy { binding.addressBuyer }
    val emailBuyer by lazy { binding.emailBuyer }
    val phoneNumberBuyer by lazy { binding.phoneNumberBuyer }

    // Date
    val sellDate by lazy { binding.sellDate }
    val issueDate by lazy { binding.issueDate }
    val targetPaymentDate by lazy { binding.paymentTargetDate }

    // Payment details
    val paymentMethod by lazy { binding.paymentMethod }
    val paymentDate by lazy { binding.paymentDate }

    // Product positions
    val productPositions by lazy {
        listOf(
            ProductPositions(
                binding.productName1,
                binding.productAmount1,
                binding.productMeasure1,
                binding.productPriceNetto1,
                binding.productVatRate1,
            ),
            ProductPositions(
                binding.productName2,
                binding.productAmount2,
                binding.productMeasure2,
                binding.productPriceNetto2,
                binding.productVatRate2
            ),
            ProductPositions(
                binding.productName3,
                binding.productAmount3,
                binding.productMeasure3,
                binding.productPriceNetto3,
                binding.productVatRate3
            ),
            ProductPositions(
                binding.productName4,
                binding.productAmount4,
                binding.productMeasure4,
                binding.productPriceNetto4,
                binding.productVatRate4
            ),
            ProductPositions(
                binding.productName5,
                binding.productAmount5,
                binding.productMeasure5,
                binding.productPriceNetto5,
                binding.productVatRate5
            )
        )
    }


    val submitButton by lazy { binding.validateButton }

    private var _binding: FragmentFormBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val galleryViewModel =
            ViewModelProvider(this).get(FormViewModel::class.java)

        _binding = FragmentFormBinding.inflate(inflater, container, false)
        val root: View = binding.root

        submitButton.setOnClickListener {
            Log.d("test-button", binding.validateButton.text.toString())
            if (validateForm()) {
                var products: Array<Product> = emptyArray()
                for (i in 0 until 3) {
                    if (productPositions[i].productName.text.toString().isNotEmpty()) {
                        products += productPositions[i].getProduct()
                    }
                }
                val invoiceData = InvoiceData(
                    Seller(
                        companyNameSeller.text.toString(),
                        addressSeller.text.toString(),
                        nipSeller.text.toString(),
                        bankAccountNumber.text.toString(),
                        phoneNumberSeller.text.toString()
                    ),
                    Buyer(
                        companyNameBuyer.text.toString(),
                        addressBuyer.text.toString(),
                        emailBuyer.text.toString(),
                        phoneNumberBuyer.text.toString()
                    ),
                    sellDate.text.toString(),
                    issueDate.text.toString(),
                    paymentMethod.text.toString(),
                    paymentDate.text.toString(),
                    targetPaymentDate.text.toString(),
                    products.toList(),
                    invoiceNumber.text.toString()
                )
                Log.d("test-data", invoiceData.toString())

                val gson = Gson()
                val json = gson.toJson(invoiceData)

                val intent = Intent(requireActivity(), InvoiceActivity::class.java)
                intent.putExtra("invoiceData", json)
                startActivity(intent)
            }
        }

        return root
    }

    private fun getProducts(): List<Map<String, String>> {
        val products = mutableListOf<Map<String, String>>()

        fun addProduct(
            name: EditText,
            amount: EditText,
            measure: EditText,
            price: EditText,
            vat: EditText
        ) {
            if (name.text.isNotBlank() && amount.text.isNotBlank() && measure.text.isNotBlank() &&
                price.text.isNotBlank() && vat.text.isNotBlank()
            ) {
                products.add(
                    mapOf(
                        "name" to name.text.toString(),
                        "amount" to amount.text.toString(),
                        "measure" to measure.text.toString(),
                        "price" to price.text.toString(),
                        "vat" to vat.text.toString()
                    )
                )
            }
        }

        addProduct(
            binding.productName1,
            binding.productAmount1,
            binding.productMeasure1,
            binding.productPriceNetto1,
            binding.productVatRate1
        )
        addProduct(
            binding.productName2,
            binding.productAmount2,
            binding.productMeasure2,
            binding.productPriceNetto2,
            binding.productVatRate2
        )
        addProduct(
            binding.productName3,
            binding.productAmount3,
            binding.productMeasure3,
            binding.productPriceNetto3,
            binding.productVatRate3
        )
        addProduct(
            binding.productName4,
            binding.productAmount4,
            binding.productMeasure4,
            binding.productPriceNetto4,
            binding.productVatRate4
        )
        addProduct(
            binding.productName5,
            binding.productAmount5,
            binding.productMeasure5,
            binding.productPriceNetto5,
            binding.productVatRate5
        )

        return products
    }

    private fun validateForm(): Boolean {
        val errors = mutableListOf<String>()
        if (companyNameSeller.text.isNullOrBlank()) errors.add("Nazwa firmy sprzedawcy jest wymagana.")
        if (addressSeller.text.isNullOrBlank()) errors.add("Adres sprzedawcy jest wymagany.")
        if (nipSeller.text.isNullOrBlank()) errors.add("NIP sprzedawcy jest wymagany.")
        if (phoneNumberSeller.text.isNullOrBlank()) errors.add("Numer telefonu sprzedawcy jest wymagany.")
        if (bankAccountNumber.text.isNullOrBlank() || bankAccountNumber.text.length != 24) errors.add(
            "Numer konta sprzedawcy jest niepoprawny."
        )

        if (companyNameBuyer.text.isNullOrBlank()) errors.add("Nazwa firmy kupującego jest wymagana.")
        if (addressBuyer.text.isNullOrBlank()) errors.add("Adres kupującego jest wymagany.")
        if (phoneNumberBuyer.text.isNullOrBlank()) errors.add("Numer telefonu kupującego jest wymagany.")
        if (emailBuyer.text.isNullOrBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(
                emailBuyer.text
            ).matches()
        ) {
            errors.add("Adres e-mail kupującego jest niepoprawny.")
        }

        if (sellDate.text.isNullOrBlank()) errors.add("Data sprzedaży jest wymagana.")
        if (issueDate.text.isNullOrBlank()) errors.add("Data wystawienia jest wymagana.")

        if (paymentMethod.text.isNullOrBlank()) errors.add("Metoda płatności jest wymagana.")
        if (paymentDate.text.isNullOrBlank()) errors.add("Data płatności jest wymagana.")

        if (errors.isNotEmpty()) {
            showErrors(errors)
            return false
        }

        return true
    }

    private fun showErrors(errors: List<String>) {
        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle("Błąd walidacji")
        builder.setMessage(errors.joinToString("\n"))
        builder.setPositiveButton("OK", null)
        builder.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class ProductPositions(
    val productName: EditText,
    val productAmount: EditText,
    val productMeasure: EditText,
    val productPriceNetto: EditText,
    val productVatRate: EditText
) {
    fun getProduct(): Product {
        return Product(
            productName.text.toString(),
            productAmount.text.toString().toFloat(),
            productMeasure.text.toString(),
            productPriceNetto.text.toString().toFloat(),
            productVatRate.text.toString().toFloat()
        )
    }
}

package com.example.invoicegenius.ui.form

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.invoicegenius.Buyer
import com.example.invoicegenius.InvoiceActivity
import com.example.invoicegenius.InvoiceData
import com.example.invoicegenius.Product
import com.example.invoicegenius.Seller
import com.example.invoicegenius.databinding.FragmentFormBinding
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.invoicegenius.R
import java.util.*

open class FormFragment : Fragment() {

    val invoiceNumber by lazy { binding.invoiceNumber }

    val companyNameSeller by lazy { binding.companyNameSeller }
    val addressSeller by lazy { binding.addressSeller }
    val nipSeller by lazy { binding.nipSeller }
    val bankAccountNumber by lazy { binding.bankAccountNumber }
    val phoneNumberSeller by lazy { binding.phoneNumberSeller }

    val companyNameBuyer by lazy { binding.companyNameBuyer }
    val addressBuyer by lazy { binding.addressBuyer }
    val emailBuyer by lazy { binding.emailBuyer }
    val phoneNumberBuyer by lazy { binding.phoneNumberBuyer }

    val sellDate by lazy { binding.sellDate }
    val issueDate by lazy { binding.issueDate }
    val targetPaymentDate by lazy { binding.paymentTargetDate }

    val paymentMethod by lazy { binding.paymentMethod }
    val paymentDate by lazy { binding.paymentDate }

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
        setupFieldListeners()

        submitButton.setOnClickListener {
            if (validateForm()) {
                var products: Array<Product> = emptyArray()
                for (i in 0 until 5) {
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

    private fun validateForm(): Boolean {
        val errors = mutableListOf<String>()
        val currentDate = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        dateFormat.isLenient = false

        val allFieldsEmpty = (
                companyNameSeller.text.isNullOrBlank() &&
                        addressSeller.text.isNullOrBlank() &&
                        nipSeller.text.isNullOrBlank() &&
                        phoneNumberSeller.text.isNullOrBlank() &&
                        bankAccountNumber.text.isNullOrBlank() &&
                        companyNameBuyer.text.isNullOrBlank() &&
                        addressBuyer.text.isNullOrBlank() &&
                        phoneNumberBuyer.text.isNullOrBlank() &&
                        emailBuyer.text.isNullOrBlank() &&
                        sellDate.text.isNullOrBlank() &&
                        targetPaymentDate.text.isNullOrBlank() &&
                        issueDate.text.isNullOrBlank() &&
                        paymentMethod.text.isNullOrBlank() &&
                        paymentDate.text.isNullOrBlank()
                )

        if (allFieldsEmpty) {
            showErrors(listOf("Proszę wypełnić przynajmniej jedno pole"))
            return false
        }

        if (companyNameSeller.text.isNullOrBlank()) {
            errors.add("Wprowadź nazwę firmy sprzedawcy.")
            setFieldError(companyNameSeller)
        }
        if (addressSeller.text.isNullOrBlank()) {
            errors.add("Wprowadź adres sprzedawcy.")
            setFieldError(addressSeller)
        }
        if (nipSeller.text.isNullOrBlank()) {
            errors.add("Wprowadź NIP sprzedawcy.")
            setFieldError(nipSeller)
        }
        if (phoneNumberSeller.text.isNullOrBlank()) {
            errors.add("Wprowadź numer telefonu sprzedawcy.")
            setFieldError(phoneNumberSeller)
        }
        if (bankAccountNumber.text.isNullOrBlank() || bankAccountNumber.text.length != 24) {
            errors.add("Wprowadź poprawny numer konta sprzedawcy (24 cyfry).")
            setFieldError(bankAccountNumber)
        }

        if (companyNameBuyer.text.isNullOrBlank()) {
            errors.add("Wprowadź nazwę firmy kupującego.")
            setFieldError(companyNameBuyer)
        }
        if (addressBuyer.text.isNullOrBlank()) {
            errors.add("Wprowadź adres kupującego.")
            setFieldError(addressBuyer)
        }
        if (phoneNumberBuyer.text.isNullOrBlank()) {
            errors.add("Wprowadź numer telefonu kupującego.")
            setFieldError(phoneNumberBuyer)
        }
        if (emailBuyer.text.isNullOrBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(
                emailBuyer.text
            ).matches()
        ) {
            errors.add("Wprowadź poprawny adres e-mail kupującego.")
            setFieldError(emailBuyer)
        }

        if (sellDate.text.isNullOrBlank()) {
            errors.add("Wprowadź datę sprzedaży.")
            setFieldError(sellDate)
        } else {
            try {
                val sellDateFormatted = dateFormat.parse(sellDate.text.toString())
                if (sellDateFormatted.after(currentDate)) errors.add("Data sprzedaży nie może być w przyszłości.")
            } catch (e: Exception) {
                errors.add("Wprowadź datę sprzedaży w formacie DD/MM/RRRR.")
                setFieldError(sellDate)
            }
        }

        if (issueDate.text.isNullOrBlank()) {
            errors.add("Wprowadź datę wystawienia faktury.")
            setFieldError(issueDate)
        } else {
            try {
                val issueDateFormatted = dateFormat.parse(issueDate.text.toString())
                if (issueDateFormatted.after(currentDate)) errors.add("Data wystawienia nie może być w przyszłości.")
            } catch (e: Exception) {
                errors.add("Wprowadź datę wystawienia faktury w formacie DD/MM/RRRR.")
                setFieldError(issueDate)
            }
        }

        if (targetPaymentDate.text.isNullOrBlank()) {
            errors.add("Wprowadź docelowy termin płatności.")
            setFieldError(targetPaymentDate)
        } else {
            try {
                val targetPaymentDateFormatted = dateFormat.parse(targetPaymentDate.text.toString())
            } catch (e: Exception) {
                errors.add("Wprowadź docelowy termin płatności w formacie DD/MM/RRRR.")
                setFieldError(targetPaymentDate)
            }
        }

        if (paymentMethod.text.isNullOrBlank()) {
            errors.add("Wprowadź metodę płatności.")
            setFieldError(paymentMethod)
        }
        if (paymentDate.text.isNullOrBlank()) {
            errors.add("Wprowadź datę płatności.")
            setFieldError(paymentDate)
        } else {
            try {
                val paymentDateFormatted = dateFormat.parse(paymentDate.text.toString())
                if (paymentDateFormatted.after(currentDate)) errors.add("Data płatności nie może być w przyszłości.")
            } catch (e: Exception) {
                errors.add("Wprowadź datę płatności w formacie DD/MM/RRRR.")
                setFieldError(paymentDate)
            }
        }

        if (errors.isNotEmpty()) {
            showErrors(errors)
            return false
        }
        return true
    }

    private fun showErrors(errors: List<String>) {
        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle("Błąd walidacji")

        val errorMessage = errors.joinToString("\n")

        builder.setMessage(errorMessage)
        builder.setPositiveButton("OK", null)
        val dialog = builder.show()

        val messageTextView = dialog.findViewById<TextView>(android.R.id.message)
        messageTextView?.setTextColor(Color.RED)
    }

    private fun setFieldError(field: EditText) {
        val errorBorder =
            ContextCompat.getDrawable(requireContext(), R.drawable.edittext_border_error)
        field.background = errorBorder
    }

    private fun resetFieldError(field: EditText) {
        field.background = ContextCompat.getDrawable(requireContext(), R.drawable.edittext_selector)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupFieldListeners() {
        val fields = listOf(
            invoiceNumber,
            companyNameSeller,
            addressSeller,
            nipSeller,
            bankAccountNumber,
            phoneNumberSeller,
            companyNameBuyer,
            addressBuyer,
            phoneNumberBuyer,
            emailBuyer,
            sellDate,
            issueDate,
            targetPaymentDate,
            paymentMethod,
            paymentDate
        )

        fields.forEach { field ->
            field.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    resetFieldError(field)
                }

                override fun afterTextChanged(s: Editable?) {}
            })
        }
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


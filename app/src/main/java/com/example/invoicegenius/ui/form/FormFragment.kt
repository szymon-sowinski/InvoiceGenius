package com.example.invoicegenius.ui.form

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.invoicegenius.R
import com.example.invoicegenius.databinding.FragmentFormBinding

class FormFragment : Fragment() {

    //basic info
    val invoiceNumber by lazy { binding.invoiceNumber }

    //seller data
    val companyNameSeller by lazy { binding.companyNameSeller } //required
    val addressSeller by lazy { binding.addressSeller } //required
    val nipSeller by lazy { binding.nipSeller } //required
    val regonSeller by lazy { binding.regon } //required
    val phoneNumberSeller by lazy { binding.phoneNumberSeller } //required
    val emailSeller by lazy { binding.emailSeller } //required

    //buyer data
    val companyNameBuyer by lazy { binding.companyNameBuyer } //required
    val addressBuyer by lazy { binding.addressBuyer } //required
    val nipBuyer by lazy { binding.nipBuyer }
    val phoneNumberBuyer by lazy { binding.phoneNumberBuyer } //required
    val emailBuyer by lazy { binding.emailBuyer } //required

    //date
    val sellDate by lazy { binding.sellDate } //required
    val issueDate by lazy { binding.issueDate } //required

    //payment details
    val paymentMethod by lazy { binding.paymentMethod } //required
    val paymentDate by lazy { binding.paymentDate } //required

    //productPosition1
    val productPosition1 by lazy { binding.productPosition1 }
    val productAmount1 by lazy { binding.productAmount1 }
    val productMeasure1 by lazy { binding.productMeasure1 }
    val productPriceNetto1 by lazy { binding.productPriceNetto1 }
    val productVatRate1 by lazy { binding.productVatRate1 }

    //productPosition2
    val productPosition2 by lazy { binding.productPosition2 }
    val productAmount2 by lazy { binding.productAmount2 }
    val productMeasure2 by lazy { binding.productMeasure2 }
    val productPriceNetto2 by lazy { binding.productPriceNetto2 }
    val productVatRate2 by lazy { binding.productVatRate2 }

    //productPosition3
    val productPosition3 by lazy { binding.productPosition3 }
    val productAmount3 by lazy { binding.productAmount3 }
    val productMeasure3 by lazy { binding.productMeasure3 }
    val productPriceNetto3 by lazy { binding.productPriceNetto3 }
    val productVatRate3 by lazy { binding.productVatRate3 }

    //productPosition4
    val productPosition4 by lazy { binding.productPosition4 }
    val productAmount4 by lazy { binding.productAmount4 }
    val productMeasure4 by lazy { binding.productMeasure4 }
    val productPriceNetto4 by lazy { binding.productPriceNetto4 }
    val productVatRate4 by lazy { binding.productVatRate4 }

    //productPosition5
    val productPosition5 by lazy { binding.productPosition5 }
    val productAmount5 by lazy { binding.productAmount5 }
    val productMeasure5 by lazy { binding.productMeasure5 }
    val productPriceNetto5 by lazy { binding.productPriceNetto5 }
    val productVatRate5 by lazy { binding.productVatRate5 }

    val submitButton by lazy { binding.validateButton }

    private var _binding: FragmentFormBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val galleryViewModel =
            ViewModelProvider(this).get(FormViewModel::class.java)

        _binding = FragmentFormBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.validateButton.setOnClickListener {
            Log.d("test-button", binding.validateButton.text.toString())
        }


        return root
    }

   private fun validateForm(): Boolean {
       val errors = mutableListOf<String>()
       
       if (companyNameSeller.text.isNullOrBlank()) errors.add("Nazwa firmy sprzedawcy jest wymagana.")
       if (addressSeller.text.isNullOrBlank()) errors.add("Adres sprzedawcy jest wymagany.")
       if (nipSeller.text.isNullOrBlank()) errors.add("NIP sprzedawcy jest wymagany.")
       if (regonSeller.text.isNullOrBlank()) errors.add("REGON sprzedawcy jest wymagany.")
       if (phoneNumberSeller.text.isNullOrBlank()) errors.add("Numer telefonu sprzedawcy jest wymagany.")
       if (emailSeller.text.isNullOrBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(
               emailSeller.text
           ).matches()
       ) {
           errors.add("Adres e-mail sprzedawcy jest niepoprawny.")
       }


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
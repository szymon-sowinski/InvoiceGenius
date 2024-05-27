package com.example.invoicegenius

import com.google.gson.Gson

data class InvoiceData(
    val seller: Seller,
    val buyer: Buyer,
    val sellDate: String,
    val issueDate: String,
    val paymentMethod: String,
    val paymentDate: String,
    val paymentTargetDate: String,
    val products: List<Product>,
    val invoiceNumber: String
)

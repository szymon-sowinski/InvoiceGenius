package com.example.invoicegenius

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

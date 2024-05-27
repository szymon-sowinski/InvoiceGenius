package com.example.invoicegenius

data class Product(
    val productName: String,
    val productAmount: Float,
    val productMeasure: String,
    val productPriceNetto: Float,
    val vatRate: Float
)
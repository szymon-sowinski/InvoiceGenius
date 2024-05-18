package com.example.invoicegenius

import android.os.Bundle
import android.view.Menu
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.invoicegenius.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    //basic info
    private val invoiceNumber by lazy {findViewById<EditText>(R.id.invoiceNumber)}

    //seller data
    private val companyNameSeller by lazy {findViewById<EditText>(R.id.companyNameSeller)}
    private val addressSeller by lazy {findViewById<EditText>(R.id.addressSeller)}
    private val nipSeller by lazy {findViewById<EditText>(R.id.nipSeller)}
    private val regonSeller by lazy {findViewById<EditText>(R.id.regon)}
    private val phoneNumberSeller by lazy {findViewById<EditText>(R.id.phoneNumberSeller)}
    private val emailSeller by lazy {findViewById<EditText>(R.id.emailSeller)}

    //buyer data
    private val companyNameBuyer by lazy {findViewById<EditText>(R.id.companyNameBuyer)}
    private val addressBuyer by lazy {findViewById<EditText>(R.id.addressBuyer)}
    private val nipBuyer by lazy {findViewById<EditText>(R.id.nipBuyer)}
    private val phoneNumberBuyer by lazy {findViewById<EditText>(R.id.phoneNumberBuyer)}
    private val emailBuyer by lazy {findViewById<EditText>(R.id.emailBuyer)}

    //date
    private val sellDate by lazy {findViewById<EditText>(R.id.sellDate)}
    private val issueDate by lazy {findViewById<EditText>(R.id.issueDate)}

    //payment details
    private val paymentMethod by lazy {findViewById<EditText>(R.id.paymentMethod)}
    private val paymentDate by lazy {findViewById<EditText>(R.id.paymentDate)}

    //productPosition1
    private val productPosition1 by lazy {findViewById<EditText>(R.id.productPosition1)}
    private val productAmount1 by lazy {findViewById<EditText>(R.id.productAmount1)}
    private val productMeasure1 by lazy {findViewById<EditText>(R.id.productMeasure1)}
    private val productPriceNetto1 by lazy {findViewById<EditText>(R.id.productPriceNetto1)}
    private val productVatRate1 by lazy {findViewById<EditText>(R.id.productVatRate1)}

    //productPosition2
    private val productPosition2 by lazy { findViewById<EditText>(R.id.productPosition2) }
    private val productAmount2 by lazy { findViewById<EditText>(R.id.productAmount2) }
    private val productMeasure2 by lazy { findViewById<EditText>(R.id.productMeasure2) }
    private val productPriceNetto2 by lazy { findViewById<EditText>(R.id.productPriceNetto2) }
    private val productVatRate2 by lazy { findViewById<EditText>(R.id.productVatRate2) }

    //productPosition3
    private val productPosition3 by lazy { findViewById<EditText>(R.id.productPosition3) }
    private val productAmount3 by lazy { findViewById<EditText>(R.id.productAmount3) }
    private val productMeasure3 by lazy { findViewById<EditText>(R.id.productMeasure3) }
    private val productPriceNetto3 by lazy { findViewById<EditText>(R.id.productPriceNetto3) }
    private val productVatRate3 by lazy { findViewById<EditText>(R.id.productVatRate3) }

    //productPosition4
    private val productPosition4 by lazy { findViewById<EditText>(R.id.productPosition4) }
    private val productAmount4 by lazy { findViewById<EditText>(R.id.productAmount4) }
    private val productMeasure4 by lazy { findViewById<EditText>(R.id.productMeasure4) }
    private val productPriceNetto4 by lazy { findViewById<EditText>(R.id.productPriceNetto4) }
    private val productVatRate4 by lazy { findViewById<EditText>(R.id.productVatRate4) }

    //productPosition5
    private val productPosition5 by lazy { findViewById<EditText>(R.id.productPosition5) }
    private val productAmount5 by lazy { findViewById<EditText>(R.id.productAmount5) }
    private val productMeasure5 by lazy { findViewById<EditText>(R.id.productMeasure5) }
    private val productPriceNetto5 by lazy { findViewById<EditText>(R.id.productPriceNetto5) }
    private val productVatRate5 by lazy { findViewById<EditText>(R.id.productVatRate5) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
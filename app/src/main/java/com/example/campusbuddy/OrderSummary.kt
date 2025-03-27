package com.example.campusbuddy

import Order
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.campusbuddy.Models.Address
import com.example.campusbuddy.Models.Post
import com.example.campusbuddy.Models.User
import com.example.campusbuddy.databinding.ActivityOrderSummaryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import java.util.UUID

class OrderSummaryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderSummaryBinding
    private lateinit var post: Post
    private lateinit var seller: User
    private lateinit var currentUser: User
    private var selectedAddress: Address? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderSummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Order Summary"

        // Safely get data from intent
        post = intent.getSerializableExtra("POST") as? Post ?: run {
            Toast.makeText(this, "Product details not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        seller = intent.getSerializableExtra("SELLER") as? User ?: run {
            Toast.makeText(this, "Seller details not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        currentUser = intent.getSerializableExtra("USER") as? User ?: run {
            Toast.makeText(this, "User details not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupUI()
        setupClickListeners()
    }

    private fun setupUI() {
        // 1. Product details
        binding.textProductName.text = post.productName ?: "N/A"
        binding.textProductCategory.text = "Category: ${post.productCategory ?: "N/A"}"
        binding.textProductQuantity.text = "Quantity: 1"
        binding.textProductDescription.text = post.productDescription ?: "No description available"
        binding.textProductPrice.text = "₹${post.productPrice ?: "0"}"

        // Load product image (if available)
        post.imageUrl?.let { imageUrl ->
            Picasso.get()
                .load(imageUrl)
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(binding.imageProduct)
        }

        // 2. Address details
        selectedAddress = currentUser.addresses.firstOrNull() ?: run {
            Address(
                fullName = intent.getStringExtra("FULL_NAME") ?: "Not Provided",
                mobileNumber = intent.getStringExtra("PHONE_NUMBER") ?: "Not Provided",
                houseNo = intent.getStringExtra("FULL_ADDRESS") ?: "Not Provided",
                city = intent.getStringExtra("CITY") ?: "Not Provided",
                state = intent.getStringExtra("STATE") ?: "Not Provided",
                pincode = intent.getStringExtra("PINCODE") ?: "Not Provided",
                isDefault = true
            )
        }

        updateAddressUI()

        // 3. Seller details
        binding.textFarmerName.text = seller.name ?: "Seller Name Not Available"
        binding.textFarmerLocation.text = seller.location ?: "Location Not Specified"

        // 4. Price details
        val productPrice = post.productPrice?.toDoubleOrNull() ?: 0.0
        binding.textItemPrice.text = "₹${"%.2f".format(productPrice)}"
        binding.textDeliveryFee.text = "₹40.00"
        calculateTotal()
    }

    private fun updateAddressUI() {
        selectedAddress?.let { address ->
            binding.textCustomerName.text = address.fullName
            binding.textCustomerAddress.text = """
                ${address.houseNo ?: ""}
                ${address.city ?: ""}, ${address.state ?: ""} - ${address.pincode ?: ""}
            """.trimIndent()
            binding.textCustomerPhone.text = "Phone: ${address.mobileNumber ?: "Not Provided"}"
        } ?: run {
            binding.textCustomerName.text = "No Address Selected"
            binding.textCustomerAddress.text = "Please select a delivery address"
            binding.textCustomerPhone.text = ""
        }
    }

    private fun calculateTotal() {
        val productPrice = post.productPrice?.toDoubleOrNull() ?: 0.0
        val deliveryFee = 40.00
        val totalAmount = productPrice + deliveryFee
        binding.textTotalAmount.text = "₹${"%.2f".format(totalAmount)}"
    }

    private fun setupClickListeners() {
        binding.textChangeAddress.setOnClickListener {
            val intent = Intent(this, Checkout_Address_Activity::class.java).apply {
                putExtra("USER", currentUser)
            }
            startActivityForResult(intent, REQUEST_SELECT_ADDRESS)
        }

        binding.buttonProceedToPayment.setOnClickListener {
            if (selectedAddress == null) {
                Toast.makeText(this, "Please select a delivery address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            showPaymentOptions()
        }
    }

    private fun showPaymentOptions() {
        val paymentMethods = arrayOf("Cash on Delivery", "Online Payment")

        AlertDialog.Builder(this)
            .setTitle("Select Payment Method")
            .setItems(paymentMethods) { _, which ->
                when (which) {
                    0 -> placeOrder("Cash on Delivery")
                    1 -> placeOrder("Online Payment")
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun placeOrder(paymentMethod: String) {
        showProgress(true)

        val order = createOrderObject(paymentMethod)

        if (paymentMethod == "Cash on Delivery") {
            saveOrderToFirestore(order)
        } else {
            initiateUPIPayment(order)
        }
    }

    private fun createOrderObject(paymentMethod: String): Order {
        return Order(
            orderId = UUID.randomUUID().toString(),
            productId = post.postId ?: "",
            productName = post.productName ?: "",
            productImage = post.imageUrl,
            price = post.productPrice?.toDoubleOrNull() ?: 0.0,
            quantity = 1,
            buyerId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
            buyerName = currentUser.name ?: "",
            sellerId = seller.userId ?: "",
            sellerName = seller.name ?: "",
            status = if (paymentMethod == "Cash on Delivery") "Pending" else "Payment Pending",
            paymentMethod = paymentMethod,
            deliveryAddress = selectedAddress ?: Address(),
            trackingNumber = null,
            orderDate = null,
            deliveryDate = null,
            farmerNotes = null,
            customerNotes = null
        )
    }

    private fun initiateUPIPayment(order: Order) {
        if (seller.upiId.isNullOrEmpty()) {
            showProgress(false)
            AlertDialog.Builder(this)
                .setTitle("Payment Not Available")
                .setMessage("This seller hasn't set up online payments. Please use Cash on Delivery.")
                .setPositiveButton("OK") { _, _ -> }
                .show()
            return
        }

        val uri = Uri.parse("upi://pay").buildUpon()
            .appendQueryParameter("pa", seller.upiId)
            .appendQueryParameter("pn", seller.name ?: "Seller")
            .appendQueryParameter("am", order.price.toString())
            .appendQueryParameter("cu", "INR")
            .appendQueryParameter("tn", "Payment for ${order.productName}")
            .build()

        try {
            startActivityForResult(
                Intent(Intent.ACTION_VIEW).setData(uri),
                UPI_PAYMENT_REQUEST
            )
        } catch (e: ActivityNotFoundException) {
            showProgress(false)
            Toast.makeText(this, "No UPI app found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveOrderToFirestore(order: Order) {
        Firebase.firestore.collection("orders")
            .add(order)
            .addOnSuccessListener { documentReference ->
                showOrderConfirmation(order)
                updateSellerStats(order.price)
                updateBuyerHistory(documentReference.id)
            }
            .addOnFailureListener { e ->
                showProgress(false)
                Toast.makeText(this, "Failed to place order: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_SELECT_ADDRESS -> {
                if (resultCode == RESULT_OK) {
                    data?.getSerializableExtra("SELECTED_ADDRESS")?.let {
                        selectedAddress = it as Address
                        updateAddressUI()
                    }
                }
            }
            UPI_PAYMENT_REQUEST -> {
                if (resultCode == RESULT_OK) {
                    val order = createOrderObject("Online Payment")
                    saveOrderToFirestore(order)
                } else {
                    showProgress(false)
                    Toast.makeText(this, "Payment cancelled or failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun updateSellerStats(orderAmount: Double) {
        Firebase.firestore.collection("users")
            .document(seller.userId ?: "")
            .update(
                "productsUploaded", FieldValue.increment(1),
                "totalSales", FieldValue.increment(orderAmount)
            )
    }

    private fun updateBuyerHistory(orderId: String) {
        Firebase.firestore.collection("users")
            .document(FirebaseAuth.getInstance().currentUser?.uid ?: "")
            .collection("orders")
            .document(orderId)
            .set(mapOf(
                "orderId" to orderId,
                "timestamp" to FieldValue.serverTimestamp()
            ))
    }

    private fun showOrderConfirmation(order: Order) {
        showProgress(false)
        AlertDialog.Builder(this)
            .setTitle("✓ Order Confirmed")
            .setMessage("""
                Your order has been placed!
                
                Order ID: ${order.orderId}
                Total: ${binding.textTotalAmount.text}
                Payment: ${order.paymentMethod}
                
                Seller will contact you soon.
            """.trimIndent())
            .setPositiveButton("Done") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }

    private fun showProgress(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.buttonProceedToPayment.isEnabled = !show
        binding.textChangeAddress.isEnabled = !show
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    companion object {
        const val REQUEST_SELECT_ADDRESS = 1001
        const val UPI_PAYMENT_REQUEST = 1002
    }
}


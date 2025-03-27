import androidx.annotation.Keep
import com.example.campusbuddy.Models.Address
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID

@Keep
data class Order(
    var orderId: String = UUID.randomUUID().toString(),
    var productId: String = "",
    var productName: String = "",
    var productImage: String? = null,
    var price: Double = 0.0,
    var quantity: Int = 1,
    var buyerId: String = "",
    var buyerName: String = "",
    var sellerId: String = "",
    var sellerName: String = "",
    var status: String = "Pending", // Pending, Shipped, Delivered, Cancelled, Returned
    var paymentMethod: String = "Cash on Delivery", // COD, Online, Wallet, UPI, etc.
    var deliveryAddress: Address = Address(),
    var trackingNumber: String? = null,

    @ServerTimestamp
    var orderDate: Timestamp? = null,
    @ServerTimestamp
    var deliveryDate: Timestamp? = null,

    // Additional fields
    var farmerNotes: String? = null,
    var customerNotes: String? = null,
    var cancellationReason: String? = null
) : Serializable {

    // Helper function to safely get order date
    fun getFormattedOrderDate(): String {
        return orderDate?.toDate()?.let {
            SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault()).format(it)
        } ?: "Date not available"
    }

    // Helper function to safely get delivery date
    fun getFormattedDeliveryDate(): String {
        return deliveryDate?.toDate()?.let {
            SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(it)
        } ?: "Not delivered yet"
    }
}
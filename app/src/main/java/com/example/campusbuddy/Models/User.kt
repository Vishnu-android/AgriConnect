
data class User(
    var userId: String? = null,
    var name: String? = null,
    var email: String? = null,
    var password: String? = null,
    var location: String? = null,
    var contact: String? = null,
    var role: String? = null,

    // Seller-specific fields (nullable and optional)
    var productsUploaded: Int? = null,
    var totalSales: Double? = null,
    var ratings: Double? = null,
    var upiId: String? = null,
    var bankAccountNumber: String? = null
) {
    // Default constructor (required for Firestore)
    constructor() : this(null, null, null, null, null, null, null, null, null, null, null)

    // Constructor for basic user registration
    constructor(userId: String?,name: String?, email: String?,contact: String?, password: String?, role: String?) : this(
        userId, name, email, password, null, contact, role, null, null, null, null, null
    )

    // Constructor for login (email and password only)
    constructor(email: String?, password: String?) : this(
        null, null, email, password, null, null, null, null, null, null, null
    )
}
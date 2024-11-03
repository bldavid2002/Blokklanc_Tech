data class Transaction(
    val sender: String,
    val recipient: String,
    val amount: Int,
    var signature: String = ""
)

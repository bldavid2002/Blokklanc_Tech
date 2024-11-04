data class Block(
    val index: Int,
    val previousHash: String,
    val timeStamp: Long,
    val merkelRoot: String,
    val nonce: Int,
    val data: List<Transaction?>,
    val hash: String
)

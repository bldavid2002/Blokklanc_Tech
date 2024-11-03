import java.security.*
import java.time.Instant
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import java.util.Base64


class Functions {
    fun generateKeyPair(): Pair<PublicKey, PrivateKey>{
        val keyGen = KeyPairGenerator.getInstance("RSA")
        keyGen.initialize(2048)
        val keyPair = keyGen.generateKeyPair()
        return Pair(keyPair.public, keyPair.private)
    }

    fun signData(data: String, privateKey: PrivateKey): String{
        val  signature = Signature.getInstance("SHA256withRSA")
        signature.initSign(privateKey)
        signature.update(data.toByteArray())
        val signedData = signature.sign()
        return Base64.getEncoder().encodeToString(signedData)
    }

    fun verifySignature(data: String, publicKey: PublicKey, signatureStr: String): Boolean {
        val signature = Signature.getInstance("SHA256withRSA")
        signature.initVerify(publicKey)
        signature.update(data.toByteArray())
        val signatureBytes = Base64.getDecoder().decode(signatureStr)
        return signature.verify(signatureBytes)
    }

    fun signTransaction(transaction: Transaction, privateKey: PrivateKey): Transaction {
        val dataToSign = "${transaction.sender}->${transaction.recipient}: ${transaction.amount} BTC"
        transaction.signature = signData(dataToSign, privateKey)
        return transaction
    }

    fun verifyTransaction(transaction: Transaction, publicKey: PublicKey): Boolean {
        val dataToVerify = "${transaction.sender}->${transaction.recipient}: ${transaction.amount} BTC"
        return verifySignature(dataToVerify, publicKey, transaction.signature)
    }


    fun sha256(input: String): String{
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString (""){"%02x".format(it)}
    }

    fun calculateHash(index: Int, previousHash: String, timestamp: Long, merkleRoot: String, nonce: Int, data: List<String>): String{
        val input = "$index$previousHash$timestamp$merkleRoot$nonce${data.joinToString()}"
        return sha256(input)
    }

    fun createGenesisBlock(): Block{
        val genesisTransaction = Transaction("Genesis", "Genesis", 0, "GENESIS_SIGNATURE")
        val transactions = listOf(genesisTransaction)

        val timestamp = Instant.now().epochSecond
        val transactionData = transactions.map { "${it.sender}->${it.recipient}: ${it.amount} BTC" }
        val merkleRoot = buildMerkelTree(transactionData).lastOrNull() ?: ""
        val hash = calculateHash(0, "0", timestamp, merkleRoot, 0, transactionData)

        
        return Block(0, "0", timestamp, merkleRoot, 0, transactions, hash)
    }

    fun buildMerkelTree(transactions: List<String>): List<String>{
        if (transactions.isEmpty()) return  listOf()
        if (transactions.size == 1) return listOf(sha256(transactions[0]))

        val newTransactions = mutableListOf<String>()
        for (i in transactions.indices step(2)) {
            val combined = if (i + 1 < transactions.size){
                sha256(transactions[i]) + sha256(transactions[i+1])
            } else{
                sha256(transactions[i])
            }
            newTransactions.add(sha256(combined))
        }
        return  buildMerkelTree(newTransactions)
    }

    fun createNewBlock(previousBlock: Block, transactions: List<Transaction>): Block {
        val index = previousBlock.index + 1
        val timestamp = Instant.now().epochSecond
        val transactionData = transactions.map { "${it.sender}->${it.recipient}: ${it.amount} BTC" }
        val merkleRoot = buildMerkelTree(transactionData).lastOrNull() ?: ""
        var nonce = 0
        var hash: String

        do {
            nonce++
            hash = calculateHash(index, previousBlock.hash, timestamp, merkleRoot, nonce, transactionData)
        } while (!hash.startsWith("0000"))

        return Block(index, previousBlock.hash, timestamp, merkleRoot, nonce, transactions, hash)
    }
}
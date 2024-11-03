import kotlin.random.Random

class Main {
    fun run() {
        val function = Functions()
        val blockchain = mutableListOf<Block>()


        val aliceKeys = function.generateKeyPair()
        val bobKeys = function.generateKeyPair()



        blockchain.add(function.createGenesisBlock())


        val numberOfBlocksToAdd = 3
        var previousBlock = blockchain[0]

        for (i in 1..numberOfBlocksToAdd) {

            val transaction = Transaction("Alice", "Bob", Random.nextInt(1, 10))
            function.signTransaction(transaction, aliceKeys.second)


            if (function.verifyTransaction(transaction, aliceKeys.first)) {
                val newBlock = function.createNewBlock(previousBlock, listOf(transaction))
                blockchain.add(newBlock)
                previousBlock = newBlock
            } else {
                println("Transaction #$i failed verification and was not added to the blockchain.")
            }
        }
        println("Blockchain verification:")

        blockchain.forEach { block ->
            println("Block #${block.index}")
            println("Hash: ${block.hash}")
            println("Previous Hash: ${block.previousHash}")
            println("Merkle Root: ${block.merkelRoot}")
            println("Time Stamp: ${block.timeStamp}")
            println("Nonce: ${block.nonce}")
            println("Transactions: ${block.data.joinToString()}\n")
        }

    }
}
    fun main(){
        val app = Main()
        app.run()
    }

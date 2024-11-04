import kotlin.random.Random

class Main {
    fun run() {
        val function = Functions()
        val blockchain = mutableListOf<Block>()


        val aliceKeys = function.generateKeyPair()
        val bobKeys = function.generateKeyPair()




        blockchain.add(function.createGenesisBlock())


        val numberOfBlocksToAdd = 3
        val numberOfTransaction = 3
        var previousBlock = blockchain[0]


        for (i in 1..numberOfBlocksToAdd) {
            val transactions = mutableListOf<Transaction>()
           for (j in 1..numberOfTransaction){
                val transaction = Transaction("Alice", "Bob", Random.nextInt(1, 10))
               transactions.add(transaction)
           }

            function.signTransaction(transactions, aliceKeys.second)


            if (function.verifyTransaction(transactions, aliceKeys.first)) {
                val newBlock = function.createNewBlock(previousBlock, transactions)
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
            block.data.forEach { tx ->
                println("Transaction: From ${tx?.sender} To ${tx?.recipient} Amount ${tx?.amount} Signiture ${tx?.signature}")
            }
            println()

        }

    }
}
    fun main(){
        val app = Main()
        app.run()
    }

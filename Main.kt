import java.math.BigInteger
import java.security.MessageDigest
import java.security.SecureRandom

// Cryptographically secure random number generator
val secureRandom = SecureRandom()

// Generate a large random prime
fun generateLargePrime(bitLength: Int): BigInteger {
    return BigInteger.probablePrime(bitLength, secureRandom)
}

// Prover's commitment: generates t and r
data class ProverResult(val t: BigInteger, val r: BigInteger)

fun proverCommitment(n: BigInteger): ProverResult {
    // Step 1: Prover chooses a random 'r' (1 <= r < n)
    val r = BigInteger(n.bitLength(), secureRandom).mod(n)

    // Step 2: Compute commitment t = r^2 % n
    val t = r.modPow(BigInteger.TWO, n)

    // Return 't' (commitment) and 'r' (witness)
    return ProverResult(t, r)
}

// Verifier's verification: Check if prover's response is valid
fun verifierVerification(s: BigInteger, t: BigInteger, ySquared: BigInteger, c: Int, n: BigInteger): Boolean {
    // Compute left-hand side: s^2 % n
    val left = s.modPow(BigInteger.TWO, n)

    // Compute right-hand side: (t * y^c) % n
    val yPowerC = if (c == 0) BigInteger.ONE else ySquared
    val right = (t * yPowerC).mod(n)

    // Verify if both sides match
    return left == right
}

// Generate challenge 'c' using a hash of the commitment 't', 'y', and 'n'
fun hashCommitment(t: BigInteger, y: BigInteger, n: BigInteger): Int {
    val hash = MessageDigest.getInstance("SHA-256")
    val data = t.toString() + y.toString() + n.toString() // Combine inputs
    val digest = hash.digest(data.toByteArray()) // Compute hash
    return BigInteger(1, digest).mod(BigInteger.TWO).toInt() // Get challenge bit (0 or 1)
}

// Prover's response: calculate response based on challenge 'c'
fun proverResponse(r: BigInteger, x: BigInteger, c: Int, n: BigInteger): BigInteger {
    return if (c == 0) {
        r
    } else {
        (r * x).mod(n)
    }
}

// SETUP PHASE: Choose public parameters and secret
data class SetupResult(val n: BigInteger, val ySquared: BigInteger, val x: BigInteger)

fun setup(bitLength: Int): SetupResult {
    // Step 1: Generate two large prime numbers, p and q
    val p = generateLargePrime(bitLength)
    val q = generateLargePrime(bitLength)
    val n = p * q  // Public modulus

    // Step 2: Prover selects a secret 'x' (1 <= x < n)
    val x = BigInteger(n.bitLength(), secureRandom).mod(n)

    // Step 3: Compute public value ySquared = x^2 % n
    val ySquared = x.modPow(BigInteger.TWO, n)

    // Return public and private values
    return SetupResult(n, ySquared, x)
}

// Main protocol execution with multiple rounds
fun main() {
    val bitLength = 1024  // Bit length for large prime generation
    val rounds = 5  // Number of rounds

    // SETUP PHASE
    val setupResult = setup(bitLength)
    val n = setupResult.n  // Public modulus
    val ySquared = setupResult.ySquared  // Public value derived from secret
    val x = setupResult.x  // Prover's secret (hidden from the verifier)

    println("Setup completed: n = $n, y^2 = $ySquared, x (secret) = $x")

    // Execute multiple rounds
    var allRoundsSuccessful = true
    repeat(rounds) { round ->
        println("\n--- Round ${round + 1} ---")

        // PROVER'S STEP
        val proverResult = proverCommitment(n)  // Prover generates commitment
        val t = proverResult.t  // Commitment (sent to verifier)
        val r = proverResult.r  // Witness (kept private by prover)

        println("Prover sends commitment: t = $t")

        // VERIFIER'S STEP: Generate challenge using hash
        val c = hashCommitment(t, ySquared, n)  // Verifier computes challenge from t, y, and n
        println("Verifier generates challenge: c = $c")

        // PROVER'S RESPONSE
        val s = proverResponse(r, x, c, n)  // Prover calculates response based on challenge
        println("Prover sends response: s = $s")

        // VERIFIER'S FINAL VERIFICATION
        val verificationResult = verifierVerification(s, t, ySquared, c, n)  // Verifier checks response

        if (verificationResult) {
            println("Verification successful for this round.")
        } else {
            println("Verification failed for this round.")
            allRoundsSuccessful = false
        }
    }

    if (allRoundsSuccessful) {
        println("Verification successful across all $rounds rounds: The prover knows 'x'.")
    } else {
        println("Verification failed in one or more rounds: The prover may not know 'x'.")
    }
}

import kotlin.random.Random
import kotlin.math.pow

// Setup phase: Choose public parameters and secret
data class SetupResult(val n: Int, val ySquared: Int, val x: Int)

fun setup(): SetupResult {
    // Step 1: Choose two prime numbers, p and q (for simplicity, using small numbers here)
    val p = 11  // Public prime (in practice, these should be much larger)
    val q = 13  // Public prime
    val n = p * q  // Public modulus (used as a shared base for computations)

    // Step 2: Prover selects a secret 'x' (1 <= x < n)
    val x = Random.nextInt(1, n)

    // Step 3: Compute public value ySquared = x^2 modulo n
    val ySquared = (x * x) % n

    // Return public and private values
    return SetupResult(n, ySquared, x)
}

// Prover's step: Create a commitment
data class ProverResult(val t: Int, val r: Int)

fun prover(n: Int): ProverResult {
    // Step 1: Prover chooses a random 'r' (1 <= r < n)
    val r = Random.nextInt(1, n)

    // Step 2: Compute commitment t = r^2 modulo n
    val t = (r * r) % n

    // Return 't' (commitment) and 'r' (witness)
    return ProverResult(t, r)
}

// Verifier's step: Generate a challenge
fun verifier(): Int {
    // Step 1: Generate a random challenge bit 'c' (0 or 1)
    return Random.nextInt(0, 2)
}

// Prover's response: Calculate response to verifier's challenge
fun proverResponse(r: Int, x: Int, c: Int, n: Int): Int {
    // Step 1: Compute response 's'
    // If c == 0, s = r (verifier will check consistency of r)
    // If c == 1, s = r * x % n (prover uses their secret x)
    return (r * (x.toDouble().pow(c).toInt())) % n
}

// Verifier's verification: Check if prover's response is valid
fun verifierVerification(s: Int, t: Int, ySquared: Int, c: Int, n: Int): Boolean {
    // Step 1: Compute left-hand side of verification equation: s^2 % n
    val left = (s * s) % n

    // Step 2: Compute right-hand side of verification equation: (t * y^c) % n
    val right = (t * ySquared.toDouble().pow(c).toInt()) % n

    // Step 3: Verify if both sides match
    return left == right
}

// Main protocol execution
fun main() {
    // SETUP PHASE
    val setupResult = setup()  // Generate public and private parameters
    val n = setupResult.n  // Public modulus
    val ySquared = setupResult.ySquared  // Public value derived from secret
    val x = setupResult.x  // Prover's secret (hidden from the verifier)

    println("Setup completed: n = $n, y^2 = $ySquared, x (secret) = $x")

    // PROVER'S STEP
    val proverResult = prover(n)  // Prover generates commitment
    val t = proverResult.t  // Commitment (sent to verifier)
    val r = proverResult.r  // Witness (kept private by prover)

    println("Prover sends commitment: t = $t")

    // VERIFIER'S STEP
    val c = verifier()  // Verifier generates a challenge bit (0 or 1)
    println("Verifier generates challenge: c = $c")

    // PROVER'S RESPONSE
    val s = proverResponse(r, x, c, n)  // Prover calculates response based on challenge
    println("Prover sends response: s = $s")

    // VERIFIER'S FINAL VERIFICATION
    val verificationResult = verifierVerification(s, t, ySquared, c, n)  // Verifier checks response

    if (verificationResult) {
        println("Verification successful: The prover knows 'x'.")
    } else {
        println("Verification failed: The prover does not know 'x'.")
    }
}
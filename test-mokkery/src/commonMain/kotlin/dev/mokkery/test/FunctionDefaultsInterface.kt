package dev.mokkery.test

class IdentityPayload {

    override fun toString(): String = "IdentityPayload"
}

interface FunctionDefaultsInterface {

    fun call(i: Int, name: String = "name($i)", mail: String = "name$i@mail.com"): String

    fun callNonDeterministic(i: Int, token: Int = nextToken()): String

    fun callIdentityDefault(i: Int, payload: IdentityPayload = IdentityPayload()): String

    suspend fun callSuspend(i: Int, name: String = "name($i)", mail: String = "name$i@mail.com"): String

    fun Int.callExtension(name: String = "name($this)", mail: String = "name$this@mail.com"): String

    companion object {
        private var tokenCounter = 0

        fun nextToken(): Int = tokenCounter++

        fun resetTokens() {
            tokenCounter = 0
        }
    }
}

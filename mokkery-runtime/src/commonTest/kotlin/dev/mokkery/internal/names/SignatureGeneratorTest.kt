package dev.mokkery.internal.names

import dev.mokkery.internal.utils.bestName
import dev.mokkery.test.fakeCallArg
import kotlin.test.Test
import kotlin.test.assertEquals

class SignatureGeneratorTest {

    private val generator = SignatureGenerator()

    @Test
    fun testGeneratesSignatureCorrectlyForNoArgs() {
        assertEquals("call()", generator.generate("call", listOf()))
    }

    @Test
    fun testGeneratesSignatureCorrectlyWithArgs() {
        val args =  listOf(
            fakeCallArg(name = "i", value = 1),
            fakeCallArg(name = "j", value = setOf(""))
        )
        assertEquals(
            "call(i: ${Int::class.bestName()}, j: ${Set::class.bestName()})",
            generator.generate("call", args)
        )
    }
}

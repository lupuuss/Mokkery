package dev.mokkery.internal.serialization;

import dev.mokkery.verify.VerifyMode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class VerifyModeSerializerTest {

    private val serializer = VerifyModeSerializer

    @Test
    fun testSerializeNot() {
        assertEquals(
            expected = "not",
            actual = serializer.serialize(VerifyMode.not)
        )
    }

    @Test
    fun testSerializeOrder() {
        assertEquals(
            expected = "order",
            actual = serializer.serialize(VerifyMode.order)
        )
    }

    @Test
    fun testSerializeExhaustive() {
        assertEquals(
            expected = "exhaustive",
            actual = serializer.serialize(VerifyMode.exhaustive)
        )
    }

    @Test
    fun testSerializeExhaustiveOrder() {
        assertEquals(
            expected = "exhaustiveOrder",
            actual = serializer.serialize(VerifyMode.exhaustiveOrder)
        )
    }

    @Test
    fun testSerializeSoft() {
        assertEquals(
            expected = "soft",
            actual = serializer.serialize(VerifyMode.soft)
        )
    }

    @Test
    fun testSerializeExactly() {
        assertEquals(
            expected = "exactly(4)",
            actual = serializer.serialize(VerifyMode.exactly(4))
        )
    }

    @Test
    fun testSerializeInRange() {
        assertEquals(
            expected = "inRange(2..10)",
            actual = serializer.serialize(VerifyMode.inRange(2..10))
        )
    }

    @Test
    fun testSerializeAtMost() {
        assertEquals(
            expected = "atMost(4)",
            actual = serializer.serialize(VerifyMode.atMost(4))
        )
    }

    @Test
    fun testSerializeAtLeast() {
        assertEquals(
            expected = "atLeast(4)",
            actual = serializer.serialize(VerifyMode.atLeast(4))
        )
    }


    @Test
    fun testDeserializeNot() {
        assertEquals(
            expected = VerifyMode.not,
            actual = serializer.deserialize("not")
        )
    }

    @Test
    fun testDeserializeOrder() {
        assertEquals(
            expected = VerifyMode.order,
            actual = serializer.deserialize("order")
        )
    }

    @Test
    fun testDeserializeExhaustive() {
        assertEquals(
            expected = VerifyMode.exhaustive,
            actual = serializer.deserialize("exhaustive")
        )
    }

    @Test
    fun testDeserializeExhaustiveOrder() {
        assertEquals(
            expected = VerifyMode.exhaustiveOrder,
            actual = serializer.deserialize("exhaustiveOrder")
        )
    }

    @Test
    fun testDeserializeSoft() {
        assertEquals(
            expected = VerifyMode.soft,
            actual = serializer.deserialize("soft")
        )
    }

    @Test
    fun testDeserializeExactly() {
        assertEquals(
            expected = VerifyMode.exactly(4),
            actual = serializer.deserialize("exactly(4)")
        )
    }

    @Test
    fun testDeserializeInRange() {
        assertEquals(
            expected = VerifyMode.inRange(2..10),
            actual = serializer.deserialize("inRange(2..10)")
        )
    }

    @Test
    fun testDeserializeAtMost() {
        assertEquals(
            expected = VerifyMode.atMost(4),
            actual = serializer.deserialize("atMost(4)")
        )
    }

    @Test
    fun testDeserializeAtLeast() {
        assertEquals(
            expected = VerifyMode.atLeast(4),
            actual = serializer.deserialize("atLeast(4)")
        )
    }

    @Test
    fun testFailsWhileDeserializingEmptyString() {
        assertFails {
            serializer.deserialize("")
        }
    }
}

package dev.mokkery.test

import dev.mokkery.context.MokkeryContext
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

fun MokkeryContext.assertContainsExactly(vararg elements: MokkeryContext.Element) {
    elements.forEach {
        val expected = it
        val actual = this[it.key]
        assertEquals(expected, actual, "Expected to contain element $expected for key ${it.key} but found element $actual")
    }
    val actualElements = mutableListOf<MokkeryContext.Element>()
    this.fold(Unit) { _, it -> actualElements.add(it) }
    assertContentEquals(elements.toList(), actualElements, "Expected context to contains only these elements: ${elements.toList()} but found $actualElements")
}

@file:Suppress("unused")

package dev.mokkery.internal

internal fun generateMockId(typeName: String) = MockUniqueReceiversGenerator.generate(typeName)

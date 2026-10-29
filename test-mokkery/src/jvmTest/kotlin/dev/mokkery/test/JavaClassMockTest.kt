package dev.mokkery.test

import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import dev.mokkery.verify
import java.io.FilterOutputStream
import kotlin.test.Test

class JavaClassMockTest {

    @Test
    fun testMocksJavaClassWithOnlyParameterizedConstructors() {
        val stream = mock<FilterOutputStream>()
        every { stream.write(1) } returns Unit
        stream.write(1)
        verify { stream.write(1) }
    }
}

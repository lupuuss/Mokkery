// FILE: test/WithField.java
package test;

public abstract class WithField {
    public int field = 0;
}

// FILE: main.kt
import dev.mokkery.every
import dev.mokkery.answering.returns
import dev.mokkery.mock
import test.WithField

fun main() {
    val foo = mock<WithField>()
    every(<!FIELD_REFERENCE_NOT_ALLOWED!>foo::field<!>) returns 1
    every(<!FIELD_REFERENCE_NOT_ALLOWED!>foo::field<!>) { }
}

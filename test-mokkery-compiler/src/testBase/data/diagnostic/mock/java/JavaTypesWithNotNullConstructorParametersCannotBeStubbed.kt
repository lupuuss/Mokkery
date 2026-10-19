// FILE: org/jetbrains/annotations/NotNull.java
package org.jetbrains.annotations;

public @interface NotNull {}

// FILE: test/Dependency.java
package test;

public class Dependency {
    public Dependency(String value) {}
}

// FILE: test/NotNullParameter.java
package test;

import org.jetbrains.annotations.NotNull;

public class NotNullParameter {
    public NotNullParameter(@NotNull Dependency dependency) {}
}

// FILE: main.kt
import dev.mokkery.mock
import test.NotNullParameter

fun test() {
    mock<<!NO_CONSTRUCTOR_TO_STUB!>NotNullParameter<!>>()
}

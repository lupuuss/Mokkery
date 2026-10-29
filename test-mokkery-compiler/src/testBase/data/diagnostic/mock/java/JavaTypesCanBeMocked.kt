// FILE: org/jetbrains/annotations/Nullable.java
package org.jetbrains.annotations;

public @interface Nullable {}

// FILE: test/Dependency.java
package test;

public class Dependency {
    public Dependency(String value) {}
}

// FILE: test/PlatformTypeParameter.java
package test;

public class PlatformTypeParameter {
    public PlatformTypeParameter(Dependency dependency) {}
}

// FILE: test/NullableParameter.java
package test;

import org.jetbrains.annotations.Nullable;

public class NullableParameter {
    public NullableParameter(@Nullable Dependency dependency) {}
}

// FILE: test/WithStaticMembers.java
package test;

public class WithStaticMembers {
    public static int field = 0;

    public static void method() {}
}

// FILE: main.kt
import dev.mokkery.mock
import dev.mokkery.spy
import test.NullableParameter
import test.PlatformTypeParameter
import test.WithStaticMembers
import java.io.FilterOutputStream
import java.io.OutputStream

fun test(stream: FilterOutputStream) {
    mock<OutputStream>()
    mock<FilterOutputStream>()
    spy(stream)
    mock<PlatformTypeParameter>()
    mock<NullableParameter>()
    mock<WithStaticMembers>()
}

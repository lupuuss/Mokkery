// FILE: test/WithFinalMethod.java
package test;

public class WithFinalMethod {
    public final void method() {}
}

// FILE: test/InheritsFinalMethod.java
package test;

public class InheritsFinalMethod extends WithFinalMethod {}

// FILE: test/WithFinalGetter.java
package test;

public class WithFinalGetter {
    public final String getValue() {
        return "";
    }
}

// FILE: main.kt
import dev.mokkery.mock
import test.InheritsFinalMethod
import test.WithFinalGetter
import test.WithFinalMethod

fun test() {
    mock<<!FINAL_MEMBERS_TYPE_CANNOT_BE_INTERCEPTED!>WithFinalMethod<!>>()
    mock<<!FINAL_MEMBERS_TYPE_CANNOT_BE_INTERCEPTED!>InheritsFinalMethod<!>>()
    mock<<!FINAL_MEMBERS_TYPE_CANNOT_BE_INTERCEPTED!>WithFinalGetter<!>>()
}

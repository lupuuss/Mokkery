package dev.mokkery.annotations

import dev.mokkery.mock
import dev.mokkery.test.AnnotatedInterface
import dev.mokkery.test.AnnotationA
import dev.mokkery.test.AnnotationB
import dev.mokkery.test.AnnotationC
import kotlin.test.Test
import kotlin.test.assertTrue

class AnnotationsTest {

    @Test
    fun test() {
        val mock = mock<AnnotatedInterface>()
        assertTrue {
            mock::class
                .java
                .methods
                .find { it.name == "annotatedA" }
                ?.annotations
                ?.any { it.annotationClass == AnnotationA::class } == true
        }
        assertTrue {
            mock::class
                .java
                .methods
                .find { it.name == "annotatedB" }
                ?.annotations
                ?.none { it.annotationClass == AnnotationB::class } == true
        }
        assertTrue {
            mock::class
                .java
                .methods
                .find { it.name == "annotatedC" }
                ?.annotations
                ?.none { it.annotationClass == AnnotationC::class } == true
        }
    }
}

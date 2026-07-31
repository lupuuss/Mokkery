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

    @Test
    fun testTypeAnnotations() {
        val mock = mock<AnnotatedInterface>()
        val kept = mock::class.java.methods.first { it.name == "typeAnnotatedA" }
        assertTrue { kept.annotatedReturnType.annotations.any { it.annotationClass == AnnotationA::class } }
        assertTrue { kept.annotatedParameterTypes[0].annotations.any { it.annotationClass == AnnotationA::class } }
        val filtered = mock::class.java.methods.first { it.name == "typeAnnotatedB" }
        assertTrue { filtered.annotatedReturnType.annotations.none { it.annotationClass == AnnotationB::class } }
        assertTrue { filtered.annotatedParameterTypes[0].annotations.none { it.annotationClass == AnnotationB::class } }
    }
}

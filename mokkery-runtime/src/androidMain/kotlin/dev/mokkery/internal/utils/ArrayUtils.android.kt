package dev.mokkery.internal.utils

import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
internal actual fun platformArrayOf(kClass: KClass<*>, elements: List<Any?>): Array<*> {
    val jvmClass = kClass.java
    val array = java.lang.reflect.Array.newInstance(jvmClass.componentType!!, elements.size)
    array as Array<Any?>
    elements.forEachIndexed { index, any ->
        array[index] = any
    }
    return array
}

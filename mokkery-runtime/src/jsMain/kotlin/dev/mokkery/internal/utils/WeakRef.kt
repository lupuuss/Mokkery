package dev.mokkery.internal.utils

internal external interface WeakRef<T> {

    @get:JsName("deref")
    val value: T?
}

internal fun <T> WeakRef(ref: T): WeakRef<T> = js("new WeakRef(ref)")

@Suppress("NOTHING_TO_INLINE")
internal inline fun <T> T.weaken(): WeakRef<T> = WeakRef(this)

package dev.mokkery.test

sealed interface Platform {

    interface Native : Platform
    interface Jvm : Platform
    interface Js : Platform
    interface Wasm : Platform

    companion object
}

expect val Platform.Companion.current: Platform

inline fun ignoreOnJs(reason: String, block: () -> Unit) = ignoreOn<Platform.Js>(reason = reason, block = block)

inline fun <reified T> ignoreOn(reason: String, block: () -> Unit) {
    when (Platform.current) {
        is T -> runCatching { block() }
            .onFailure {
                println("## Test failed but it was ignored on ${T::class.simpleName}! | Reason: $reason | Stack trace: ")
                it.printStackTrace()
            }
            .getOrNull()
        else -> block()
    }
}

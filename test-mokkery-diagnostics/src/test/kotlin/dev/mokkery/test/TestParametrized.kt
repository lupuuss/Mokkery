package dev.mokkery.test

inline fun <T> tests(vararg params: T, block: (T) -> Unit) {
    params.forEach {
        try {
            block(it)
        } catch (e: AssertionError) {
            throw AssertionError("Test not passed with param ${it}!", e)
        } catch (e: Exception) {
            throw AssertionError("Test failed with param ${it}!", e)
        }
    }
}

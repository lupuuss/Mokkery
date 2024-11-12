package dev.mokkery

/**
 * Determines behaviour for a mock when user-configured answer is missing.
 */
@Suppress("EnumEntryName")
public enum class MockMode {
    /**
     * Fails on missing answer.
     */
    strict,

    /**
     * Provides default *empty* value (e.g. 0 for numbers, "" for string, null for complex types).
     *
     * For generic types, defaults match the specified type argument
     * (e.g., `mock<List<String>>(autofill).get(0)` returns `""` instead of `null`).
     * Mokkery achieves this by preserving type arguments at runtime.
     *
     * This mock mode can be enhanced by registering custom providers to `AutofillProvider.forMockMode`.
     */
    autofill,

    /**
     * Returns [Unit] for functions that return [Unit], otherwise fails.
     *
     * For generic types, the default return value will be `Unit` based on the specified type argument
     * (e.g., `mock<() -> Unit>(autoUnit).invoke()` returns `Unit` instead of `null`).
     * Mokkery achieves this by preserving type arguments at runtime.
     */
    autoUnit,

    /**
     * Calls original implementation if present, otherwise fails.
     */
    original
}

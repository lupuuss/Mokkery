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
     * Provides default *empty* value (e.g. 0 for numbers, "" for string, null for complex types)
     */
    autofill,

    /**
     * Returns [Unit] for functions that return [Unit], otherwise fails.
     */
    autoUnit
}

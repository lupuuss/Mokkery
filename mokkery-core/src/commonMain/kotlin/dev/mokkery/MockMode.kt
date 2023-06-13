package dev.mokkery

public enum class MockMode {
    Autofill, Strict, AutoUnit;

    public companion object {
        public val Default: MockMode = Strict
    }
}

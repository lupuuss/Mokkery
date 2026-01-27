package dev.mokkery.gradle

import dev.mokkery.MockMode
import dev.mokkery.options.AnnotationSelector
import dev.mokkery.verify.VerifyMode
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Nested

public interface MokkeryGradleExtension {
    /**
     * Determines source sets to be affected by Mokkery.
     *
     * By default, it is [ApplicationRule.AllTests]
     */
    public val rule: Property<ApplicationRule>

    /**
     * Determines default mock mode for created mocks.
     *
     * By default, it is [MockMode.strict].
     */
    public val defaultMockMode: Property<MockMode>

    /**
     * Determines default verification mode for `verify` calls.
     *
     * By default, it is [VerifyMode.soft].
     */
    public val defaultVerifyMode: Property<VerifyMode>

    /**
     * Allows creating mocks of open/abstract classes with `inline` members.
     * Compiler plugin no longer fails with inline members. However, it is not possible to change or track
     * original behaviour of inline members.
     *
     * By default, it is disabled.
     */
    public val ignoreInlineMembers: Property<Boolean>

    /**
     * Allows creating mocks of open/abstract classes with `final` members.
     * Compiler plugin no longer fails with final members. However, it is not possible to change or track
     * original behaviour of final members.
     *
     * By default, it is disabled.
     */
    public val ignoreFinalMembers: Property<Boolean>

    /**
     * Enables Mokkery FIR level checkers. By default, it is enabled.
     *
     * Generally, it shouldn't be disabled as it detects errors in Mokkery usage, but it might
     * be used in case of a false-positives that prevents correct Mokkery code from being compiled.
     */
    public val enableFirDiagnostics: Property<Boolean>

    /**
     * Allows adjusting Mokkery stubbing mechanism.
     *
     * @see [MokkeryStubsOptions]
     */
    @get:Nested
    public val stubs: MokkeryStubsOptions

    /**
     * Allows adjusting how Mokkery handles annotations.
     */
    @get:Nested
    public val annotations: MokkeryAnnotationsOptions
}

/**
 * When mocking an abstract or open class, its constructor may require arguments.
 * Since Mokkery must invoke this constructor, all required parameters must be provided.
 *
 * Mokkery uses the following strategies to supply constructor arguments:
 * * `null` for nullable types
 * * `0` for all numeric types
 * * `false` for `Boolean`
 * * `'\u0000'` for `Char`
 * * `Any()` for `Any`
 * * A `kotlin.reflect.KClass` instance matching the required type
 *   (e.g. `Int::class` for `KClass<Int>`)
 * * The `Unit` object for `Unit`
 * * Empty arrays for all array types
 * * Empty collections for `Iterable`, `Collection`, `List`, `Set`, `Map`, and their mutable counterparts
 * * The first declared entry for enum types
 * * Lambdas that return values resolved using the same strategies
 * * Generated stub implementations for interfaces
 * * Instantiating concrete classes using available constructors, following the same rules.
 *   By default, this works only for inline classes, `Throwable` subclasses, and classes from `kotlin.collections`, `kotlin.sequences`, and `kotlin.ranges`.
 *   **For other types, explicit permission is required because invoking constructors may execute unintended code during tests.**
 *   Public and internal constructors are supported, with default constructors preferred.
 *   Enable this behavior in your Gradle build file using the `mokkery.stubs.allowConcreteClassInstantiation` flag.
 * * Generating stub implementations for classes.
 *   **This requires explicit permission for the same reason as concrete classes instantiation.**
 *   Enable this behavior in your Gradle build file using `mokkery.stubs.allowClassInheritance` flag.
 *
 * Mokkery selects the first applicable strategy based on the order listed above.
 */
public interface MokkeryStubsOptions {

    /**
     * @see [MokkeryStubsOptions]
     */
    public val allowConcreteClassInstantiation: Property<Boolean>

    /**
     * @see [MokkeryStubsOptions]
     */
    public val allowClassInheritance: Property<Boolean>
}

/**
 * Allows adjusting how Mokkery handles annotations.
 */
public interface MokkeryAnnotationsOptions {

    /**
     * Specifies the selector expression that determines which annotations
     * will be copied from the type being mocked to the generated mock.
     *
     * Examples:
     * ```kotlin
     * import dev.mokkery.options.AnnotationSelector.Companion.all
     * import dev.mokkery.options.AnnotationSelector.Companion.none
     * import dev.mokkery.options.AnnotationSelector.Companion.named
     * import dev.mokkery.options.AnnotationSelector.Companion.matches
     *
     * mokkery {
     *     annotations {
     *
     *         // No annotations
     *         copyToMock = none
     *
     *         // All annotations except "example.A"
     *         copyToMock = all - named("example.A")
     *
     *         // Only "example.A"
     *         copyToMock = named("example.A")
     *
     *         // All annotations matching the regex "internal.*"
     *         copyToMock = matches(Regex("internal.*"))
     *
     *         // Combine rules: all except "example.A" and all annotations starting with "internal"
     *         copyToMock = all - named("example.A") - matches(Regex("internal.*"))
     *     }
     * }
     * ```
     */
    public val copyToMock: Property<AnnotationSelector>
}

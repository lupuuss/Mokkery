package dev.mokkery.internal.options

import dev.mokkery.MockMode
import dev.mokkery.MokkeryCompilerDefaults
import dev.mokkery.annotations.InternalMokkeryApi
import dev.mokkery.internal.options.MokkeryOptionType.Companion.annotationSelector
import dev.mokkery.internal.options.MokkeryOptionType.Companion.boolean
import dev.mokkery.internal.options.MokkeryOptionType.Companion.mockMode
import dev.mokkery.internal.options.MokkeryOptionType.Companion.verifyMode
import dev.mokkery.options.AnnotationSelector
import dev.mokkery.options.AnnotationSelector.Companion.all
import dev.mokkery.verify.VerifyMode

@InternalMokkeryApi
public object MokkeryOptions : MokkeryOptionsContainer() {

    init {
        this += Core
        this += Stubs
        this += Annotations
    }

    @InternalMokkeryApi
    public object Core : MokkeryOptionsContainer() {

        private val root = MokkeryNamespace.root

        public val defaultVerifyMode: MokkeryOption<VerifyMode> by root.defaultSingleOption(
            type = verifyMode,
            description = "Default VerifyMode for every verify block.",
            defaultValue = MokkeryCompilerDefaults.verifyMode
        )
        public val defaultMockMode: MokkeryOption<MockMode> by root.defaultSingleOption(
            type = mockMode,
            description = "Default MockMode for every mock.",
            defaultValue = MokkeryCompilerDefaults.mockMode
        )
        public val ignoreInlineMembers: MokkeryOption<Boolean> by root.defaultSingleOption(
            type = boolean,
            description = "Ignores inline members of mocked class if raised",
            defaultValue = false,
        )
        public val ignoreFinalMembers: MokkeryOption<Boolean> by root.defaultSingleOption(
            type = boolean,
            description = "Ignores final members of mocked class if raised.",
            defaultValue = false,
        )
        public val enableFirDiagnostics: MokkeryOption<Boolean> by root.defaultSingleOption(
            type = boolean,
            description = "Enables FIR diagnostics if raised.",
            defaultValue = true,
        )
    }


    @InternalMokkeryApi
    public object Stubs : MokkeryOptionsContainer() {

        private val stubs by MokkeryNamespace.named

        public val allowClassInheritance: MokkeryOption<Boolean> by stubs.defaultSingleOption(
            type = boolean,
            description = "Allows Mokkery to inherit from existing classes when required for mocked class constructor.",
            defaultValue = false,
        )
        public val allowConcreteClassInstantiation: MokkeryOption<Boolean> by stubs.defaultSingleOption(
            type = boolean,
            description = "Allows Mokkery to instantiate existing classes required for mocked class constructor.",
            defaultValue = false,
        )
    }

    @InternalMokkeryApi
    public object Annotations : MokkeryOptionsContainer() {

        private val annotations by MokkeryNamespace.named

        public val copyToMock: MokkeryOption<AnnotationSelector> by annotations.defaultSingleOption(
            type = annotationSelector,
            description = "Describes which annotations should be copied from type to mock to mock implementation.",
            defaultValue = all
        )
    }
}

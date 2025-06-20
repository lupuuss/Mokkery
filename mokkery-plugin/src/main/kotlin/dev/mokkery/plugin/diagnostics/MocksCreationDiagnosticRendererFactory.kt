package dev.mokkery.plugin.diagnostics

import dev.mokkery.plugin.core.Mokkery.MocksCreationErrors
import dev.mokkery.plugin.diagnostics.MocksCreationChecker.Diagnostics
import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactoryToRendererMap
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory
import org.jetbrains.kotlin.diagnostics.rendering.CommonRenderers
import org.jetbrains.kotlin.diagnostics.rendering.DiagnosticParameterRenderer
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirDiagnosticRenderers
import org.jetbrains.kotlin.fir.types.ConeKotlinType

class MocksCreationDiagnosticRendererFactory : BaseDiagnosticRendererFactory() {

    private val typeRenderer: DiagnosticParameterRenderer<ConeKotlinType> = FirDiagnosticRenderers.RENDER_TYPE

    override val MAP = KtDiagnosticFactoryToRendererMap("MokkeryMocksCreationDiagnostic").apply {
        put(
            factory = Diagnostics.INDIRECT_INTERCEPTION,
            message = MocksCreationErrors.indirectCall(typeArgument = "{1}", functionName = "{0}"),
            rendererA = CommonRenderers.NAME,
            rendererB = typeRenderer
        )
        put(
            factory = Diagnostics.SEALED_TYPE_CANNOT_BE_INTERCEPTED,
            message = MocksCreationErrors.sealedTypeCannotBeIntercepted(typeName = "{1}", functionName = "{0}"),
            rendererA = CommonRenderers.NAME,
            rendererB = typeRenderer,
        )
        put(
            factory = Diagnostics.FINAL_TYPE_CANNOT_BE_INTERCEPTED,
            message = MocksCreationErrors.finalTypeCannotBeIntercepted(typeName = "{1}", functionName = "{0}"),
            rendererA = CommonRenderers.NAME,
            rendererB = typeRenderer,
        )
        put(
            factory = Diagnostics.PRIMITIVE_TYPE_CANNOT_BE_INTERCEPTED,
            message = MocksCreationErrors.primitiveTypeCannotBeIntercepted(typeName = "{1}", functionName = "{0}"),
            rendererA = CommonRenderers.NAME,
            rendererB = typeRenderer,
        )
        put(
            factory = Diagnostics.FINAL_MEMBERS_TYPE_CANNOT_BE_INTERCEPTED,
            message = MocksCreationErrors.finalMembersTypeCannotBeIntercepted(
                typeName = "{1}",
                functionName = "{0}",
                nonAbstractMembers = "{2}"
            ),
            rendererA = CommonRenderers.NAME,
            rendererB = typeRenderer,
            rendererC = CommonRenderers.commaSeparated(FirDiagnosticRenderers.SYMBOL)
        )
        put(
            factory = Diagnostics.NO_PUBLIC_CONSTRUCTOR_TYPE_CANNOT_BE_INTERCEPTED,
            message = MocksCreationErrors.noPublicConstructorTypeCannotBeIntercepted(
                typeName = "{1}",
                functionName = "{0}",
            ),
            rendererA = CommonRenderers.NAME,
            rendererB = typeRenderer,
        )
        put(
            factory = Diagnostics.MULTIPLE_SUPER_CLASSES_FOR_MOCK_MANY,
            message = MocksCreationErrors.singleSuperClass("{0}", "{1}"),
            rendererA = CommonRenderers.NAME,
            rendererB = CommonRenderers.commaSeparated(typeRenderer)
        )
        put(
            factory = Diagnostics.DUPLICATE_TYPES_FOR_MOCK_MANY,
            message = MocksCreationErrors.noDuplicatesForMockMany("{0}", "{1}", "{2}"),
            rendererA = typeRenderer,
            rendererB = CommonRenderers.NAME,
            rendererC = CommonRenderers.STRING
        )
        put(
            factory = Diagnostics.FUNCTIONAL_TYPE_ON_JS_FOR_MOCK_MANY,
            message = MocksCreationErrors.functionalTypeNotAllowedOnJs("{0}", "{1}"),
            rendererA = typeRenderer,
            rendererB = CommonRenderers.NAME
        )
    }
}

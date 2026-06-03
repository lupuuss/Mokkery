package dev.mokkery.mockable.plugin.fir.gen

import dev.mokkery.mockable.plugin.MokkeryMockable
import dev.mokkery.mockable.plugin.fir.isMockableAnnotated
import dev.mokkery.plugin.core.MokkeryCore
import dev.mokkery.plugin.core.fir.isMokkeryGeneratedConstructor
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.StandardTypes
import org.jetbrains.kotlin.fir.declarations.FirConstructor
import org.jetbrains.kotlin.fir.declarations.constructors
import org.jetbrains.kotlin.fir.declarations.getDeprecationsProviderFromAnnotations
import org.jetbrains.kotlin.fir.expressions.builder.buildAnnotation
import org.jetbrains.kotlin.fir.expressions.builder.buildAnnotationArgumentMapping
import org.jetbrains.kotlin.fir.expressions.builder.buildEnumEntryDeserializedAccessExpression
import org.jetbrains.kotlin.fir.expressions.builder.buildLiteralExpression
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
import org.jetbrains.kotlin.fir.plugin.createConstructor
import org.jetbrains.kotlin.fir.resolve.defaultType
import org.jetbrains.kotlin.fir.resolve.getSuperClassSymbolOrAny
import org.jetbrains.kotlin.fir.scopes.getDeclaredConstructors
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirConstructorSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol
import org.jetbrains.kotlin.fir.types.ConeTypeProjection
import org.jetbrains.kotlin.fir.types.builder.buildResolvedTypeRef
import org.jetbrains.kotlin.fir.types.constructClassType
import org.jetbrains.kotlin.fir.types.isUnit
import org.jetbrains.kotlin.fir.types.toLookupTag
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.types.ConstantValueKind

class MokkeryMockableGenerator(session: FirSession) : FirDeclarationGenerationExtension(session) {

    private val unitType = StandardClassIds.Unit
        .toLookupTag()
        .constructClassType(ConeTypeProjection.EMPTY_ARRAY, isMarkedNullable = false)

    override fun getCallableNamesForClass(
        classSymbol: FirClassSymbol<*>,
        context: MemberGenerationContext
    ): Set<Name> = when {
        classSymbol is FirRegularClassSymbol && session.isMockableAnnotated(classSymbol) -> setOf(SpecialNames.INIT)
        else -> emptySet()
    }

    override fun generateConstructors(context: MemberGenerationContext): List<FirConstructorSymbol> {
        val cls = context.owner as FirRegularClassSymbol
        val scope = context.declaredScope ?: return emptyList()
        val unitParamsCount = scope.getDeclaredConstructors().maxOfOrNull { ctor ->
            when {
                ctor.valueParameterSymbols.isEmpty() -> 0
                ctor.valueParameterSymbols.all { it.resolvedReturnType.isUnit } -> ctor.valueParameterSymbols.size
                else -> 0
            }
        } ?: 0
        val superClass = cls.getSuperClassSymbolOrAny(session)
        val superConstructors = superClass?.constructors(session)
        if (superConstructors != null
            && superClass.defaultType() != StandardTypes.Any
            && superConstructors.none { it.isMokkeryGeneratedConstructor() }
        ) return emptyList()
        val ctor = createConstructor(owner = cls, key = MokkeryMockable.Key) {
            valueParameter(MokkeryCore.Names.mockableConstructorMarkerParam, unitType)
            // in case there is a colliding constructor with single Unit parameter
            // more Unit parameter is added to avoid clash
            // although, it should never happen
            repeat(unitParamsCount) {
                valueParameter(Name.identifier("p$it"), unitType)
            }
        }
        ctor.configureDeprecation()
        return listOf(ctor.symbol)
    }

    private fun FirConstructor.configureDeprecation() {
        val kotlinDeprecationAnnotation = buildAnnotation {
            annotationTypeRef = buildResolvedTypeRef {
                coneType = StandardClassIds
                    .Annotations
                    .Deprecated
                    .toLookupTag()
                    .constructClassType(typeArguments = ConeTypeProjection.EMPTY_ARRAY, isMarkedNullable = false)
            }
            argumentMapping = buildAnnotationArgumentMapping {
                mapping[StandardClassIds.Annotations.ParameterNames.deprecatedMessage] = buildLiteralExpression(
                    source = null,
                    kind = ConstantValueKind.String,
                    value = "Mokkery mockable generated constructor should not be called directly!",
                    setType = true
                )

                mapping[StandardClassIds.Annotations.ParameterNames.deprecatedLevel] =
                    buildEnumEntryDeserializedAccessExpression {
                        enumClassId = StandardClassIds.DeprecationLevel
                        enumEntryName = Name.identifier(DeprecationLevel.HIDDEN.name)
                    }
            }
        }
        val annotations = listOf(kotlinDeprecationAnnotation)
        this.replaceAnnotations(annotations)
        this.replaceDeprecationsProvider(
            annotations.getDeprecationsProviderFromAnnotations(
                session = session,
                fromJava = false
            )
        )
    }
}

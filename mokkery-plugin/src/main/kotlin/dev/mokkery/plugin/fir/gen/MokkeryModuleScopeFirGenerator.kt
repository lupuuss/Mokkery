package dev.mokkery.plugin.fir.gen

import dev.mokkery.plugin.Mokkery
import dev.mokkery.plugin.ir.MokkeryIr
import dev.mokkery.plugin.moduleHash
import dev.mokkery.plugin.moduleScopeName
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.getDeprecationsProvider
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.expressions.builder.buildAnnotation
import org.jetbrains.kotlin.fir.expressions.builder.buildAnnotationArgumentMapping
import org.jetbrains.kotlin.fir.expressions.builder.buildEnumEntryDeserializedAccessExpression
import org.jetbrains.kotlin.fir.expressions.builder.buildLiteralExpression
import org.jetbrains.kotlin.fir.extensions.ExperimentalTopLevelDeclarationsGenerationApi
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
import org.jetbrains.kotlin.fir.moduleData
import org.jetbrains.kotlin.fir.plugin.createConeType
import org.jetbrains.kotlin.fir.plugin.createTopLevelProperty
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.types.builder.buildResolvedTypeRef
import org.jetbrains.kotlin.fir.types.constructClassLikeType
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.types.ConstantValueKind

@ExperimentalTopLevelDeclarationsGenerationApi
class MokkeryModuleScopeFirGenerator(
    session: FirSession,
    private val config: CompilerConfiguration,
) : FirDeclarationGenerationExtension(session) {

    private val moduleCallableId by lazy { CallableId(Mokkery.dev_mokkery_internal, config.moduleScopeName) }

    override fun getTopLevelCallableIds(): Set<CallableId> {
        if (session.moduleData.dependsOnDependencies.isNotEmpty()) return emptySet()
        return setOf(moduleCallableId)
    }

    override fun generateProperties(callableId: CallableId, context: MemberGenerationContext?): List<FirPropertySymbol> {
        if (callableId != moduleCallableId) return emptyList()
        val property = createTopLevelProperty(
            key = MokkeryIr.Key,
            callableId = callableId,
            returnType = Mokkery.ClassId.MokkeryScope.createConeType(session),
            hasBackingField = false,
            containingFileName = "MokkeryModuleScope_${config.moduleHash}",
        ) {
            visibility = Visibilities.Internal
        }
        property.replaceAnnotations(listOf(hiddenDeprecation()))
        property.replaceDeprecationsProvider(property.getDeprecationsProvider(session))
        return listOf(property.symbol)
    }

    private fun hiddenDeprecation(): FirAnnotation = buildAnnotation {
        annotationTypeRef = buildResolvedTypeRef {
            coneType = StandardClassIds.Annotations.Deprecated.constructClassLikeType()
        }
        argumentMapping = buildAnnotationArgumentMapping {
            mapping[Name.identifier("message")] = buildLiteralExpression(
                source = null,
                kind = ConstantValueKind.String,
                value = "Use MokkeryScope.module",
                setType = true,
            )
            mapping[Name.identifier("level")] = buildEnumEntryDeserializedAccessExpression {
                enumClassId = StandardClassIds.DeprecationLevel
                enumEntryName = Name.identifier("HIDDEN")
            }
        }
    }
}

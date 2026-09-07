package dev.mokkery.plugin.fir.gen

import dev.mokkery.plugin.Mokkery
import dev.mokkery.plugin.ir.MokkeryIr
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.moduleName
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.extensions.ExperimentalTopLevelDeclarationsGenerationApi
import org.jetbrains.kotlin.fir.extensions.FirDeclarationGenerationExtension
import org.jetbrains.kotlin.fir.extensions.MemberGenerationContext
import org.jetbrains.kotlin.fir.moduleData
import org.jetbrains.kotlin.fir.plugin.createConeType
import org.jetbrains.kotlin.fir.plugin.createTopLevelProperty
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.name.CallableId

@ExperimentalTopLevelDeclarationsGenerationApi
class MokkeryModuleScopeFirGenerator(
    session: FirSession,
    config: CompilerConfiguration,
) : FirDeclarationGenerationExtension(session) {

    private val moduleName = config.moduleName.orEmpty()

    override fun getTopLevelCallableIds(): Set<CallableId> {
        session.moduleData.stableModuleName
        if (session.moduleData.dependsOnDependencies.isNotEmpty()) return emptySet()
        return setOf(Mokkery.Callable.module)
    }

    override fun generateProperties(callableId: CallableId, context: MemberGenerationContext?): List<FirPropertySymbol> {
        if (callableId != Mokkery.Callable.module) return emptyList()
        val property = createTopLevelProperty(
            key = MokkeryIr.Key,
            callableId = callableId,
            returnType = Mokkery.ClassId.MokkeryScope.createConeType(session),
            hasBackingField = false,
            containingFileName = containingFileName(),
        ) {
            visibility = Visibilities.Internal
            extensionReceiverType(Mokkery.ClassId.MokkeryScopeCompanion.createConeType(session))
        }
        return listOf(property.symbol)
    }

    private fun containingFileName(): String = moduleName
        .map { if (it.isLetterOrDigit()) it else '_' }
        .joinToString(separator = "")
        .trim('_')
        .let { "MokkeryModuleScope_$it" }
}

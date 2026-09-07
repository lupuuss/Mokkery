package dev.mokkery.plugin.ir.transformer.core

import dev.mokkery.plugin.core.context.configuration
import dev.mokkery.plugin.core.ir.transformer.TransformerScope
import dev.mokkery.plugin.core.ir.transformer.currentFileValue
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.incremental.components.LookupTracker
import org.jetbrains.kotlin.incremental.components.Position
import org.jetbrains.kotlin.incremental.components.ScopeKind
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.resolve.sam.SAM_LOOKUP_NAME
import kotlin.collections.forEach


context(scope: TransformerScope)
fun recordSuperTypesLookUp(types: List<IrClass>) {
    val tracker = configuration[CommonConfigurationKeys.LOOKUP_TRACKER]
    if (tracker == null || tracker === LookupTracker.DO_NOTHING) return
    val filePath = currentFileValue.fileEntry.name
    val visited = mutableSetOf<IrClass>()
    types.forEach { it.recordWithSupertypes(tracker, filePath, visited) }
}

private fun IrClass.recordWithSupertypes(
    tracker: LookupTracker,
    filePath: String,
    visited: MutableSet<IrClass>,
) {
    if (!visited.add(this)) return
    tracker.record(
        filePath = filePath,
        position = Position.NO_POSITION,
        scopeFqName = kotlinFqName.asString(),
        scopeKind = ScopeKind.CLASSIFIER,
        name = SAM_LOOKUP_NAME.asString(),
    )
    superTypes.forEach { it.classOrNull?.owner?.recordWithSupertypes(tracker, filePath, visited) }
}

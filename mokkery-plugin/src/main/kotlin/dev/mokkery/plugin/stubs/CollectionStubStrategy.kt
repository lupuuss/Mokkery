package dev.mokkery.plugin.stubs

import dev.mokkery.plugin.ir.irCall
import dev.mokkery.plugin.ir.referenceKotlinCollectionsFunctions
import dev.mokkery.plugin.ir.referenceKotlinSequencesFunctions
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.isCollection
import org.jetbrains.kotlin.ir.types.isIterable
import org.jetbrains.kotlin.ir.types.isSequence

object CollectionStubStrategy : StubStrategy {

    context(scope: StubStrategyScope)
    override fun provide(type: IrType): Stub? = when {
        type.isIterable() || type.isCollection() || type.isList() -> emptyListStub()
        type.isMutableIterable() || type.isMutableCollection() || type.isMutableList() -> mutableListStub()
        type.isSet() -> emptySetStub()
        type.isMutableSet() -> mutableSetStub()
        type.isMap() -> emptyMapStub()
        type.isMutableMap() -> mutableMapStub()
        type.isSequence() -> emptySequenceStub()
        else -> null
    }

    context(scope: StubStrategyScope)
    private fun IrType.isList() = this.classOrNull == scope.irBuiltIns.listClass

    context(scope: StubStrategyScope)
    private fun IrType.isMutableList() = this.classOrNull == scope.irBuiltIns.mutableListClass

    context(scope: StubStrategyScope)
    private fun IrType.isMutableCollection() = this.classOrNull == scope.irBuiltIns.mutableCollectionClass

    context(scope: StubStrategyScope)
    private fun IrType.isMutableIterable() = this.classOrNull == scope.irBuiltIns.mutableIterableClass

    context(scope: StubStrategyScope)
    private fun IrType.isSet() = this.classOrNull == scope.irBuiltIns.setClass

    context(scope: StubStrategyScope)
    private fun IrType.isMutableSet() = this.classOrNull == scope.irBuiltIns.mutableSetClass

    context(scope: StubStrategyScope)
    private fun IrType.isMap() = this.classOrNull == scope.irBuiltIns.mapClass

    context(scope: StubStrategyScope)
    private fun IrType.isMutableMap() = this.classOrNull == scope.irBuiltIns.mutableMapClass

    context(scope: StubStrategyScope)
    private fun emptyListStub() = stub {
        scope.builder.irCall(
            scope
                .plugin
                .referenceKotlinCollectionsFunctions("emptyList")
                .first { it.owner.parameters.isEmpty() }
                .owner
        )
    }

    context(scope: StubStrategyScope)
    private fun mutableListStub() = stub {
        scope.builder.irCall(
            scope
                .plugin
                .referenceKotlinCollectionsFunctions("mutableListOf")
                .first { it.owner.parameters.isEmpty() }
                .owner
        )
    }

    context(scope: StubStrategyScope)
    private fun emptySetStub() = stub {
        scope.builder.irCall(
            scope
                .plugin
                .referenceKotlinCollectionsFunctions("emptySet")
                .first { it.owner.parameters.isEmpty() }
                .owner
        )
    }

    context(scope: StubStrategyScope)
    private fun mutableSetStub() = stub {
        scope.builder.irCall(
            scope
                .plugin
                .referenceKotlinCollectionsFunctions("mutableSetOf")
                .first { it.owner.parameters.isEmpty() }
                .owner
        )
    }

    context(scope: StubStrategyScope)
    private fun emptyMapStub() = stub {
        scope.builder.irCall(
            scope
                .plugin
                .referenceKotlinCollectionsFunctions("emptyMap")
                .first { it.owner.parameters.isEmpty() }
                .owner
        )
    }

    context(scope: StubStrategyScope)
    private fun mutableMapStub() = stub {
        scope.builder.irCall(
            scope
                .plugin
                .referenceKotlinCollectionsFunctions("mutableMapOf")
                .first { it.owner.parameters.isEmpty() }
                .owner
        )
    }

    context(scope: StubStrategyScope)
    private fun emptySequenceStub() = stub {
        scope.builder.irCall(
            scope
                .plugin
                .referenceKotlinSequencesFunctions("emptySequence")
                .first { it.owner.parameters.isEmpty() }
                .owner
        )
    }

}

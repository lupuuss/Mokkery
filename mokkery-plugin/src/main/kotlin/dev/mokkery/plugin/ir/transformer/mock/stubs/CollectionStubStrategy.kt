package dev.mokkery.plugin.ir.transformer.mock.stubs

import dev.mokkery.plugin.core.ir.irBuiltIns
import dev.mokkery.plugin.core.ir.transformer.referenced
import dev.mokkery.plugin.ir.KotlinIr
import dev.mokkery.plugin.ir.irCall
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
    private fun IrType.isList() = this.classOrNull == irBuiltIns.listClass

    context(scope: StubStrategyScope)
    private fun IrType.isMutableList() = this.classOrNull == irBuiltIns.mutableListClass

    context(scope: StubStrategyScope)
    private fun IrType.isMutableCollection() = this.classOrNull == irBuiltIns.mutableCollectionClass

    context(scope: StubStrategyScope)
    private fun IrType.isMutableIterable() = this.classOrNull == irBuiltIns.mutableIterableClass

    context(scope: StubStrategyScope)
    private fun IrType.isSet() = this.classOrNull == irBuiltIns.setClass

    context(scope: StubStrategyScope)
    private fun IrType.isMutableSet() = this.classOrNull == irBuiltIns.mutableSetClass

    context(scope: StubStrategyScope)
    private fun IrType.isMap() = this.classOrNull == irBuiltIns.mapClass

    context(scope: StubStrategyScope)
    private fun IrType.isMutableMap() = this.classOrNull == irBuiltIns.mutableMapClass

    context(scope: StubStrategyScope)
    private fun emptyListStub() = stub {
        scope.builder.irCall(referenced(KotlinIr.Function.emptyList))
    }

    context(scope: StubStrategyScope)
    private fun mutableListStub() = stub {
        scope.builder.irCall(referenced(KotlinIr.Function.mutableListOf))
    }

    context(scope: StubStrategyScope)
    private fun emptySetStub() = stub {
        scope.builder.irCall(referenced(KotlinIr.Function.emptySet))

    }

    context(scope: StubStrategyScope)
    private fun mutableSetStub() = stub {
        scope.builder.irCall(referenced(KotlinIr.Function.mutableSetOf))
    }

    context(scope: StubStrategyScope)
    private fun emptyMapStub() = stub {
        scope.builder.irCall(referenced(KotlinIr.Function.emptyMap))
    }

    context(scope: StubStrategyScope)
    private fun mutableMapStub() = stub {
        scope.builder.irCall(referenced(KotlinIr.Function.mutableMapOf))
    }

    context(scope: StubStrategyScope)
    private fun emptySequenceStub() = stub {
        scope.builder.irCall(referenced(KotlinIr.Function.emptySequence))
    }

}

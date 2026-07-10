package dev.mokkery.plugin.ir.transformer.mock.stubs

import dev.mokkery.plugin.core.ir.IrFunctionReferencer
import dev.mokkery.plugin.core.ir.irBuiltIns
import dev.mokkery.plugin.core.ir.transformer.referenced
import dev.mokkery.plugin.ir.KotlinIr
import dev.mokkery.plugin.ir.argumentTypes
import dev.mokkery.plugin.ir.irCall
import dev.mokkery.plugin.ir.typeArgumentsFrom
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.isCollection
import org.jetbrains.kotlin.ir.types.isIterable
import org.jetbrains.kotlin.ir.types.isSequence

object CollectionStubStrategy : StubStrategy {

    context(scope: StubStrategyScope)
    override fun provide(type: IrType): Stub? = when {
        type.isIterable() || type.isCollection() || type.isList() -> type.emptyStub(KotlinIr.Function.emptyList)
        type.isMutableIterable() || type.isMutableCollection() || type.isMutableList() -> {
            type.emptyStub(KotlinIr.Function.mutableListOf)
        }
        type.isSet() -> type.emptyStub(KotlinIr.Function.emptySet)
        type.isMutableSet() -> type.emptyStub(KotlinIr.Function.mutableSetOf)
        type.isMap() -> type.emptyStub(KotlinIr.Function.emptyMap)
        type.isMutableMap() -> type.emptyStub(KotlinIr.Function.mutableMapOf)
        type.isSequence() -> type.emptyStub(KotlinIr.Function.emptySequence)
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
    private fun IrType.emptyStub(function: IrFunctionReferencer) = stub {
        val factory = referenced(function)
        val factoryTypeArguments = factory.typeArgumentsFrom(argumentTypes)
        scope.builder.irCall(factory) {
            factoryTypeArguments.forEachIndexed { index, it -> typeArguments[index] = it }
        }
    }
}

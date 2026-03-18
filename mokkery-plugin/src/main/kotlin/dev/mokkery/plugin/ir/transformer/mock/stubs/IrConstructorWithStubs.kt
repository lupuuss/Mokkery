package dev.mokkery.plugin.ir.transformer.mock.stubs

import dev.mokkery.plugin.context.configuration
import dev.mokkery.plugin.ir.irBuiltIns
import dev.mokkery.plugin.ir.irCallConstructor
import dev.mokkery.plugin.ir.transformer.core.TransformerScope
import dev.mokkery.plugin.stubsConfig
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.IrBuilder
import org.jetbrains.kotlin.ir.builders.irDelegatingConstructorCall
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrDelegatingConstructorCall
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.util.BodyPrintingStrategy
import org.jetbrains.kotlin.ir.util.KotlinLikeDumpOptions
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.defaultConstructor
import org.jetbrains.kotlin.ir.util.dumpKotlinLike
import org.jetbrains.kotlin.ir.util.hasDefaultValue
import org.jetbrains.kotlin.ir.util.primaryConstructor

context(scope: TransformerScope)
fun IrBlockBodyBuilder.irDelegatingConstructorWithStubs(
    irClass: IrClass?
): IrDelegatingConstructorCall {
    val defaultConstructor = irClass?.defaultConstructor
    return when {
        irClass == null -> irDelegatingConstructorCall(irBuiltIns.anyClass.owner.primaryConstructor!!)
        defaultConstructor != null -> irDelegatingConstructorCall(defaultConstructor)
        else -> {
            val strategy = StubStrategy.default(configuration.stubsConfig)
            context(stubStrategyScope(strategy = strategy, builder = this)) {
                val constructor = strategy
                    .provideConstructorWithStubs(
                        cls = irClass,
                        visibilities = ConstructableClassStubStrategy.acceptedVisibilities
                    ) ?: failedToProvideStubsError(irClass)
                irDelegatingConstructorWithStubs(constructor)
            }
        }
    }
}

fun IrBuilder.irCallConstructorWithStubs(
    constructorWithStubs: Pair<IrConstructor, List<Stub>>,
    typeArguments: List<IrType> = emptyList(),
    block: IrConstructorCall.() -> Unit = { },
): IrConstructorCall {
    val (constructor, stubs) = constructorWithStubs
    return irCallConstructor(constructor, typeArguments) {
        stubs.forEachIndexed { i, stub ->
            val params = constructor.parameters
            if (!params[i].hasDefaultValue()) {
                arguments[i] = stub.expression
            }
        }
        block()
    }
}

fun IrBuilder.irDelegatingConstructorWithStubs(constructorWithStubs: Pair<IrConstructor, List<Stub>>): IrDelegatingConstructorCall {
    val (constructor, stubs) = constructorWithStubs
    return irDelegatingConstructorCall(constructor).apply {
        val params = constructor.parameters
        stubs.forEachIndexed { i, stub ->
            if (!params[i].hasDefaultValue()) {
                arguments[i] = stub.expression
            }
        }
    }
}


private fun failedToProvideStubsError(irClass: IrClass): Nothing {
    val dumpOptions = KotlinLikeDumpOptions(bodyPrintingStrategy = BodyPrintingStrategy.NO_BODIES)
    val constructorsDump = irClass
        .constructors
        .joinToString {
            it.dumpKotlinLike(options = dumpOptions)
                .removeSuffix("\n")
        }
    error(
        "Failed to mock `${irClass.name.asString()}`. " +
                "Mokkery is unable to supply all required arguments for any declared constructor: $constructorsDump"
    )

}

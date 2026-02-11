package dev.mokkery.internal.serialization.compiler.ast

import dev.mokkery.internal.serialization.compiler.core.asPeekStream
import dev.mokkery.internal.serialization.compiler.core.collectConsumedWhile

internal interface Evaluation {

    val type: Type

    fun evaluate(): Any?
}

internal fun Evaluation(type: Type, block: () -> Any?) = object : Evaluation {
    override val type: Type = type

    override fun evaluate(): Any? = block()
}

internal interface EvaluationFactory {

    fun createFrom(expression: Expression): Evaluation
}

internal fun EvaluationFactory(symbolTable: SymbolTable): EvaluationFactory = object : EvaluationFactory {

    override fun createFrom(expression: Expression) = when (expression) {
        is Expression.Call -> createForFunction(expression.id, expression.arguments)
        is Expression.StringLiteral -> Evaluation(Type.String) { expression.value }
        is Expression.IntLiteral -> Evaluation(Type.Int) { expression.value }
        is Expression.Access -> createForProperty(expression.id)
        is Expression.BinaryOperator -> createForFunction(expression.id, listOf(expression.left, expression.right))
        is Expression.UnaryOperator -> createForFunction(expression.id, listOf(expression.operand))
        is Expression.Infix -> createForFunction(expression.id, listOf(expression.left, expression.right))
    }

    private fun createForFunction(id: Identifier, arguments: List<Expression>): Evaluation {
        val candidates = symbolTable.resolveFunctions(id)
        if (candidates.isEmpty()) error("Symbol ${id.render()} could not be resolved!")
        val arguments = arguments.map { createFrom(it) }
        val result = candidates.firstNotNullOfOrNull { it.safeEvaluationOrNull(arguments) }
        if (result != null) return result
        error("No matching candidates for ${id.render()}! Available candidates: ${candidates.joinToString { it.render() }}")
    }

    private fun createForProperty(id: Identifier): Evaluation {
        val candidates = symbolTable.resolveProperty(id)
        if (candidates.isEmpty()) error("Symbol ${id.render()} could not be resolved!")
        return candidates
            .singleOrNull()
            ?.evaluation()
            ?: error("Ambiguous reference ${id.render()}!")
    }
}

internal fun FunctionSymbol.safeEvaluationOrNull(arguments: List<Evaluation>): Evaluation? {
    val params = parameters.asPeekStream()
    val args = arguments.asPeekStream()
    val argsToCall = mutableListOf<Evaluation>()
    while (true) {
        val arg = args.peek()
        val param = params.peek()
        when {
            arg == null && param == null -> return evaluation(argsToCall)
            param == null -> return null
            param.isVararg -> {
                val elementType = param.type.elementType
                val varargs = args.collectConsumedWhile { it.type == elementType }
                params.consumed()
                argsToCall.add(Evaluation(param.type) { varargs.map { it.evaluate() } })
            }
            arg == null -> return null
            param.type == arg.type -> {
                argsToCall.add(arg)
                params.consumed()
                args.consumed()
            }
            else -> return null
        }
    }
}

internal fun FunctionSymbol.evaluation(args: List<Evaluation>): Evaluation = Evaluation(type) {
    body(FunctionSymbol.Args(args))
}

internal fun PropertySymbol.evaluation(): Evaluation = Evaluation(type) {
    access()
}

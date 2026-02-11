package dev.mokkery.internal.serialization.compiler.ast

internal data class Identifier(val value: String) {

    fun render() = "'$value'"

    override fun toString() = "id($value)"

    companion object {
        val Plus = Identifier("+")
        val Minus = Identifier("-")
        val Range = Identifier("..")
    }
}

internal sealed interface Expression {

    data class StringLiteral(val value: String) : Expression {
        override fun toString(): String = "str[$value]"
    }

    data class IntLiteral(val value: Int) : Expression {
        override fun toString(): String = "int[$value]"
    }

    data class Access(val id: Identifier) : Expression {
        override fun toString(): String = "access[$id]"
    }

    data class Call(val id: Identifier, val arguments: List<Expression>) : Expression {

        override fun toString(): String = "call[$id, ${arguments.joinToString()}]"
    }

    data class Infix(val id: Identifier, val left: Expression, val right: Expression) : Expression {
        override fun toString(): String = "infix[$id, $left, $right]"
    }

    data class UnaryOperator(val id: Identifier, val operand: Expression) : Expression {
        override fun toString(): String = "unary[$id, $operand]"
    }

    data class BinaryOperator(val id: Identifier, val left: Expression, val right: Expression) : Expression {
        override fun toString(): String = "binary[$id, $left, $right]"
    }
}

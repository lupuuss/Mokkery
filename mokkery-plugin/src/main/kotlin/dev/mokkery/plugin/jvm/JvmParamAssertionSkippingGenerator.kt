package dev.mokkery.plugin.jvm

import org.jetbrains.kotlin.backend.jvm.JvmSymbols
import org.jetbrains.kotlin.backend.jvm.extensions.ClassGenerator
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.org.objectweb.asm.MethodVisitor
import org.jetbrains.org.objectweb.asm.Opcodes

object IntrinsicConsts {

    const val OWNER = JvmSymbols.INTRINSICS_CLASS_NAME
    const val PARAM_NULL_CHECK_DESCRIPTOR = "(Ljava/lang/Object;Ljava/lang/String;)V"
    val PARAM_NULL_CHECK_NAMES = arrayOf("checkParameterIsNotNull", "checkNotNullParameter")
}

class JvmParamAssertionSkippingGenerator(
    private val delegate: ClassGenerator,
    private val collector: MessageCollector
) : ClassGenerator by delegate {

    override fun newMethod(
        declaration: IrFunction?,
        access: Int,
        name: String,
        desc: String,
        signature: String?,
        exceptions: Array<out String>?
    ) = delegate
        .newMethod(declaration, access, name, desc, signature, exceptions)
        .let { Visitor(it, declaration, collector) }


    class Visitor(
        visitor: MethodVisitor,
        private val declaration: IrFunction?,
        private val collector: MessageCollector
    ) : MethodVisitor(Opcodes.API_VERSION, visitor) {

        override fun visitMethodInsn(
            opcode: Int,
            owner: String?,
            name: String?,
            descriptor: String?,
            isInterface: Boolean
        ) {
            if (
                opcode != Opcodes.INVOKESTATIC ||
                isInterface ||
                owner != IntrinsicConsts.OWNER ||
                descriptor != IntrinsicConsts.PARAM_NULL_CHECK_DESCRIPTOR ||
                name !in IntrinsicConsts.PARAM_NULL_CHECK_NAMES
            ) {
                return super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
            }
            collector.report(CompilerMessageSeverity.INFO, "Remove not-null assertion from ${declaration?.name}!")
        }
    }
}


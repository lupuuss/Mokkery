package dev.mokkery.plugin

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.util.companionObject
import org.jetbrains.kotlin.ir.util.getPropertyGetter
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

object MokkeryDeclarations {

    private val mokkeryPackage = FqName("dev.mokkery")

    val mockFunctionName = mokkeryPackage.child(Name.identifier("mock"))
    val everyFunctionName = mokkeryPackage.child(Name.identifier("every"))
    val everySuspendFunctionName = mokkeryPackage.child(Name.identifier("everySuspend"))
    val verifyFunctionName = mokkeryPackage.child(Name.identifier("verify"))
    val verifySuspendFunctionName = mokkeryPackage.child(Name.identifier("verifySuspend"))

    fun irClass(
        context: IrPluginContext
    ) = context.referenceClass(ClassId(mokkeryPackage, Name.identifier("Mokkery")))!!

    fun baseMokkeryScopeClass(context: IrPluginContext) = context
        .referenceClass(ClassId(mokkeryPackage, Name.identifier("BaseMokkeryScope")))!!
        .owner

    fun internalEvery(context: IrPluginContext) = context
        .referenceFunctions(CallableId(mokkeryPackage, Name.identifier("internalEvery")))
        .first()

    fun internalVerify(context: IrPluginContext) = context
        .referenceFunctions(CallableId(mokkeryPackage, Name.identifier("internalVerify")))
        .first()


    fun internalEverySuspend(context: IrPluginContext) = context
        .referenceFunctions(CallableId(mokkeryPackage, Name.identifier("internalEverySuspend")))
        .first()

    fun internalVerifySuspend(context: IrPluginContext) = context
        .referenceFunctions(CallableId(mokkeryPackage, Name.identifier("internalVerifySuspend")))
        .first()
}

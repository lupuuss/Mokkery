package dev.mokkery.plugin.ext

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.ClassId

fun IrPluginContext.getClass(classId: ClassId) = referenceClass(classId)!!.owner

fun IrPluginContext.firstFunction(callableId: CallableId) = referenceFunctions(callableId).first()

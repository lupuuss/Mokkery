package dev.mokkery.test

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.NativePointed
import platform.posix.FILE

@OptIn(ExperimentalForeignApi::class)
interface NativeInteropInterface {

    fun call(file: FILE): FILE

    fun <T : NativePointed> callGeneric(value: T): T
}

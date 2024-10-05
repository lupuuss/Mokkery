package dev.mokkery.test

import platform.Foundation.NSDecimalNumber
import platform.Foundation.NSNumber

interface ObjcInteropInterface {
    fun call(file: NSDecimalNumber): NSDecimalNumber

    fun <T : NSNumber> callGeneric(value: T): T
}

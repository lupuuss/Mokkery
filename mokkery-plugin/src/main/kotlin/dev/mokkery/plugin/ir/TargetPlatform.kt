package dev.mokkery.plugin.ir

import org.jetbrains.kotlin.platform.TargetPlatform
import org.jetbrains.kotlin.platform.isJs
import org.jetbrains.kotlin.platform.isWasm

fun TargetPlatform?.isJsOrWasm() = isJs() || isWasm()

@file:Suppress("unused", "UnusedReceiverParameter")

package dev.mokkery.templating

import dev.mokkery.internal.utils.toBeReplacedByCompilerPlugin

context(scope: TemplatingScope)
public inline fun <T, R> T.ext(block: T.() -> R): R = toBeReplacedByCompilerPlugin

context(scope: TemplatingScope)
public inline fun <A, R> ctx(
    a: A,
    block: context(A) () -> R
): R = toBeReplacedByCompilerPlugin

context(scope: TemplatingScope)
public inline fun <A, B, R> ctx(
    a: A,
    b: B,
    block: context(A, B) () -> R
): R = toBeReplacedByCompilerPlugin

context(scope: TemplatingScope)
public inline fun <A, B, C, R> ctx(
    a: A,
    b: B,
    c: C,
    block: context(A, B, C) () -> R
): R = toBeReplacedByCompilerPlugin

context(scope: TemplatingScope)
public inline fun <A, B, C, D, R> ctx(
    a: A,
    b: B,
    c: C,
    d: D,
    block: context(A, B, C, D) () -> R
): R = toBeReplacedByCompilerPlugin

context(scope: TemplatingScope)
public inline fun <A, B, C, D, E, R> ctx(
    a: A,
    b: B,
    c: C,
    d: D,
    e: E,
    block: context(A, B, C, D, E) () -> R
): R = toBeReplacedByCompilerPlugin

context(scope: TemplatingScope)
public inline fun <A, B, C, D, E, F, R> ctx(
    a: A,
    b: B,
    c: C,
    d: D,
    e: E,
    f: F,
    block: context(A, B, C, D, E, F) () -> R
): R = toBeReplacedByCompilerPlugin

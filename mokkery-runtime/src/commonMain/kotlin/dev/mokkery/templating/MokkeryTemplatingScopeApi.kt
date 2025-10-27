@file:Suppress("unused", "UnusedReceiverParameter")

package dev.mokkery.templating

import dev.mokkery.internal.utils.mokkeryIntrinsic

/**
 * Allows mocking methods with extension receiver.
 *
 * Example:
 * ```kotlin
 * interface Foo {
 *      fun Int.foo(): Int
 * }
 *
 * val mock = mock<Foo>()
 * every { mock.ext { any<Int>().foo() } } returnsArgAt 0
 * ```
 */
context(scope: MokkeryTemplatingScope)
public inline fun <T, R> T.ext(block: T.() -> R): R = mokkeryIntrinsic

/**
 * Allows mocking methods with context parameters.
 *
 * Example:
 * ```kotlin
 * interface Foo {
 *      context(ctx: Int)
 *      fun foo(): Int
 * }
 *
 * val mock = mock<Foo>()
 * every { ctx(any<Int>()) { mock.foo() } } returnsArgAt 0
 * ```
 */
context(scope: MokkeryTemplatingScope)
public inline fun <A, R> ctx(
    a: A,
    block: context(A) () -> R
): R = mokkeryIntrinsic

/**
 * Allows mocking methods with context parameters.
 *
 * Example:
 * ```kotlin
 * interface Foo {
 *      context(ctx: Int)
 *      fun foo(): Int
 * }
 *
 * val mock = mock<Foo>()
 * every { ctx(any<Int>()) { mock.foo() } } returnsArgAt 0
 * ```
 */
context(scope: MokkeryTemplatingScope)
public inline fun <A, B, R> ctx(
    a: A,
    b: B,
    block: context(A, B) () -> R
): R = mokkeryIntrinsic

/**
 * Allows mocking methods with context parameters.
 *
 * Example:
 * ```kotlin
 * interface Foo {
 *      context(ctx: Int)
 *      fun foo(): Int
 * }
 *
 * val mock = mock<Foo>()
 * every { ctx(any<Int>()) { mock.foo() } } returnsArgAt 0
 * ```
 */
context(scope: MokkeryTemplatingScope)
public inline fun <A, B, C, R> ctx(
    a: A,
    b: B,
    c: C,
    block: context(A, B, C) () -> R
): R = mokkeryIntrinsic

/**
 * Allows mocking methods with context parameters.
 *
 * Example:
 * ```kotlin
 * interface Foo {
 *      context(ctx: Int)
 *      fun foo(): Int
 * }
 *
 * val mock = mock<Foo>()
 * every { ctx(any<Int>()) { mock.foo() } } returnsArgAt 0
 * ```
 */
context(scope: MokkeryTemplatingScope)
public inline fun <A, B, C, D, R> ctx(
    a: A,
    b: B,
    c: C,
    d: D,
    block: context(A, B, C, D) () -> R
): R = mokkeryIntrinsic

/**
 * Allows mocking methods with context parameters.
 *
 * Example:
 * ```kotlin
 * interface Foo {
 *      context(ctx: Int)
 *      fun foo(): Int
 * }
 *
 * val mock = mock<Foo>()
 * every { ctx(any<Int>()) { mock.foo() } } returnsArgAt 0
 * ```
 */
context(scope: MokkeryTemplatingScope)
public inline fun <A, B, C, D, E, R> ctx(
    a: A,
    b: B,
    c: C,
    d: D,
    e: E,
    block: context(A, B, C, D, E) () -> R
): R = mokkeryIntrinsic

/**
 * Allows mocking methods with context parameters.
 *
 * Example:
 * ```kotlin
 * interface Foo {
 *      context(ctx: Int)
 *      fun foo(): Int
 * }
 *
 * val mock = mock<Foo>()
 * every { ctx(any<Int>()) { mock.foo() } } returnsArgAt 0
 * ```
 */
context(scope: MokkeryTemplatingScope)
public inline fun <A, B, C, D, E, F, R> ctx(
    a: A,
    b: B,
    c: C,
    d: D,
    e: E,
    f: F,
    block: context(A, B, C, D, E, F) () -> R
): R = mokkeryIntrinsic

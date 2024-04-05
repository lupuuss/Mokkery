# Mokkery

[![Gradle Plugin Portal Beta](https://img.shields.io/gradle-plugin-portal/v/dev.mokkery?versionPrefix=2)](https://plugins.gradle.org/plugin/dev.mokkery/2.0.0-Beta3)
[![Gradle Plugin Portal Stable](https://img.shields.io/gradle-plugin-portal/v/dev.mokkery?versionPrefix=1)](https://plugins.gradle.org/plugin/dev.mokkery/1.9.23-1.6.1)
[![Kotlin](https://img.shields.io/badge/kotlin-1.9.23-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![GitHub](https://img.shields.io/github/license/lupuuss/Mokkery)](https://github.com/lupuuss/Mokkery/blob/main/LICENSE)
[![API reference](https://img.shields.io/static/v1?label=api&message=reference&labelColor=gray&color=blueviolet&logo=gitbook&logoColor=white)](https://mokkery.dev)


Mokkery is a mocking library for Kotlin Multiplatform, easy to use, boilerplate-free and compiler plugin driven.

```kotlin
val repository = mock<BookRepository> {
    everySuspend { findById(any()) } returns stubBook()
}
val service = BookService(repository)

@Test
fun `rent should call repository for each book`() = runTest {
    service.rentAll(listOf("1", "2"))
    verifySuspend(exhaustiveOrder) {
        repository.findById("1")
        repository.findById("2")
    }
}
```

As shown in the example above, this library is highly inspired by the [MockK](https://mockk.io).
If you have any experience with MockK, it should be easy to start with Mokkery!

1. [Setup](#setup)
2. [Compatibility](#compatibility)
3. [Targets](#targets)
4. [Mock & Spy](#mock-and-spy)
5. [Mocking](#mocking)
6. [Calling original implementations](#calling-original-implementations)
7. [Verifying](#verifying)
8. [Mocking and verifying limitations](#mocking-and-verifying-limitations)
9. [Regular matchers](#regular-matchers)
10. [Logical matchers](#logical-matchers)
11. [Vararg matchers](#vararg-matchers)
12. [Custom matchers](#custom-matchers)
13. [Arguments capturing](#arguments-capturing)
### Setup

Apply Gradle plugin to your kotlin project:

```kotlin
plugins {
    kotlin("multiplatform") version "1.9.23"
    id("dev.mokkery") version "1.9.23-1.6.1"
}
```

Please ensure that the Kotlin versions for both the Kotlin plugin and the Mokkery plugin are the same.
Refer to the [compatibility](#compatibility) section to find the supported versions.

The plugin will be applied to all Kotlin source sets in the project that contain the "test" phrase. To change this behavior, you can provide a different rule in your Gradle file:
```kotlin
plugins {
    kotlin("multiplatform") version "1.9.23"
    id("dev.mokkery") version "1.9.23-1.6.1"
}

mokkery {
    rule.set(ApplicationRule.Listed("jvmTest")) // now Mokkery affects only jvmTest
    // or provide a custom rule
    rule.set { sourceSet -> sourceSet.name.endsWith("Test") } // now Mokkery affects all "*Test" source sets
}
```

Make sure that you have `mavenCentral()` in your repository list:
```kotlin
repositories {
    mavenCentral()
}
```

Also, it might be necessary to add `gradlePluginPortal()` and `mavenCentral()` to your plugin repositories:
```kotlin
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}
```

### Compatibility

The goal is to support 4 latest versions of Kotlin for each library release. However, if a new Kotlin release introduces 
breaking changes, especially in the compiler API, it might result in dropped support for older versions. The latest 
Kotlin version is always prioritized.

| Mokkery version                  	 | Supported Kotlin version                               | Plugin version           |
|------------------------------------|--------------------------------------------------------|--------------------------|
| 2.0.0-Beta3 	                      | 2.0.0-Beta5+	                                          | `"2.0.0-Beta3"`          |
| 2.0.0-Beta2 	                      | 2.0.0-Beta5+	                                          | `"2.0.0-Beta2"`          |
| 2.0.0-Beta1 	                      | 2.0.0-Beta4	                                           | `"2.0.0-Beta1"`          |
| 1.6.1 	                            | 1.9.23                                                 | `"$kotlinVersion-1.6.1"` |
| 1.6.0 	                            | 1.9.23                                                 | `"$kotlinVersion-1.6.0"` |
| 1.5.0 	                            | 1.9.23, 1.9.22	                                        | `"$kotlinVersion-1.5.0"` |
| 1.4.0 	                            | 1.9.22, 1.9.21, 1.9.20 	                               | `"$kotlinVersion-1.4.0"` |
| 1.3.2 	                            | 1.9.22, 1.9.21, 1.9.20  	                              | `"$kotlinVersion-1.3.2"` |
| 1.3.1 	                            | 1.9.22, 1.9.21, 1.9.20                                 | `"$kotlinVersion-1.3.1"` |
| 1.3.0 	                            | 1.9.20, 1.9.10, 1.9.0, 1.8.22, 1.8.21, 1.8.20        	 | `"$kotlinVersion-1.3.0"` |
| 1.2.0 	                            | 1.9.0, 1.8.22, 1.8.21, 1.8.20 	                        | `"$kotlinVersion-1.2.0"` |
| 1.1.0 	                            | 1.9.0, 1.8.22, 1.8.21, 1.8.20 	                        | `"$kotlinVersion-1.1.0"` |
| 1.0.1 	                            | 1.9.0, 1.8.22, 1.8.21, 1.8.20 	                        | `"$kotlinVersion-1.0.1"` |

> **Warning**
> Mokkery 2.0.0 pre-releases are not locked to specific Kotlin pre-release and optimistically assume that compiler 
> API is not going to change. However, breaking changes might happen and result in runtime errors during compilation.

### Targets

Mokkery currently supports JVM, JS, Wasm-JS, Wasm-WASI (without coroutines), 
and [all 3 tiers of Kotlin Native targets](https://kotlinlang.org/docs/native-target-support.html).
You can refer to [this file](build-mokkery/src/main/kotlin/mokkery-multiplatform.gradle.kts) for more details.

### <a id="mock-and-spy" /> Mock & Spy

Mokkery supports the creation of mocks and spies, although not for every type. At the moment, it is possible
to mock/spy interfaces, functional types and all fully overridable classes with no-args constructors. 

Mocking final classes is partially supported with [all-open plugin](#mocking-final-classes).

#### Mock:

Mock tracks all method calls and allows [defining their answers](#mocking). By default, mocks
are strict. If answer is not defined, it throws runtime exception. However, it is possible to
change the default mock answer:

```kotlin
import dev.mokkery.MockMode.strict
import dev.mokkery.MockMode.autofill
import dev.mokkery.MockMode.autoUnit

// default - fails on missing answers
val repository = mock<BookRepository>(strict)

// autofill - returns empty values e.g. 0 for numbers, "" for string and null for complex types.
val repository = mock<BookRepository>(autofill)

// autoUnit - does not fail on Unit returning methods. 
val repository = mock<BookRepository>(autoUnit)
```

You can change the default `MockMode` on the Gradle plugin level like this:

```kotlin
mokkery.defaultMockMode.set(MockMode.autoUnit)
```

#### Spy:

Similarly to mock, spy tracks method calls. It is not possible to change spy behavior. It always calls spied object
method.

```kotlin
val repository = BookRepositoryImpl()
val spiedRepository = spy<BookRepository>(repository)
```

> **Warning**
> Ensure that you specify a type for the `spy`. Inferred type would be `BookRepositoryImpl`, which is very likely to be a
> final class, and it is not supported to spy them!

#### Mock creation limitations

Type passed to `spy` and `mock` must be directly specified. Following code is illegal:

```kotlin
inline fun <reified T : Any> myMock() = mock<T>()
```

However, it is not completely forbidden to use generic parameters. Following code is allowed:
```kotlin
fun <T : Any> myListMock() = mock<List<T>>()
```

#### Mocking final classes

Mocking final classes that are already compiled is currently not possible. This includes any class defined in main
source set, therefore this feature is not supported. However, you can "open" your final classes from 
main source set with [all-open plugin](https://kotlinlang.org/docs/all-open-plugin.html#gradle). 

### Mocking

To define an answer for a method call, you have to use `every` or `everySuspend`:

```kotlin
// Inside mock block
val repository = mock<BookRepository> {
    // Notice that everySuspend does not require suspension!
    everySuspend { getById(id = "1") } returns stubBook()
}

@Test
fun test() = runTest {
    repository.getById("1") // returns stubBook() result
    repository.getById("2") // fails - answer is not defined for call with arg "2"
}
```

To throw an exception use `throws`:

```kotlin
// this answer is defined for call with `any()` id. 
everySuspend { repository.getById(id = any()) } throws IllegalArgumentException()

runCatching {
    repository.getById("2") // fails with IllegalArgumentException
}
runCatching {
    repository.getById("3") // fails with IllegalArgumentException
}
```

To provide more complex answer use `calls`:

```kotlin
everySuspend { repository.getById(id = any()) } calls { (id: String) ->
    delay(1_000) // suspension is allowed here!
    stubBook()
}
```

You can define a sequence of answers using `sequentially`:

```kotlin
everySuspend { repository.getById(id = any()) } sequentially {
    returns(stubBook("1"))
    calls { stubBook("2") }
    throws(IllegalStateException())
}
repository.getById("1") // returns stubBook("1")
repository.getById("2") // returns stubBook("2")
runCatching { repository.getById("3") } // throws IllegalStateException
repository.getById("4") // fails - no more answers
```
At the end of `sequentially` block you can repeat a sequence of answers with `repeat`:

```kotlin
everySuspend { repository.getById(id = any()) } sequentially {
    returns(stubBook("1"))
    repeat { returns(stubBook("2")) }
}
repository.getById("1") // returns stubBook("1")
repository.getById("2") // returns stubBook("2")
repository.getById("3") // returns stubBook("2")
repository.getById("4") // returns stubBook("2")
```

You can use `sequentiallyReturns` and `sequentiallyThrows` as a shorthand:
```kotlin
everySuspend { repository.getById(id = any()) } sequentiallyReturns listOf(stubBook("1"), stubBook("2"))
repository.getById("1") // returns stubBook("1")
repository.getById("2") // returns stubBook("2")
repository.getById("3") // no more answers
```

You can nest `sequentially` calls:

```kotlin
everySuspend { repository.getById(id = any()) } sequentially {
    returns(stubBook("1"))
    sequentially {
        returns(stubBook("2"))
        returns(stubBook("3"))
    }
    returns(stubBook("4"))
}
repository.getById("1") // returns stubBook("1")
repository.getById("2") // returns stubBook("2")
repository.getById("3") // returns stubBook("3")
repository.getById("4") // returns stubBook("4")
repository.getById("5") // fails - no more answers
```

Also, it is possible to implement your own reusable answer by
implementing [Answer](https://www.mokkery.dev/mokkery-runtime/dev.mokkery.answering/-answer/index.html) and pass it to
`answers`:

```kotlin
object RandomIntAnswer : Answer<Int> {
    override fun call(scope: FunctionScope) = Random.nextInt()
}
// ...
everySuspend { repository.countAllBooks() } answers RandomIntAnswer
```

> **Warning**
>When multiple answers match a call, the last one takes precedence.
### Calling original implementations

The most straightforward way to call original method is to use `calls` overload:

```kotlin
everySuspend { repository.findById(any()) } calls original

respository.findById("2") // this calls original method implementation with "2"
```

You can pass different arguments to original call:

```kotlin
everySuspend { repository.findById(any()) } calls originalWith("3")

respository.findById("2") // this calls original method implementation with "3"
```

> **Warning**
> If mocked type is an interface, the default implementation is called.

Under the hood, `original` performs call to super method from mocked type. In Kotlin source code, it is not allowed to call super method
of indirect supertype. However, this kind of call is possible to be generated on the compiler plugin level.
It's important to note that indirect super calls for Java types (including kotlin.collections.List) and for super
methods from interfaces compiled to Java defaults are not allowed.
This restriction exists because such calls are validated in the JVM bytecode and result in runtime errors.

Indirect super calls feature has to be explicitly allowed in Gradle files:
```kotlin
mokkery {
    allowIndirectSuperCalls.set(true)
}
```

Calling super methods from indirect supertypes is similar to calling original methods:

```kotlin
everySuspend { repository.findById(any()) } calls superOf<BaseRepository>()

respository.findById("2") // this calls super method implementation from BaseRepository with "2"
```

You can pass different arguments to super call:

```kotlin
everySuspend { repository.findById(any()) } calls superWith<BaseRepository>("3")

respository.findById("2") // this calls super method implementation from BaseRepository with "3"
```

All of those features are accessible from `calls` scope:
```kotlin
everySuspend { repository.findById(any()) } calls {
    callOriginal()
    callOriginalWith("3")
    callSuper(BaseRepository::class)
    callSuperWith(BaseRepository::class, "3")
}
```
### Verifying

To verify method call use `verify` or `verifySuspend`. Verification result depends on the `VerifyMode`.
It determines the behavior and criteria for verification.

You can change the default `VerifyMode` in the `build.gradle` file:

```kotlin
mokkery.defaultVerifyMode.set(VerifyMode.exhaustiveOrder)
```

#### Soft modes family

By default `verify` uses `VerifyMode.soft`. It checks only if calls from the verification block happened and marks all
matching calls as verified.

```kotlin
repository.findById("1")
repository.findById("2")
repository.findAll()
verifySuspend { 
    // Verification passes and marks `findById("1")` and `findById("2")` as verified.
    repository.findById(any())
}
```

You can restrict number of calls with `atLeast`, `atMost`, `exactly` and `inRange`:

```kotlin
repository.findById("1")
repository.findById("2")
repository.findAll()
verifySuspend(atMost(1)) { 
    // Verification fails - 2 matching calls, but expected 1 at most
    repository.findById(any())
}
```

#### Exhaustive

`VerifyMode.exhaustive` acts the same way as soft, but checks if all calls have been verified.

```kotlin
repository.findById("1")
repository.findById("2")
repository.findAll()
verifySuspend(exhaustive) { 
    // Verification fails - `findAll` not verified
    repository.findById(any())
}
```

#### Order

`VerifyMode.order` verifies that each call from the verification block happened once in the specified order:

```kotlin
repository.findById("1")
repository.findById("2")
repository.findAll()

verifySuspend(order) { 
    // Verification passes - only `findById("1")` and `findAll()` is marked as verified
    repository.findById(any())
    repository.findAll()
}

```
```kotlin
repository.findById("1")
repository.findById("2")
repository.findAll()

verifySuspend(order) {
    // Verification fails - findById(any()) does not occur after `findAll()`
    repository.findAll()
    repository.findById(any())
}
```

#### Exhaustive order

`VerifyMode.exhaustiveOrder` verifies that all calls occurred in the exact same way. No extra calls are allowed beyond
what is specified for verification:

```kotlin
repository.findById("1")
repository.findById("2")
repository.findAll()
verifySuspend(exhaustiveOrder) {
    // Verification passes - each call matches
    repository.findById(any())
    repository.findById(any())
    repository.findAll()
}
```

### Mocking and verifying limitations

To ensure that `every` and `verify` work as expected, compiler plugin transforms the code inside their blocks. This 
transformation currently restricts those blocks from extracting their parts into separate functions. It also dictates that block 
parameter must always be a lambda expression (not function reference nor lambda assigned to a variable).

However, it is perfectly fine to extract whole `verify` or `every` call to separate function.

### Regular matchers

Matchers are quite straightforward to use. Instead of literal argument, you have to pass a matcher. You can use named
parameters and change their order. Mixing matchers and literal arguments is also allowed. The only limitation is that 
you **must not** assign matchers to variables.

Full list of matchers with documentation is available [here](https://www.mokkery.dev/mokkery-runtime/dev.mokkery.matcher/-arg-matchers-scope/index.html).

### Logical matchers

Logical matchers allows combining regular matchers into logical expressions. 

```kotlin
everySuspend { repository.findById(or(eq("1"), eq("2"))) } returns stubBook()
```

Full list of logical matchers is available [here](https://www.mokkery.dev/mokkery-runtime/dev.mokkery.matcher.logical/index.html).

> **Warning**
>You must not use literals with logical matchers. Only matchers allowed!

### Vararg matchers

To match a method with varargs you can use regular matchers:

```kotlin
everySuspend { repository.findAllById("1", any(), "3") } returns emptyList()
```

The problem with regular matchers here is that the number of varargs is always fixed. Answer definition above works only
for calls with "1" at index 0, any arg at index 1 and "3" at index 2.

To solve this problem you can use wildcard matchers:

```kotlin
everySuspend { repository.findAllById("1", *anyVarargs(), "3") } returns emptyList()
```

Now all `findAllById` calls with "1" as the first argument and "3" as the last argument return an empty list.

You can apply restrictions with wildcard matchers using `varargsAny` and `varargsAll`:

```kotlin
everySuspend { repository.findAllById("1", *varargsAll { it != "2" }, "3") } returns emptyList()

repository.findAllById("1", "3", "3", "3") // returns empty list
repository.findAllById("1", "2", "3", "3") // fails - method not mocked
```

Since `1.3.0` wildcard vararg matchers can be used with composite matchers (e.g. [logical matchers](#logical-matchers)):

```
everySuspend { repository.findAllById(*anyVarargs()) } returns listOf(stubBook("1"))
everySuspend { repository.findAllById(*not(varargsAll { it == "2" })) } returns listOf(stubBook("2"))

resporitory.findAllById("2", "2", "2") // returns listOf(stubBook("1"))
resporitory.findAllById("1", "2", "2") // returns listOf(stubBook("2"))
```

#### Varargs ambiguity

If you pass varargs as array, it might sometimes lead to ambiguity. Calls presented below are prohibited:

```kotlin
everySuspend { repository.findAllById(ids = arrayOf("1", *anyVarargs(), "3")) } returns emptyList()
everySuspend { repository.findAllById(ids = arrayOf("1", any())) } returns emptyList()
```
If you have to pass varargs as arrays make sure that you don't mix matchers with literals. Calls presented below are allowed:

```kotlin
everySuspend { repository.findAllById(ids = arrayOf(eq("1"), *anyVarargs(), eq("3"))) } returns emptyList()
everySuspend { repository.findAllById(ids = arrayOf(eq("1"), any())) } returns emptyList()
```

### Custom matchers

The most straightforward way to define a custom matcher is by defining an extension on `ArgMatchersScope`:

```kotlin
// only for strings
fun ArgMatchersScope.regex(
    regex: Regex
): String = matching(toString = { "regex($regex)" }, predicate = regex::matches)

// for any type
inline fun <reified T> ArgMatchersScope.eqAnyOf(
    vararg values: T
): T = matching(
    toString = { "eqAnyOf(${values.contentToString()})" },
    predicate = { values.contains(it) }
)
```

It is possible to use `matching` as anonymous matcher directly.

You can also implement [ArgMatcher](https://www.mokkery.dev/mokkery-runtime/dev.mokkery.matcher/-arg-matcher/index.html) 
and pass its instance as an argument to [ArgMatchersScope.matches](https://www.mokkery.dev/mokkery-runtime/dev.mokkery.matcher/matches.html) method.

### Arguments capturing

Arguments capturing allows accessing arguments passed to mocks:

```kotlin
val slot = Capture.slot<String>() // stores only the latest value
everySuspend { repository.findById(capture(slot)) } returns stubBook()

repository.findById("1")

println(slot.get()) // prints "1"
```

By default `capture` matches any argument. You can change it by providing a different matcher:

```kotlin
val slot = Capture.slot<String>()
everySuspend { repository.findById(capture(slot, neq("1"))) } returns stubBook()

repository.findById("2")

println(slot.get()) // prints "2"

repository.findById("1") // fails - no answer provided for arg "1"
```

Argument capture occurs only if given definition is actually used to provide an answer for a call:

```kotlin

val container = Capture.container<String>() // stores multiple values

everySuspend { repository.findByName(query = any(), limit = any()) } returns listOf(stubBook())
everySuspend { repository.findByName(query = capture(container), limit = eq(10)) } returns listOf(stubBook())
everySuspend { repository.findByName(query = eq("Book 3"), limit = any()) } returns listOf(stubBook())

repository.findByName(query = "Book 1", limit = 10) // `query` arg is captured
repository.findByName(query = "Book 2", limit = 20) // `limit` parameter does not match - argument is not captured
repository.findByName(query = "Book 3", limit = 10) // answer defined later is selected here - argument is not captured

println(container.values) // prints ["Book 1"]
```

Since `1.3.0` argument capturing works with vararg matchers (including wildcard matchers):

```
val slot = Capture.slot<Array<String>>()

everySuspend { repository.findAllById(*capture(slot, anyVarargs())) } returns listOf(stubBook("1"))

resporitory.findAllById("1", "2", "3")
// slot contains arrayOf("1", "2", "3")
```

For positional vararg matchers syntax is exactly the same as for regular matchers.

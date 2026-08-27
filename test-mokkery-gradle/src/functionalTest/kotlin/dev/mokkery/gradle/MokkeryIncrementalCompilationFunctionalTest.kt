package dev.mokkery.gradle

import dev.mokkery.internal.MokkeryConfig
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.io.File
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MokkeryIncrementalCompilationFunctionalTest {

    @field:TempDir
    private lateinit var projectDir: File

    private val jvm = Target.Jvm

    @Test
    fun `recompiles every mock of a type that gained a member on the current Kotlin version`() {
        val build = jvm.project(MokkeryConfig.COMPILED_KOTLIN_VERSION)
        build.build(jvm.testTask)

        build.file(jvm.mainSource("Shared"), sharedInterface(extraMember = addedMember))
        build.file(jvm.testSource("SharedMockTest"), sharedMockTest(callsAddedMember = true))
        val result = build.build(jvm.testCompilation)

        assertEquals(
            jvm.mockUsers + jvm.testSource("SharedMockTest"),
            result.compiledSources(jvm.testCompilation),
        )

        build.build(jvm.testTask)
    }

    @ParameterizedTest
    @EnumSource(Target::class)
    fun `recompiles every mock of a type that gained a member`(target: Target) {
        val build = target.project()
        build.build(target.testTask)

        build.file(target.mainSource("Shared"), sharedInterface(extraMember = addedMember))
        build.file(target.testSource("SharedMockTest"), sharedMockTest(callsAddedMember = true))
        val result = build.build(target.testCompilation)

        assertEquals(setOf(target.mainSource("Shared")), result.compiledSources(target.mainCompilation))
        assertEquals(
            target.mockUsers + target.testSource("SharedMockTest"),
            result.compiledSources(target.testCompilation),
        )

        build.build(target.testTask)
    }

    @ParameterizedTest
    @EnumSource(Target::class)
    fun `recompiles every mock of a type whose supertype gained a member`(target: Target) {
        val build = target.project()
        build.file(target.mainSource("Base"), baseInterface(extraMember = ""))
        build.file(target.mainSource("Shared"), sharedInterface(extraMember = "", superType = "Base"))
        build.build(target.testTask)

        build.file(target.mainSource("Base"), baseInterface(extraMember = addedMember))
        build.file(target.testSource("SharedMockTest"), sharedMockTest(callsAddedMember = true))
        val result = build.build(target.testCompilation)

        assertTrue(result.compiledSources(target.testCompilation).containsAll(target.mockUsers))

        build.build(target.testTask)
    }

    @ParameterizedTest
    @EnumSource(Target::class)
    fun `does not recompile mocks when an unrelated file changes`(target: Target) {
        val build = target.project()
        build.build(target.testTask)

        build.file(target.mainSource("Unrelated"), unrelatedClass(value = 2))
        val result = build.build(target.testCompilation)

        assertEquals(setOf(target.mainSource("Unrelated")), result.compiledSources(target.mainCompilation))
        assertEquals(emptySet(), result.compiledSources(target.testCompilation))
    }

    @ParameterizedTest
    @EnumSource(Target::class)
    fun `does not recompile mocks of untouched types`(target: Target) {
        val build = target.project()
        build.file(target.mainSource("Other"), otherInterface(extraMember = ""))
        build.file(target.testSource("OtherMockUser"), mockUser(name = "OtherMockUser", mockedType = "Other"))
        build.build(target.testTask)

        build.file(target.mainSource("Other"), otherInterface(extraMember = addedMember))
        val result = build.build(target.testCompilation)

        assertEquals(setOf(target.testSource("OtherMockUser")), result.compiledSources(target.testCompilation))
    }

    @Test
    fun `recompiles the mockMany user when one of the mocked types gains a member`() {
        val build = jvm.project()
        build.file(jvm.mainSource("Extra"), extraInterface(extraMember = ""))
        build.file(jvm.testSource("ManyMockUser"), manyMockUser)
        build.file(jvm.testSource("ManyMockTest"), manyMockTest)
        build.build(jvm.testTask)

        build.file(jvm.mainSource("Extra"), extraInterface(extraMember = addedMember))
        val result = build.build(jvm.testCompilation)

        assertEquals(setOf(jvm.testSource("ManyMockUser")), result.compiledSources(jvm.testCompilation))

        build.build(jvm.testTask)
    }

    @Test
    fun `recompiles the mock user when a stubbed constructor parameter type gains a member`() {
        val build = jvm.project()
        build.file(jvm.mainSource("StubParam"), stubParamInterface(extraMember = ""))
        build.file(jvm.mainSource("WithStub"), withStubClass)
        build.file(jvm.testSource("StubMockUser"), mockUser(name = "StubMockUser", mockedType = "WithStub"))
        build.file(jvm.testSource("StubMockTest"), stubMockTest)
        build.build(jvm.testTask)

        build.file(jvm.mainSource("StubParam"), stubParamInterface(extraMember = addedMember))
        val result = build.build(jvm.testCompilation)

        assertEquals(setOf(jvm.testSource("StubMockUser")), result.compiledSources(jvm.testCompilation))

        build.build(jvm.testTask)
    }

    @Test
    fun `regenerates the module scope when an unrelated test source changes`() {
        val build = jvm.project()
        build.build(jvm.testTask)

        build.file(jvm.testSource("UnrelatedTest"), unrelatedTest(repeats = 2))
        val result = build.build(jvm.testCompilation)

        assertEquals(setOf(jvm.testSource("UnrelatedTest")), result.compiledSources(jvm.testCompilation))

        build.build(jvm.testTask)
    }

    @Test
    fun `keeps the remaining mocks usable when a file that mocks a type is deleted`() {
        val build = jvm.project()
        build.build(jvm.testTask)

        build.delete(jvm.testSource(mockUserNames.last()))
        build.file(jvm.testSource("SharedMockTest"), sharedMockTest(users = mockUserNames.dropLast(1)))
        val result = build.build(jvm.testCompilation)

        assertEquals(setOf(jvm.testSource("SharedMockTest")), result.compiledSources(jvm.testCompilation))

        build.build(jvm.testTask)
    }

    @Test
    fun `recompiles every mock of a type that lost a member`() {
        val build = jvm.project()
        build.build(jvm.testTask)

        build.file(jvm.mainSource("Shared"), sharedInterface(secondMember = ""))
        val result = build.build(jvm.testCompilation)

        assertEquals(jvm.mockUsers, result.compiledSources(jvm.testCompilation))

        build.build(jvm.testTask)
    }

    @Test
    fun `recompiles every mock of a type whose member changed signature`() {
        val build = jvm.project()
        build.build(jvm.testTask)

        build.file(jvm.mainSource("Shared"), sharedInterface(secondMember = "fun b(value: Int): String"))
        val result = build.build(jvm.testCompilation)

        assertEquals(jvm.mockUsers, result.compiledSources(jvm.testCompilation))

        build.build(jvm.testTask)
    }

    @Test
    fun `reports the mocking diagnostic again when the mocked type becomes final`() {
        val build = jvm.project()
        build.file(jvm.mainSource("Open"), openClass(modifier = "open "))
        build.file(jvm.testSource("OpenMockUser"), mockUser(name = "OpenMockUser", mockedType = "Open"))
        build.build(jvm.testCompilation)

        build.file(jvm.mainSource("Open"), openClass(modifier = ""))
        val result = build.buildAndFail(jvm.testCompilation)

        assertContains(result.output, "Type 'Open' is final and cannot be used with 'mock'")
    }

    @Test
    fun `keeps mocks valid across consecutive incremental rounds`() {
        val build = jvm.project()
        build.build(jvm.testTask)

        build.file(jvm.mainSource("Shared"), sharedInterface(extraMember = addedMember))
        build.file(jvm.testSource("SharedMockTest"), sharedMockTest(callsAddedMember = true))
        build.build(jvm.testTask)

        build.file(jvm.mainSource("Shared"), sharedInterface())
        build.file(jvm.testSource("SharedMockTest"), sharedMockTest())
        val result = build.build(jvm.testCompilation)

        assertEquals(
            jvm.mockUsers + jvm.testSource("SharedMockTest"),
            result.compiledSources(jvm.testCompilation),
        )

        build.build(jvm.testTask)
    }

    @Test
    fun `does not recompile mocks when only a member body changes`() {
        val build = jvm.project()
        build.file(jvm.mainSource("Shared"), sharedInterface(extraMember = defaultMember(value = 1)))
        build.build(jvm.testTask)

        build.file(jvm.mainSource("Shared"), sharedInterface(extraMember = defaultMember(value = 2)))
        val result = build.build(jvm.testCompilation)

        assertEquals(setOf(jvm.mainSource("Shared")), result.compiledSources(jvm.mainCompilation))
        assertEquals(emptySet(), result.compiledSources(jvm.testCompilation))
    }

    private fun Target.project(kotlinVersion: String = MokkeryConfig.MINIMUM_KOTLIN_VERSION) = incrementalBuild(projectDir) {
        settings(kotlinPlugin, projectName = "ic-test", kotlinVersion = kotlinVersion)
        file("build.gradle.kts", buildScript)
        file(mainSource("Shared"), sharedInterface())
        file(mainSource("Unrelated"), unrelatedClass(value = 1))
        file(testSource("UnrelatedTest"), unrelatedTest(repeats = 1))
        file(testSource("SharedMockTest"), sharedMockTest())
        mockUserNames.forEach { file(testSource(it), mockUser(name = it, mockedType = "Shared")) }
    }
}

enum class Target(
    val kotlinPlugin: String,
    val buildScript: String,
    private val mainDir: String,
    private val testDir: String,
    val mainCompilation: String,
    val testCompilation: String,
    val testTask: String,
) {
    Jvm(
        kotlinPlugin = "jvm",
        buildScript = jvmBuildScript,
        mainDir = "src/main/kotlin",
        testDir = "src/test/kotlin",
        mainCompilation = ":compileKotlin",
        testCompilation = ":compileTestKotlin",
        testTask = ":test",
    ),
    Js(
        kotlinPlugin = "multiplatform",
        buildScript = jsBuildScript,
        mainDir = "src/jsMain/kotlin",
        testDir = "src/jsTest/kotlin",
        mainCompilation = ":compileKotlinJs",
        testCompilation = ":compileTestKotlinJs",
        testTask = ":jsNodeTest",
    );

    val mockUsers get() = mockUserNames.map { testSource(it) }.toSet()

    fun mainSource(name: String) = "$mainDir/$packageDir/$name.kt"

    fun testSource(name: String) = "$testDir/$packageDir/$name.kt"
}

private const val packageName = "dev.mokkery.ic"
private const val packageDir = "dev/mokkery/ic"
private const val addedMember = "fun added(value: Double): Boolean"

private val mockUserNames = listOf("MockUserA", "MockUserB", "MockUserC")

@Language("kts")
private val jvmBuildScript = """
    plugins {
        kotlin("jvm")
        id("dev.mokkery")
    }

    dependencies {
        testImplementation(kotlin("test"))
    }

    tasks.test {
        useJUnitPlatform()
    }
"""

@Language("kts")
private val jsBuildScript = """
    plugins {
        kotlin("multiplatform")
        id("dev.mokkery")
    }

    kotlin {
        js(IR) { nodejs() }
        sourceSets.getByName("jsTest").dependencies {
            implementation(kotlin("test"))
        }
    }
"""

private fun sharedInterface(
    secondMember: String = "fun b(text: String): String",
    extraMember: String = "",
    superType: String? = null,
) = """
    package $packageName

    interface Shared${superType?.let { " : $it" }.orEmpty()} {
        fun a(): Int
        $secondMember
        $extraMember
    }
"""

private fun defaultMember(value: Int) = "fun withDefault(): Int = $value"

private fun extraInterface(extraMember: String) = """
    package $packageName

    interface Extra {
        fun extra(): Int
        $extraMember
    }
"""

private fun stubParamInterface(extraMember: String) = """
    package $packageName

    interface StubParam {
        fun param(): Int
        $extraMember
    }
"""

private val withStubClass = """
    package $packageName

    abstract class WithStub(param: StubParam) {

        abstract fun call(): Int
    }
"""

private fun openClass(modifier: String) = """
    package $packageName

    ${modifier}class Open {

        ${modifier}fun value(): Int = 1
    }
"""

private fun baseInterface(extraMember: String) = """
    package $packageName

    interface Base {
        fun base(): Int
        $extraMember
    }
"""

private fun otherInterface(extraMember: String) = """
    package $packageName

    interface Other {
        fun other(): Int
        $extraMember
    }
"""

private fun unrelatedClass(value: Int) = """
    package $packageName

    class Unrelated {
        fun value(): Int = $value
    }
"""

private fun mockUser(name: String, mockedType: String) = """
    package $packageName

    import dev.mokkery.mock

    class $name {
        val mock: $mockedType = mock()
    }
"""

private val manyMockUser = """
    package $packageName

    import dev.mokkery.mockMany

    class ManyMockUser {
        val mock = mockMany<Shared, Extra>()
    }
"""

private val manyMockTest = """
    package $packageName

    import dev.mokkery.answering.returns
    import dev.mokkery.every
    import dev.mokkery.t1
    import dev.mokkery.t2
    import kotlin.test.Test
    import kotlin.test.assertEquals

    class ManyMockTest {

        @Test
        fun callsMock() {
            val mock = ManyMockUser().mock
            every { mock.t1.a() } returns 1
            every { mock.t2.extra() } returns 2
            assertEquals(1, mock.t1.a())
            assertEquals(2, mock.t2.extra())
        }
    }
"""

private val stubMockTest = """
    package $packageName

    import dev.mokkery.answering.returns
    import dev.mokkery.every
    import kotlin.test.Test
    import kotlin.test.assertEquals

    class StubMockTest {

        @Test
        fun callsMock() {
            val mock = StubMockUser().mock
            every { mock.call() } returns 3
            assertEquals(3, mock.call())
        }
    }
"""

private fun unrelatedTest(repeats: Int) = """
    package $packageName

    import kotlin.test.Test
    import kotlin.test.assertEquals

    class UnrelatedTest {

        @Test
        fun usesUnrelated() {
            repeat($repeats) {
                assertEquals(Unrelated().value(), Unrelated().value())
            }
        }
    }
"""

private fun sharedMockTest(callsAddedMember: Boolean = false, users: List<String> = mockUserNames) = """
    package $packageName

    import dev.mokkery.answering.returns
    import dev.mokkery.every
    import dev.mokkery.matcher.any
    import kotlin.test.Test
    import kotlin.test.assertEquals

    class SharedMockTest {

        @Test
        fun callsMocks() {
            listOf(${users.joinToString { "$it().mock" }}).forEach { mock ->
                ${if (callsAddedMember) addedMemberCall else existingMemberCall}
            }
        }
    }
"""

private val existingMemberCall = """
                every { mock.a() } returns 1
                assertEquals(1, mock.a())
"""

private val addedMemberCall = """
                every { mock.added(any()) } returns true
                assertEquals(true, mock.added(1.0))
"""

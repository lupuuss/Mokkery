package dev.mokkery.gradle

import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

abstract class BaseMokkeryGradleTest {

    @TempDir
    lateinit var projectPath: Path
    @TempDir
    lateinit var repoPath: Path
}

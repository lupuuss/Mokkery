<div align="center">
    <a href="https://mokkery.dev">
        <img src="./website/static/img/logo-github.png" alt="Mokkery" />
    </a>
</div>

</br>
</br>

[![Gradle Plugin Portal Stable](https://img.shields.io/gradle-plugin-portal/v/dev.mokkery)](https://plugins.gradle.org/plugin/dev.mokkery)
[![Kotlin](https://img.shields.io/badge/kotlin-2.1.21-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![GitHub](https://img.shields.io/github/license/lupuuss/Mokkery)](https://github.com/lupuuss/Mokkery/blob/main/LICENSE)
[![Docs](https://img.shields.io/static/v1?label=api&message=reference&labelColor=gray&color=blueviolet&logo=gitbook&logoColor=white)](https://mokkery.dev/api_reference)

The mocking library for Kotlin Multiplatform, easy to use, boilerplate-free and compiler plugin driven.

```kotlin
class BookServiceTest {

    val repository = mock<BookRepository> {
        everySuspend { findById(any()) } calls { (id: String) -> Book(id) }
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
}
```

As shown in the example above, this library is highly inspired by the [MockK](https://mockk.io).
If you have any experience with MockK, it should be easy to start with Mokkery!

### [Documentation is available here!](https://mokkery.dev/)

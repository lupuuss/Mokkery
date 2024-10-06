object MokkeryAttributes {

    const val GitBase = "github.com/lupuuss/Mokkery"
    const val GitHttpsUrl = "https://$GitBase"
    const val GitVscUrl = "${GitHttpsUrl}.git"
    const val GitConnectionUrl = "scm:git:git://$GitBase.git"
    const val GitDevConnectionUrl = "scm:git:ssh://git@$GitBase.git"
    const val GitIssuesUrl = "$GitHttpsUrl/issues"

    const val WebsiteUrl = "https://mokkery.dev"

    const val DisplayName = "Mokkery"
    const val Description = "Mokkery is a mocking library for Kotlin Multiplatform, easy to use, boilerplate-free and compiler plugin driven."
}

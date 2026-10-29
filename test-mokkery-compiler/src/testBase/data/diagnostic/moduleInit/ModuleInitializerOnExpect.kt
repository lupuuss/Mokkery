// LANGUAGE: +MultiPlatformProjects

import dev.mokkery.annotations.MokkeryModuleInit
import dev.mokkery.configurer.MokkeryConfigurer

@MokkeryModuleInit
expect fun foo()

@MokkeryModuleInit
expect fun bar(configurer: MokkeryConfigurer)

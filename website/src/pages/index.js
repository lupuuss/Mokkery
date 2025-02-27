import clsx from 'clsx';
import Link from '@docusaurus/Link';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import Layout from '@theme/Layout';
import CodeBlock from '@theme/CodeBlock';
import Heading from '@theme/Heading';
import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';
import {kotlinVersion, mokkeryVersion} from "../versions";
import Logo from './logo.svg'

const simpleTabBlock = `class BookServiceTest {

    val repository = mock<BookRepository> {
        everySuspend { findById(any()) } calls { (id: String) -> Book(id) }
    }
    val service = BookService(repository)

    @Test
    fun \`rent should call repository for each book\`() = runTest {
        service.rentAll(listOf("1", "2"))
        verifySuspend(exhaustiveOrder) {
            repository.findById("1")
            repository.findById("2")
        }
    }
}`
const setupTabBlockK1 = `plugins {
    kotlin("multiplatform") version "1.9.25" // ...or any other Kotlin plugin
    id("dev.mokkery") version "1.9.25-1.7.0"
}
`
const setupTabBlockK2 = `plugins {
    kotlin("multiplatform") version "${kotlinVersion}" // ...or any other Kotlin plugin
    id("dev.mokkery") version "${mokkeryVersion}"
}
`

const extensibleMatchersTabBlock = `// For any type!
inline fun <reified T : List<*>> ArgMatchersScope.hasSize(size: Int): T = matching(
    toString = { "hasSize($size)" }, // prettify its presence!
    predicate = { it.size == size }
)

// By function reference!
fun ArgMatchersScope.isNotEmpty(): String = matchingBy(String::isNotEmpty)
`
const extensilbeAnswersTabBlock = `// Custom answer for blocking functions!
fun BlockingAnsweringScope<Int>.randomInt() = calls { Random.nextInt() }

// Custom answer for suspending functions!
infix fun <T> SuspendAnsweringScope<T>.returnsDelayed(value: T) = calls {
    delay(1_000)
    value
}`

const customizableGloballyTabBlock = `mokkery {
    defaultMockMode.set(autoUnit)
    defaultVerifyMode.set(exhaustiveOrder)
}
`
const customizableLocallyTabBlock = `class FooTest {
    private val bar = mock<Bar>(autoUnit)
    // ...
    @Test
    fun fooTest() {
        // ...
        verify (exhaustiveOrder) { /* ... */ }
    }
}
`

export default function Home() {
    const {siteConfig} = useDocusaurusContext();
    return (
        <Layout description={siteConfig.tagline}>
            <HomepageHeader/>
            <main><WhyMokkeryTabs/></main>
        </Layout>
    );
}

function HomepageHeader() {
    const {siteConfig} = useDocusaurusContext();
    return (
        <div className={clsx('gradient', 'main-page')}>
            <div className="main-page-description">
                <div style={{display: "flex", flexDirection: "row", alignItems: "center", gap: "16px"}}>
                    <Logo width="100" height="100" className="logoTheme"/>
                    <Heading as="h1" className="hero__title" style={{letterSpacing: "-2px", marginTop: "10px" }}>
                        {siteConfig.title}
                    </Heading>
                </div>
                <p className="hero__subtitle" style={{lineHeight: "1.2", fontSize: '2rem' }}>{siteConfig.tagline}</p>
                <div style={{
                    display: "flex",
                    flexDirection: "row",
                    gap: "2rem",
                    alignItems: "center",
                    flexWrap: "wrap"
                }}>
                    <Link
                        data-umami-event="home-get-started-button"
                        className="button button--secondary button--lg"
                        to="../docs/Setup">
                        Get started!
                    </Link>
                    <iframe
                        src="https://ghbtns.com/github-btn.html?user=lupuuss&repo=mokkery&type=star&count=true&size=large"
                        frameBorder="0"
                        scrolling="0"
                        width="170"
                        height="30"
                        title="GitHub">
                    </iframe>
                </div>
            </div>
        </div>
    );
}

function WhyMokkeryTabs() {
    return (
        <div className="container" style={{maxWidth: "900px"}}>
            <div style={{height: "2rem"}}/>
            <h2 style={{fontWeight: "400"}}>Why Mokkery?</h2>
            <div style={{minHeight: "500px"}}>
                <Tabs>
                    <TabItem value="simple" label="🌿&nbsp;Simple" data-umami-event="home-tabs" data-umami-event-tab="simple">
                        <CodeBlock
                            language="kotlin"
                            showLineNumbers>
                            {simpleTabBlock}
                        </CodeBlock>
                    </TabItem>
                    <TabItem value="setup" label="⌚&nbsp;Easy&nbsp;setup" data-umami-event="home-tabs" data-umami-event-tab="setup">
                        <h3 style={{fontWeight: "400"}}>Just apply Gradle plugin and...</h3>
                        <Tabs
                            groupId="kotlinVersion"
                            defaultValue="k2"
                            values={[
                                {label: 'K1', value: 'k1'},
                                {label: 'K2', value: 'k2'},
                            ]}>
                            <TabItem value="k1">
                                <CodeBlock
                                    language="kotlin"
                                    showLineNumbers>
                                    {setupTabBlockK1}
                                    </CodeBlock>
                            </TabItem>
                            <TabItem value="k2">
                                <CodeBlock
                                    language="kotlin"
                                    showLineNumbers>
                                    {setupTabBlockK2}
                                    </CodeBlock>
                            </TabItem>
                            </Tabs>
                        <h3 style={{fontWeight: "400"}}>...that's it!</h3>
                        <h3 style={{fontWeight: "400"}}>
                            Please refer to the <Link to="../docs/Setup">setup section</Link>, as additional configuration may be required in some cases!
                        </h3>
                    </TabItem>
                    <TabItem value="multiplatform" label="🌍&nbsp;Multiplatform" data-umami-event="home-tabs" data-umami-event-tab="multiplatform">
                        <div style={{fontSize: "1.4rem", fontWeight: "300", width: "100%"}}>
                            <div style={{height: "1rem"}}/>
                            <ul>
                                <li>☕ JVM & Android</li>
                                <li>🔧 All Kotlin Native targets</li>
                                <li>🌐 JS (Browser, Node.js)</li>
                                <li>🧪 Wasm-JS (Browser, Node.js)</li>
                                <li>🧪 Wasm-WASI (Node.js)</li>
                            </ul>
                        </div>
                    </TabItem>
                    <TabItem value="customizable" label="🖌️&nbsp;Customizable" data-umami-event="home-tabs" data-umami-event-tab="customizable">
                        <h3 style={{fontWeight: "400"}}>Change Mokkery strictness globally...</h3>
                        <CodeBlock
                            title="build.gradle.kts"
                            language="kotlin"
                            showLineNumbers>
                            {customizableGloballyTabBlock}
                        </CodeBlock>
                        <h3 style={{fontWeight: "400"}}>...or locally!</h3>
                        <CodeBlock
                            title="FooTest.kt"
                            language="kotlin"
                            showLineNumbers>
                            {customizableLocallyTabBlock}
                        </CodeBlock>
                    </TabItem>
                    <TabItem value="extensible" label="🧩&nbsp;Extensible" data-umami-event="home-tabs" data-umami-event-tab="extensible">
                        <h3 style={{fontWeight: "400"}}>Custom matchers!</h3>
                        <CodeBlock
                            language="kotlin"
                            showLineNumbers>
                            {extensibleMatchersTabBlock}
                        </CodeBlock>
                        <h3 style={{fontWeight: "400"}}>Custom answers!</h3>
                        <CodeBlock
                            language="kotlin"
                            showLineNumbers>
                            {extensilbeAnswersTabBlock}
                        </CodeBlock>
                    </TabItem>
                </Tabs>
            </div>
        </div>
    )
}

import Link from '@docusaurus/Link';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import Layout from '@theme/Layout';
import CodeBlock from '@theme/CodeBlock';
import Heading from '@theme/Heading';
import React, {useEffect, useState} from 'react';
import {Highlight, themes} from 'prism-react-renderer';
import {kotlinVersion, mokkeryVersion} from "../versions";
import Logo from './logo.svg'

const heroBlock = `class BookServiceTest {

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
const pluginDeclaration = `id("dev.mokkery") version "${mokkeryVersion}"`
const simpleBlock = `val repository = mock<BookRepository>()
every { repository.findById("1") } returns Book("1")

repository.findById("1")

verify { repository.findById("1") }`
const setupBlock = `plugins {
    kotlin("multiplatform") version "${kotlinVersion}" // ...or any other Kotlin plugin
    ${pluginDeclaration}
}`
const customizableGloballyBlock = `mokkery {
    defaultMockMode.set(autoUnit)
    defaultVerifyMode.set(exhaustiveOrder)
}`
const customizableLocallyBlock = `class FooTest {
    private val bar = mock<Bar>(autoUnit)
    // ...
    @Test
    fun fooTest() {
        // ...
        verify(exhaustiveOrder) { /* ... */ }
    }
}`
const extensibleBlock = `// For any type!
fun <T : List<*>> MokkeryMatcherScope.hasSize(size: Int): T = matches(
    toString = { "hasSize($size)" }, // prettify its presence!
    predicate = { it.size == size }
)

// Custom answer for suspending functions!
infix fun <T> SuspendAnsweringScope<T>.returnsDelayed(value: T) = calls {
    delay(1_000)
    value
}`

const icons = {
    simple: <path d="M6 20c0-8 4-13 13-14-1 9-6 13-13 14zM6 20l7-7"/>,
    setup: <><circle cx="12" cy="12" r="8"/><path d="M12 8v4l3 2"/></>,
    multiplatform: <><circle cx="12" cy="12" r="8"/><path d="M4 12h16M12 4c2.5 2.5 3.5 5 3.5 8s-1 5.5-3.5 8c-2.5-2.5-3.5-5-3.5-8s1-5.5 3.5-8z"/></>,
    customizable: <><path d="M4 7h10M18 7h2M4 17h2M10 17h10"/><circle cx="16" cy="7" r="2"/><circle cx="8" cy="17" r="2"/></>,
    extensible: <path d="M10 4h4v3a2 2 0 1 0 4 0V4h2v6h-3a2 2 0 1 0 0 4h3v6h-6v-3a2 2 0 1 0-4 0v3H4v-6h3a2 2 0 1 0 0-4H4V4z"/>,
    arrow: <path d="M5 12h14M13 6l6 6-6 6"/>,
    star: <path d="M12 3l2.8 5.7 6.2.9-4.5 4.4 1.1 6.2L12 17.3 6.4 20.2l1.1-6.2L3 9.6l6.2-.9z"/>,
    copy: <><rect x="9" y="9" width="11" height="11" rx="2"/><path d="M5 15V6a2 2 0 0 1 2-2h8"/></>,
    check: <path d="M5 12.5l4.5 4.5L19 7.5"/>,
}

const features = [
    {id: 'simple', label: 'Simple'},
    {id: 'setup', label: 'Easy setup'},
    {id: 'multiplatform', label: 'Multiplatform'},
    {id: 'customizable', label: 'Customizable'},
    {id: 'extensible', label: 'Extensible'},
]

function Icon({name, size = 16}) {
    return (
        <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"
             strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
            {icons[name]}
        </svg>
    );
}

export default function Home() {
    const {siteConfig} = useDocusaurusContext();
    return (
        <Layout description={siteConfig.tagline}>
            <HomepageHeader/>
            <main className="home-main"><WhyMokkery/></main>
        </Layout>
    );
}

function HomepageHeader() {
    const {siteConfig} = useDocusaurusContext();
    return (
        <header className="home-hero-bg">
            <div className="home-hero-blobs" aria-hidden="true">
                <span/><span/><span/><span/><span/>
            </div>
            <div className="home-hero">
                <div className="home-hero-text">
                    <span className="home-hero-pill">Mokkery {mokkeryVersion} · Kotlin {kotlinVersion}</span>
                    <div className="home-hero-title">
                        <Logo width="66" height="76" className="home-hero-logo"/>
                        <Heading as="h1">{siteConfig.title}</Heading>
                    </div>
                    <p className="home-hero-tagline">{siteConfig.tagline}</p>
                    <div className="home-hero-buttons">
                        <Link
                            data-umami-event="home-get-started-button"
                            className="home-button home-button--primary"
                            to="/docs/Setup">
                            Get started <Icon name="arrow" size={18}/>
                        </Link>
                        <GitHubStarButton/>
                    </div>
                    <InstallLine/>
                </div>
                <EditorWindow fileName="BookServiceTest.kt" code={heroBlock}/>
            </div>
        </header>
    );
}

function GitHubStarButton() {
    const [stars, setStars] = useState(null);
    useEffect(() => {
        fetch('https://api.github.com/repos/lupuuss/Mokkery')
            .then(response => response.ok ? response.json() : null)
            .then(repo => repo && setStars(repo.stargazers_count))
            .catch(() => {});
    }, []);
    return (
        <Link
            data-umami-event="home-github-button"
            className="home-button home-button--outline"
            to="https://github.com/lupuuss/Mokkery">
            <Icon name="star" size={18}/> {stars === null ? 'Star on GitHub' : `${stars} on GitHub`}
        </Link>
    );
}

function InstallLine() {
    const [copied, setCopied] = useState(false);
    const copy = () => {
        navigator.clipboard.writeText(pluginDeclaration).then(() => {
            setCopied(true);
            setTimeout(() => setCopied(false), 1500);
        });
    };
    return (
        <div className="home-install">
            <code>{pluginDeclaration}</code>
            <button
                type="button"
                data-umami-event="home-copy-plugin"
                aria-label="Copy plugin declaration"
                onClick={copy}>
                <Icon name={copied ? 'check' : 'copy'}/>
            </button>
        </div>
    );
}

function EditorWindow({fileName, code}) {
    return (
        <div className="home-editor">
            <div className="home-editor-bar">
                <span/><span/><span/>
                <span className="home-editor-file">{fileName}</span>
            </div>
            <Highlight code={code} language="kotlin" theme={themes.oneDark}>
                {({tokens, getLineProps, getTokenProps}) => (
                    <pre className="home-editor-code">
                        {tokens.map((line, i) => (
                            <div key={i} {...getLineProps({line})}>
                                {line.map((token, key) => <span key={key} {...getTokenProps({token})}/>)}
                            </div>
                        ))}
                    </pre>
                )}
            </Highlight>
        </div>
    );
}

function Feature({id, title, description, children}) {
    return (
        <section id={id} className="home-feature">
            <Heading as="h3">{title}</Heading>
            <p>{description}</p>
            {children}
        </section>
    );
}

function useActiveFeature() {
    const [active, setActive] = useState(null);
    useEffect(() => {
        const observer = new IntersectionObserver(
            entries => entries
                .filter(entry => entry.isIntersecting)
                .forEach(entry => setActive(entry.target.id)),
            {rootMargin: '-35% 0px -60% 0px'}
        );
        features.forEach(({id}) => observer.observe(document.getElementById(id)));
        return () => observer.disconnect();
    }, []);
    return active;
}

function scrollToFeature(event, id) {
    event.preventDefault();
    const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    document.getElementById(id).scrollIntoView({behavior: reduceMotion ? 'auto' : 'smooth'});
    history.replaceState(null, '', `#${id}`);
}

function FeatureNav() {
    const active = useActiveFeature();
    return (
        <nav aria-label="Why Mokkery" className="home-chips-bar">
            <div className="home-chips">
                {features.map(({id, label}) => (
                    <a
                        key={id}
                        href={`#${id}`}
                        aria-current={active === id ? 'true' : undefined}
                        className={active === id ? 'home-chip home-chip--active' : 'home-chip'}
                        data-umami-event={`home-chip-${id}`}
                        onClick={event => scrollToFeature(event, id)}>
                        <Icon name={id}/>{label}
                    </a>
                ))}
            </div>
        </nav>
    );
}

function WhyMokkery() {
    return (
        <div>
            <div className="home-why home-why-intro">
                <Heading as="h2">Why Mokkery?</Heading>
            </div>
            <FeatureNav/>
            <div className="home-why">
                <Feature
                    id="simple"
                    title="Simple"
                    description="Create a mock, define what it returns, check what was called. Suspend functions work the same way with everySuspend and verifySuspend.">
                    <CodeBlock language="kotlin">{simpleBlock}</CodeBlock>
                </Feature>
                <Feature
                    id="setup"
                    title="Easy setup"
                    description="Just apply the Gradle plugin and that's it!">
                    <CodeBlock language="kotlin" title="build.gradle.kts">{setupBlock}</CodeBlock>
                    <p className="home-note">
                        Android, custom source sets or K1? See the <Link to="/docs/Setup">setup section</Link>.
                    </p>
                </Feature>
                <Feature
                    id="multiplatform"
                    title="Multiplatform"
                    description="Mocks are generated at compile time, so the same tests run on every target.">
                    <ul className="home-targets">
                        <li>JVM & Android</li>
                        <li>All Kotlin Native targets</li>
                        <li>JS (Browser, Node.js)</li>
                        <li>Wasm-JS (Browser, Node.js)</li>
                        <li>Wasm-WASI (Node.js)</li>
                    </ul>
                </Feature>
                <Feature
                    id="customizable"
                    title="Customizable"
                    description="Change Mokkery strictness globally... or locally!">
                    <CodeBlock language="kotlin" title="build.gradle.kts">{customizableGloballyBlock}</CodeBlock>
                    <CodeBlock language="kotlin" title="FooTest.kt">{customizableLocallyBlock}</CodeBlock>
                </Feature>
                <Feature
                    id="extensible"
                    title="Extensible"
                    description="Custom matchers and answers are plain extension functions.">
                    <CodeBlock language="kotlin">{extensibleBlock}</CodeBlock>
                    <p className="home-note">
                        Everything else is in the <Link to="/docs/Setup">documentation</Link>.
                    </p>
                </Feature>
                <p className="home-maintainer">
                    Mokkery is maintained by one person. If it saves you time,
                    consider <Link to="https://ko-fi.com/lupuuss" data-umami-event="home-kofi-link">supporting it</Link>.
                </p>
            </div>
        </div>
    );
}

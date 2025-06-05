// @ts-check
// `@type` JSDoc annotations allow editor autocompletion and type checking
// (when paired with `@ts-check`).
// There are various equivalent ways to declare your Docusaurus config.
// See: https://docusaurus.io/docs/api/docusaurus-config

import {themes as prismThemes} from 'prism-react-renderer';

const tagLine = 'The mocking library for Kotlin Multiplatform, easy to use, boilerplate-free and compiler plugin driven.'

/** @type {import('@docusaurus/types').Config} */
const config = {
    title: 'Mokkery',
    tagline: tagLine,
    headTags: [
        {
            tagName: 'link',
            attributes: {
                rel: 'icon',
                type: 'image/png',
                href: '/img/favicon.png',
            },
        },
        {
            tagName: 'link',
            attributes: {
                rel: 'icon',
                type: 'image/svg+xml',
                href: '/img/favicon.svg',
            },
        }
    ],
    url: 'https://mokkery.dev',
    baseUrl: '/',
    organizationName: 'lupuuss',
    projectName: 'Mokkery',
    onBrokenLinks: 'throw',
    onBrokenMarkdownLinks: 'warn',
    customFields: {description: tagLine},
    i18n: {
        defaultLocale: 'en',
        locales: ['en'],
    },
    presets: [
        [
            'classic',
            /** @type {import('@docusaurus/preset-classic').Options} */
            ({
                docs: {
                    sidebarPath: './sidebars.js',
                    editUrl: 'https://github.com/lupuuss/Mokkery/edit/master/website'
                },
                theme: {
                    customCss: './src/css/custom.css',
                },
            }),
        ],
    ],
    themeConfig: /** @type {import('@docusaurus/preset-classic').ThemeConfig} */ ({

            image: 'img/logo-social.jpg',
            colorMode: {
                defaultMode: 'dark'
            },
            navbar: {
                title: 'Mokkery',
                hideOnScroll: true,
                logo: {
                    alt: 'Mokkery logo',
                    src: 'img/logo_purple.svg',
                    srcDark: 'img/logo_white.svg',
                },
                items: [
                    {
                        type: 'docSidebar',
                        sidebarId: 'docs',
                        position: 'left',
                        label: 'Docs',
                        'data-umami-event': 'header-docs'
                    },
                    {
                        href: 'pathname:///api_reference',
                        position: 'left',
                        label: 'API',
                        'data-umami-event': 'header-api-reference-button'
                    },
                    {
                        href: 'https://github.com/lupuuss/Mokkery/releases',
                        position: 'left',
                        label: 'Releases',
                        'data-umami-event': 'header-github-releases-button'
                    },
                    {
                        href: 'https://github.com/lupuuss/Mokkery/discussions/categories/q-a',
                        position: 'left',
                        label: 'Ask a Question',
                        'data-umami-event': 'header-github-discussions-button'
                    },
                    {
                        to: "https://ko-fi.com/lupuuss",
                        position: 'right',
                        html: `<span class="header-kofi-link"><img class="header-kofi-link-icon" src="https://storage.ko-fi.com/cdn/logomarkLogo.png" alt="" /> Support me</span>`,
                        'data-umami-event': 'header-kofi-button'
                    },
                    {
                        to: 'https://github.com/lupuuss/Mokkery',
                        position: 'right',
                        html: `<span class="header-github-link"><span class="header-github-link-icon"></span> Github</span>`,
                        'data-umami-event': 'header-github-button'
                    }
                ],
        },
        prism: {
            theme: prismThemes.oneLight,
            darkTheme: prismThemes.oneDark,
        },
        algolia: {
            appId: 'N0WOZ1O51K',
            apiKey: '7f1f027d4ff4397b01a917d0867d2013',
            indexName: 'mokkery',
            contextualSearch: false
        },
    }),
    plugins: [
        [
            "@dipakparmar/docusaurus-plugin-umami",
            /** @type {import('@dipakparmar/docusaurus-plugin-umami').Options} */
            ({
                websiteID: "8ac18f2b-fd23-4ba9-ae10-0ae329f59a2f",
                analyticsDomain: "umami-for-mokkery.vercel.app"
            }),
        ],
    ],
};

export default config;

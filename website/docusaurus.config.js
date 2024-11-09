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
    ],    themeConfig:
    /** @type {import('@docusaurus/preset-classic').ThemeConfig} */
        ({
            image: 'img/logo-social.jpg',
            colorMode: {
                defaultMode: 'dark'
            },
            navbar: {
                title: 'Mokkery',
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
                    },
                    {
                        href: 'pathname:///api_reference',
                        position: 'left',
                        label: 'API'
                    },
                    {
                        href: 'https://github.com/lupuuss/Mokkery/releases',
                        position: 'left',
                        label: 'Releases'
                    },
                    {
                        href: 'https://github.com/lupuuss/Mokkery/discussions/categories/q-a',
                        position: 'right',
                        label: 'Ask a Question'
                    },
                    {
                        href: 'https://github.com/lupuuss/Mokkery',
                        position: 'right',
                        className: 'header-github-link'
                    },
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
};

export default config;

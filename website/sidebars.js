
// @ts-check

/** @type {import('@docusaurus/plugin-content-docs').SidebarsConfig} */
const sidebars = {
  docs: [
   'Setup',
   'Quick_start',
   'Limitations',
   'Thread_safety',
   {
    type: 'category',
    label: 'Guides',
    link: {
     description: 'Discover Mokkery\'s concepts through detailed guides, one step at a time!',
     type: 'generated-index',
    },
    collapsed: false,
    items: [
     'Guides/Mocking',
     'Guides/Mocking_multiple_types',
     'Guides/Answers',
     'Guides/Matchers',
     'Guides/Spying',
     'Guides/Verifying',
     'Guides/Coroutines'
    ]
   }
  ]
};

export default sidebars;

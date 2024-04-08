
// @ts-check

/** @type {import('@docusaurus/plugin-content-docs').SidebarsConfig} */
const sidebars = {
  docs: [
   'Setup',
   'Quick_start',
   'Limitations',
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
     'Guides/Answers',
     'Guides/Matchers',
     'Guides/Spying',
     'Guides/Verifying',
    ]
   }
  ]
};

export default sidebars;

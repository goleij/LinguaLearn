// Adds the DOM matchers (toBeInTheDocument, toHaveTextContent, ...) to Vitest's
// expect, and clears the DOM between tests.
import '@testing-library/jest-dom/vitest';
import { cleanup } from '@testing-library/react';
import { afterEach } from 'vitest';

// jsdom implements no media queries at all, and MainLayout asks for one on its
// very first render to decide whether the drawer starts open. The stub reports
// a desktop viewport, which is the layout the component tests care about.
if (!window.matchMedia) {
  window.matchMedia = (query: string): MediaQueryList =>
    ({
      matches: query.includes('min-width'),
      media: query,
      onchange: null,
      addEventListener: () => {},
      removeEventListener: () => {},
      addListener: () => {},
      removeListener: () => {},
      dispatchEvent: () => false,
    }) as MediaQueryList;
}

afterEach(() => {
  cleanup();
});

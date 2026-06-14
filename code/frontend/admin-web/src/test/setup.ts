import "@testing-library/jest-dom";

const originalGetComputedStyle = window.getComputedStyle;

Object.defineProperty(window, "matchMedia", {
  writable: true,
  value: (query: string) => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: () => undefined,
    removeListener: () => undefined,
    addEventListener: () => undefined,
    removeEventListener: () => undefined,
    dispatchEvent: () => false,
  }),
});

Object.defineProperty(window, "getComputedStyle", {
  value: (elt: Element) => originalGetComputedStyle(elt),
});

import { useEffect, useRef, useState } from 'react';

/**
 * Reveals an element the first time it scrolls into view.
 *
 * Deliberately one-way: elements do not fade back out when they leave, because
 * content that disappears while you scroll back up reads as a bug rather than
 * as polish.
 *
 * Anyone who has asked their system to reduce motion is given the finished
 * state immediately, and so is any browser without IntersectionObserver, so the
 * page can never end up with invisible content.
 */
export function useReveal<T extends HTMLElement>() {
  const ref = useRef<T | null>(null);
  const [shown, setShown] = useState(false);

  useEffect(() => {
    const element = ref.current;
    if (!element) return;

    const prefersReducedMotion =
      typeof window.matchMedia === 'function' &&
      window.matchMedia('(prefers-reduced-motion: reduce)').matches;

    if (prefersReducedMotion || typeof IntersectionObserver === 'undefined') {
      setShown(true);
      return;
    }

    const observer = new IntersectionObserver(
      (entries) => {
        for (const entry of entries) {
          if (entry.isIntersecting) {
            setShown(true);
            observer.disconnect();
          }
        }
      },
      // Fire a little before the element reaches the bottom edge, so it is
      // already settled by the time it is properly on screen
      { threshold: 0.15, rootMargin: '0px 0px -60px 0px' },
    );

    observer.observe(element);
    return () => observer.disconnect();
  }, []);

  return { ref, shown };
}

# LinguaLearn — Design System

The single source of truth for how this app looks. Everything here is
implemented in `frontend/tailwind.config.js` and `frontend/src/index.css`;
components must use the tokens rather than raw values.

Every contrast figure below was measured against the rendered page with an
alpha-compositing checker, not estimated.

---

## 1. Colour

### Brand

| Token | Value | Use |
| ----- | ----- | --- |
| `brand-green` | `#58CC02` | Primary fills, progress, correct states |
| `brand-green-dark` | `#46A302` | Hover/pressed state of a green fill |
| `brand-green-ink` | `#108400` | Green **as text** on a light surface |
| `brand-blue` | `#1CB0F6` | Secondary fills, badges, icons |
| `brand-blue-ink` | `#0076BC` | Blue **as text** on a light surface |
| `brand-yellow` | `#FFC800` | XP |
| `brand-orange` | `#FF9600` | Streak, "due now" |
| `brand-orange-ink` | `#A84900` | Orange **as text** on a light surface |

**The `-ink` rule.** The brand colours are bright, which is right for a fill and
wrong for text: `#58CC02` on white measures 2.09:1 and `#1CB0F6` measures
2.44:1, both far below the 4.5:1 needed for body text. Each has a darkened
sibling for text use. Fills keep the original — a coloured *shape* only needs
3:1 against what surrounds it, which they meet.

Never write `text-brand-green` on a light background. Use `text-brand-green-ink`.

### Ink and surfaces

| Token | Value | Contrast on white / `#F7F7F7` |
| ----- | ----- | ----------------------------- |
| `ink` | `#3C3C3C` | 10.4 / 9.7 |
| `ink-muted` | `#707070` | 4.95 / 4.62 — body copy, passes on both |
| `ink-faint` | `#8C8C8C` | 3.36 / 3.14 — decorative only, never essential text |
| `surface-page` | `#F7F7F7` | the app background |
| `surface-soft` | `#F0F0F0` | hover on a page-coloured row |
| `surface-grey` | `#E5E5E5` | borders, tracks, disabled fills |
| `surface-dark` | `#0B1220` | landing hero and CTA panels |
| `surface-darker` | `#060B14` | reserved for deeper dark panels |

`ink-muted` was moved from `#777777`, which measured 4.48:1 and missed the
threshold by a hair. `ink-faint` does **not** reach 4.5:1 and must not carry
information a reader needs — use it for ornament, and `ink-muted` for anything
that must be read.

### Feedback

| Token | Value | Use |
| ----- | ----- | --- |
| `feedback-correct` | `#D7FFB8` | correct-answer panel |
| `feedback-wrong` | `#FFDFE0` | wrong-answer panel |
| `feedback-error` | `#FF4B4B` | error fills |
| `feedback-error-ink` | `#CC1818` | error **text** on `feedback-wrong` |

---

## 2. Typography

| Role | Family | Notes |
| ---- | ------ | ----- |
| Headings | **Outfit** (`font-display`) | 700, negative tracking |
| Body / UI | **Inter** (`font-sans`) | 400/500/600 |

Both load from Google Fonts with `display=swap` and fall back to the system
stack, so a blocked font never blocks the page.

Headings scale with `clamp()` rather than breakpoints:

| Element | Size | Tracking |
| ------- | ---- | -------- |
| `h1` | `clamp(2.25rem, 1.4rem + 3.4vw, 3.75rem)` | `-0.03em` |
| `h2` | `clamp(1.75rem, 1.3rem + 1.8vw, 2.5rem)` | `-0.02em` |
| `h3` | `1.25rem` | `-0.01em` |
| body | `1rem` / 1.5 | default |

Tightening tracking as size grows is most of what separates a styled heading
from a default one; a geometric sans looks loose at display sizes.

Minimum body size on mobile is 16px, which also stops iOS zooming a focused
input.

---

## 3. Shape and depth

| Token | Value | Use |
| ----- | ----- | --- |
| `rounded-card` | 20px | lesson and content cards |
| `rounded-panel` | 24px | dialogs, hero panels, flashcards |
| `rounded-pill` | 25px | pill badges |
| `rounded-xl` / `2xl` | 12 / 16px | buttons, rows, small chips |

| Shadow | Use |
| ------ | --- |
| `shadow-unit` | a resting row or tile |
| `shadow-card` | a raised card or dialog |
| `shadow-lift` | hover state of an interactive card |
| `shadow-float` | a panel that floats above the page (landing only) |

`shadow-float` is layered rather than one blur, so a floating panel reads as an
object with weight instead of a rectangle with a glow.

---

## 4. Layout

- Container: `max-w-container` (1120px). Reading columns: `max-w-prose` (460px).
- Spacing follows the 4px scale. Prefer `gap-*` over margins in flex and grid.
- Breakpoints tested: **375 / 768 / 1024 / 1440**.
- No horizontal scroll at any width — verified at 375px.
- Line length 60–75 characters on desktop; constrain with `max-w-[NNch]`.

---

## 5. Motion

| Token | Use |
| ----- | --- |
| `animate-rise` | first paint of hero copy |
| `animate-float` | the hero composition, 6s loop |
| `Reveal` component | scroll-triggered entrance, 700ms, `cubic-bezier(0.22, 1, 0.36, 1)` |

Rules:

- Animate `transform` and `opacity` only. Never width, height, top or left.
- Stagger siblings 60–90ms apart so a row reads as one movement.
- Reveals are **one-way**. Content that fades out on scroll-up reads as a bug.
- `prefers-reduced-motion: reduce` is honoured globally in `index.css`, and
  `useReveal` returns the finished state immediately when it is set or when
  `IntersectionObserver` is unavailable. Content is never left invisible.

---

## 6. Icons

`components/Icon.tsx` — stroked paths on a 24×24 grid, `currentColor`,
`strokeWidth` 1.8.

- **No emoji anywhere in the interface.** They are font-dependent, differ per
  platform, and cannot be themed.
- One family, one stroke width. Sizes come from the `size` prop: 16 / 18 / 22 / 28.
- Decorative icons beside visible text get `aria-hidden` (the component sets it).
- An icon-only control needs an `aria-label`; a badge that is an icon plus a
  bare number needs one too, or it announces as a meaningless digit.

---

## 7. Accessibility floor

Checked on every change:

- Text contrast **4.5:1**, or 3:1 for large text (≥24px, or ≥18.66px bold).
- Pointer targets **≥24×24 CSS px** (WCAG 2.2 AA). Currently 0 below.
- `cursor-pointer` on everything clickable. Currently 0 missing.
- Visible focus: the `.focus-ring` utility, never `outline: none` alone.
- Colour is never the only signal — pair it with an icon or text.
- Headings run `h1 → h2 → h3` without skipping.

### Known exception

White text on `brand-green` (2.09:1) and on `brand-blue` (2.44:1) fails AA.
This is the app's existing button and badge identity, kept deliberately. Three
ways out, if it is ever revisited:

1. Leave it — a common choice for this style of app, and a real AA failure.
2. `ink` text on the green fill — **5.28:1**, keeps `#58CC02` exactly.
3. Darken the fill to `#148800` for white text — passes, but changes the brand.

---

## 8. Working on this system

**Editing `tailwind.config.js` requires a full dev-server restart.** PostCSS
resolves that file once and caches it for the life of the Node process, and
Vite's own `server.restart()` reuses the process, so a new token is simply
missing from the served CSS.

This fails silently and looks like a design mistake rather than an error:
`bg-surface-dark` missing left white text on an unpainted background;
`max-w-container` missing let the layout stretch to the window edges. Both were
mistaken for bad design choices before the cause was found.

After changing a token: stop the dev server, start it again, and confirm the
class exists.

```bash
curl -s http://localhost:5173/src/index.css | grep -c '\.max-w-container'
```

`npm run build` always compiles from the current config, so the production
bundle is never affected.

---

## 9. Patterns

### Surfaces

There are two, and the boundary between them is deliberate:

| Surface | Where | Why |
| ------- | ----- | --- |
| **Dark** (`surface-dark` + `BrandGlow`) | Landing, log in, create account | Everything before you are signed in. One identity, so arriving from the front page does not feel like a different product. |
| **Light** (`surface-page`) | Dashboard, lesson, word bank, profile | The workspace. Long reading sessions, and the content is the subject. |

Crossing that line is the only place a change of surface is meant to be felt.
The signed-out pages share `AuthLayout` and `BrandGlow` rather than repeating
the frame, so they cannot drift apart again.

**Landing** — Hero (dark, with a live product composition) → How it works →
Differentiator showcase → Features → Level path → CTA → Footer. No testimonials:
the product has no users to quote, and invented praise reads as amateur.

**App** — persistent left drawer with the current page marked, top bar carrying
identity and the two live stats, content in a centred column.

**Lesson** — one stage named at a time with a segmented bar, never a row of
seven labels. The exercise is the focus; chrome stays thin.

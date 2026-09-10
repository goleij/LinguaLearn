import { Link, Navigate } from 'react-router-dom';
import Icon, { type IconName } from '../components/Icon';
import BrandGlow from '../components/BrandGlow';
import Reveal from '../components/Reveal';
import { useAuth } from '../hooks/useAuth';

/**
 * The public front page.
 *
 * Someone already signed in never sees it — they go straight to their
 * dashboard.
 *
 * Two decisions shape it. The hero is dark, so the bright brand colours read
 * as accents on a considered surface rather than as the whole background; and
 * the product is shown rather than described, because a language app that
 * explains itself in paragraphs is arguing against its own premise. There is
 * no testimonial section: this app has no users to quote yet, and invented
 * praise is the fastest way to look amateur.
 */
export default function LandingPage() {
  const { user, loading } = useAuth();

  if (!loading && user) {
    return <Navigate to="/learn" replace />;
  }

  return (
    <div className="min-h-full bg-white">
      <SiteHeader />
      <Hero />
      <HowItWorks />
      <WordBankShowcase />
      <Features />
      <LevelPath />
      <ClosingCta />
      <SiteFooter />
    </div>
  );
}

/* ------------------------------------------------------------------ header */

function SiteHeader() {
  return (
    <header className="sticky top-0 z-40 border-b border-white/10 bg-surface-dark supports-[backdrop-filter]:bg-surface-dark/90 supports-[backdrop-filter]:backdrop-blur-md">
      <div className="mx-auto flex w-full max-w-container items-center justify-between gap-3 px-5 py-3.5">
        <span className="flex shrink-0 items-center gap-2 whitespace-nowrap font-display text-base font-bold text-white sm:text-lg">
          <span className="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-brand-green text-white">
            <Icon name="layers" size={18} />
          </span>
          German Learning
        </span>

        <nav className="flex items-center gap-1 sm:gap-2">
          <Link
            to="/login"
            className="focus-ring cursor-pointer whitespace-nowrap rounded-xl px-2.5 py-2 text-sm font-semibold text-white/80 transition-colors duration-200 hover:bg-white/10 hover:text-white sm:px-4"
          >
            Log in
          </Link>
          <Link
            to="/register"
            className="btn-base focus-ring cursor-pointer whitespace-nowrap bg-brand-green px-3.5 py-2 text-sm text-white transition-colors duration-200 hover:bg-brand-green-dark sm:px-5"
          >
            Get started
          </Link>
        </nav>
      </div>
    </header>
  );
}

/* -------------------------------------------------------------------- hero */

function Hero() {
  return (
    <section className="relative overflow-hidden bg-surface-dark px-5 pb-20 pt-14 text-white sm:pt-20 lg:pb-24">
      <BrandGlow />

      <div className="relative mx-auto grid w-full max-w-container items-center gap-16 lg:grid-cols-[1.02fr_1fr] lg:gap-16 xl:gap-20">
        <div className="flex animate-rise flex-col items-start gap-6">
          <span className="inline-flex items-center gap-2 rounded-full border border-white/15 bg-white/5 px-3 py-1.5 text-xs font-semibold tracking-wide text-white/80">
            <span className="h-1.5 w-1.5 rounded-full bg-brand-green" />
            A1 to B2 · free · no ads
          </span>

          <h1 className="m-0 max-w-[14ch] text-white">
            German that actually{' '}
            <span className="bg-gradient-to-r from-brand-green to-brand-blue bg-clip-text text-transparent">
              stays
            </span>
          </h1>

          <p className="m-0 max-w-[46ch] text-lg leading-relaxed text-white/70">
            Short lessons that explain before they ask. Nothing is tested until it has been
            taught.
          </p>

          <div className="flex w-full flex-col gap-3 sm:w-auto sm:flex-row">
            <Link
              to="/register"
              className="btn-base focus-ring cursor-pointer bg-brand-green px-8 py-4 text-center text-base text-white transition-colors duration-200 hover:bg-brand-green-dark"
            >
              Start learning free
            </Link>
            <Link
              to="/login"
              className="btn-base focus-ring cursor-pointer border border-white/20 bg-white/5 px-8 py-4 text-center text-base text-white transition-colors duration-200 hover:bg-white/10"
            >
              I have an account
            </Link>
          </div>

          <dl className="mt-2 flex flex-wrap gap-x-8 gap-y-3 border-t border-white/10 pt-6">
            {[
              ['4', 'CEFR levels'],
              ['14', 'exercise types'],
              ['0', 'ads, ever'],
            ].map(([value, label]) => (
              // Reversed so the number reads above the label while the markup
              // keeps its required term-then-definition order
              <div key={label} className="flex flex-col-reverse">
                <dt className="text-sm text-white/50">{label}</dt>
                <dd className="m-0 font-display text-2xl font-bold text-white">{value}</dd>
              </div>
            ))}
          </dl>
        </div>

        <HeroComposition />
      </div>
    </section>
  );
}

/**
 * The product, not a description of it: the lesson card the learner answers,
 * with the two things that happen around it — the missed word being filed, and
 * the streak ticking up. Plain markup rather than a screenshot, so it stays
 * sharp and cannot drift out of date.
 */
function HeroComposition() {
  return (
    <div aria-hidden="true" className="relative mx-auto w-full max-w-[400px] animate-float px-2 py-6 lg:mx-0 lg:px-0">
      <div className="rounded-panel bg-white p-7 text-ink shadow-float">
        <span className="font-display text-sm font-bold text-ink">Practice</span>

        <div className="mt-2.5 flex gap-1">
          {[1, 1, 1, 1, 0, 0].map((done, index) => (
            <span
              key={index}
              className={`h-1.5 flex-1 rounded-full ${done ? 'bg-brand-green' : 'bg-surface-grey'}`}
            />
          ))}
        </div>

        <p className="mb-6 mt-8 text-center font-display text-[1.6rem] font-bold leading-snug text-ink">
          Ich <span className="text-brand-blue-ink">___</span> aus Berlin.
        </p>

        <div className="flex flex-col gap-2.5">
          {[
            { text: 'komme', state: 'right' },
            { text: 'kommt', state: 'idle' },
            { text: 'kommen', state: 'idle' },
          ].map((option) => (
            <span
              key={option.text}
              className={`flex items-center gap-3 rounded-xl px-4 py-3 text-[0.95rem] transition-colors ${
                option.state === 'right'
                  ? 'bg-feedback-correct font-semibold text-ink ring-1 ring-brand-green/40'
                  : 'bg-surface-page text-ink-muted'
              }`}
            >
              <span
                className={`flex h-[18px] w-[18px] items-center justify-center rounded-full border-2 ${
                  option.state === 'right' ? 'border-brand-green' : 'border-surface-grey'
                }`}
              >
                {option.state === 'right' && (
                  <span className="h-2 w-2 rounded-full bg-brand-green" />
                )}
              </span>
              {option.text}
            </span>
          ))}
        </div>
      </div>

      {/* The word bank catching a mistake, which is the point of the product */}
      <div className="absolute -bottom-2 -left-4 w-[15.5rem] rotate-[-3deg] rounded-2xl bg-white p-4 shadow-float sm:-left-10">
        <div className="flex items-start gap-3">
          <span className="flex h-9 w-9 shrink-0 items-center justify-center rounded-xl bg-brand-blue/10 text-brand-blue">
            <Icon name="book" size={18} />
          </span>
          <div className="min-w-0">
            <p className="m-0 text-[0.8rem] font-semibold text-ink">Saved to your words</p>
            <p className="m-0 truncate text-xs text-ink-muted">Gute Nacht — Good night</p>
          </div>
        </div>
      </div>

      {/* And the reward loop */}
      <div className="absolute -right-4 top-0 flex items-center gap-2.5 rounded-2xl bg-white px-4 py-3 shadow-float sm:-right-10">
        <span className="flex h-8 w-8 items-center justify-center rounded-lg bg-brand-orange/15 text-brand-orange">
          <Icon name="flame" size={16} />
        </span>
        <span className="font-display text-sm font-bold text-ink">
          12<span className="ml-1 font-sans text-xs font-medium text-ink-muted">day streak</span>
        </span>
      </div>
    </div>
  );
}

/* ------------------------------------------------------------ how it works */

const STEPS: { icon: IconName; title: string; text: string }[] = [
  { icon: 'bulb', title: 'Learn', text: 'New words and one grammar idea, in plain English.' },
  { icon: 'layers', title: 'Practise', text: 'Guided first, then alone. Mistakes cost nothing.' },
  { icon: 'target', title: 'Checkpoint', text: 'A short check at the end unlocks the next lesson.' },
];

function HowItWorks() {
  return (
    <section className="px-5 py-16 sm:py-24">
      <div className="mx-auto w-full max-w-container">
        <SectionHeading eyebrow="How a lesson runs" title="Taught, then tested" />

        <ol className="m-0 mt-12 grid list-none gap-4 p-0 md:grid-cols-3">
          {STEPS.map((step, index) => (
            <Reveal
              as="li"
              key={step.title}
              delay={index * 80}
              className="relative rounded-2xl border border-surface-grey bg-white p-6 transition-shadow duration-200 hover:shadow-lift"
            >
              <div className="flex items-center gap-3">
                <span className="flex h-11 w-11 items-center justify-center rounded-xl bg-brand-green/10 text-brand-green">
                  <Icon name={step.icon} size={22} />
                </span>
                <span className="rounded-full bg-surface-page px-2.5 py-1 font-display text-xs font-bold text-ink-muted">
                  Step {index + 1}
                </span>
              </div>
              <h3 className="mb-1.5 mt-4 text-ink">{step.title}</h3>
              <p className="m-0 text-[0.95rem] leading-relaxed text-ink-muted">{step.text}</p>
            </Reveal>
          ))}
        </ol>
      </div>
    </section>
  );
}

/* ------------------------------------------------------- word bank feature */

function WordBankShowcase() {
  return (
    <section className="bg-surface-page px-5 py-16 sm:py-24">
      <div className="mx-auto grid w-full max-w-container items-center gap-12 lg:grid-cols-2">
        <Reveal>
          <p className="m-0 text-xs font-bold uppercase tracking-[0.18em] text-brand-blue-ink">
            Word bank
          </p>
          <h2 className="mb-4 mt-3 text-ink">You never build the deck</h2>
          <p className="m-0 max-w-[42ch] text-[1.05rem] leading-relaxed text-ink-muted">
            Flashcards usually mean typing out every card yourself. Here the deck writes itself
            from the words you actually got wrong.
          </p>

          <ul className="m-0 mt-7 flex list-none flex-col gap-3 p-0">
            {[
              'Nothing to set up',
              'Gaps widen as a word starts to stick',
              'Tap any word to add your own',
            ].map((point) => (
              <li key={point} className="flex items-start gap-3 text-ink">
                <span className="mt-0.5 flex h-5 w-5 shrink-0 items-center justify-center rounded-full bg-brand-green text-white">
                  <Icon name="check" size={13} />
                </span>
                <span className="text-[0.98rem]">{point}</span>
              </li>
            ))}
          </ul>
        </Reveal>

        <Reveal delay={120}>
          <FlashcardVisual />
        </Reveal>
      </div>
    </section>
  );
}

function FlashcardVisual() {
  return (
    <div aria-hidden="true" className="relative mx-auto w-full max-w-[400px]">
      {/* The card behind, to suggest a stack */}
      <div className="absolute inset-x-5 -top-3 h-full rounded-panel bg-white/70 shadow-lift" />

      <div className="relative rounded-panel bg-white p-7 shadow-float">
        <div className="flex items-center justify-between">
          <span className="rounded-full bg-brand-orange/15 px-2.5 py-1 text-xs font-semibold text-brand-orange-ink">
            Due now
          </span>
          <span className="text-xs font-medium text-ink-muted">3 left</span>
        </div>

        <p className="mb-2 mt-8 text-center font-display text-4xl font-bold text-ink">
          die Rechnung
        </p>
        <p className="m-0 mb-8 text-center text-lg text-brand-green-ink">the bill</p>

        <div className="flex gap-2">
          <span className="flex-1 rounded-xl bg-feedback-wrong py-3 text-center text-sm font-semibold text-feedback-error-ink">
            Didn&apos;t know it
          </span>
          <span className="flex-1 rounded-xl bg-brand-green py-3 text-center text-sm font-semibold text-white">
            Knew it
          </span>
        </div>

        <div className="mt-7 border-t border-surface-page pt-5">
          <div className="flex items-center justify-between text-xs text-ink-muted">
            <span>Review schedule</span>
            <span>35 days</span>
          </div>
          <div className="mt-2.5 flex items-center gap-1.5">
            {['now', '1d', '3d', '7d', '16d', '35d'].map((step, index) => (
              <div key={step} className="flex flex-1 flex-col items-center gap-1.5">
                <span
                  className={`h-1.5 w-full rounded-full ${
                    index < 3 ? 'bg-brand-blue' : 'bg-surface-grey'
                  }`}
                />
                <span
                  className={`text-[0.65rem] ${
                    index < 3 ? 'text-brand-blue-ink' : 'text-ink-muted'
                  }`}
                >
                  {step}
                </span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}

/* ---------------------------------------------------------------- features */

const FEATURES: { icon: IconName; title: string; text: string }[] = [
  {
    icon: 'search',
    title: 'Look up any word',
    text: 'Tap a German word anywhere for its article, meaning and examples.',
  },
  {
    icon: 'pen',
    title: 'Write, then get corrected',
    text: 'Real writing tasks with grammar feedback before you submit.',
  },
  {
    icon: 'volume',
    title: 'Hear it spoken',
    text: 'Listening tasks read the German aloud so it is not all reading.',
  },
  {
    icon: 'bolt',
    title: 'XP you cannot farm',
    text: 'Earned once per exercise, so the number means something.',
  },
];

function Features() {
  return (
    <section className="px-5 py-16 sm:py-24">
      <div className="mx-auto w-full max-w-container">
        <SectionHeading eyebrow="In every lesson" title="Built to teach, not to quiz" />

        <div className="mt-12 grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
          {FEATURES.map((feature, index) => (
            <Reveal
              as="article"
              key={feature.title}
              delay={index * 70}
              className="group rounded-2xl border border-surface-grey p-6 transition-all duration-200 hover:-translate-y-1 hover:border-brand-green/40 hover:shadow-lift"
            >
              <span className="flex h-11 w-11 items-center justify-center rounded-xl bg-surface-page text-ink transition-colors duration-200 group-hover:bg-brand-green/10 group-hover:text-brand-green">
                <Icon name={feature.icon} size={21} />
              </span>
              <h3 className="mb-1.5 mt-4 text-[1.05rem] text-ink">{feature.title}</h3>
              <p className="m-0 text-sm leading-relaxed text-ink-muted">{feature.text}</p>
            </Reveal>
          ))}
        </div>
      </div>
    </section>
  );
}

/* ------------------------------------------------------------------ levels */

const LEVELS = [
  { level: 'A1', label: 'Beginner', text: 'Everyday phrases' },
  { level: 'A2', label: 'Elementary', text: 'Familiar routines' },
  { level: 'B1', label: 'Intermediate', text: 'Opinions and plans' },
  { level: 'B2', label: 'Upper intermediate', text: 'Detailed argument' },
];

function LevelPath() {
  return (
    <section className="bg-surface-page px-5 py-16 sm:py-24">
      <div className="mx-auto w-full max-w-container">
        <SectionHeading eyebrow="The path" title="From first hello to real argument" />

        <div className="relative mt-12">
          {/* The line the levels sit on */}
          <div
            aria-hidden="true"
            className="absolute left-0 right-0 top-6 hidden h-0.5 bg-gradient-to-r from-brand-green via-brand-blue to-brand-blue/20 lg:block"
          />

          <ol className="relative m-0 grid list-none grid-cols-2 gap-4 p-0 lg:grid-cols-4">
            {LEVELS.map((level, index) => (
              <Reveal
                as="li"
                key={level.level}
                delay={index * 90}
                className="flex flex-col items-start"
              >
                <span
                  className={`flex h-12 w-12 items-center justify-center rounded-full font-display text-base font-bold text-white ring-8 ring-surface-page ${
                    index === 0 ? 'bg-brand-green' : 'bg-brand-blue'
                  }`}
                >
                  {level.level}
                </span>
                <h3 className="mb-1 mt-4 text-[1.05rem] text-ink">{level.label}</h3>
                <p className="m-0 text-sm text-ink-muted">{level.text}</p>
              </Reveal>
            ))}
          </ol>
        </div>
      </div>
    </section>
  );
}

/* --------------------------------------------------------------------- cta */

function ClosingCta() {
  return (
    <section className="px-5 py-16 sm:py-20">
      <div className="relative mx-auto flex w-full max-w-container flex-col items-center gap-5 overflow-hidden rounded-[28px] bg-surface-dark px-6 py-16 text-center">
        <BrandGlow />

        <h2 className="relative m-0 max-w-[18ch] text-white">Your first lesson takes five minutes</h2>
        <p className="relative m-0 max-w-[44ch] text-white/70">
          Start at A1 with greetings, or jump straight to the level that fits you.
        </p>
        <Link
          to="/register"
          className="btn-base focus-ring relative cursor-pointer bg-brand-green px-9 py-4 text-base text-white transition-colors duration-200 hover:bg-brand-green-dark"
        >
          Create a free account
        </Link>
      </div>
    </section>
  );
}

/* ------------------------------------------------------------------ footer */

function SiteFooter() {
  return (
    <footer className="border-t border-surface-grey px-5 py-8">
      <div className="mx-auto flex w-full max-w-container flex-col items-center gap-2 text-center">
        <span className="font-display text-sm font-bold text-ink">German Learning</span>
        <p className="m-0 max-w-[62ch] text-xs leading-relaxed text-ink-muted">
          Built with Spring Boot and React. Dictionary data from German Wiktionary, example
          sentences from Tatoeba, grammar checking by LanguageTool.
        </p>
      </div>
    </footer>
  );
}

/* ----------------------------------------------------------------- shared */

function SectionHeading({ eyebrow, title }: { eyebrow: string; title: string }) {
  return (
    <Reveal className="flex flex-col items-center text-center">
      <p className="m-0 text-xs font-bold uppercase tracking-[0.18em] text-brand-blue-ink">{eyebrow}</p>
      <h2 className="m-0 mt-3 max-w-[20ch] text-ink">{title}</h2>
    </Reveal>
  );
}

import { useCallback, useEffect, useState } from 'react';
import ReviewSession from '../components/ReviewSession';
import { vocabularyService } from '../services/vocabularyService';
import type { SavedWord, WordBank } from '../types';
import Icon from '../components/Icon';

type Mode = 'list' | 'review';

/**
 * The word bank: everything the learner has collected, and the review queue
 * built from it.
 *
 * Words arrive here on their own after a wrong answer, or deliberately from
 * the Word Explorer and lesson word lists, so the page has to read well both
 * when it is empty and when it is long.
 */
export default function WordBankPage() {
  const [bank, setBank] = useState<WordBank | null>(null);
  const [loading, setLoading] = useState(true);
  const [mode, setMode] = useState<Mode>('list');

  const load = useCallback(() => {
    setLoading(true);
    vocabularyService
      .getWordBank()
      .then(setBank)
      .catch((error) => console.error('Failed to load the word bank', error))
      .finally(() => setLoading(false));
  }, []);

  useEffect(load, [load]);

  const remove = async (word: SavedWord) => {
    try {
      await vocabularyService.remove(word.id);
      load();
    } catch (error) {
      console.error('Could not remove the word', error);
    }
  };

  if (loading) {
    return <div className="p-3 text-ink-muted sm:p-5">Loading…</div>;
  }

  if (!bank) {
    return <div className="p-3 text-ink-muted sm:p-5">Your words are unavailable right now.</div>;
  }

  if (mode === 'review') {
    return (
      <ReviewSession
        onFinish={() => {
          setMode('list');
          load();
        }}
      />
    );
  }

  return (
    <div className="mx-auto flex w-full max-w-[760px] flex-col gap-4 p-3 sm:p-5">
      <header className="flex flex-col gap-1">
        <h2 className="m-0 text-ink">My words</h2>
        <p className="m-0 text-ink-muted">
          Words you save, and the ones you miss in a lesson, land here and come back until they
          stick.
        </p>
      </header>

      <div className="grid grid-cols-3 gap-2 sm:gap-3">
        <StatTile label="Saved" value={bank.total} color="bg-brand-blue" />
        <StatTile label="Due now" value={bank.due} color="bg-brand-orange" />
        <StatTile label="Learned" value={bank.learned} color="bg-brand-green" />
      </div>

      {bank.due > 0 ? (
        <button
          type="button"
          onClick={() => setMode('review')}
          className="btn-base w-full bg-brand-green px-6 py-4 text-lg text-white hover:bg-brand-green-dark"
        >
          Review {bank.due} {bank.due === 1 ? 'word' : 'words'}
        </button>
      ) : (
        bank.total > 0 && (
          <p className="rounded-2xl bg-feedback-correct px-4 py-3 text-center font-medium text-ink">
            Nothing due right now — everything is scheduled for later.
          </p>
        )
      )}

      {bank.words.length === 0 ? (
        <EmptyState />
      ) : (
        <ul className="m-0 flex list-none flex-col gap-2 p-0">
          {bank.words.map((word) => (
            <WordRow key={word.id} word={word} onRemove={() => remove(word)} />
          ))}
        </ul>
      )}
    </div>
  );
}

function StatTile({ label, value, color }: { label: string; value: number; color: string }) {
  return (
    <div className={`flex flex-col items-center rounded-2xl px-2 py-4 ${color}`}>
      <span className="text-2xl font-bold text-white">{value}</span>
      <span className="text-xs text-white/80">{label}</span>
    </div>
  );
}

function EmptyState() {
  return (
    <div className="flex flex-col items-center gap-2 rounded-card bg-white p-8 text-center shadow-unit">
      <span className="flex h-14 w-14 items-center justify-center rounded-full bg-brand-blue/10 text-brand-blue">
        <Icon name="book" size={28} />
      </span>
      <h3 className="m-0 text-ink">No words yet</h3>
      <p className="m-0 max-w-[420px] text-ink-muted">
        Tap the star next to any word in a lesson, or save one from the Word Explorer. Anything
        you get wrong in a lesson is added here by itself.
      </p>
    </div>
  );
}

function WordRow({ word, onRemove }: { word: SavedWord; onRemove: () => void }) {
  return (
    <li className="flex flex-wrap items-center gap-x-3 gap-y-2 rounded-2xl bg-white p-4 shadow-unit">
      <div className="min-w-0 flex-1">
        <p className="m-0 text-lg font-bold text-ink">{word.german}</p>
        <p className="m-0 text-ink-muted">{word.english}</p>
        {/* How it got here, which is often the most interesting part */}
        <p className="m-0 text-xs text-ink-faint">
          {word.sourceLabel}
          {word.lessonName ? ` · ${word.lessonName}` : ''}
        </p>
      </div>

      <div className="flex flex-col items-end gap-1">
        <BoxProgress box={word.box} maxBox={word.maxBox} learned={word.learned} />
        <span
          className={`text-xs font-medium ${
            word.learned ? 'text-brand-green-ink' : word.due ? 'text-brand-orange-ink' : 'text-ink-muted'
          }`}
        >
          {word.learned ? 'Learned' : word.due ? 'Due now' : 'Scheduled'}
        </span>
      </div>

      <button
        type="button"
        onClick={onRemove}
        aria-label={`Remove ${word.german}`}
        title="Remove from my words"
        className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-surface-page text-ink-faint transition hover:bg-feedback-wrong hover:text-feedback-error-ink"
      >
        <Icon name="close" size={16} />
      </button>
    </li>
  );
}

/** The Leitner box as a row of dots, so progress is visible at a glance. */
function BoxProgress({ box, maxBox, learned }: { box: number; maxBox: number; learned: boolean }) {
  return (
    <span
      className="flex items-center gap-1"
      role="img"
      aria-label={`Review level ${box} of ${maxBox}`}
    >
      {Array.from({ length: maxBox }, (_, index) => (
        <span
          key={index}
          className={`h-2 w-2 rounded-full ${
            index < box ? (learned ? 'bg-brand-green' : 'bg-brand-blue') : 'bg-surface-grey'
          }`}
        />
      ))}
    </span>
  );
}

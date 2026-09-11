import { useEffect, useState } from 'react';
import { vocabularyService } from '../services/vocabularyService';
import type { SavedWord } from '../types';
import Icon from './Icon';

/**
 * The flashcard run over everything that is due.
 *
 * German first, because recalling the meaning is the harder direction and the
 * one worth practising. The card is only graded after it has been turned over,
 * so the learner commits before seeing the answer. A word that was forgotten
 * goes back to the end of this same queue as well as back to box zero on the
 * server, so a session finishes only once every card has been remembered at
 * least once.
 */
export default function ReviewSession({ onFinish }: { onFinish: () => void }) {
  const [queue, setQueue] = useState<SavedWord[]>([]);
  const [loading, setLoading] = useState(true);
  const [revealed, setRevealed] = useState(false);
  const [reviewed, setReviewed] = useState(0);
  const [remembered, setRemembered] = useState(0);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    vocabularyService
      .getDue()
      .then(setQueue)
      .catch((error) => console.error('Failed to load the review queue', error))
      .finally(() => setLoading(false));
  }, []);

  const answer = async (knewIt: boolean) => {
    const card = queue[0];
    if (!card || saving) return;

    setSaving(true);
    try {
      await vocabularyService.review(card.id, knewIt);
    } catch (error) {
      console.error('Could not record the review', error);
    } finally {
      setSaving(false);
    }

    setReviewed((count) => count + 1);
    if (knewIt) {
      setRemembered((count) => count + 1);
      setQueue((current) => current.slice(1));
    } else {
      // Forgotten cards come round again before the session ends
      setQueue((current) => [...current.slice(1), card]);
    }
    setRevealed(false);
  };

  if (loading) {
    return <div className="p-3 text-ink-muted sm:p-5">Loading…</div>;
  }

  if (queue.length === 0) {
    return <SessionSummary reviewed={reviewed} remembered={remembered} onFinish={onFinish} />;
  }

  const card = queue[0];

  return (
    <div className="mx-auto flex w-full max-w-[560px] flex-col gap-4 p-3 sm:p-5">
      <div className="flex items-center gap-3">
        <button
          type="button"
          onClick={onFinish}
          aria-label="Back to my words"
          className="flex h-10 w-10 shrink-0 items-center justify-center rounded-[10px] bg-surface-grey text-ink transition hover:brightness-95"
        >
          <Icon name="arrowLeft" size={18} />
        </button>
        <h2 className="m-0 min-w-0 flex-1 truncate text-lg text-brand-green-ink sm:text-2xl">Review</h2>
        <span className="shrink-0 rounded-full bg-surface-grey px-3 py-1 text-sm font-bold text-ink-muted">
          {queue.length} left
        </span>
      </div>

      <button
        type="button"
        onClick={() => setRevealed(true)}
        disabled={revealed}
        aria-label={revealed ? undefined : `Show the meaning of ${card.german}`}
        className="flex min-h-[220px] w-full cursor-pointer flex-col items-center justify-center gap-3 rounded-panel bg-white p-6 text-center shadow-card transition disabled:cursor-default"
      >
        <span className="text-3xl font-bold text-ink sm:text-4xl">{card.german}</span>

        {revealed ? (
          <>
            <span className="h-px w-16 bg-surface-grey" />
            <span className="text-xl text-brand-green-ink sm:text-2xl">{card.english}</span>
          </>
        ) : (
          <span className="text-sm text-ink-faint">Tap to see the meaning</span>
        )}

        {revealed && card.lessonName && (
          <span className="text-xs text-ink-faint">from {card.lessonName}</span>
        )}
      </button>

      {revealed ? (
        <div className="flex flex-col gap-2 sm:flex-row">
          <button
            type="button"
            onClick={() => answer(false)}
            disabled={saving}
            className="btn-base flex-1 bg-feedback-error px-6 py-4 text-white hover:brightness-105"
          >
            Didn&apos;t know it
          </button>
          <button
            type="button"
            onClick={() => answer(true)}
            disabled={saving}
            className="btn-base flex-1 bg-brand-green px-6 py-4 text-white hover:bg-brand-green-dark"
          >
            Knew it
          </button>
        </div>
      ) : (
        <button
          type="button"
          onClick={() => setRevealed(true)}
          className="btn-base w-full bg-brand-blue px-6 py-4 text-white hover:brightness-105"
        >
          Show meaning
        </button>
      )}
    </div>
  );
}

function SessionSummary({
  reviewed,
  remembered,
  onFinish,
}: {
  reviewed: number;
  remembered: number;
  onFinish: () => void;
}) {
  const nothingWasDue = reviewed === 0;

  return (
    <div className="mx-auto flex w-full max-w-[560px] flex-col items-center gap-3 p-3 sm:p-5">
      <div className="flex w-full flex-col items-center gap-3 rounded-panel bg-feedback-correct p-8 text-center">
        <span
          className={`flex h-16 w-16 items-center justify-center rounded-full ${
            nothingWasDue ? 'bg-white/70 text-ink-muted' : 'bg-white/70 text-brand-green'
          }`}
        >
          <Icon name={nothingWasDue ? 'refresh' : 'trophy'} size={32} />
        </span>

        <h2 className="m-0 text-brand-green">
          {nothingWasDue ? 'Nothing due' : 'Review complete!'}
        </h2>

        {nothingWasDue ? (
          <p className="m-0 text-ink-muted">
            Every word is scheduled for later. Come back when one is due.
          </p>
        ) : (
          <p className="m-0 text-ink">
            {remembered} of {reviewed} {reviewed === 1 ? 'card' : 'cards'} remembered on the first
            try.
          </p>
        )}

        <button
          type="button"
          onClick={onFinish}
          className="btn-base mt-2 bg-brand-green px-8 py-3 text-white hover:bg-brand-green-dark"
        >
          Back to my words
        </button>
      </div>
    </div>
  );
}

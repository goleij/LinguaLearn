import type { WordInfo } from '../types';
import Icon from './Icon';

type SaveState = 'idle' | 'saving' | 'saved' | 'failed';

/**
 * The Word Explorer panel.
 *
 * The meaning the lesson taught goes at the top, because that is the answer to
 * the question the learner actually asked. The German Wiktionary entry follows
 * as background: it is uneven, sometimes describes a different sense of the
 * word, and is written in German, so it is presented as extra reading rather
 * than as the translation. Every section is optional and a missing entry is
 * stated plainly rather than shown as an error.
 */
export default function WordPopover({
  word,
  knownMeaning,
  info,
  loading,
  failed,
  saveState,
  onSave,
  onClose,
}: {
  word: string;
  knownMeaning?: string;
  info: WordInfo | null;
  loading: boolean;
  failed: boolean;
  saveState: SaveState;
  onSave: (meaning: string) => void;
  onClose: () => void;
}) {
  // Something has to be saved as the meaning; the lesson's English wins, and
  // the first dictionary sense stands in when there is none
  const meaningToSave = knownMeaning ?? info?.meanings[0] ?? null;

  return (
    <div className="fixed inset-0 z-50 flex items-end justify-center sm:items-center">
      <div
        className="absolute inset-0 bg-ink/40 backdrop-blur-[2px]"
        onClick={onClose}
        role="presentation"
        aria-hidden="true"
      />

      <div
        role="dialog"
        aria-label={`About ${word}`}
        className="relative z-10 max-h-[85vh] w-full max-w-prose overflow-auto rounded-t-panel bg-white p-5 shadow-card sm:rounded-panel"
      >
        <div className="mb-3 flex items-start justify-between gap-3">
          <div className="min-w-0">
            <h3 className="m-0 break-words text-ink">
              {info?.article ? `${info.article} ` : ''}
              {word}
            </h3>
            {info?.wordType && <span className="text-sm text-ink-muted">{info.wordType}</span>}
          </div>
          <button
            type="button"
            onClick={onClose}
            aria-label="Close"
            className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-surface-grey text-ink transition hover:brightness-95"
          >
            <Icon name="close" size={18} />
          </button>
        </div>

        {/* The translation the learner came for */}
        {knownMeaning && (
          <div className="mb-4 rounded-2xl bg-feedback-correct px-4 py-3">
            <p className="m-0 text-xs font-bold uppercase tracking-wide text-brand-green-ink">
              In this lesson
            </p>
            <p className="m-0 text-lg font-bold text-ink">{knownMeaning}</p>
          </div>
        )}

        {meaningToSave && (
          <SaveButton state={saveState} onSave={() => onSave(meaningToSave)} />
        )}

        {loading && <p className="text-ink-muted">Looking it up…</p>}

        {failed && (
          <p className="text-ink-muted">
            The dictionary could not be reached right now. Please try again in a moment.
          </p>
        )}

        {!loading && !failed && info && !info.found && (
          <p className="text-ink-muted">
            No German Wiktionary entry for this word. It may be an inflected form — try the base
            form.
          </p>
        )}

        {!loading && info?.found && (
          <div className="flex flex-col gap-4">
            {info.meanings.length > 0 && (
              <Section title={knownMeaning ? 'In the German dictionary' : 'Meaning'}>
                <ul className="m-0 list-none space-y-1 p-0">
                  {info.meanings.map((meaning) => (
                    <li key={meaning} className="text-ink">
                      {meaning}
                    </li>
                  ))}
                </ul>
              </Section>
            )}

            {info.examples.length > 0 && (
              <Section title="Examples">
                <ul className="m-0 list-none space-y-1 p-0">
                  {info.examples.map((example) => (
                    <li key={example} className="italic text-ink">
                      {example}
                    </li>
                  ))}
                </ul>
              </Section>
            )}

            {info.synonyms.length > 0 && (
              <Section title="Synonyms">
                <p className="m-0 text-ink">{info.synonyms.join(', ')}</p>
              </Section>
            )}

            {info.origin && (
              <Section title="Origin">
                <p className="m-0 text-ink">{info.origin}</p>
              </Section>
            )}
          </div>
        )}

        {info && (
          <p className="mt-4 text-xs text-ink-faint">
            Source:{' '}
            <a
              className="text-brand-blue hover:underline"
              href={info.sourceUrl}
              target="_blank"
              rel="noreferrer"
            >
              {info.source}
            </a>
          </p>
        )}
      </div>
    </div>
  );
}

function Section({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <section>
      <h4 className="mb-1 text-sm font-bold uppercase tracking-wide text-ink-muted">{title}</h4>
      {children}
    </section>
  );
}

function SaveButton({ state, onSave }: { state: SaveState; onSave: () => void }) {
  if (state === 'saved') {
    return (
      <p className="mb-4 flex items-center gap-2 rounded-2xl bg-surface-page px-4 py-3 text-sm font-medium text-brand-green-ink">
        <Icon name="check" size={18} /> Saved to your words
      </p>
    );
  }

  return (
    <div className="mb-4">
      <button
        type="button"
        onClick={onSave}
        disabled={state === 'saving'}
        className="btn-base w-full bg-brand-blue px-4 py-3 text-sm text-white hover:brightness-105"
      >
        {state === 'saving' ? (
          'Saving…'
        ) : (
          <span className="flex items-center justify-center gap-2">
            <Icon name="star" size={16} /> Save to my words
          </span>
        )}
      </button>
      {state === 'failed' && (
        <p className="mt-1 text-center text-xs text-feedback-error-ink">
          Could not save it. Please try again.
        </p>
      )}
    </div>
  );
}

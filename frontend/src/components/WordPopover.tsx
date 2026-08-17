import type { WordInfo } from '../types';

/**
 * The Word Explorer panel.
 *
 * Wiktionary entries are uneven, so every section is optional and a missing
 * entry is stated plainly rather than shown as an error.
 */
export default function WordPopover({
  word,
  info,
  loading,
  failed,
  onClose,
}: {
  word: string;
  info: WordInfo | null;
  loading: boolean;
  failed: boolean;
  onClose: () => void;
}) {
  return (
    <div className="fixed inset-0 z-50 flex items-end justify-center sm:items-center">
      <div
        className="absolute inset-0 bg-ink/30"
        onClick={onClose}
        role="presentation"
        aria-hidden="true"
      />

      <div className="relative z-10 max-h-[80vh] w-full max-w-[460px] overflow-auto rounded-t-[20px] bg-white p-5 shadow-card sm:rounded-[20px]">
        <div className="mb-3 flex items-start justify-between gap-3">
          <div>
            <h3 className="m-0 text-brand-green">
              {info?.article ? `${info.article} ` : ''}
              {word}
            </h3>
            {info?.wordType && <span className="text-sm text-ink-muted">{info.wordType}</span>}
          </div>
          <button
            type="button"
            onClick={onClose}
            aria-label="Close"
            className="rounded-[10px] bg-surface-grey px-3 py-1 text-ink transition hover:brightness-95"
          >
            ✕
          </button>
        </div>

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
              <section>
                <h4 className="mb-1 text-sm font-bold uppercase tracking-wide text-ink-muted">
                  Meaning
                </h4>
                <ul className="m-0 list-none space-y-1 p-0">
                  {info.meanings.map((meaning) => (
                    <li key={meaning} className="text-ink">
                      {meaning}
                    </li>
                  ))}
                </ul>
              </section>
            )}

            {info.examples.length > 0 && (
              <section>
                <h4 className="mb-1 text-sm font-bold uppercase tracking-wide text-ink-muted">
                  Examples
                </h4>
                <ul className="m-0 list-none space-y-1 p-0">
                  {info.examples.map((example) => (
                    <li key={example} className="italic text-ink">
                      {example}
                    </li>
                  ))}
                </ul>
              </section>
            )}

            {info.synonyms.length > 0 && (
              <section>
                <h4 className="mb-1 text-sm font-bold uppercase tracking-wide text-ink-muted">
                  Synonyms
                </h4>
                <p className="m-0 text-ink">{info.synonyms.join(', ')}</p>
              </section>
            )}

            {info.origin && (
              <section>
                <h4 className="mb-1 text-sm font-bold uppercase tracking-wide text-ink-muted">
                  Origin
                </h4>
                <p className="m-0 text-ink">{info.origin}</p>
              </section>
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

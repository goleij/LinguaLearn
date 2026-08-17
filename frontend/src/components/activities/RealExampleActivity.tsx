import { useEffect, useState } from 'react';
import ClickableText from '../ClickableText';
import { learningService } from '../../services/learningService';
import type { Activity, ExampleSentence } from '../../types';

/**
 * Language in context: the lesson's own examples first, then optional real
 * sentences fetched through our backend.
 *
 * The external sentences are enrichment — if the lookup returns nothing, the
 * activity simply shows the curated examples and moves on.
 */
export default function RealExampleActivity({
  activity,
  onContinue,
}: {
  activity: Activity;
  onContinue: () => void;
}) {
  const [external, setExternal] = useState<ExampleSentence[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!activity.exampleQuery) return;

    let active = true;
    setLoading(true);

    learningService
      .getExamples(activity.exampleQuery)
      .then((sentences) => {
        if (active) setExternal(sentences);
      })
      .catch(() => {
        if (active) setExternal([]);
      })
      .finally(() => {
        if (active) setLoading(false);
      });

    return () => {
      active = false;
    };
  }, [activity.exampleQuery]);

  return (
    <div className="flex w-full flex-col items-center gap-3 p-4">
      <span className="rounded-[20px] bg-brand-green px-4 py-1 text-xs font-bold uppercase tracking-wide text-white">
        In context
      </span>

      <h3 className="text-center text-brand-green">{activity.prompt}</h3>

      {activity.context && (
        <p className="max-w-[460px] text-center text-ink-muted">{activity.context}</p>
      )}

      <div className="flex w-full max-w-[460px] flex-col gap-2">
        {(activity.examples ?? []).map((example) => (
          <div key={example.de} className="rounded-xl bg-surface-page px-4 py-3">
            <ClickableText text={example.de} className="block text-lg text-ink" />
            <span className="text-sm text-ink-muted">{example.en}</span>
          </div>
        ))}
      </div>

      {activity.exampleQuery && (
        <div className="w-full max-w-[460px]">
          <h4 className="mb-2 mt-2 text-sm font-bold uppercase tracking-wide text-ink-muted">
            Real examples
          </h4>

          {loading && <p className="text-ink-muted">Looking for real sentences…</p>}

          {!loading && external.length === 0 && (
            <p className="text-sm text-ink-faint">
              No extra examples available right now — the lesson examples above are enough.
            </p>
          )}

          <div className="flex flex-col gap-2">
            {external.map((sentence) => (
              <div
                key={sentence.german}
                className="rounded-xl border border-surface-grey px-4 py-3"
              >
                <ClickableText text={sentence.german} className="block text-ink" />
                <span className="text-sm text-ink-muted">{sentence.english}</span>
                <span className="mt-1 block text-xs text-ink-faint">via {sentence.source}</span>
              </div>
            ))}
          </div>
        </div>
      )}

      <button type="button" className="btn-primary mt-2" onClick={onContinue}>
        Got it
      </button>
    </div>
  );
}

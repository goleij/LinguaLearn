import ClickableText from '../ClickableText';
import type { Activity } from '../../types';

/** The word list of a lesson. Every German word opens the Word Explorer. */
export default function VocabularyActivity({
  activity,
  onContinue,
}: {
  activity: Activity;
  onContinue: () => void;
}) {
  return (
    <div className="flex w-full flex-col items-center gap-3 p-4">
      <span className="rounded-[20px] bg-brand-blue px-4 py-1 text-xs font-bold uppercase tracking-wide text-white">
        Vocabulary
      </span>

      <h3 className="text-center text-brand-blue">{activity.prompt}</h3>

      {activity.context && (
        <p className="max-w-[460px] text-center text-ink-muted">{activity.context}</p>
      )}

      <div className="flex w-full max-w-[460px] flex-col gap-2">
        {(activity.vocabulary ?? []).map((item) => (
          <div
            key={item.de}
            className="flex flex-wrap items-baseline justify-between gap-2 rounded-xl bg-surface-page px-4 py-3"
          >
            <ClickableText text={item.de} className="text-lg font-bold text-ink" />
            <span className="text-ink-muted">{item.en}</span>
          </div>
        ))}
      </div>

      <p className="text-xs text-ink-faint">Tap any German word to look it up.</p>

      <button type="button" className="btn-primary mt-2" onClick={onContinue}>
        Got it
      </button>
    </div>
  );
}

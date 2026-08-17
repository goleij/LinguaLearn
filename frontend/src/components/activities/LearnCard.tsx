import ClickableText from '../ClickableText';
import type { Activity } from '../../types';

/**
 * Teaching content. Never graded and never worth XP: the learner reads it and
 * moves on.
 */
export default function LearnCard({
  activity,
  onContinue,
}: {
  activity: Activity;
  onContinue: () => void;
}) {
  return (
    <div className="flex w-full flex-col items-center gap-3 p-4">
      <span className="rounded-[20px] bg-brand-blue px-4 py-1 text-xs font-bold uppercase tracking-wide text-white">
        Learn
      </span>

      <h3 className="text-center text-brand-blue">{activity.prompt}</h3>

      {activity.context && (
        <p className="max-w-[460px] text-center text-ink-muted">{activity.context}</p>
      )}

      {activity.examples && activity.examples.length > 0 && (
        <div className="flex w-full max-w-[460px] flex-col gap-2">
          {activity.examples.map((example) => (
            <div
              key={example.de}
              className="flex flex-wrap items-baseline justify-between gap-2 rounded-xl bg-surface-page px-4 py-3"
            >
              <ClickableText text={example.de} className="text-lg font-bold text-ink" />
              <span className="text-ink-muted">{example.en}</span>
            </div>
          ))}
        </div>
      )}

      {activity.bullets && activity.bullets.length > 0 && (
        <ul className="w-full max-w-[460px] list-none space-y-2">
          {activity.bullets.map((bullet) => (
            <li key={bullet} className="flex gap-2 text-ink">
              <span className="text-brand-green">•</span>
              <ClickableText text={bullet} />
            </li>
          ))}
        </ul>
      )}

      <button type="button" className="btn-primary mt-5" onClick={onContinue}>
        Got it
      </button>
    </div>
  );
}

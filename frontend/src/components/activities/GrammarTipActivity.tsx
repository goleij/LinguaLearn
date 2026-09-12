import ClickableText from '../ClickableText';
import type { Activity } from '../../types';

/** A short grammar explanation with examples, before any exercise asks for it. */
export default function GrammarTipActivity({
  activity,
  onContinue,
}: {
  activity: Activity;
  onContinue: () => void;
}) {
  return (
    <div className="flex w-full flex-col items-center gap-3 p-4">
      <span className="rounded-card bg-brand-orange px-4 py-1 text-xs font-bold uppercase tracking-wide text-white">
        Grammar
      </span>

      <h3 className="text-center text-brand-orange">{activity.prompt}</h3>

      {activity.context && (
        <p className="max-w-prose text-center text-ink-muted">{activity.context}</p>
      )}

      {activity.bullets && activity.bullets.length > 0 && (
        <ul className="w-full max-w-prose list-none space-y-2 p-0">
          {activity.bullets.map((bullet) => (
            <li key={bullet} className="flex gap-2 rounded-xl bg-surface-page px-4 py-3">
              <span className="text-brand-orange">•</span>
              <ClickableText text={bullet} className="text-ink" />
            </li>
          ))}
        </ul>
      )}

      <button type="button" className="btn-primary mt-2" onClick={onContinue}>
        Got it
      </button>
    </div>
  );
}

import ChoiceOptions from './ChoiceOptions';
import type { Activity } from '../../types';

/** A choice where the situation matters as much as the words. */
export default function ContextChoiceActivity({
  activity,
  onAnswer,
  disabled,
}: {
  activity: Activity;
  onAnswer: (answer: string) => void;
  disabled: boolean;
}) {
  return (
    <div className="flex w-full flex-col items-center gap-2 p-4">
      <h3 className="text-ink">{activity.instruction ?? 'Choose what fits the situation'}</h3>

      {activity.context && (
        <p className="w-full max-w-prose rounded-xl bg-surface-page px-4 py-3 text-center italic text-ink-muted">
          {activity.context}
        </p>
      )}

      <p className="text-center text-2xl font-bold text-brand-blue">{activity.prompt}</p>

      <ChoiceOptions
        activityId={activity.id}
        options={activity.options ?? []}
        onAnswer={onAnswer}
        disabled={disabled}
      />
    </div>
  );
}

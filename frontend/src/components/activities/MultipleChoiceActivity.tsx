import ChoiceOptions from './ChoiceOptions';
import type { Activity } from '../../types';

export default function MultipleChoiceActivity({
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
      <h3 className="text-ink">{activity.instruction ?? 'Choose the correct answer'}</h3>

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

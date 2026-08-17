import ChoiceOptions from './ChoiceOptions';
import ClickableText from '../ClickableText';
import type { Activity } from '../../types';

/**
 * A conversation. In the context phase it is simply read; when the activity
 * carries options it asks the learner to choose the right reply.
 */
export default function DialogueActivity({
  activity,
  onAnswer,
  onContinue,
  disabled,
}: {
  activity: Activity;
  onAnswer: (answer: string) => void;
  onContinue: () => void;
  disabled: boolean;
}) {
  const asksForReply = activity.graded && (activity.options?.length ?? 0) > 0;

  return (
    <div className="flex w-full flex-col items-center gap-3 p-4">
      {!asksForReply && (
        <span className="rounded-[20px] bg-brand-green px-4 py-1 text-xs font-bold uppercase tracking-wide text-white">
          In context
        </span>
      )}

      <h3 className="text-center text-ink">
        {activity.instruction ?? (asksForReply ? 'Continue the conversation' : 'Read the conversation')}
      </h3>

      {!asksForReply && activity.context && (
        <p className="max-w-[460px] text-center text-ink-muted">{activity.context}</p>
      )}

      <div className="flex w-full max-w-[460px] flex-col gap-2">
        {(activity.lines ?? []).map((line, index) => (
          <div key={`${line.speaker}-${index}`} className="rounded-2xl bg-surface-page px-4 py-3">
            <span className="block text-xs font-bold uppercase tracking-wide text-brand-blue">
              {line.speaker}
            </span>
            <ClickableText text={line.text} className="text-lg text-ink" />
          </div>
        ))}
      </div>

      {asksForReply ? (
        <>
          <p className="text-center text-lg text-ink-muted">{activity.prompt}</p>
          <ChoiceOptions
            activityId={activity.id}
            options={activity.options ?? []}
            onAnswer={onAnswer}
            disabled={disabled}
          />
        </>
      ) : (
        <button type="button" className="btn-primary mt-2" onClick={onContinue}>
          Got it
        </button>
      )}
    </div>
  );
}

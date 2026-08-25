import { useState } from 'react';
import WritingCoach from './WritingCoach';
import type { Activity } from '../../types';

/**
 * The APPLY task: produce your own German.
 *
 * The learner can ask the writing coach for grammar and spelling suggestions
 * before submitting; the check is optional and never blocks the answer.
 */
export default function ShortWritingActivity({
  activity,
  onAnswer,
  disabled,
}: {
  activity: Activity;
  onAnswer: (answer: string) => void;
  disabled: boolean;
}) {
  const [text, setText] = useState('');
  const minWords = activity.minWords ?? 3;
  const wordCount = text.trim().split(/\s+/).filter(Boolean).length;
  const canSubmit = wordCount >= minWords && !disabled;

  return (
    <div className="flex w-full flex-col items-center gap-2 p-4">
      <span className="rounded-[20px] bg-brand-orange px-4 py-1 text-xs font-bold uppercase tracking-wide text-white">
        Apply
      </span>

      <h3 className="text-ink">{activity.instruction ?? 'Write your answer'}</h3>

      <p className="text-center text-xl font-bold text-brand-blue">{activity.prompt}</p>

      {activity.context && (
        <p className="max-w-[460px] text-center text-ink-muted">{activity.context}</p>
      )}

      {activity.mustUseWords && activity.mustUseWords.length > 0 && (
        <div className="flex flex-wrap items-center justify-center gap-2">
          <span className="text-sm text-ink-muted">Use:</span>
          {activity.mustUseWords.map((word) => (
            <span
              key={word}
              className="rounded-[20px] bg-surface-grey px-3 py-1 text-sm font-medium text-ink"
            >
              {word}
            </span>
          ))}
        </div>
      )}

      <textarea
        className="field-input min-h-[130px] w-full max-w-[460px] resize-y text-lg"
        placeholder="Write in German…"
        value={text}
        onChange={(event) => setText(event.target.value)}
        autoFocus
      />

      <p className="text-sm text-ink-faint">
        {wordCount} / {minWords} words
      </p>

      <WritingCoach text={text} />

      <button
        type="button"
        className="btn-primary mt-3"
        disabled={!canSubmit}
        onClick={() => onAnswer(text.trim())}
      >
        Submit answer
      </button>
    </div>
  );
}

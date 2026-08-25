import { useState, type KeyboardEvent } from 'react';
import type { Activity } from '../../types';

/** The sentence is split on "___" and the input sits in the gap. Enter submits. */
export default function FillBlankActivity({
  activity,
  onAnswer,
  disabled,
}: {
  activity: Activity;
  onAnswer: (answer: string) => void;
  disabled: boolean;
}) {
  const [answer, setAnswer] = useState('');
  const parts = (activity.sentence ?? activity.prompt ?? '').split('___');
  const canSubmit = answer.trim().length > 0 && !disabled;

  const submit = () => {
    if (canSubmit) {
      onAnswer(answer.trim());
    }
  };

  const handleKeyDown = (event: KeyboardEvent<HTMLInputElement>) => {
    if (event.key === 'Enter') {
      event.preventDefault();
      submit();
    }
  };

  return (
    <div className="flex w-full flex-col items-center gap-2 p-4">
      <h3 className="text-ink">{activity.instruction ?? 'Fill in the blank'}</h3>

      <p className="text-lg text-ink-muted">{activity.prompt}</p>

      <div className="flex w-full flex-wrap items-center justify-center gap-2">
        {parts[0] && <span className="text-2xl text-ink">{parts[0]}</span>}

        <input
          className="w-[110px] rounded-lg bg-surface-grey sm:w-[150px] px-3 py-2 text-center text-2xl text-ink outline-none focus:ring-2 focus:ring-brand-blue/40"
          placeholder="..."
          value={answer}
          onChange={(event) => setAnswer(event.target.value)}
          onKeyDown={handleKeyDown}
          autoFocus
        />

        {parts.length >= 2 && parts[1] && <span className="text-2xl text-ink">{parts[1]}</span>}
      </div>

      {activity.hint && <p className="text-sm text-ink-faint">{activity.hint}</p>}

      <button type="button" className="btn-primary mt-5" disabled={!canSubmit} onClick={submit}>
        Check Answer
      </button>
    </div>
  );
}

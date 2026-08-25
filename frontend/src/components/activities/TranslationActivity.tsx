import { useState, type KeyboardEvent } from 'react';
import type { Activity } from '../../types';

/** Type the translation of the given phrase. */
export default function TranslationActivity({
  activity,
  onAnswer,
  disabled,
}: {
  activity: Activity;
  onAnswer: (answer: string) => void;
  disabled: boolean;
}) {
  const [answer, setAnswer] = useState('');
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
      <h3 className="text-ink">{activity.instruction ?? 'Translate'}</h3>

      <p className="text-center text-2xl font-bold text-brand-blue">{activity.prompt}</p>

      <input
        className="field-input w-full max-w-[420px] text-center text-lg"
        placeholder="Type your translation"
        value={answer}
        onChange={(event) => setAnswer(event.target.value)}
        onKeyDown={handleKeyDown}
        autoFocus
      />

      {activity.hint && <p className="text-sm text-ink-faint">{activity.hint}</p>}

      <button type="button" className="btn-primary mt-5" disabled={!canSubmit} onClick={submit}>
        Check Answer
      </button>
    </div>
  );
}

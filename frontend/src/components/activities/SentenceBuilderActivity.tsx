import { useState } from 'react';
import type { Activity } from '../../types';

/**
 * Tap words in the bank to build the sentence, tap them again to send them
 * back. Check is enabled only once the bank is empty.
 *
 * Words are tracked by their position in the original list, so a sentence that
 * repeats a word still works and rapid taps cannot mix the words up.
 */
export default function SentenceBuilderActivity({
  activity,
  onAnswer,
  disabled,
}: {
  activity: Activity;
  onAnswer: (answer: string) => void;
  disabled: boolean;
}) {
  const words = activity.words ?? [];
  const [selectedIndexes, setSelectedIndexes] = useState<number[]>([]);

  const availableIndexes = words
    .map((_, index) => index)
    .filter((index) => !selectedIndexes.includes(index));

  const canSubmit = selectedIndexes.length > 0 && availableIndexes.length === 0 && !disabled;

  const takeWord = (index: number) => {
    if (disabled) return;
    setSelectedIndexes((current) => (current.includes(index) ? current : [...current, index]));
  };

  const returnWord = (index: number) => {
    if (disabled) return;
    setSelectedIndexes((current) => current.filter((selected) => selected !== index));
  };

  return (
    <div className="flex w-full flex-col items-center gap-2 p-4">
      <h3 className="text-ink">{activity.instruction ?? 'Arrange the words in correct order'}</h3>

      <p className="text-lg text-ink-muted">{activity.prompt}</p>

      <div className="flex min-h-[60px] w-full flex-wrap items-center justify-center gap-[10px] rounded-xl bg-surface-soft p-4">
        {selectedIndexes.length === 0 ? (
          <p className="text-ink-faint">Tap words below to build the sentence</p>
        ) : (
          selectedIndexes.map((index) => (
            <button
              key={index}
              type="button"
              onClick={() => returnWord(index)}
              className="rounded-card bg-brand-green px-5 py-[10px] font-medium text-white transition-all duration-200"
            >
              {words[index]}
            </button>
          ))
        )}
      </div>

      <div className="mt-5 flex w-full flex-wrap justify-center gap-[10px]">
        {availableIndexes.map((index) => (
          <button
            key={index}
            type="button"
            onClick={() => takeWord(index)}
            className="rounded-card bg-brand-blue px-5 py-[10px] font-medium text-white transition-all duration-200"
          >
            {words[index]}
          </button>
        ))}
      </div>

      <div className="mt-5 flex gap-2">
        <button
          type="button"
          className="btn-secondary"
          onClick={() => setSelectedIndexes([])}
          disabled={disabled}
        >
          Clear
        </button>
        <button
          type="button"
          className="btn-primary"
          disabled={!canSubmit}
          onClick={() => onAnswer(selectedIndexes.map((index) => words[index]).join(' '))}
        >
          Check Answer
        </button>
      </div>
    </div>
  );
}

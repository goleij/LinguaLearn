import { useState } from 'react';
import type { Activity } from '../../types';

/**
 * Pick an item on the left (turns blue), then its partner on the right (both
 * turn green). Submits "left:right;left:right", the encoding the grader expects.
 */
export default function MatchPairsActivity({
  activity,
  onAnswer,
  disabled,
}: {
  activity: Activity;
  onAnswer: (answer: string) => void;
  disabled: boolean;
}) {
  const leftItems = activity.leftItems ?? [];
  const rightItems = activity.rightItems ?? [];

  const [matches, setMatches] = useState<Record<string, string>>({});
  const [selectedLeft, setSelectedLeft] = useState<string | null>(null);

  const matchedRightItems = Object.values(matches);
  const allMatched = Object.keys(matches).length === leftItems.length && leftItems.length > 0;

  const handleLeftClick = (item: string) => {
    if (disabled) return;
    setSelectedLeft(item);
  };

  const handleRightClick = (item: string) => {
    if (disabled || selectedLeft === null) return;
    setMatches((current) => ({ ...current, [selectedLeft]: item }));
    setSelectedLeft(null);
  };

  const submit = () => {
    const encoded = Object.entries(matches)
      .map(([left, right]) => `${left}:${right}`)
      .join(';');
    onAnswer(encoded);
  };

  const buttonClasses = (state: 'idle' | 'selected' | 'matched') => {
    const base =
      'w-full cursor-pointer rounded-xl p-3 text-center font-medium transition-all duration-200 sm:p-4';
    if (state === 'selected') return `${base} bg-brand-blue text-white`;
    if (state === 'matched') return `${base} bg-brand-green text-white`;
    return `${base} bg-surface-grey text-ink`;
  };

  return (
    <div className="flex w-full flex-col items-center gap-2 p-4">
      <h3 className="text-ink">{activity.instruction ?? 'Match the pairs'}</h3>

      <p className="text-lg text-ink-muted">{activity.prompt}</p>

      <div className="flex w-full justify-center gap-2">
        <div className="flex w-full min-w-0 max-w-[200px] flex-1 flex-col items-center gap-2">
          {leftItems.map((item) => (
            <div
              key={item}
              role="button"
              tabIndex={0}
              onClick={() => handleLeftClick(item)}
              onKeyDown={(event) => event.key === 'Enter' && handleLeftClick(item)}
              className={buttonClasses(
                selectedLeft === item ? 'selected' : matches[item] ? 'matched' : 'idle',
              )}
            >
              {item}
            </div>
          ))}
        </div>

        <div className="flex w-full min-w-0 max-w-[200px] flex-1 flex-col items-center gap-2">
          {rightItems.map((item) => (
            <div
              key={item}
              role="button"
              tabIndex={0}
              onClick={() => handleRightClick(item)}
              onKeyDown={(event) => event.key === 'Enter' && handleRightClick(item)}
              className={buttonClasses(matchedRightItems.includes(item) ? 'matched' : 'idle')}
            >
              {item}
            </div>
          ))}
        </div>
      </div>

      <button
        type="button"
        className="btn-primary mt-5"
        disabled={!allMatched || disabled}
        onClick={submit}
      >
        Check Answer
      </button>
    </div>
  );
}

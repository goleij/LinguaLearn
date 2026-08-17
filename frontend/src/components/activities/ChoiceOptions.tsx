import { useState } from 'react';

/**
 * The option list shared by every activity where the learner picks one answer.
 * Submits the index of the chosen option, which is what the graders expect.
 */
export default function ChoiceOptions({
  activityId,
  options,
  onAnswer,
  disabled,
}: {
  activityId: number;
  options: string[];
  onAnswer: (answer: string) => void;
  disabled: boolean;
}) {
  const [selectedIndex, setSelectedIndex] = useState<number | null>(null);

  return (
    <>
      <div className="flex w-full max-w-[400px] flex-col gap-3">
        {options.map((option, index) => (
          <label
            key={option}
            className={`flex w-full cursor-pointer items-center gap-3 rounded-xl px-4 py-3 transition-colors duration-200 ${
              selectedIndex === index
                ? 'bg-brand-green/10'
                : 'bg-surface-page hover:bg-surface-grey'
            }`}
          >
            <input
              type="radio"
              name={`activity-${activityId}`}
              className="h-4 w-4 accent-brand-green"
              checked={selectedIndex === index}
              onChange={() => setSelectedIndex(index)}
            />
            <span className="text-ink">{option}</span>
          </label>
        ))}
      </div>

      <button
        type="button"
        className="btn-primary mt-5"
        disabled={selectedIndex === null || disabled}
        onClick={() => selectedIndex !== null && onAnswer(String(selectedIndex))}
      >
        Check Answer
      </button>
    </>
  );
}

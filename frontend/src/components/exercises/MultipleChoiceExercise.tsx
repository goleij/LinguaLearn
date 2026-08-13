import { useState } from 'react';
import type { Exercise } from '../../types';

/** Submits the index of the chosen option. */
export default function MultipleChoiceExercise({
  exercise,
  onAnswer,
  disabled,
}: {
  exercise: Exercise;
  onAnswer: (answer: string) => void;
  disabled: boolean;
}) {
  const [selectedIndex, setSelectedIndex] = useState<number | null>(null);
  const options = exercise.options ?? [];

  return (
    <div className="flex w-full flex-col items-center gap-2 p-4">
      <h3 className="text-ink">Choose the correct answer</h3>

      <p className="text-center text-2xl font-bold text-brand-blue">{exercise.question}</p>

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
              name={`exercise-${exercise.id}`}
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
    </div>
  );
}

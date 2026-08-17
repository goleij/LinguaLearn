import type { ActivityPhase } from '../types';

type Stage = ActivityPhase | 'RESULT';

const STAGES: { key: Stage; label: string }[] = [
  { key: 'LEARN', label: 'Learn' },
  { key: 'CONTEXT', label: 'Context' },
  { key: 'GUIDED_PRACTICE', label: 'Guided' },
  { key: 'PRACTICE', label: 'Practice' },
  { key: 'APPLY', label: 'Apply' },
  { key: 'CHECKPOINT', label: 'Checkpoint' },
  { key: 'RESULT', label: 'Result' },
];

/**
 * Where in the lesson the learner is. Stages the lesson does not contain are
 * still shown, greyed out, so the shape of the journey stays visible.
 */
export default function PhaseIndicator({
  current,
  presentPhases,
}: {
  current: Stage;
  presentPhases?: string[];
}) {
  const currentIndex = STAGES.findIndex((stage) => stage.key === current);

  return (
    <div className="flex w-full flex-wrap items-center justify-center gap-x-1 gap-y-2">
      {STAGES.map((stage, index) => {
        const done = index < currentIndex;
        const active = index === currentIndex;
        const present =
          stage.key === 'RESULT' || !presentPhases || presentPhases.includes(stage.key);

        return (
          <div key={stage.key} className="flex items-center gap-1">
            <span
              className={`rounded-[20px] px-3 py-1 text-xs font-bold transition-colors ${
                active
                  ? 'bg-brand-green text-white'
                  : done
                    ? 'bg-brand-green/20 text-brand-green'
                    : present
                      ? 'bg-surface-grey text-ink-muted'
                      : 'bg-surface-grey/50 text-ink-faint'
              }`}
            >
              {stage.label}
            </span>
            {index < STAGES.length - 1 && (
              <span className={done ? 'text-brand-green' : 'text-ink-faint'}>›</span>
            )}
          </div>
        );
      })}
    </div>
  );
}

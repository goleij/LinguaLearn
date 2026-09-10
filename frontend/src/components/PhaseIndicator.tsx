import type { ActivityPhase } from '../types';

type Stage = ActivityPhase | 'RESULT';

const STAGES: { key: Stage; label: string; blurb: string }[] = [
  { key: 'LEARN', label: 'Learn', blurb: 'New words and rules' },
  { key: 'CONTEXT', label: 'Context', blurb: 'See them in use' },
  { key: 'GUIDED_PRACTICE', label: 'Guided practice', blurb: 'Try it with help' },
  { key: 'PRACTICE', label: 'Practice', blurb: 'Try it on your own' },
  { key: 'APPLY', label: 'Apply', blurb: 'Write something yourself' },
  { key: 'CHECKPOINT', label: 'Checkpoint', blurb: 'This part counts' },
  { key: 'RESULT', label: 'Result', blurb: 'How it went' },
];

/**
 * Where in the lesson the learner is.
 *
 * Seven labels in a row read as a wall of text and pushed the actual exercise
 * off the screen, so only the current stage is named. The stages around it are
 * a row of segments: filled behind, hollow ahead, and only as many segments as
 * this particular lesson actually contains.
 */
export default function PhaseIndicator({
  current,
  presentPhases,
  progressLabel,
}: {
  current: Stage;
  presentPhases?: string[];
  /** How far through the activities the learner is, e.g. "4 / 12". */
  progressLabel?: string;
}) {
  // Only the stages this lesson really has; RESULT always closes it
  const stages = STAGES.filter(
    (stage) =>
      stage.key === 'RESULT' || !presentPhases || presentPhases.includes(stage.key),
  );

  const currentIndex = stages.findIndex((stage) => stage.key === current);
  const active = stages[currentIndex];

  if (!active) {
    return null;
  }

  return (
    <div className="flex w-full flex-col gap-2">
      <div className="flex items-baseline justify-between gap-3">
        <div className="flex min-w-0 items-baseline gap-2">
          <span className="truncate text-sm font-bold text-ink">{active.label}</span>
          <span className="hidden truncate text-xs text-ink-faint sm:inline">{active.blurb}</span>
        </div>
        <span className="shrink-0 text-xs font-medium text-ink-faint">
          {progressLabel ?? `Step ${currentIndex + 1} of ${stages.length}`}
        </span>
      </div>

      <ol
        className="m-0 flex list-none gap-1 p-0"
        aria-label={`Lesson stage: ${active.label}, step ${currentIndex + 1} of ${stages.length}`}
      >
        {stages.map((stage, index) => (
          <li
            key={stage.key}
            title={stage.label}
            aria-current={index === currentIndex ? 'step' : undefined}
            className={`h-1.5 flex-1 rounded-full transition-colors duration-300 ${
              index < currentIndex
                ? 'bg-brand-green'
                : index === currentIndex
                  ? 'bg-brand-green/60'
                  : 'bg-surface-grey'
            }`}
          />
        ))}
      </ol>
    </div>
  );
}

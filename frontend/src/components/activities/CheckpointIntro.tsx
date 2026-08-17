import type { Activity } from '../../types';

/** The banner that announces the graded section of the lesson. */
export default function CheckpointIntro({
  activity,
  onContinue,
}: {
  activity: Activity;
  onContinue: () => void;
}) {
  return (
    <div className="flex w-full flex-col items-center gap-3 rounded-2xl bg-surface-page p-6">
      <span className="text-[48px] leading-none">🎯</span>

      <h3 className="text-brand-orange">{activity.prompt ?? 'Checkpoint'}</h3>

      {activity.context && (
        <p className="max-w-[420px] text-center text-ink-muted">{activity.context}</p>
      )}

      <p className="text-center text-sm text-ink-faint">
        Practice mistakes do not count. This part decides whether the lesson is complete.
      </p>

      <button type="button" className="btn-primary mt-2" onClick={onContinue}>
        Start checkpoint
      </button>
    </div>
  );
}

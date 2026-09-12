import type { Activity } from '../../types';
import Icon from '../Icon';

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
      <span className="flex h-14 w-14 items-center justify-center rounded-full bg-brand-orange/15 text-brand-orange">
        <Icon name="target" size={28} />
      </span>

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

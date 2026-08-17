import type { CefrLevel, CefrLevelInfo } from '../types';

/**
 * The first choice a learner makes. Levels with no content yet are shown but
 * cannot be selected, so the ladder ahead is visible.
 */
export default function LevelPicker({
  levels,
  selected,
  onSelect,
}: {
  levels: CefrLevelInfo[];
  selected: CefrLevel | null;
  onSelect: (level: CefrLevel) => void;
}) {
  return (
    <section className="mb-5 w-full rounded-2xl bg-white p-5 shadow-unit">
      <h3 className="mt-0 text-brand-green">Choose your level</h3>
      <p className="mt-0 text-ink-muted">
        Each level builds on the one before it — same situations, more German.
      </p>

      <div className="mt-4 flex flex-wrap gap-3">
        {levels.map((level) => {
          const active = level.level === selected;
          const available = level.lessonCount > 0;

          return (
            <button
              key={level.level}
              type="button"
              disabled={!available}
              onClick={() => onSelect(level.level)}
              className={`flex min-w-[150px] flex-1 flex-col items-start rounded-2xl border-2 p-4 text-left transition ${
                active
                  ? 'border-brand-green bg-brand-green/10'
                  : available
                    ? 'border-surface-grey bg-white hover:border-brand-green/50'
                    : 'cursor-not-allowed border-surface-grey bg-surface-page opacity-60'
              }`}
            >
              <span
                className={`text-xl font-bold ${active ? 'text-brand-green' : 'text-ink'}`}
              >
                {level.level}
              </span>
              <span className="text-sm font-medium text-ink">{level.label}</span>
              <span className="mt-1 text-xs text-ink-muted">{level.description}</span>
              <span className="mt-2 text-xs text-ink-faint">
                {available
                  ? `${level.lessonCount} lesson${level.lessonCount === 1 ? '' : 's'}`
                  : 'Coming soon'}
              </span>
            </button>
          );
        })}
      </div>
    </section>
  );
}

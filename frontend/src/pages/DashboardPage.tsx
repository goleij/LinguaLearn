import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import LevelPicker from '../components/LevelPicker';
import { useAuth } from '../hooks/useAuth';
import { learningService } from '../services/learningService';
import type { CefrLevel, CefrLevelInfo, CourseContent, LessonStatus } from '../types';

const LEVEL_STORAGE_KEY = 'lingualearn.level';

/** Choose a level, then work through its units and lessons. */
export default function DashboardPage() {
  const { user } = useAuth();
  const navigate = useNavigate();

  const [levels, setLevels] = useState<CefrLevelInfo[]>([]);
  const [level, setLevel] = useState<CefrLevel | null>(
    (localStorage.getItem(LEVEL_STORAGE_KEY) as CefrLevel | null) ?? null,
  );
  const [content, setContent] = useState<CourseContent | null>(null);
  const [loading, setLoading] = useState(true);

  // Load the level list once, and fall back to the first level that has content
  useEffect(() => {
    learningService
      .getLevels()
      .then((available) => {
        setLevels(available);
        setLevel((current) => {
          if (current && available.some((item) => item.level === current && item.lessonCount > 0)) {
            return current;
          }
          return available.find((item) => item.lessonCount > 0)?.level ?? null;
        });
      })
      .catch((error) => console.error('Failed to load levels', error));
  }, []);

  useEffect(() => {
    if (!level) return;

    localStorage.setItem(LEVEL_STORAGE_KEY, level);
    setLoading(true);

    learningService
      .getDashboard(level)
      .then(setContent)
      .catch((error) => console.error('Failed to load the dashboard', error))
      .finally(() => setLoading(false));
  }, [level]);

  return (
    <div className="flex h-full flex-col gap-2 p-3 sm:p-5">
      <h2 className="text-ink">Welcome back, {user?.username}!</h2>
      <p className="text-ink-muted">Continue your German learning journey</p>

      <LevelPicker levels={levels} selected={level} onSelect={setLevel} />

      {loading && <p className="text-ink-muted">Loading…</p>}

      {!loading && content && (
        <>
          <div className="mb-1 flex flex-wrap items-baseline gap-2">
            <h3 className="m-0 text-ink">{content.course.name}</h3>
            {content.course.description && (
              <span className="text-ink-muted">— {content.course.description}</span>
            )}
          </div>

          {content.units.map((unit) => (
            <section key={unit.id} className="mb-5 w-full rounded-2xl bg-white p-4 shadow-unit sm:p-5">
              <h3 className="text-brand-green">{unit.name}</h3>
              {unit.description && <p className="text-ink-muted">{unit.description}</p>}

              <div className="mt-4 flex flex-wrap items-center gap-2">
                {unit.lessons.map((lesson) => (
                  <LessonTile
                    key={lesson.id}
                    lesson={lesson}
                    onOpen={() => navigate(`/lesson/${lesson.id}`)}
                  />
                ))}
              </div>
            </section>
          ))}
        </>
      )}

      {!loading && !content && level && (
        <p className="text-ink-muted">No course for this level yet.</p>
      )}
    </div>
  );
}

/** The 120x120 lesson button: green when completed, blue when unlocked, grey when locked. */
function LessonTile({ lesson, onOpen }: { lesson: LessonStatus; onOpen: () => void }) {
  const colors = lesson.completed
    ? 'bg-brand-green text-white'
    : lesson.unlocked
      ? 'bg-brand-blue text-white'
      : 'bg-surface-grey text-ink-faint';

  const icon = lesson.completed ? '✓' : lesson.unlocked ? '▶' : '🔒';

  return (
    <div
      role={lesson.unlocked ? 'button' : undefined}
      tabIndex={lesson.unlocked ? 0 : undefined}
      onClick={lesson.unlocked ? onOpen : undefined}
      onKeyDown={(event) => {
        if (lesson.unlocked && (event.key === 'Enter' || event.key === ' ')) {
          event.preventDefault();
          onOpen();
        }
      }}
      className={`flex h-[120px] w-[120px] flex-col items-center justify-center rounded-2xl transition-transform duration-200 ${colors} ${
        lesson.unlocked ? 'cursor-pointer hover:scale-105' : 'cursor-not-allowed'
      }`}
    >
      <span className="text-2xl">{icon}</span>
      <span className="mt-2 px-2 text-center text-xs font-bold">{lesson.name}</span>
    </div>
  );
}

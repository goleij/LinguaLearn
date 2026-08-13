import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { learningService } from '../services/learningService';
import type { CourseContent, LessonStatus } from '../types';

/** Welcome text plus one card per unit with its lesson tiles. */
export default function DashboardPage() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [content, setContent] = useState<CourseContent | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    learningService
      .getDashboard()
      .then(setContent)
      .catch((error) => console.error('Failed to load the dashboard', error))
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="flex h-full flex-col gap-2 p-5">
      <h2 className="text-ink">Welcome back, {user?.username}!</h2>
      <p className="text-ink-muted">Continue your German learning journey</p>

      {loading && <p className="text-ink-muted">Loading…</p>}

      {content?.units.map((unit) => (
        <section key={unit.id} className="mb-5 w-full rounded-2xl bg-white p-5 shadow-unit">
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

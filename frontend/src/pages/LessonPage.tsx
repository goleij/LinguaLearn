import { useCallback, useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import ActivityRenderer from '../components/activities/ActivityRenderer';
import PhaseIndicator from '../components/PhaseIndicator';
import { useAuth } from '../hooks/useAuth';
import { useNotification } from '../hooks/useNotification';
import { learningService } from '../services/learningService';
import type { Activity, AnswerResult, LessonCompletion, LessonDetail } from '../types';

/**
 * Runs a lesson as Learn → Context → Guided practice → Practice → Apply →
 * Checkpoint → Result.
 *
 * Owns no XP or progress logic: every number it renders comes from the API
 * responses, which are exactly what ProgressService stored. Teaching
 * activities are never sent to the server; graded ones are.
 */
export default function LessonPage() {
  const { lessonId } = useParams();
  const navigate = useNavigate();
  const { show } = useNotification();
  const { setUser } = useAuth();

  const [lesson, setLesson] = useState<LessonDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [index, setIndex] = useState(0);
  const [feedback, setFeedback] = useState<AnswerResult | null>(null);
  const [completion, setCompletion] = useState<LessonCompletion | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const id = Number(lessonId);

  useEffect(() => {
    let active = true;

    learningService
      .getLesson(id)
      .then(async (detail) => {
        if (!active) return;

        if (!detail.unlocked) {
          show('This lesson is locked!', { position: 'middle' });
          navigate('/', { replace: true });
          return;
        }

        if (detail.activities.length > 0) {
          // A fresh run: the service clears the per-attempt counters, streak included
          await learningService.startAttempt(id);
          setUser((current) => (current ? { ...current, currentStreak: 0 } : current));
        }

        if (active) setLesson(detail);
      })
      .catch(() => {
        if (!active) return;
        show('Lesson not found', { position: 'middle' });
        navigate('/', { replace: true });
      })
      .finally(() => {
        if (active) setLoading(false);
      });

    return () => {
      active = false;
    };
  }, [id, navigate, show, setUser]);

  // Running past the last activity finishes the lesson
  useEffect(() => {
    if (!lesson || lesson.activities.length === 0) return;
    if (index < lesson.activities.length || completion) return;

    learningService
      .completeLesson(id)
      .then((result) => {
        setCompletion(result);
        setUser((current) => (current ? { ...current, totalXp: result.userTotalXp } : current));
      })
      .catch((error) => console.error('Failed to complete the lesson', error));
  }, [index, lesson, completion, id, setUser]);

  const handleAnswer = useCallback(
    async (answer: string) => {
      if (!lesson || submitting) return;

      setSubmitting(true);
      try {
        const activity = lesson.activities[index];
        const result = await learningService.submitAnswer(id, activity.id, answer);
        setFeedback(result);
        // Keep the navbar badges in sync with what was just stored
        setUser((current) =>
          current
            ? { ...current, totalXp: result.userTotalXp, currentStreak: result.currentStreak }
            : current,
        );
      } catch (error) {
        console.error('Failed to submit the answer', error);
        show('Could not submit your answer. Please try again.', { position: 'middle' });
      } finally {
        setSubmitting(false);
      }
    },
    [lesson, index, id, submitting, setUser, show],
  );

  const goToNext = () => {
    setFeedback(null);
    setIndex((current) => current + 1);
  };

  const restart = async () => {
    await learningService.startAttempt(id);
    setUser((current) => (current ? { ...current, currentStreak: 0 } : current));
    setIndex(0);
    setFeedback(null);
    setCompletion(null);
  };

  const presentPhases = useMemo(
    () =>
      lesson
        ? Object.entries(lesson.phaseCounts)
            .filter(([, count]) => count > 0)
            .map(([phase]) => phase)
        : [],
    [lesson],
  );

  if (loading) {
    return <div className="p-3 text-ink-muted sm:p-5">Loading…</div>;
  }

  if (!lesson) {
    return null;
  }

  if (lesson.activities.length === 0) {
    return (
      <div className="flex h-full flex-col items-center p-3 sm:p-5">
        <div className="flex flex-col items-center rounded-[20px] bg-white p-5">
          <h2 className="text-ink">No activities available for this lesson</h2>
          <button type="button" className="btn-secondary mt-4" onClick={() => navigate('/')}>
            Back to Dashboard
          </button>
        </div>
      </div>
    );
  }

  const total = lesson.activities.length;
  const current = lesson.activities[Math.min(index, total - 1)];
  const stage = completion ? 'RESULT' : current.phase;
  const progress = Math.min(index / total, 1);

  return (
    <div className="flex h-full flex-col items-center p-3 sm:p-5">
      <div className="flex w-full max-w-[600px] flex-col gap-4 rounded-[20px] bg-white p-4 shadow-card sm:p-5">
        <div className="flex w-full items-center gap-3">
          <button
            type="button"
            onClick={() => navigate('/')}
            className="shrink-0 rounded-[10px] bg-surface-grey px-3 py-2 text-ink transition hover:brightness-95 sm:px-4"
          >
            ← Back
          </button>
          <h2 className="m-0 min-w-0 flex-1 text-lg text-brand-green sm:text-2xl">{lesson.name}</h2>
          {lesson.cefrLevel && (
            <span className="rounded-[20px] bg-brand-blue px-3 py-1 text-xs font-bold text-white">
              {lesson.cefrLevel}
            </span>
          )}
        </div>

        <PhaseIndicator current={stage} presentPhases={presentPhases} />

        <div className="flex w-full items-center gap-3">
          <div className="h-3 flex-1 overflow-hidden rounded-md bg-surface-grey">
            <div
              className="h-full rounded-md bg-brand-green transition-[width] duration-300"
              style={{ width: `${progress * 100}%` }}
            />
          </div>
          <span className="min-w-[60px] text-ink-muted">
            {Math.min(index, total)} / {total}
          </span>
        </div>

        <div className="w-full">
          {completion ? (
            <ResultCard
              completion={completion}
              lesson={lesson}
              onRetry={restart}
              onHome={() => navigate('/')}
            />
          ) : feedback ? (
            <FeedbackCard result={feedback} onContinue={goToNext} />
          ) : index < total ? (
            <>
              <GuidedHint activity={current} />
              <ActivityRenderer
                key={current.id}
                activity={current}
                onAnswer={handleAnswer}
                onContinue={goToNext}
                disabled={submitting}
              />
            </>
          ) : (
            // The last answer is in; the result is on its way
            <p className="p-4 text-center text-ink-muted">Finishing lesson…</p>
          )}
        </div>
      </div>
    </div>
  );
}

/** Guided practice offers help before the answer, not after. */
function GuidedHint({ activity }: { activity: Activity }) {
  const [open, setOpen] = useState(false);

  if (activity.phase !== 'GUIDED_PRACTICE' || !activity.hint) {
    return null;
  }

  return (
    <div className="mb-2 flex flex-col items-center">
      {open ? (
        <p className="m-0 rounded-xl bg-brand-yellow/20 px-4 py-2 text-center text-ink">
          💡 {activity.hint}
        </p>
      ) : (
        <button
          type="button"
          onClick={() => setOpen(true)}
          className="text-sm font-medium text-brand-blue hover:underline"
        >
          Need a hint?
        </button>
      )}
    </div>
  );
}

function FeedbackCard({ result, onContinue }: { result: AnswerResult; onContinue: () => void }) {
  const correct = result.correct;
  const forgiving = result.phase === 'PRACTICE' || result.phase === 'GUIDED_PRACTICE';

  return (
    <div
      className={`flex w-full flex-col items-center gap-2 rounded-2xl p-5 ${
        correct ? 'bg-feedback-correct' : 'bg-feedback-wrong'
      }`}
    >
      <span
        className={`text-[48px] leading-none ${correct ? 'text-brand-green' : 'text-feedback-error'}`}
      >
        {correct ? '✓' : '✗'}
      </span>

      <h3 className={`m-0 ${correct ? 'text-brand-green' : 'text-feedback-error'}`}>
        {correct ? 'Correct!' : 'Not quite right'}
      </h3>

      {/* The specific reason, e.g. "2 of 3 pairs were right" */}
      {result.feedback && <p className="m-0 text-center font-medium text-ink">{result.feedback}</p>}

      {!correct && result.correctAnswer && (
        <p className="m-0 font-bold text-ink">Correct answer: {result.correctAnswer}</p>
      )}

      {result.explanation && (
        <p className="m-0 text-center text-ink-muted">{result.explanation}</p>
      )}

      {!correct && result.hint && (
        <p className="m-0 text-center text-sm text-ink-muted">💡 {result.hint}</p>
      )}

      {!correct && forgiving && (
        <p className="m-0 text-center text-sm text-ink-faint">
          This is practice — mistakes here do not affect the checkpoint.
        </p>
      )}

      <div className="flex flex-wrap items-center justify-center gap-2">
        {result.skill && (
          <span className="rounded-[20px] bg-white/70 px-3 py-1 text-xs font-medium text-ink-muted">
            {result.skill.replace(/_/g, ' ').toLowerCase()}
          </span>
        )}
        {result.xpAwarded > 0 && (
          <span className="rounded-[20px] bg-brand-yellow px-[15px] py-[5px] font-bold text-ink">
            +{result.xpAwarded} XP
          </span>
        )}
      </div>

      <button
        type="button"
        onClick={onContinue}
        className={`mt-3 w-full rounded-xl px-6 py-4 font-bold text-white transition sm:w-auto sm:px-10 ${
          correct ? 'bg-brand-green' : 'bg-brand-blue'
        }`}
      >
        Continue
      </button>
    </div>
  );
}

function ResultCard({
  completion,
  lesson,
  onRetry,
  onHome,
}: {
  completion: LessonCompletion;
  lesson: LessonDetail;
  onRetry: () => void;
  onHome: () => void;
}) {
  const practiceCorrect = completion.correctAnswers - completion.checkpointCorrectAnswers;
  const practiceTotal = completion.totalAnswers - completion.checkpointTotalAnswers;

  // Which skills this lesson actually trained
  const skills = Array.from(
    new Set(
      lesson.activities
        .filter((activity) => activity.graded && activity.skill)
        .map((activity) => activity.skill as string),
    ),
  );

  return (
    <div className="flex w-full flex-col items-center gap-2 rounded-2xl bg-feedback-correct p-5">
      <span className="text-[64px] leading-none">🏆</span>

      <h2 className="m-0 text-brand-green">Lesson Complete!</h2>

      {completion.hasCheckpoint ? (
        <div className="my-2 flex w-full max-w-[380px] flex-col gap-2">
          <div className="flex items-center justify-between rounded-xl bg-white/70 px-4 py-2">
            <span className="text-ink-muted">Practice</span>
            <span className="font-bold text-ink">
              {practiceCorrect}/{practiceTotal}
            </span>
          </div>
          <div className="flex items-center justify-between rounded-xl bg-white/70 px-4 py-2">
            <span className="text-ink-muted">Checkpoint</span>
            <span className="font-bold text-ink">
              {completion.checkpointCorrectAnswers}/{completion.checkpointTotalAnswers} (
              {Math.round(completion.scorePercentage)}%)
            </span>
          </div>
        </div>
      ) : (
        <p className="text-lg text-ink">
          Score: {completion.correctAnswers}/{completion.totalAnswers} (
          {Math.round(completion.scorePercentage)}%)
        </p>
      )}

      {completion.practiceMode ? (
        <span className="rounded-[25px] bg-ink-muted px-[25px] py-[10px] text-base font-bold text-white">
          🔄 Practice Mode - No XP Earned
        </span>
      ) : (
        <span className="rounded-[25px] bg-brand-yellow px-[25px] py-[10px] text-xl font-bold text-ink">
          +{completion.lessonXpEarned} XP earned!
        </span>
      )}

      {skills.length > 0 && (
        <div className="mt-1 flex flex-wrap items-center justify-center gap-2">
          <span className="text-sm text-ink-muted">Skills practiced:</span>
          {skills.map((skill) => (
            <span
              key={skill}
              className="rounded-[20px] bg-white/70 px-3 py-1 text-xs font-medium text-ink"
            >
              {skill.replace(/_/g, ' ').toLowerCase()}
            </span>
          ))}
        </div>
      )}

      <p className={`font-bold ${completion.passed ? 'text-brand-green' : 'text-brand-orange'}`}>
        {completion.passed
          ? "Great job! You've unlocked the next lesson!"
          : `You need ${Math.round(completion.passThreshold)}% in the checkpoint to pass. Try again!`}
      </p>

      <div className="mt-2 flex w-full flex-col gap-2 sm:w-auto sm:flex-row">
        <button
          type="button"
          onClick={onRetry}
          className="w-full rounded-xl bg-brand-blue px-6 py-[15px] font-medium text-white transition sm:w-auto sm:px-[30px]"
        >
          Practice Again
        </button>
        <button
          type="button"
          onClick={onHome}
          className="w-full rounded-xl bg-brand-green px-6 py-[15px] font-medium text-white transition sm:w-auto sm:px-[30px]"
        >
          Back to Dashboard
        </button>
      </div>
    </div>
  );
}

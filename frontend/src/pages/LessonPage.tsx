import { useCallback, useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import FillBlankExercise from '../components/exercises/FillBlankExercise';
import MatchPairsExercise from '../components/exercises/MatchPairsExercise';
import MultipleChoiceExercise from '../components/exercises/MultipleChoiceExercise';
import SentenceOrderExercise from '../components/exercises/SentenceOrderExercise';
import { useAuth } from '../hooks/useAuth';
import { useNotification } from '../hooks/useNotification';
import { learningService } from '../services/learningService';
import type { AnswerResult, Exercise, LessonCompletion, LessonDetail } from '../types';

/**
 * Owns no XP or progress logic: every number it renders comes from the API
 * responses, which are exactly what ProgressService stored.
 */
export default function LessonPage() {
  const { lessonId } = useParams();
  const navigate = useNavigate();
  const { show } = useNotification();
  const { setUser } = useAuth();

  const [lesson, setLesson] = useState<LessonDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [exerciseIndex, setExerciseIndex] = useState(0);
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

        if (detail.exercises.length > 0) {
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

  // Finishing the last exercise completes the lesson
  useEffect(() => {
    if (!lesson || lesson.exercises.length === 0) return;
    if (exerciseIndex < lesson.exercises.length || completion) return;

    learningService
      .completeLesson(id)
      .then((result) => {
        setCompletion(result);
        setUser((current) => (current ? { ...current, totalXp: result.userTotalXp } : current));
      })
      .catch((error) => console.error('Failed to complete the lesson', error));
  }, [exerciseIndex, lesson, completion, id, setUser]);

  const handleAnswer = useCallback(
    async (answer: string) => {
      if (!lesson || submitting) return;

      setSubmitting(true);
      try {
        const exercise = lesson.exercises[exerciseIndex];
        const result = await learningService.submitAnswer(id, exercise.id, answer);
        setFeedback(result);
        // Keep the navbar badges in sync with what was just stored: the XP
        // total and the streak, which grows on a correct answer and drops back
        // to zero on a wrong one.
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
    [lesson, exerciseIndex, id, submitting, setUser, show],
  );

  const restart = async () => {
    await learningService.startAttempt(id);
    setUser((current) => (current ? { ...current, currentStreak: 0 } : current));
    setExerciseIndex(0);
    setFeedback(null);
    setCompletion(null);
  };

  if (loading) {
    return <div className="p-5 text-ink-muted">Loading…</div>;
  }

  if (!lesson) {
    return null;
  }

  if (lesson.exercises.length === 0) {
    return (
      <div className="flex h-full flex-col items-center p-5">
        <div className="flex flex-col items-center rounded-[20px] bg-white p-5">
          <h2 className="text-ink">No exercises available for this lesson</h2>
          <button type="button" className="btn-secondary mt-4" onClick={() => navigate('/')}>
            Back to Dashboard
          </button>
        </div>
      </div>
    );
  }

  const total = lesson.exercises.length;
  const progress = Math.min(exerciseIndex / total, 1);

  return (
    <div className="flex h-full flex-col items-center p-5">
      <div className="flex w-full max-w-[600px] flex-col gap-4 rounded-[20px] bg-white p-5 shadow-card">
        <div className="flex w-full items-center gap-3">
          <button
            type="button"
            onClick={() => navigate('/')}
            className="rounded-[10px] bg-surface-grey px-4 py-2 text-ink transition hover:brightness-95"
          >
            ← Back
          </button>
          <h2 className="flex-1 text-brand-green">{lesson.name}</h2>
        </div>

        <div className="flex w-full items-center gap-3">
          <div className="h-3 flex-1 overflow-hidden rounded-md bg-surface-grey">
            <div
              className="h-full rounded-md bg-brand-green transition-[width] duration-300"
              style={{ width: `${progress * 100}%` }}
            />
          </div>
          <span className="min-w-[60px] text-ink-muted">
            {Math.min(exerciseIndex, total)} / {total}
          </span>
        </div>

        <div className="w-full">
          {completion ? (
            <CompletionCard completion={completion} onRetry={restart} onHome={() => navigate('/')} />
          ) : feedback ? (
            <FeedbackCard
              result={feedback}
              onContinue={() => {
                setFeedback(null);
                setExerciseIndex((index) => index + 1);
              }}
            />
          ) : exerciseIndex < total ? (
            <ExerciseRenderer
              exercise={lesson.exercises[exerciseIndex]}
              onAnswer={handleAnswer}
              disabled={submitting}
            />
          ) : (
            // The last answer is in; the completion result is on its way
            <p className="p-4 text-center text-ink-muted">Finishing lesson…</p>
          )}
        </div>
      </div>
    </div>
  );
}

function ExerciseRenderer({
  exercise,
  onAnswer,
  disabled,
}: {
  exercise: Exercise;
  onAnswer: (answer: string) => void;
  disabled: boolean;
}) {
  const props = { exercise, onAnswer, disabled };

  switch (exercise.type) {
    case 'FILL_BLANK':
      return <FillBlankExercise key={exercise.id} {...props} />;
    case 'MATCH_PAIRS':
      return <MatchPairsExercise key={exercise.id} {...props} />;
    case 'SENTENCE_ORDER':
      return <SentenceOrderExercise key={exercise.id} {...props} />;
    case 'MULTIPLE_CHOICE':
    default:
      return <MultipleChoiceExercise key={exercise.id} {...props} />;
  }
}

function FeedbackCard({ result, onContinue }: { result: AnswerResult; onContinue: () => void }) {
  const correct = result.correct;

  return (
    <div
      className={`flex w-full flex-col items-center rounded-2xl p-5 ${
        correct ? 'bg-feedback-correct' : 'bg-feedback-wrong'
      }`}
    >
      <span
        className={`text-[48px] leading-none ${correct ? 'text-brand-green' : 'text-feedback-error'}`}
      >
        {correct ? '✓' : '✗'}
      </span>

      <h3 className={correct ? 'text-brand-green' : 'text-feedback-error'}>
        {correct ? 'Correct!' : 'Not quite right'}
      </h3>

      {!correct && result.correctAnswer && (
        <p className="font-bold text-ink">Correct answer: {result.correctAnswer}</p>
      )}

      {result.explanation && <p className="text-center text-ink-muted">{result.explanation}</p>}

      {result.xpAwarded > 0 && (
        <span className="rounded-[20px] bg-brand-yellow px-[15px] py-[5px] font-bold text-ink">
          +{result.xpAwarded} XP
        </span>
      )}

      <button
        type="button"
        onClick={onContinue}
        className={`mt-5 rounded-xl px-10 py-4 font-bold text-white transition ${
          correct ? 'bg-brand-green' : 'bg-brand-blue'
        }`}
      >
        Continue
      </button>
    </div>
  );
}

function CompletionCard({
  completion,
  onRetry,
  onHome,
}: {
  completion: LessonCompletion;
  onRetry: () => void;
  onHome: () => void;
}) {
  return (
    <div className="flex w-full flex-col items-center rounded-2xl bg-feedback-correct p-5">
      <span className="text-[64px] leading-none">🏆</span>

      <h2 className="text-brand-green">Lesson Complete!</h2>

      <p className="text-lg text-ink">
        Score: {completion.correctAnswers}/{completion.totalAnswers} (
        {Math.round(completion.scorePercentage)}%)
      </p>

      {completion.practiceMode ? (
        <span className="rounded-[25px] bg-ink-muted px-[25px] py-[10px] text-base font-bold text-white">
          🔄 Practice Mode - No XP Earned
        </span>
      ) : (
        <span className="rounded-[25px] bg-brand-yellow px-[25px] py-[10px] text-xl font-bold text-ink">
          +{completion.lessonXpEarned} XP earned!
        </span>
      )}

      <p
        className={`font-bold ${completion.passed ? 'text-brand-green' : 'text-brand-orange'}`}
      >
        {completion.passed
          ? "Great job! You've unlocked the next lesson!"
          : `You need ${Math.round(completion.passThreshold)}% to pass. Try again!`}
      </p>

      <div className="mt-2 flex gap-2">
        <button
          type="button"
          onClick={onRetry}
          className="rounded-xl bg-brand-blue px-[30px] py-[15px] font-medium text-white transition"
        >
          Practice Again
        </button>
        <button
          type="button"
          onClick={onHome}
          className="rounded-xl bg-brand-green px-[30px] py-[15px] font-medium text-white transition"
        >
          Back to Dashboard
        </button>
      </div>
    </div>
  );
}

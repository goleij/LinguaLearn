import { useState } from 'react';
import { toolsService } from '../../services/toolsService';
import type { GrammarFeedback } from '../../types';

/**
 * "Check my German": sends the draft to our backend, which asks LanguageTool.
 *
 * Only ever runs when the learner presses the button, and never blocks
 * submitting the answer — if the checker is unavailable the learner is told and
 * can carry on.
 */
export default function WritingCoach({ text }: { text: string }) {
  const [feedback, setFeedback] = useState<GrammarFeedback | null>(null);
  const [checking, setChecking] = useState(false);

  const check = async () => {
    setChecking(true);
    try {
      setFeedback(await toolsService.checkWriting(text));
    } catch {
      setFeedback({
        available: false,
        text,
        issueCount: 0,
        issues: [],
        message: 'The grammar checker could not be reached. Your answer can still be submitted.',
      });
    } finally {
      setChecking(false);
    }
  };

  return (
    <div className="w-full max-w-[460px]">
      <button
        type="button"
        className="btn-secondary w-full"
        onClick={check}
        disabled={checking || text.trim().length === 0}
      >
        {checking ? 'Checking…' : 'Check my German'}
      </button>

      {feedback && (
        <div className="mt-3 rounded-xl bg-surface-page p-4">
          <p className={`m-0 font-bold ${feedback.issueCount === 0 ? 'text-brand-green' : 'text-ink'}`}>
            {feedback.message}
          </p>

          <div className="mt-3 flex flex-col gap-3">
            {feedback.issues.map((issue, index) => (
              <div key={`${issue.offset}-${index}`} className="rounded-xl bg-white p-3">
                {issue.excerpt && (
                  <p className="m-0 font-bold text-feedback-error">„{issue.excerpt}“</p>
                )}
                <p className="m-0 text-sm text-ink">{issue.message}</p>

                {issue.suggestions.length > 0 && (
                  <p className="m-0 mt-1 text-sm text-ink-muted">
                    Try: {issue.suggestions.map((suggestion) => `„${suggestion}“`).join(', ')}
                  </p>
                )}

                {issue.category && (
                  <span className="mt-2 inline-block rounded-[10px] bg-surface-grey px-2 py-[2px] text-xs text-ink-muted">
                    {issue.category}
                  </span>
                )}
              </div>
            ))}
          </div>

          <p className="mt-3 text-xs text-ink-faint">
            Suggestions come from LanguageTool. They are hints, not marking — you decide.
          </p>
        </div>
      )}
    </div>
  );
}

import { useState } from 'react';
import ClickableText from '../ClickableText';
import { vocabularyService } from '../../services/vocabularyService';
import type { Activity } from '../../types';
import Icon from '../Icon';

/**
 * The word list of a lesson.
 *
 * Every German word opens the Word Explorer, and because this list is the one
 * place that holds both halves of the pair, it hands the English over with it
 * so the panel can lead with the meaning rather than with a German definition.
 * Each row can also be saved straight to the word bank.
 */
export default function VocabularyActivity({
  activity,
  onContinue,
}: {
  activity: Activity;
  onContinue: () => void;
}) {
  const words = activity.vocabulary ?? [];

  return (
    <div className="flex w-full flex-col items-center gap-3 p-4">
      <span className="rounded-full bg-brand-blue px-4 py-1 text-xs font-bold uppercase tracking-wide text-white">
        Vocabulary
      </span>

      <h3 className="text-center text-brand-blue">{activity.prompt}</h3>

      {activity.context && (
        <p className="max-w-prose text-center text-ink-muted">{activity.context}</p>
      )}

      <ul className="m-0 flex w-full max-w-prose list-none flex-col gap-2 p-0">
        {words.map((item) => (
          <WordRow key={item.de} german={item.de} english={item.en} topic={activity.topic} />
        ))}
      </ul>

      <p className="text-xs text-ink-faint">Tap a German word to look it up, or the star to save it.</p>

      <button type="button" className="btn-primary mt-2" onClick={onContinue}>
        Got it
      </button>
    </div>
  );
}

function WordRow({
  german,
  english,
  topic,
}: {
  german: string;
  english: string;
  topic?: string;
}) {
  const [saved, setSaved] = useState(false);
  const [saving, setSaving] = useState(false);

  const save = async () => {
    setSaving(true);
    try {
      await vocabularyService.add({ german, english, source: 'VOCABULARY', topic });
      setSaved(true);
    } catch (error) {
      console.error('Could not save the word', error);
    } finally {
      setSaving(false);
    }
  };

  return (
    <li className="flex flex-wrap items-center gap-2 rounded-2xl bg-surface-page px-4 py-3 transition-colors hover:bg-surface-soft">
      <ClickableText
        text={german}
        className="text-lg font-bold text-ink"
        context={{ meaning: english, topic, source: 'VOCABULARY' }}
      />

      <span className="ml-auto text-ink-muted">{english}</span>

      <button
        type="button"
        onClick={save}
        disabled={saved || saving}
        aria-label={saved ? `${german} is saved` : `Save ${german} to my words`}
        title={saved ? 'Saved to your words' : 'Save to my words'}
        className={`flex h-8 w-8 shrink-0 items-center justify-center rounded-full text-base transition ${
          saved
            ? 'bg-brand-yellow text-white'
            : 'bg-surface-grey text-ink-muted hover:bg-brand-yellow hover:text-white'
        } disabled:cursor-default`}
      >
        <Icon name="star" size={16} filled={saved} />
      </button>
    </li>
  );
}

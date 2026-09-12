import { useEffect, useState } from 'react';
import ChoiceOptions from './ChoiceOptions';
import type { Activity } from '../../types';
import Icon from '../Icon';

/**
 * Plays the German line with the browser's own speech synthesis, then asks
 * what it meant. The question is the meaning, not a transcription, so sending
 * the spoken text to the browser does not give the answer away.
 */
export default function ListeningActivity({
  activity,
  onAnswer,
  disabled,
}: {
  activity: Activity;
  onAnswer: (answer: string) => void;
  disabled: boolean;
}) {
  const [supported, setSupported] = useState(true);

  useEffect(() => {
    setSupported(typeof window !== 'undefined' && 'speechSynthesis' in window);
  }, []);

  const play = () => {
    if (!supported || !activity.audioText) return;
    const utterance = new SpeechSynthesisUtterance(activity.audioText);
    utterance.lang = activity.audioLang ?? 'de-DE';
    utterance.rate = 0.9;
    window.speechSynthesis.cancel();
    window.speechSynthesis.speak(utterance);
  };

  return (
    <div className="flex w-full flex-col items-center gap-2 p-4">
      <h3 className="text-ink">{activity.instruction ?? 'Listen and choose the meaning'}</h3>

      <button
        type="button"
        onClick={play}
        className="my-2 flex h-[88px] w-[88px] items-center justify-center rounded-full bg-brand-blue text-[36px] text-white transition hover:brightness-95"
        aria-label="Play the audio"
      >
        <Icon name="volume" size={26} />
      </button>

      {!supported && (
        // Without speech synthesis the learner would be stuck, so show the line
        <p className="text-lg font-bold text-ink">{activity.audioText}</p>
      )}

      <p className="text-center text-lg text-ink-muted">{activity.prompt}</p>

      <ChoiceOptions
        activityId={activity.id}
        options={activity.options ?? []}
        onAnswer={onAnswer}
        disabled={disabled}
      />
    </div>
  );
}

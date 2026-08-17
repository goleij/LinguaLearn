import CheckpointIntro from './CheckpointIntro';
import ContextChoiceActivity from './ContextChoiceActivity';
import DialogueActivity from './DialogueActivity';
import FillBlankActivity from './FillBlankActivity';
import GrammarTipActivity from './GrammarTipActivity';
import LearnCard from './LearnCard';
import ListeningActivity from './ListeningActivity';
import MatchPairsActivity from './MatchPairsActivity';
import MultipleChoiceActivity from './MultipleChoiceActivity';
import RealExampleActivity from './RealExampleActivity';
import SentenceBuilderActivity from './SentenceBuilderActivity';
import ShortWritingActivity from './ShortWritingActivity';
import TranslationActivity from './TranslationActivity';
import VocabularyActivity from './VocabularyActivity';
import type { Activity } from '../../types';

interface ActivityRendererProps {
  activity: Activity;
  /** Called by graded activities with the encoded answer. */
  onAnswer: (answer: string) => void;
  /** Called by activities that are read and acknowledged rather than answered. */
  onContinue: () => void;
  disabled?: boolean;
}

/**
 * The one place that maps an activity type to a component.
 *
 * Adding a type means adding a component and one case here; the lesson flow,
 * the progress handling and the API stay untouched.
 */
export default function ActivityRenderer({
  activity,
  onAnswer,
  onContinue,
  disabled = false,
}: ActivityRendererProps) {
  const graded = { activity, onAnswer, disabled };

  switch (activity.type) {
    // --- teaching -----------------------------------------------------------
    case 'LEARN_CARD':
      return <LearnCard activity={activity} onContinue={onContinue} />;
    case 'VOCABULARY':
      return <VocabularyActivity activity={activity} onContinue={onContinue} />;
    case 'GRAMMAR_TIP':
      return <GrammarTipActivity activity={activity} onContinue={onContinue} />;
    case 'REAL_EXAMPLE':
      return <RealExampleActivity activity={activity} onContinue={onContinue} />;
    case 'CHECKPOINT':
      return <CheckpointIntro activity={activity} onContinue={onContinue} />;

    // A dialogue either just shows a conversation or asks for the reply
    case 'DIALOGUE':
      return <DialogueActivity {...graded} onContinue={onContinue} />;

    // --- interactive --------------------------------------------------------
    case 'MULTIPLE_CHOICE':
      return <MultipleChoiceActivity {...graded} />;
    case 'CONTEXT_CHOICE':
      return <ContextChoiceActivity {...graded} />;
    case 'LISTENING':
      return <ListeningActivity {...graded} />;
    case 'FILL_BLANK':
      return <FillBlankActivity {...graded} />;
    case 'TRANSLATION':
      return <TranslationActivity {...graded} />;
    case 'SENTENCE_BUILDER':
      return <SentenceBuilderActivity {...graded} />;
    case 'MATCH_PAIRS':
      return <MatchPairsActivity {...graded} />;
    case 'SHORT_WRITING':
      return <ShortWritingActivity {...graded} />;

    default:
      return (
        <p className="p-4 text-center text-ink-muted">
          This activity type is not supported yet.
        </p>
      );
  }
}

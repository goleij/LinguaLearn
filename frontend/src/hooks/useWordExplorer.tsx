import { createContext, useCallback, useContext, useMemo, useState, type ReactNode } from 'react';
import WordPopover from '../components/WordPopover';
import { toolsService } from '../services/toolsService';
import { vocabularyService } from '../services/vocabularyService';
import type { WordInfo, WordSource } from '../types';

/** What the page already knows about the word it is asking about. */
export interface WordContext {
  /** The English the lesson teaches, when the word came from a word list. */
  meaning?: string;
  topic?: string | null;
  lessonName?: string | null;
  source?: WordSource;
}

interface WordExplorerContextValue {
  /** Opens the panel and looks the word up. */
  explore: (word: string, context?: WordContext) => void;
}

const WordExplorerContext = createContext<WordExplorerContextValue | undefined>(undefined);

type SaveState = 'idle' | 'saving' | 'saved' | 'failed';

/**
 * Any German word in the app can be clicked; this owns the lookup and the
 * panel so individual activities do not have to.
 *
 * Wiktionary is a German-German dictionary, so on its own it answers the wrong
 * question for a beginner: looking up "Hallo" returns the noun ("lautes
 * Rufen"), not the greeting the lesson just taught. Where the caller knows the
 * English — a vocabulary list does — that meaning is shown first and the
 * dictionary entry becomes the extra detail underneath.
 */
export function WordExplorerProvider({ children }: { children: ReactNode }) {
  const [word, setWord] = useState<string | null>(null);
  const [context, setContext] = useState<WordContext>({});
  const [info, setInfo] = useState<WordInfo | null>(null);
  const [loading, setLoading] = useState(false);
  const [failed, setFailed] = useState(false);
  const [saveState, setSaveState] = useState<SaveState>('idle');

  const explore = useCallback((clicked: string, wordContext: WordContext = {}) => {
    const cleaned = clicked.trim();
    if (!cleaned) return;

    setWord(cleaned);
    setContext(wordContext);
    setInfo(null);
    setFailed(false);
    setSaveState('idle');
    setLoading(true);

    toolsService
      .lookupWord(cleaned)
      .then(setInfo)
      .catch(() => setFailed(true))
      .finally(() => setLoading(false));
  }, []);

  const close = useCallback(() => {
    setWord(null);
    setInfo(null);
    setFailed(false);
    setSaveState('idle');
  }, []);

  /**
   * Files the word for review. The English the lesson taught is preferred; if
   * there is none, the first Wiktionary sense is offered instead, which is at
   * least a definition the learner just read.
   */
  const save = useCallback(
    async (meaning: string) => {
      if (!word) return;

      setSaveState('saving');
      try {
        await vocabularyService.add({
          german: word,
          english: meaning,
          source: context.source ?? (context.meaning ? 'VOCABULARY' : 'WORD_EXPLORER'),
          topic: context.topic ?? null,
          lessonName: context.lessonName ?? null,
        });
        setSaveState('saved');
      } catch {
        setSaveState('failed');
      }
    },
    [word, context],
  );

  const value = useMemo(() => ({ explore }), [explore]);

  return (
    <WordExplorerContext.Provider value={value}>
      {children}
      {word && (
        <WordPopover
          word={word}
          knownMeaning={context.meaning}
          info={info}
          loading={loading}
          failed={failed}
          saveState={saveState}
          onSave={save}
          onClose={close}
        />
      )}
    </WordExplorerContext.Provider>
  );
}

export function useWordExplorer(): WordExplorerContextValue {
  const context = useContext(WordExplorerContext);
  if (!context) {
    throw new Error('useWordExplorer must be used inside a WordExplorerProvider');
  }
  return context;
}

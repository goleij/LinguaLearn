import { createContext, useCallback, useContext, useMemo, useState, type ReactNode } from 'react';
import WordPopover from '../components/WordPopover';
import { toolsService } from '../services/toolsService';
import type { WordInfo } from '../types';

interface WordExplorerContextValue {
  /** Opens the panel and looks the word up. */
  explore: (word: string) => void;
}

const WordExplorerContext = createContext<WordExplorerContextValue | undefined>(undefined);

/**
 * Any German word in the app can be clicked; this owns the lookup and the
 * panel so individual activities do not have to.
 */
export function WordExplorerProvider({ children }: { children: ReactNode }) {
  const [word, setWord] = useState<string | null>(null);
  const [info, setInfo] = useState<WordInfo | null>(null);
  const [loading, setLoading] = useState(false);
  const [failed, setFailed] = useState(false);

  const explore = useCallback((clicked: string) => {
    const cleaned = clicked.trim();
    if (!cleaned) return;

    setWord(cleaned);
    setInfo(null);
    setFailed(false);
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
  }, []);

  const value = useMemo(() => ({ explore }), [explore]);

  return (
    <WordExplorerContext.Provider value={value}>
      {children}
      {word && (
        <WordPopover word={word} info={info} loading={loading} failed={failed} onClose={close} />
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

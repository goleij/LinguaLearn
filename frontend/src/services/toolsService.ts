import { api } from './api';
import type { GrammarFeedback, WordInfo } from '../types';

/**
 * The learner-facing tools. Both sit behind our own backend: the browser never
 * talks to Wiktionary or LanguageTool directly, and no endpoint or
 * configuration is exposed here.
 */
export const toolsService = {
  /** Word Explorer: German Wiktionary through the backend. */
  lookupWord: (word: string) => api.get<WordInfo>(`/dictionary/${encodeURIComponent(word)}`),

  /** Writing Coach: LanguageTool through the backend, only when asked. */
  checkWriting: (text: string) => api.post<GrammarFeedback>('/writing/check', { text }),
};

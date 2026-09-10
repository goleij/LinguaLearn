import { api } from './api';
import type { SavedWord, WordBank, WordSource } from '../types';

export interface NewWord {
  german: string;
  english: string;
  source?: WordSource;
  topic?: string | null;
  lessonName?: string | null;
}

/** The learner's word bank and its review queue. */
export const vocabularyService = {
  getWordBank: () => api.get<WordBank>('/vocabulary'),

  getDue: () => api.get<SavedWord[]>('/vocabulary/due'),

  add: (word: NewWord) => api.post<SavedWord>('/vocabulary', word),

  /** One flashcard answer; the backend moves the word along its schedule. */
  review: (wordId: number, remembered: boolean) =>
    api.post<SavedWord>(`/vocabulary/${wordId}/review`, { remembered }),

  remove: (wordId: number) => api.delete<void>(`/vocabulary/${wordId}`),
};

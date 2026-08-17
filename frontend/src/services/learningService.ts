import { api } from './api';
import type {
  AnswerResult,
  CefrLevel,
  CefrLevelInfo,
  Course,
  CourseContent,
  ExampleSentence,
  LessonCompletion,
  LessonDetail,
  Profile,
} from '../types';

export const learningService = {
  /** The CEFR levels a learner can choose, with how much content each holds. */
  getLevels: () => api.get<CefrLevelInfo[]>('/levels'),

  /** The dashboard for a level: first course with its units and lesson tiles. */
  getDashboard: (level?: CefrLevel) =>
    api.get<CourseContent | null>(level ? `/dashboard?level=${level}` : '/dashboard'),

  getCourses: (level?: CefrLevel) =>
    api.get<Course[]>(level ? `/courses?level=${level}` : '/courses'),

  getCourseContent: (courseId: number) => api.get<CourseContent>(`/courses/${courseId}`),

  getLesson: (lessonId: number) => api.get<LessonDetail>(`/lessons/${lessonId}`),

  /** Clears the per-attempt counters before a run. */
  startAttempt: (lessonId: number) => api.post<void>(`/lessons/${lessonId}/attempt`),

  submitAnswer: (lessonId: number, activityId: number, answer: string) =>
    api.post<AnswerResult>(`/lessons/${lessonId}/activities/${activityId}/answer`, { answer }),

  completeLesson: (lessonId: number) => api.post<LessonCompletion>(`/lessons/${lessonId}/complete`),

  getProfile: () => api.get<Profile>('/profile'),

  /**
   * Optional real-world examples for a word. Enrichment only: an empty list is
   * a normal answer and the lesson carries on without it.
   */
  getExamples: (query: string) =>
    api.get<ExampleSentence[]>(`/examples?query=${encodeURIComponent(query)}`),
};

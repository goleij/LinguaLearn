import { api } from './api';
import type {
  AnswerResult,
  Course,
  CourseContent,
  LessonCompletion,
  LessonDetail,
  Profile,
} from '../types';

export const learningService = {
  /** The dashboard: first course with its units and lesson tiles. */
  getDashboard: () => api.get<CourseContent>('/dashboard'),

  getCourses: () => api.get<Course[]>('/courses'),

  getCourseContent: (courseId: number) => api.get<CourseContent>(`/courses/${courseId}`),

  getLesson: (lessonId: number) => api.get<LessonDetail>(`/lessons/${lessonId}`),

  /** Clears the per-attempt counters before a run. */
  startAttempt: (lessonId: number) => api.post<void>(`/lessons/${lessonId}/attempt`),

  submitAnswer: (lessonId: number, exerciseId: number, answer: string) =>
    api.post<AnswerResult>(`/lessons/${lessonId}/exercises/${exerciseId}/answer`, { answer }),

  completeLesson: (lessonId: number) => api.post<LessonCompletion>(`/lessons/${lessonId}/complete`),

  getProfile: () => api.get<Profile>('/profile'),
};

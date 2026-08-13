// Mirrors the Spring Boot DTOs in com.germanlearning.dto

export interface User {
  id: number;
  username: string;
  email: string;
  totalXp: number;
  currentStreak: number;
  longestStreak: number;
  createdAt: string;
}

export interface Course {
  id: number;
  name: string;
  language: string;
  level: string;
  description: string | null;
}

export interface LessonStatus {
  id: number;
  name: string;
  description: string | null;
  orderIndex: number;
  unlocked: boolean;
  completed: boolean;
  progressPercentage: number;
}

export interface Unit {
  id: number;
  name: string;
  description: string | null;
  orderIndex: number;
  lessons: LessonStatus[];
}

export interface CourseContent {
  course: Course;
  units: Unit[];
}

export type ExerciseType = 'MULTIPLE_CHOICE' | 'FILL_BLANK' | 'MATCH_PAIRS' | 'SENTENCE_ORDER';

/**
 * Only what the browser is allowed to know. The correct answer, the
 * explanation and the pair mapping stay on the server until an answer is
 * submitted.
 */
export interface Exercise {
  id: number;
  type: ExerciseType;
  question: string;
  options?: string[];
  sentenceTemplate?: string;
  leftItems?: string[];
  rightItems?: string[];
  words?: string[];
}

export interface LessonDetail {
  id: number;
  name: string;
  description: string | null;
  unlocked: boolean;
  practiceMode: boolean;
  exercises: Exercise[];
}

export interface AnswerResult {
  correct: boolean;
  correctAnswer: string | null;
  explanation: string | null;
  /** XP actually written to the database for this answer */
  xpAwarded: number;
  lessonXpEarned: number;
  userTotalXp: number;
  currentStreak: number;
  correctAnswers: number;
  totalAnswers: number;
}

export interface LessonCompletion {
  passed: boolean;
  practiceMode: boolean;
  completed: boolean;
  scorePercentage: number;
  correctAnswers: number;
  totalAnswers: number;
  lessonXpEarned: number;
  userTotalXp: number;
  passThreshold: number;
}

export interface Activity {
  lessonId: number;
  lessonName: string;
  completedAt: string | null;
  xpEarned: number;
}

export interface Profile {
  user: User;
  completedLessonsCount: number;
  recentActivity: Activity[];
}

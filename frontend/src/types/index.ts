// Mirrors the Spring Boot DTOs in com.germanlearning.dto

export type CefrLevel = 'A1' | 'A2' | 'B1' | 'B2';

export interface CefrLevelInfo {
  level: CefrLevel;
  label: string;
  description: string;
  courseCount: number;
  lessonCount: number;
}

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
  level: CefrLevel | null;
  levelLabel: string | null;
  description: string | null;
}

export interface LessonStatus {
  id: number;
  name: string;
  description: string | null;
  cefrLevel: CefrLevel | null;
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

export type ActivityType =
  | 'LEARN_CARD'
  | 'VOCABULARY'
  | 'GRAMMAR_TIP'
  | 'DIALOGUE'
  | 'REAL_EXAMPLE'
  | 'CHECKPOINT'
  | 'MULTIPLE_CHOICE'
  | 'FILL_BLANK'
  | 'SENTENCE_BUILDER'
  | 'MATCH_PAIRS'
  | 'TRANSLATION'
  | 'CONTEXT_CHOICE'
  | 'SHORT_WRITING'
  | 'LISTENING';

/** Learn → Context → Guided practice → Practice → Apply → Checkpoint. */
export type ActivityPhase =
  | 'LEARN'
  | 'CONTEXT'
  | 'GUIDED_PRACTICE'
  | 'PRACTICE'
  | 'APPLY'
  | 'CHECKPOINT';

export type Skill =
  | 'VOCABULARY'
  | 'GRAMMAR'
  | 'WORD_ORDER'
  | 'LISTENING'
  | 'WRITING'
  | 'READING'
  | 'ARTICLES'
  | 'PREPOSITIONS'
  | 'VERB_CONJUGATION';

export type Difficulty = 'EASY' | 'MEDIUM' | 'HARD';

export interface DialogueLine {
  speaker: string;
  text: string;
}

export interface TextPair {
  de: string;
  en: string;
}

/**
 * One step of a lesson. Only what the browser is allowed to know: no correct
 * answer, no correct option index and no pair mapping.
 */
export interface Activity {
  id: number;
  type: ActivityType;
  phase: ActivityPhase;
  phaseLabel: string;
  position: number;
  graded: boolean;
  xpReward: number;
  instruction?: string;
  prompt?: string;
  context?: string;
  /** Sent only for guided practice, where help up front is the point. */
  hint?: string;
  skill?: Skill;
  difficulty?: Difficulty;
  cefrLevel?: CefrLevel;
  topic?: string;
  // type specific
  options?: string[];
  sentence?: string;
  words?: string[];
  leftItems?: string[];
  rightItems?: string[];
  lines?: DialogueLine[];
  examples?: TextPair[];
  vocabulary?: TextPair[];
  bullets?: string[];
  audioText?: string;
  audioLang?: string;
  minWords?: number;
  mustUseWords?: string[];
  exampleQuery?: string;
}

export interface LessonDetail {
  id: number;
  name: string;
  description: string | null;
  cefrLevel: CefrLevel | null;
  unlocked: boolean;
  practiceMode: boolean;
  phaseCounts: Record<string, number>;
  activities: Activity[];
}

export interface AnswerResult {
  correct: boolean;
  correctAnswer: string | null;
  /** Why it was right or wrong, specific enough to act on. */
  feedback: string | null;
  explanation: string | null;
  hint: string | null;
  phase: ActivityPhase;
  skill: Skill | null;
  cefrLevel: CefrLevel | null;
  topic: string | null;
  xpAwarded: number;
  lessonXpEarned: number;
  userTotalXp: number;
  currentStreak: number;
  correctAnswers: number;
  totalAnswers: number;
  checkpointCorrectAnswers: number;
  checkpointTotalAnswers: number;
}

export interface LessonCompletion {
  passed: boolean;
  practiceMode: boolean;
  completed: boolean;
  hasCheckpoint: boolean;
  scorePercentage: number;
  correctAnswers: number;
  totalAnswers: number;
  checkpointCorrectAnswers: number;
  checkpointTotalAnswers: number;
  lessonXpEarned: number;
  userTotalXp: number;
  passThreshold: number;
}

/** A completed lesson on the profile page. */
export interface RecentActivity {
  lessonId: number;
  lessonName: string;
  completedAt: string | null;
  xpEarned: number;
}

export interface Profile {
  user: User;
  completedLessonsCount: number;
  recentActivity: RecentActivity[];
}

// --- external enrichment, always served through our own backend -------------

export interface ExampleSentence {
  german: string;
  english: string;
  source: string;
}

export interface WordInfo {
  word: string;
  found: boolean;
  wordType?: string;
  article?: string;
  meanings: string[];
  examples: string[];
  synonyms: string[];
  origin?: string;
  source: string;
  sourceUrl: string;
}

export interface GrammarIssue {
  message: string;
  shortMessage?: string;
  category?: string;
  excerpt?: string;
  offset: number;
  length: number;
  suggestions: string[];
}

export interface GrammarFeedback {
  available: boolean;
  text: string;
  issueCount: number;
  issues: GrammarIssue[];
  message: string;
}

// --- the word bank -----------------------------------------------------------

export type WordSource = 'WORD_EXPLORER' | 'VOCABULARY' | 'MISTAKE' | 'MANUAL';

export interface SavedWord {
  id: number;
  german: string;
  english: string;
  source: WordSource;
  sourceLabel: string;
  topic: string | null;
  cefrLevel: CefrLevel | null;
  lessonName: string | null;
  /** Leitner box: 0 is brand new, maxBox is learned. */
  box: number;
  maxBox: number;
  due: boolean;
  learned: boolean;
  reviewCount: number;
  correctCount: number;
  dueAt: string | null;
  addedAt: string;
}

export interface WordBank {
  total: number;
  due: number;
  learned: number;
  words: SavedWord[];
}

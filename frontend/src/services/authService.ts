import { api } from './api';
import type { User } from '../types';

export const authService = {
  me: () => api.get<User>('/auth/me'),

  login: (username: string, password: string) =>
    api.post<User>('/auth/login', { username, password }),

  register: (username: string, email: string, password: string) =>
    api.post<User>('/auth/register', { username, email, password }),

  logout: () => api.post<void>('/auth/logout'),
};

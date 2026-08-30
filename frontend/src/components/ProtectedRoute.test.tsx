import { render, screen } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';
import ProtectedRoute from './ProtectedRoute';
import type { User } from '../types';

const useAuth = vi.hoisted(() => vi.fn());
vi.mock('../hooks/useAuth', () => ({ useAuth }));

const anna: User = {
  id: 1,
  username: 'anna',
  email: 'anna@example.com',
  totalXp: 40,
  currentStreak: 2,
  longestStreak: 5,
  createdAt: '2026-01-01T10:00:00',
};

function renderAt(path: string) {
  return render(
    <MemoryRouter initialEntries={[path]}>
      <Routes>
        <Route path="/login" element={<p>Login page</p>} />
        <Route element={<ProtectedRoute />}>
          <Route path="/profile" element={<p>Profile page</p>} />
        </Route>
      </Routes>
    </MemoryRouter>,
  );
}

/**
 * The guard in front of every page that needs an account. The interesting case
 * is the middle one: while /auth/me is still in flight there is no user yet,
 * and redirecting then would throw a signed in visitor back to the login page
 * on every reload.
 */
describe('ProtectedRoute', () => {
  it('waits instead of redirecting while the session is still being restored', () => {
    useAuth.mockReturnValue({ user: null, loading: true });

    renderAt('/profile');

    expect(screen.getByText('Loading…')).toBeInTheDocument();
    expect(screen.queryByText('Login page')).not.toBeInTheDocument();
  });

  it('sends an anonymous visitor to the login page', () => {
    useAuth.mockReturnValue({ user: null, loading: false });

    renderAt('/profile');

    expect(screen.getByText('Login page')).toBeInTheDocument();
    expect(screen.queryByText('Profile page')).not.toBeInTheDocument();
  });

  it('renders the protected page once there is a user', () => {
    useAuth.mockReturnValue({ user: anna, loading: false });

    renderAt('/profile');

    expect(screen.getByText('Profile page')).toBeInTheDocument();
  });
});

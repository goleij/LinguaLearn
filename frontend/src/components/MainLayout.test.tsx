import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import MainLayout from './MainLayout';
import type { User } from '../types';

const useAuth = vi.hoisted(() => vi.fn());
vi.mock('../hooks/useAuth', () => ({ useAuth }));

const logout = vi.fn();

const anna: User = {
  id: 1,
  username: 'anna',
  email: 'anna@example.com',
  totalXp: 120,
  currentStreak: 4,
  longestStreak: 9,
  createdAt: '2026-01-01T10:00:00',
};

// A fresh element every time: React bails out of a re-render when handed the
// exact same element object, which would hide the change this file is testing.
const layout = () => (
  <MemoryRouter>
    <MainLayout />
  </MemoryRouter>
);

function renderLayout(user: User | null) {
  useAuth.mockReturnValue({ user, logout });
  return render(layout());
}

/**
 * The navbar badges are the only place the learner sees their XP and daily
 * streak while a lesson is running, and LessonPage keeps the XP current by
 * writing the number the API returned straight back into the auth context.
 * What these tests protect is that both badges really do read from that
 * context rather than from a copy taken at mount.
 */
describe('MainLayout', () => {
  beforeEach(() => {
    logout.mockReset().mockResolvedValue(undefined);
  });

  it('shows the XP and streak of the signed in user', () => {
    renderLayout(anna);

    // The badges are an icon and a bare number, so their accessible label is
    // the only thing that says which number is which
    expect(screen.getByLabelText('Total XP: 120')).toHaveTextContent('120');
    expect(screen.getByLabelText('Day streak: 4')).toHaveTextContent('4');
  });

  it('re-renders both badges when the context changes mid lesson', () => {
    useAuth.mockReturnValue({ user: anna, logout });
    const { rerender } = render(layout());

    // What LessonPage does after every answer: write the stored numbers back
    useAuth.mockReturnValue({ user: { ...anna, totalXp: 135, currentStreak: 5 }, logout });
    rerender(layout());

    expect(screen.getByLabelText('Total XP: 135')).toBeInTheDocument();
    expect(screen.queryByLabelText('Total XP: 120')).not.toBeInTheDocument();
    expect(screen.getByLabelText('Day streak: 5')).toBeInTheDocument();
  });

  it('hides the badges and the logout button when nobody is signed in', () => {
    renderLayout(null);

    expect(screen.queryByLabelText(/^Total XP/)).not.toBeInTheDocument();
    expect(screen.queryByLabelText(/^Day streak/)).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Logout' })).not.toBeInTheDocument();
  });

  it('logs out when the button is pressed', async () => {
    renderLayout(anna);

    await userEvent.click(screen.getByRole('button', { name: 'Logout' }));

    expect(logout).toHaveBeenCalledOnce();
  });
});

import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import RegisterPage from './RegisterPage';
import { NotificationProvider } from '../hooks/useNotification';

const useAuth = vi.hoisted(() => vi.fn());
vi.mock('../hooks/useAuth', () => ({ useAuth }));

const register = vi.fn();

function renderPage() {
  render(
    <MemoryRouter>
      <NotificationProvider>
        <RegisterPage />
      </NotificationProvider>
    </MemoryRouter>,
  );
}

async function fillIn(values: {
  username?: string;
  email?: string;
  password?: string;
  confirm?: string;
}) {
  const user = userEvent.setup();
  if (values.username) await user.type(screen.getByLabelText('Username'), values.username);
  if (values.email) await user.type(screen.getByLabelText('Email'), values.email);
  if (values.password) await user.type(screen.getByLabelText('Password'), values.password);
  if (values.confirm) await user.type(screen.getByLabelText('Confirm Password'), values.confirm);
  await user.click(screen.getByRole('button', { name: 'Create Account' }));
}

/**
 * The register form repeats the backend's rules so a bad account is refused
 * before a request is ever made. These tests check both halves of that: the
 * message the visitor sees, and that no call went out.
 */
describe('RegisterPage', () => {
  beforeEach(() => {
    register.mockReset().mockResolvedValue(undefined);
    useAuth.mockReturnValue({ user: null, loading: false, register });
  });

  it('refuses an empty form', async () => {
    renderPage();

    await fillIn({});

    expect(await screen.findByRole('alert')).toHaveTextContent('Please fill in all fields');
    expect(register).not.toHaveBeenCalled();
  });

  it('refuses a username shorter than three characters', async () => {
    renderPage();

    await fillIn({
      username: 'an',
      email: 'anna@example.com',
      password: 'secret123',
      confirm: 'secret123',
    });

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'Username must be at least 3 characters',
    );
    expect(register).not.toHaveBeenCalled();
  });

  it('refuses a password shorter than six characters', async () => {
    renderPage();

    await fillIn({
      username: 'anna',
      email: 'anna@example.com',
      password: '12345',
      confirm: '12345',
    });

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'Password must be at least 6 characters',
    );
    expect(register).not.toHaveBeenCalled();
  });

  it('refuses two passwords that do not match', async () => {
    renderPage();

    await fillIn({
      username: 'anna',
      email: 'anna@example.com',
      password: 'secret123',
      confirm: 'secret124',
    });

    expect(await screen.findByRole('alert')).toHaveTextContent('Passwords do not match');
    expect(register).not.toHaveBeenCalled();
  });

  it('registers with the trimmed username and email', async () => {
    renderPage();

    await fillIn({
      username: '  anna  ',
      email: '  anna@example.com  ',
      password: 'secret123',
      confirm: 'secret123',
    });

    expect(register).toHaveBeenCalledWith('anna', 'anna@example.com', 'secret123');
  });

  it('shows the reason when the backend refuses the account', async () => {
    const { ApiError } = await import('../services/api');
    register.mockRejectedValue(new ApiError(400, 'Username already exists'));
    renderPage();

    await fillIn({
      username: 'anna',
      email: 'anna@example.com',
      password: 'secret123',
      confirm: 'secret123',
    });

    expect(await screen.findByRole('alert')).toHaveTextContent('Username already exists');
  });
});

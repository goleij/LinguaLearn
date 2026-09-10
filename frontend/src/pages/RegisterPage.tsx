import { useState, type FormEvent } from 'react';
import { Link, Navigate, useNavigate } from 'react-router-dom';
import AuthLayout from '../components/AuthLayout';
import { useAuth } from '../hooks/useAuth';
import { useNotification } from '../hooks/useNotification';
import { ApiError } from '../services/api';

/** Account creation form, in the shared signed-out shell. */
export default function RegisterPage() {
  const { user, loading, register } = useAuth();
  const navigate = useNavigate();
  const { show } = useNotification();

  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [submitting, setSubmitting] = useState(false);

  if (!loading && user) {
    return <Navigate to="/learn" replace />;
  }

  const showError = (message: string) =>
    show(message, { variant: 'error', position: 'top-center' });

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();

    const trimmedUsername = username.trim();
    const trimmedEmail = email.trim();

    // Exactly the checks RegisterView ran, in the same order
    if (!trimmedUsername || !trimmedEmail || !password) {
      showError('Please fill in all fields');
      return;
    }
    if (trimmedUsername.length < 3) {
      showError('Username must be at least 3 characters');
      return;
    }
    if (password.length < 6) {
      showError('Password must be at least 6 characters');
      return;
    }
    if (password !== confirmPassword) {
      showError('Passwords do not match');
      return;
    }

    setSubmitting(true);
    try {
      await register(trimmedUsername, trimmedEmail, password);
      navigate('/login', { state: { registered: true } });
    } catch (e) {
      showError(e instanceof ApiError ? e.message : 'Registration failed');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <AuthLayout
      title="Create your account"
      subtitle="Free, and it stays free."
      footer={
        <>
          Already have an account?{' '}
          <Link to="/login" className="focus-ring font-semibold text-white hover:underline">
            Log in
          </Link>
        </>
      }
    >
      <form onSubmit={handleSubmit} className="flex w-full flex-col gap-4" noValidate>
        <label className="flex flex-col gap-1.5 text-sm font-medium text-ink-muted">
          Username
          <input
            className="field-input"
            placeholder="Choose a username"
            value={username}
            onChange={(event) => setUsername(event.target.value)}
            autoComplete="username"
          />
        </label>

        <label className="flex flex-col gap-1.5 text-sm font-medium text-ink-muted">
          Email
          <input
            className="field-input"
            type="email"
            placeholder="your@email.com"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            autoComplete="email"
          />
        </label>

        <label className="flex flex-col gap-1.5 text-sm font-medium text-ink-muted">
          Password
          <input
            className="field-input"
            type="password"
            placeholder="At least 6 characters"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            autoComplete="new-password"
          />
        </label>

        <label className="flex flex-col gap-1.5 text-sm font-medium text-ink-muted">
          Confirm Password
          <input
            className="field-input"
            type="password"
            placeholder="Repeat your password"
            value={confirmPassword}
            onChange={(event) => setConfirmPassword(event.target.value)}
            autoComplete="new-password"
          />
        </label>

        <button
          type="submit"
          className="btn-primary focus-ring mt-2 w-full cursor-pointer"
          disabled={submitting}
        >
          {submitting ? 'Creating account…' : 'Create Account'}
        </button>
      </form>
    </AuthLayout>
  );
}

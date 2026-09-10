import { useEffect, useState, type FormEvent } from 'react';
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom';
import AuthLayout from '../components/AuthLayout';
import { useAuth } from '../hooks/useAuth';
import { useNotification } from '../hooks/useNotification';
import { ApiError } from '../services/api';

/** Sign in form, in the shared signed-out shell. */
export default function LoginPage() {
  const { user, loading, login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const { show } = useNotification();

  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  // The register page redirects here with a success message
  const registered = (location.state as { registered?: boolean } | null)?.registered;
  useEffect(() => {
    if (registered) {
      show('Account created successfully! Please login.', {
        variant: 'success',
        position: 'top-center',
      });
      navigate('/login', { replace: true, state: null });
    }
  }, [registered, show, navigate]);

  if (!loading && user) {
    return <Navigate to="/learn" replace />;
  }

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    setError(null);
    setSubmitting(true);

    try {
      await login(username, password);
      navigate('/learn', { replace: true });
    } catch (e) {
      setError(
        e instanceof ApiError
          ? e.message
          : 'Check that you have entered the correct username and password and try again.',
      );
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <AuthLayout
      title="Welcome back"
      subtitle="Pick up where you left off."
      footer={
        <>
          New here?{' '}
          <Link to="/register" className="focus-ring font-semibold text-white hover:underline">
            Create an account
          </Link>
        </>
      }
    >
      <form onSubmit={handleSubmit} className="flex w-full flex-col gap-4">
        {error && (
          <div role="alert" className="rounded-xl bg-feedback-wrong p-4 text-sm text-ink">
            <strong className="block text-feedback-error-ink">
              Incorrect username or password
            </strong>
            {error}
          </div>
        )}

        <label className="flex flex-col gap-1.5 text-sm font-medium text-ink-muted">
          Username
          <input
            className="field-input"
            value={username}
            onChange={(event) => setUsername(event.target.value)}
            autoComplete="username"
            autoFocus
          />
        </label>

        <label className="flex flex-col gap-1.5 text-sm font-medium text-ink-muted">
          Password
          <input
            className="field-input"
            type="password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            autoComplete="current-password"
          />
        </label>

        <button
          type="submit"
          className="btn-primary focus-ring mt-2 w-full cursor-pointer"
          disabled={submitting}
        >
          {submitting ? 'Logging in…' : 'Log in'}
        </button>
      </form>
    </AuthLayout>
  );
}

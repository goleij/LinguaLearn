import { useEffect, useState, type FormEvent } from 'react';
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { useNotification } from '../hooks/useNotification';
import { ApiError } from '../services/api';

/** Sign in form on the gradient background. */
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
    return <Navigate to="/" replace />;
  }

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    setError(null);
    setSubmitting(true);

    try {
      await login(username, password);
      navigate('/', { replace: true });
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
    <div className="flex min-h-full items-center justify-center bg-auth-gradient p-5">
      <div className="w-full max-w-[400px] rounded-[20px] bg-white p-6 shadow-card sm:p-10">
        <div className="flex flex-col items-center">
          <h1 className="mb-[10px] text-center text-brand-green">German Learning</h1>
          <p className="mb-5 text-center text-ink-muted">Learn German the fun way!</p>
        </div>

        <form onSubmit={handleSubmit} className="flex w-full flex-col gap-4">
          {error && (
            <div className="rounded-xl bg-feedback-wrong p-4 text-sm text-ink">
              <strong className="block text-feedback-error">Incorrect username or password</strong>
              {error}
            </div>
          )}

          <label className="flex flex-col gap-1 text-sm font-medium text-ink-muted">
            Username
            <input
              className="field-input"
              value={username}
              onChange={(event) => setUsername(event.target.value)}
              autoComplete="username"
              autoFocus
            />
          </label>

          <label className="flex flex-col gap-1 text-sm font-medium text-ink-muted">
            Password
            <input
              className="field-input"
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              autoComplete="current-password"
            />
          </label>

          <button type="submit" className="btn-primary mt-2 w-full" disabled={submitting}>
            {submitting ? 'Logging in…' : 'Log in'}
          </button>
        </form>

        <div className="mt-5 text-center">
          <Link to="/register" className="text-brand-blue hover:underline">
            Don&apos;t have an account? Register here
          </Link>
        </div>
      </div>
    </div>
  );
}

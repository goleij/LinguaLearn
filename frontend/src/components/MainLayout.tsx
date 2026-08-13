import { useState } from 'react';
import { Link, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';

/**
 * App shell: a navbar with the drawer toggle, the logo, the XP and streak
 * badges and a logout button, plus a drawer holding the Dashboard and Profile
 * links.
 */
export default function MainLayout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [drawerOpen, setDrawerOpen] = useState(true);

  const handleLogout = async () => {
    await logout();
    navigate('/login', { replace: true });
  };

  return (
    <div className="flex h-full flex-col bg-surface-page">
      <header className="flex h-16 shrink-0 items-center gap-2 border-b border-surface-grey bg-white px-4 shadow-navbar">
        <button
          type="button"
          aria-label="Toggle menu"
          onClick={() => setDrawerOpen((open) => !open)}
          className="flex h-10 w-10 items-center justify-center rounded-lg text-ink transition hover:bg-surface-page"
        >
          <span className="text-xl leading-none">☰</span>
        </button>

        <h1 className="m-3 flex-1 text-lg font-semibold text-brand-green">German Learning</h1>

        {user && (
          <div className="flex items-center gap-3">
            <span className="rounded-[20px] bg-brand-yellow px-4 py-1 font-bold text-ink">
              XP: {user.totalXp}
            </span>
            <span className="rounded-[20px] bg-brand-orange px-4 py-1 font-bold text-white">
              🔥 {user.currentStreak}
            </span>
            <button
              type="button"
              onClick={handleLogout}
              className="rounded-xl bg-surface-grey px-4 py-2 font-medium text-ink transition hover:brightness-95"
            >
              Logout
            </button>
          </div>
        )}
      </header>

      <div className="flex min-h-0 flex-1">
        {drawerOpen && (
          <nav className="w-64 shrink-0 border-r border-surface-grey bg-white p-4">
            <h2 className="mb-2 text-ink">Menu</h2>
            <Link
              to="/"
              className="block rounded-[10px] px-4 py-[10px] font-medium text-ink no-underline transition hover:bg-surface-page"
            >
              Dashboard
            </Link>
            <Link
              to="/profile"
              className="block rounded-[10px] px-4 py-[10px] font-medium text-ink no-underline transition hover:bg-surface-page"
            >
              Profile
            </Link>
          </nav>
        )}

        <main className="min-h-0 flex-1 overflow-auto bg-surface-page">
          <Outlet />
        </main>
      </div>
    </div>
  );
}

import { useEffect, useRef, useState } from 'react';
import { Link, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';

const DESKTOP = '(min-width: 768px)';

/**
 * App shell: a navbar with the drawer toggle, the logo, the XP and streak
 * badges and a logout button, plus a drawer holding the Dashboard and Profile
 * links.
 *
 * The drawer is permanent on a wide screen and an overlay on a narrow one,
 * where a fixed 256px sidebar would otherwise eat most of the viewport.
 */
export default function MainLayout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const [isDesktop, setIsDesktop] = useState(
    () => typeof window !== 'undefined' && window.matchMedia(DESKTOP).matches,
  );
  const [drawerOpen, setDrawerOpen] = useState(isDesktop);

  // Follow the viewport: open the drawer when the layout becomes wide enough,
  // hide it again on the way back. Watching resize as well as the media query
  // keeps this right when a change event does not arrive, and comparing
  // against the previous match means an ordinary resize never overrides a
  // drawer the reader opened or closed themselves.
  const wasDesktop = useRef(isDesktop);
  useEffect(() => {
    const query = window.matchMedia(DESKTOP);

    const sync = () => {
      const isNow = query.matches;
      if (isNow === wasDesktop.current) {
        return;
      }
      wasDesktop.current = isNow;
      setIsDesktop(isNow);
      setDrawerOpen(isNow);
    };

    query.addEventListener('change', sync);
    window.addEventListener('resize', sync);
    return () => {
      query.removeEventListener('change', sync);
      window.removeEventListener('resize', sync);
    };
  }, []);

  const handleLogout = async () => {
    await logout();
    navigate('/login', { replace: true });
  };

  // On a narrow screen the drawer covers the page, so tapping a link closes it
  const closeOnMobile = () => {
    if (!isDesktop) setDrawerOpen(false);
  };

  const navLink =
    'block rounded-[10px] px-4 py-[10px] font-medium text-ink no-underline transition hover:bg-surface-page';

  return (
    <div className="flex h-full flex-col bg-surface-page">
      <header className="flex h-16 shrink-0 items-center gap-1 border-b border-surface-grey bg-white px-2 shadow-navbar sm:gap-2 sm:px-4">
        <button
          type="button"
          aria-label="Toggle menu"
          aria-expanded={drawerOpen}
          onClick={() => setDrawerOpen((open) => !open)}
          className="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg text-ink transition hover:bg-surface-page"
        >
          <span className="text-xl leading-none">☰</span>
        </button>

        <h1 className="m-0 min-w-0 flex-1 truncate text-base font-semibold text-brand-green sm:m-3 sm:text-lg">
          German Learning
        </h1>

        {user && (
          <div className="flex shrink-0 items-center gap-1 sm:gap-3">
            <span className="rounded-[20px] bg-brand-yellow px-2 py-1 text-sm font-bold text-ink sm:px-4 sm:text-base">
              <span className="hidden sm:inline">XP: </span>
              {user.totalXp}
            </span>
            <span className="rounded-[20px] bg-brand-orange px-2 py-1 text-sm font-bold text-white sm:px-4 sm:text-base">
              🔥 {user.currentStreak}
            </span>
            <button
              type="button"
              onClick={handleLogout}
              aria-label="Logout"
              className="rounded-xl bg-surface-grey px-2 py-2 text-sm font-medium text-ink transition hover:brightness-95 sm:px-4 sm:text-base"
            >
              <span className="hidden sm:inline">Logout</span>
              <span className="sm:hidden">⏻</span>
            </button>
          </div>
        )}
      </header>

      <div className="relative flex min-h-0 flex-1">
        {/* Backdrop only exists while the drawer floats above the page */}
        {drawerOpen && !isDesktop && (
          <div
            className="absolute inset-0 z-30 bg-ink/30 md:hidden"
            onClick={() => setDrawerOpen(false)}
            role="presentation"
            aria-hidden="true"
          />
        )}

        {drawerOpen && (
          <nav className="absolute inset-y-0 left-0 z-40 w-64 max-w-[80%] shrink-0 overflow-auto border-r border-surface-grey bg-white p-4 md:static md:z-auto md:max-w-none">
            <h2 className="mb-2 text-ink">Menu</h2>
            <Link to="/" className={navLink} onClick={closeOnMobile}>
              Dashboard
            </Link>
            <Link to="/profile" className={navLink} onClick={closeOnMobile}>
              Profile
            </Link>
          </nav>
        )}

        <main className="min-h-0 min-w-0 flex-1 overflow-auto bg-surface-page">
          <Outlet />
        </main>
      </div>
    </div>
  );
}

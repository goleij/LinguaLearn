import { useEffect, useRef, useState } from 'react';
import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import Icon from './Icon';

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

  // NavLink hands the active state in, so the drawer says where you are
  const navLink = ({ isActive }: { isActive: boolean }) =>
    `flex items-center gap-3 rounded-xl px-3 py-[10px] font-medium no-underline transition ${
      isActive
        ? 'bg-brand-green/10 text-brand-green-ink'
        : 'text-ink hover:bg-surface-page'
    }`;

  return (
    <div className="flex h-full flex-col bg-surface-page">
      {/*
        The top bar carries the same identity as the landing and sign-in pages:
        the dark surface and the identical logo lockup. The content below stays
        light, so the brand frames the workspace instead of competing with it.
      */}
      <header className="flex h-16 shrink-0 items-center gap-1 bg-surface-dark px-2 sm:gap-2 sm:px-4">
        <button
          type="button"
          aria-label="Toggle menu"
          aria-expanded={drawerOpen}
          onClick={() => setDrawerOpen((open) => !open)}
          className="focus-ring flex h-10 w-10 shrink-0 cursor-pointer items-center justify-center rounded-lg text-white/80 transition-colors duration-200 hover:bg-white/10 hover:text-white"
        >
          <Icon name="menu" size={22} />
        </button>

        <Link
          to="/learn"
          className="focus-ring flex min-w-0 flex-1 cursor-pointer items-center gap-2 truncate font-display text-base font-bold text-white no-underline sm:ml-2 sm:text-lg"
        >
          <span className="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-brand-green text-white">
            <Icon name="layers" size={18} />
          </span>
          <span className="truncate">German Learning</span>
        </Link>

        {user && (
          <div className="flex shrink-0 items-center gap-1.5 sm:gap-2">
            <span
              aria-label={`Total XP: ${user.totalXp}`}
              className="flex items-center gap-1.5 rounded-xl bg-white/10 px-2.5 py-1.5 text-sm font-bold text-white sm:px-3"
            >
              <Icon name="bolt" size={16} className="text-brand-yellow" />
              {user.totalXp}
            </span>
            <span
              aria-label={`Day streak: ${user.currentStreak}`}
              className="flex items-center gap-1.5 rounded-xl bg-white/10 px-2.5 py-1.5 text-sm font-bold text-white sm:px-3"
            >
              <Icon name="flame" size={16} className="text-brand-orange" />
              {user.currentStreak}
            </span>
            <button
              type="button"
              onClick={handleLogout}
              aria-label="Logout"
              className="focus-ring flex h-9 items-center justify-center rounded-xl bg-white/10 px-2.5 text-sm font-medium text-white transition-colors duration-200 hover:bg-white/20 sm:px-4"
            >
              <span className="hidden sm:inline">Logout</span>
              <span className="sm:hidden">
                <Icon name="power" size={18} />
              </span>
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
            <p className="mb-2 px-3 text-xs font-bold uppercase tracking-wide text-ink-muted">
              Menu
            </p>
            <div className="flex flex-col gap-1">
              <NavLink to="/learn" className={navLink} onClick={closeOnMobile}>
                <Icon name="home" /> Dashboard
              </NavLink>
              <NavLink to="/words" className={navLink} onClick={closeOnMobile}>
                <Icon name="book" /> My words
              </NavLink>
              <NavLink to="/profile" className={navLink} onClick={closeOnMobile}>
                <Icon name="user" /> Profile
              </NavLink>
            </div>
          </nav>
        )}

        <main className="min-h-0 min-w-0 flex-1 overflow-auto bg-surface-page">
          <Outlet />
        </main>
      </div>
    </div>
  );
}

import type { ReactNode } from 'react';
import { Link } from 'react-router-dom';
import BrandGlow from './BrandGlow';
import Icon from './Icon';

/**
 * The shell around signing in and creating an account.
 *
 * Signed-out pages share one identity with the landing page — the same dark
 * surface, the same lighting, the same wordmark — so arriving from the front
 * page does not feel like landing on a different product. Once past the
 * sign-in, the app itself is light: the change of surface marks the boundary
 * between the brand and the workspace, which is the one place a difference is
 * meant to be felt.
 *
 * Both forms live in here rather than repeating the frame, so they cannot
 * drift apart again.
 */
export default function AuthLayout({
  title,
  subtitle,
  children,
  footer,
}: {
  title: string;
  subtitle: string;
  children: ReactNode;
  footer: ReactNode;
}) {
  return (
    <div className="relative flex min-h-full flex-col items-center justify-center overflow-hidden bg-surface-dark p-5">
      <BrandGlow />

      <Link
        to="/"
        className="focus-ring relative mb-7 flex cursor-pointer items-center gap-2 font-display text-lg font-bold text-white no-underline"
      >
        <span className="flex h-8 w-8 items-center justify-center rounded-lg bg-brand-green text-white">
          <Icon name="layers" size={18} />
        </span>
        German Learning
      </Link>

      <div className="relative w-full max-w-[400px] rounded-panel bg-white p-6 shadow-float sm:p-9">
        <h1 className="m-0 text-center text-[1.75rem] text-ink">{title}</h1>
        <p className="mb-6 mt-2 text-center text-ink-muted">{subtitle}</p>

        {children}
      </div>

      <div className="relative mt-6 text-center text-sm text-white/70">{footer}</div>
    </div>
  );
}

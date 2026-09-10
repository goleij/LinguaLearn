export type IconName =
  | 'home'
  | 'book'
  | 'user'
  | 'flame'
  | 'bolt'
  | 'menu'
  | 'power'
  | 'close'
  | 'check'
  | 'cross'
  | 'bulb'
  | 'trophy'
  | 'refresh'
  | 'star'
  | 'lock'
  | 'play'
  | 'volume'
  | 'target'
  | 'search'
  | 'pen'
  | 'arrowLeft'
  | 'layers';

/**
 * The app's icons, drawn rather than typed.
 *
 * Emoji were doing this job before, which meant the interface changed shape
 * from one platform to the next and read as decoration rather than as part of
 * the design. These are stroked paths on a 24x24 grid that inherit
 * `currentColor`, so an icon takes the colour and size of the text around it.
 */
export default function Icon({
  name,
  size = 20,
  filled = false,
  className = '',
}: {
  name: IconName;
  size?: number;
  /** Only meaningful for `star`, which has a hollow and a solid state. */
  filled?: boolean;
  className?: string;
}) {
  return (
    <svg
      xmlns="http://www.w3.org/2000/svg"
      viewBox="0 0 24 24"
      width={size}
      height={size}
      fill={name === 'star' && filled ? 'currentColor' : 'none'}
      stroke="currentColor"
      strokeWidth={1.8}
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
      focusable="false"
      className={`shrink-0 ${className}`}
    >
      {PATHS[name]}
    </svg>
  );
}

const PATHS: Record<IconName, React.ReactNode> = {
  home: <path d="M3 10.5 12 3l9 7.5V20a1 1 0 0 1-1 1h-5v-6H9v6H4a1 1 0 0 1-1-1z" />,

  book: (
    <>
      <path d="M4 4.5A1.5 1.5 0 0 1 5.5 3H19v15H5.5A1.5 1.5 0 0 0 4 19.5z" />
      <path d="M4 19.5A1.5 1.5 0 0 0 5.5 21H19v-3" />
      <path d="M8 7.5h7M8 11h5" />
    </>
  ),

  user: (
    <>
      <circle cx="12" cy="8" r="3.5" />
      <path d="M4.5 20a7.5 7.5 0 0 1 15 0" />
    </>
  ),

  flame: (
    <>
      <path d="M12 3c.5 3 3 4 4.5 6.5A6.5 6.5 0 1 1 5.5 13c0-2 1-3.5 2-4.5 0 1.5.8 2.5 2 2.5 1.5 0 2.5-2 2.5-8z" />
    </>
  ),

  bolt: <path d="M13 2 4.5 13.5H11l-1 8.5 8.5-11.5H12z" />,

  menu: <path d="M4 7h16M4 12h16M4 17h16" />,

  power: (
    <>
      <path d="M12 3v9" />
      <path d="M7.5 6.3a7.5 7.5 0 1 0 9 0" />
    </>
  ),

  close: <path d="M6 6l12 12M18 6L6 18" />,

  check: <path d="M4.5 12.5 9.5 17.5 19.5 6.5" />,

  cross: <path d="M6.5 6.5l11 11M17.5 6.5l-11 11" />,

  bulb: (
    <>
      <path d="M9 17h6" />
      <path d="M10 20.5h4" />
      <path d="M12 3a6 6 0 0 0-3.5 10.9c.3.2.5.6.5 1V17h6v-2.1c0-.4.2-.8.5-1A6 6 0 0 0 12 3z" />
    </>
  ),

  trophy: (
    <>
      <path d="M7 4h10v5a5 5 0 0 1-10 0z" />
      <path d="M7 5.5H4.5v1A3.5 3.5 0 0 0 8 10M17 5.5h2.5v1A3.5 3.5 0 0 1 16 10" />
      <path d="M12 14v3.5M8.5 20.5h7" />
    </>
  ),

  refresh: (
    <>
      <path d="M20 12a8 8 0 1 1-2.6-5.9" />
      <path d="M20.5 4v4.5H16" />
    </>
  ),

  star: (
    <path d="m12 3.5 2.7 5.5 6 .9-4.35 4.25 1.03 6-5.38-2.83L6.6 20.15l1.03-6L3.3 9.9l6-.9z" />
  ),

  lock: (
    <>
      <rect x="4.5" y="10.5" width="15" height="10" rx="2" />
      <path d="M8 10.5V7.5a4 4 0 0 1 8 0v3" />
    </>
  ),

  play: <path d="M8 5.5v13l11-6.5z" />,

  volume: (
    <>
      <path d="M4 9.5h3.5L12 5.5v13L7.5 14.5H4z" />
      <path d="M16 9a4.5 4.5 0 0 1 0 6M18.5 6.5a8 8 0 0 1 0 11" />
    </>
  ),

  target: (
    <>
      <circle cx="12" cy="12" r="8.5" />
      <circle cx="12" cy="12" r="4.5" />
      <circle cx="12" cy="12" r="1" fill="currentColor" stroke="none" />
    </>
  ),

  search: (
    <>
      <circle cx="10.5" cy="10.5" r="6.5" />
      <path d="m15.5 15.5 4.5 4.5" />
    </>
  ),

  pen: (
    <>
      <path d="M4 20h4L19.5 8.5a2.1 2.1 0 0 0-3-3L5 17z" />
      <path d="m14.5 6.5 3 3" />
    </>
  ),

  arrowLeft: <path d="M19 12H5m0 0 6-6m-6 6 6 6" />,

  layers: (
    <>
      <path d="m12 3 9 4.5-9 4.5-9-4.5z" />
      <path d="m3 12.5 9 4.5 9-4.5" />
    </>
  ),
};

/**
 * The lighting on the dark brand surfaces.
 *
 * Two soft colour washes, so a dark panel is a lit surface rather than a flat
 * rectangle. It lives here rather than in one page so every dark surface in
 * the app is lit the same way.
 *
 * Purely decorative: the parent needs `relative` and its own dark background.
 */
export default function BrandGlow() {
  return (
    <div aria-hidden="true" className="pointer-events-none absolute inset-0 overflow-hidden">
      <div className="absolute -left-40 -top-40 h-[26rem] w-[26rem] rounded-full bg-brand-green/25 blur-[120px]" />
      <div className="absolute -bottom-52 right-[-10rem] h-[32rem] w-[32rem] rounded-full bg-brand-blue/25 blur-[130px]" />
    </div>
  );
}

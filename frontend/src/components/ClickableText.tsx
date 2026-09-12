import { useWordExplorer, type WordContext } from '../hooks/useWordExplorer';

/**
 * German text where every word can be clicked to open the Word Explorer.
 *
 * Splitting keeps punctuation outside the clickable part so "Rechnung," looks
 * up "Rechnung".
 */
export default function ClickableText({
  text,
  className = '',
  context,
}: {
  text: string;
  className?: string;
  /** What the page already knows, e.g. the English a word list teaches. */
  context?: WordContext;
}) {
  const { explore } = useWordExplorer();

  const tokens = text.split(/(\s+)/);

  return (
    <span className={className}>
      {tokens.map((token, index) => {
        if (!token.trim()) {
          return <span key={index}>{token}</span>;
        }

        const match = token.match(/^([^\p{L}]*)(\p{L}[\p{L}\-]*)(.*)$/u);
        if (!match) {
          return <span key={index}>{token}</span>;
        }

        const [, before, word, after] = match;

        return (
          <span key={index}>
            {before}
            <button
              type="button"
              onClick={() => explore(word, context)}
              title={`Look up "${word}"`}
              className="cursor-pointer border-0 bg-transparent p-0 font-inherit text-inherit underline decoration-dotted decoration-1 underline-offset-2 transition hover:text-brand-blue"
            >
              {word}
            </button>
            {after}
          </span>
        );
      })}
    </span>
  );
}

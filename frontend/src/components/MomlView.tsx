import { useMemo, useState } from "react";
import { useSessionStore } from "../state/sessionStore";

export function MomlView() {
  const moml = useSessionStore((s) => s.moml);
  const [copied, setCopied] = useState(false);
  const lines = useMemo(() => moml.split("\n"), [moml]);

  const copy = async () => {
    try { await navigator.clipboard.writeText(moml); } catch { /* best-effort */ }
    setCopied(true);
    setTimeout(() => setCopied(false), 1200);
  };

  return (
    <div className="h-full flex flex-col bg-ink-950">
      <div className="px-3 pt-2 pb-1 flex items-center justify-between border-b border-ink-700">
        <span className="section-title">MoML source</span>
        <div className="flex items-center gap-2 text-[10px] text-ink-400">
          <span className="font-mono">{lines.length} lines · {moml.length} chars</span>
          <button className="btn-ghost" onClick={copy} disabled={!moml}>
            {copied ? "Copied ✓" : "Copy"}
          </button>
        </div>
      </div>
      <div className="flex-1 min-h-0 overflow-auto">
        {moml ? (
          <table className="w-full text-[12px] font-mono leading-relaxed">
            <tbody>
              {lines.map((line, i) => (
                <tr key={i} className="hover:bg-stone-50">
                  <td className="select-none text-right pr-3 pl-3 text-stone-300
                                 align-top w-[2.75rem] sticky left-0 bg-ink-950/95">
                    {i + 1}
                  </td>
                  <td className="pr-4 whitespace-pre align-top"
                      dangerouslySetInnerHTML={{ __html: highlight(line) }} />
                </tr>
              ))}
            </tbody>
          </table>
        ) : (
          <div className="p-4 text-sm text-ink-400 italic">
            No model loaded yet.
          </div>
        )}
      </div>
    </div>
  );
}

function highlight(line: string): string {
  const esc = line
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;");

  if (esc.trim().startsWith("&lt;!--"))
    return `<span style="color:#a8a29e;font-style:italic">${esc}</span>`;
  if (esc.trim().startsWith("&lt;?") || esc.trim().startsWith("&lt;!DOCTYPE"))
    return `<span style="color:#a8a29e">${esc}</span>`;

  // Attribute values (quoted strings) — amber
  let out = esc.replace(
    /&quot;([^&]*?)&quot;/g,
    '<span style="color:#b45309">&quot;$1&quot;</span>'
  );
  // Attribute names — violet
  out = out.replace(
    /([A-Za-z_][\w:-]*)=/g,
    '<span style="color:#7c3aed">$1</span>='
  );
  // Tag names — blue
  out = out.replace(
    /(&lt;\/?)([A-Za-z_][\w:-]*)/g,
    '$1<span style="color:#2563eb">$2</span>'
  );
  return `<span style="color:#44403c">${out}</span>`;
}

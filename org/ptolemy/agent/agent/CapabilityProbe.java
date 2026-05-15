/* Pre-planning library capability scan.

 Copyright (c) 2024-2026 The Regents of the University of California.
 All rights reserved.

 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY
 */
package org.ptolemy.agent.agent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ptolemy.agent.library.LibraryIndex;

///////////////////////////////////////////////////////////////////
//// CapabilityProbe

/**
 Library-driven pre-planning capability scan.  Given a free-form
 user goal, return a compact JSON {@code recipe} containing actors
 the loaded Ptolemy library actually exposes that look relevant,
 plus a short list of universal pitfalls.  Two kinds of evidence go
 into the recipe:

 <ol>
 <li>Literal token search in {@link LibraryIndex} for every
     non-stopword token of the goal.</li>
 <li>A tiny set of high-level domain hints (LSTM, PID, RC, FIR,
     ODE, …) that augment the search with a few extra keywords so
     classic compositions are surfaced without forcing the user to
     spell out every primitive.</li>
 </ol>

 <p>The recipe is intentionally a <b>soft prior</b>, not a rule
 list.  It enumerates suggested actors and well-known pitfalls but
 never declares any actor forbidden.  The LLM keeps full agency;
 the recipe just removes search-space ambiguity before phase-0
 planning so it doesn't reach for {@code Expression} as a generic
 "I'll write any formula" fallback when dedicated activations
 (Sigmoid, TrigFunction) already exist in the library.

 <p>Augmentations and pitfalls are accumulated wisdom about a few
 frequently-seen modeling domains.  They are deliberately short and
 advisory rather than exhaustive.  If a domain is not on the
 augmentation list, the probe still works — it just relies on the
 literal goal tokens to match library actors directly.

 @author Ptolemy II Agent contributors
 @version $Id$
 @since Ptolemy II 11.1
 */
public final class CapabilityProbe {

    /** Maximum recommended actors carried into the prompt; keeps the
     *  injected context block small. */
    private static final int MAX_RECOMMENDED = 12;

    /** Library search depth per term.  Library entries that score
     *  hits across several terms get de-duplicated. */
    private static final int PER_TERM_LIMIT = 6;

    /** High-level domain hints.  Mapping is LOW-RESOLUTION on
     *  purpose: each entry just expands a single goal token into a
     *  handful of canonical primitive keywords that the library
     *  search will then resolve into real actor entries.  Entries
     *  here are suggestions, not rules; nothing else in the system
     *  enforces these mappings. */
    private static final Map<String, List<String>> AUGMENT;
    static {
        Map<String, List<String>> m = new LinkedHashMap<>();
        m.put("lstm",     Arrays.asList("sigmoid", "trigfunction",
                "multiplydivide", "addsubtract", "sampledelay",
                "const", "scale", "exp"));
        m.put("rnn",      Arrays.asList("sigmoid", "trigfunction",
                "multiplydivide", "addsubtract", "sampledelay",
                "const", "scale", "exp"));
        m.put("gru",      Arrays.asList("sigmoid", "trigfunction",
                "multiplydivide", "addsubtract", "sampledelay",
                "const", "scale", "exp"));
        m.put("gate",     Arrays.asList("sigmoid", "multiplydivide",
                "addsubtract"));
        m.put("activation", Arrays.asList("sigmoid", "trigfunction"));
        m.put("tanh",     Arrays.asList("trigfunction"));
        m.put("sigmoid",  Arrays.asList("sigmoid", "exp"));
        m.put("pid",      Arrays.asList("scale", "integrator",
                "derivative", "addsubtract"));
        m.put("controller", Arrays.asList("scale", "integrator",
                "derivative", "addsubtract"));
        m.put("rc",       Arrays.asList("integrator", "scale",
                "addsubtract"));
        m.put("lowpass",  Arrays.asList("integrator", "sampledelay",
                "scale", "addsubtract"));
        m.put("fir",      Arrays.asList("sampledelay", "scale",
                "addsubtract"));
        m.put("iir",      Arrays.asList("sampledelay", "scale",
                "addsubtract"));
        m.put("filter",   Arrays.asList("integrator", "sampledelay",
                "scale", "addsubtract"));
        m.put("ode",      Arrays.asList("integrator", "derivative"));
        m.put("spring",   Arrays.asList("integrator", "derivative"));
        m.put("pendulum", Arrays.asList("integrator", "derivative",
                "trigfunction"));
        m.put("mass",     Arrays.asList("integrator", "derivative"));
        AUGMENT = Collections.unmodifiableMap(m);
    }

    /** When an AUGMENT-expanded keyword returns zero hits from the
     *  scanned library but the canonical actor class is still on the
     *  classpath, surface it from this fallback so a library-scan
     *  gap (e.g. Sigmoid not being in basicLibrary.xml) doesn't
     *  silently strip it out of the recipe.  This is a thin escape
     *  hatch, not a rule list. */
    private static final Map<String, String[]> FALLBACK_CLASSES;
    static {
        Map<String, String[]> m = new LinkedHashMap<>();
        // No dedicated Sigmoid actor exists in this Ptolemy tree;
        // surface UnaryMathFunction so the LLM can compose sigmoid
        // as 1 / (1 + exp(-x)) without falling back to Expression.
        m.put("sigmoid", new String[] {"ptolemy.actor.lib.UnaryMathFunction"});
        m.put("exp",     new String[] {"ptolemy.actor.lib.UnaryMathFunction"});
        m.put("log",     new String[] {"ptolemy.actor.lib.UnaryMathFunction"});
        m.put("tanh",    new String[] {"ptolemy.actor.lib.TrigFunction"});
        FALLBACK_CLASSES = Collections.unmodifiableMap(m);
    }

    /** Universal modeling lessons surfaced in every recipe.  Each
     *  entry is phrased as a "prefer X over Y because Z" suggestion
     *  so the LLM has actionable alternatives, not just a ban. */
    private static final String[] UNIVERSAL_PITFALLS = {
            "Prefer dedicated actors (Sigmoid, TrigFunction with"
                    + " tanh/sin/cos, Integrator, Derivative) over"
                    + " Expression.  Expression's named input ports must"
                    + " be declared as PortParameter properties; the"
                    + " agent's add_entity tool creates a plain actor"
                    + " and the expected named inputs will be missing"
                    + " unless you add those PortParameters yourself.",
            "For headless agent runs use Recorder.  Avoid Display,"
                    + " TimedPlotter, SequencePlotter and XYPlotter —"
                    + " they require Swing and will fail in this"
                    + " environment.",
            "SDF feedback loops MUST pass through SampleDelay (or"
                    + " another sample-delay actor); otherwise the SDF"
                    + " scheduler reports a topology cycle.",
    };

    /** Goal-token stopwords; trimmed before searching the library. */
    private static final Set<String> STOPWORDS = new HashSet<>(
            Arrays.asList("the", "a", "an", "of", "for", "and", "or",
                    "to", "in", "is", "with", "model", "modeling",
                    "build", "make", "create", "that", "this", "use",
                    "using", "want", "like", "please", "add", "help",
                    "as", "by", "into", "from", "we", "i", "me", "you",
                    "demo", "example", "simple", "small",
                    "ptolemy", "actor", "system"));

    private CapabilityProbe() {
    }

    /** Compute a recipe for a user goal against the live library.
     *  Always returns a JSON object, possibly with an empty
     *  {@code recommendedActors} array when no token resolved to any
     *  library entry.  Pitfalls are always included.
     *  @param userGoal Free-form natural-language goal.
     *  @param lib Shared library index.
     *  @return Recipe JSON. */
    public static JSONObject probe(String userGoal, LibraryIndex lib) {
        JSONObject out = new JSONObject();
        out.put("source", "capability-probe-v1");

        Set<String> tokens = _tokens(userGoal);
        Set<String> matchedHints = new LinkedHashSet<>();
        // High-signal canonical keywords go FIRST so their actor hits
        // fill the top recipe slots before generic literal-token
        // matches.  This prevents "sdf"-tagged utility actors
        // (ArrayToSequence, BitsToInt, ...) from drowning out the
        // canonical LSTM primitives (Sigmoid, TrigFunction, etc.).
        Set<String> searchTerms = new LinkedHashSet<>();
        for (String tok : tokens) {
            List<String> aug = AUGMENT.get(tok);
            if (aug != null) {
                matchedHints.add(tok);
                for (String a : aug) {
                    searchTerms.add(a);
                }
            }
        }
        for (String tok : tokens) {
            if (tok.length() >= 3) {
                searchTerms.add(tok);
            }
        }

        JSONArray recommended = new JSONArray();
        if (lib != null && !searchTerms.isEmpty()) {
            Map<String, JSONObject> hits = new LinkedHashMap<>();
            for (String term : searchTerms) {
                if (term.length() < 2) {
                    continue;
                }
                int beforeSize = hits.size();
                JSONArray results = lib.search(term, "", 0,
                        PER_TERM_LIMIT, true);
                if (results != null) {
                    for (int i = 0; i < results.length(); i++) {
                        JSONObject entry = results.optJSONObject(i);
                        if (entry == null) {
                            continue;
                        }
                        String cls = entry.optString("className", "");
                        if (cls.isEmpty() || _isHeadlessUnfriendly(cls)) {
                            continue;
                        }
                        // Keep the FIRST hit per class so the
                        // most-direct term wins.  We don't try to
                        // score across terms; the prompt block is
                        // short enough that ordering by insertion is
                        // fine.
                        hits.putIfAbsent(cls, entry);
                    }
                }
                // Library scans miss some otherwise-canonical actors
                // (e.g. Sigmoid is not in basicLibrary.xml).  If this
                // augment term contributed nothing new but a
                // well-known class for it exists on the classpath,
                // surface a minimal entry so the LLM can still see
                // it as a recommendation.
                if (hits.size() == beforeSize) {
                    _addFallbackEntries(term, hits);
                }
            }
            int n = 0;
            for (JSONObject entry : hits.values()) {
                if (n++ >= MAX_RECOMMENDED) {
                    break;
                }
                recommended.put(entry);
            }
        }

        out.put("matchedDomainKeywords",
                new JSONArray(new ArrayList<>(matchedHints)));
        out.put("recommendedActors", recommended);
        out.put("pitfalls", _pitfalls());
        return out;
    }

    /** Render the recipe as a short prompt block.  Returns an empty
     *  string when there is nothing useful to surface (no actor hits
     *  AND no goal context) so callers can skip the injection
     *  cleanly.
     *  @param recipe Recipe produced by {@link #probe}.
     *  @return Markdown-flavoured prompt block, or empty string. */
    public static String promptBlock(JSONObject recipe) {
        if (recipe == null) {
            return "";
        }
        JSONArray actors = recipe.optJSONArray("recommendedActors");
        JSONArray pitfalls = recipe.optJSONArray("pitfalls");
        boolean hasActors = actors != null && actors.length() > 0;
        boolean hasPitfalls = pitfalls != null && pitfalls.length() > 0;
        if (!hasActors && !hasPitfalls) {
            return "";
        }
        StringBuilder b = new StringBuilder();
        b.append("Library capability scan for your goal (use this as a")
                .append(" SOFT prior — these are actors that already")
                .append(" exist in the loaded library and look relevant.")
                .append("  Reach for them BEFORE falling back to a")
                .append(" generic Expression formula.  Nothing here is a")
                .append(" hard rule; if a recommended actor really does")
                .append(" not fit, ignore it and explain why.):\n\n```json\n");
        b.append(recipe.toString(2));
        b.append("\n```\n");
        return b.toString();
    }

    /** Split a goal into normalized tokens for library matching. */
    private static Set<String> _tokens(String text) {
        Set<String> out = new LinkedHashSet<>();
        if (text == null || text.isEmpty()) {
            return out;
        }
        String[] raw = text.toLowerCase(Locale.US)
                .split("[^a-z0-9]+");
        for (String r : raw) {
            if (r.length() >= 2 && !STOPWORDS.contains(r)) {
                out.add(r);
            }
        }
        return out;
    }

    /** Filter out actors that don't work in this headless backend.
     *  GUI plotters / scopes / sketched sources require Swing and
     *  fail the way any other graphical class fails under
     *  RemoveGraphicalClasses.  Their presence in recipe hits just
     *  pushes truly-useful actors off the top of the list, so drop
     *  them up front. */
    private static boolean _isHeadlessUnfriendly(String className) {
        if (className == null) {
            return false;
        }
        return className.contains(".gui.")
                || className.startsWith("ptolemy.vergil.");
    }

    /** Best-effort: surface canonical classes for keywords that the
     *  library scan failed to index. */
    private static void _addFallbackEntries(String term,
            Map<String, JSONObject> hits) {
        String[] classes = FALLBACK_CLASSES.get(term);
        if (classes == null) {
            return;
        }
        for (String cls : classes) {
            if (hits.containsKey(cls)) {
                continue;
            }
            try {
                Class.forName(cls);
            } catch (Throwable ignored) {
                continue;
            }
            JSONObject entry = new JSONObject();
            entry.put("className", cls);
            int dot = cls.lastIndexOf('.');
            entry.put("displayName",
                    dot >= 0 ? cls.substring(dot + 1) : cls);
            entry.put("category", "fallback");
            entry.put("note", "class on classpath but not in"
                    + " library index — usable via add_entity");
            hits.put(cls, entry);
        }
    }

    private static JSONArray _pitfalls() {
        JSONArray out = new JSONArray();
        for (String p : UNIVERSAL_PITFALLS) {
            out.put(p);
        }
        return out;
    }
}

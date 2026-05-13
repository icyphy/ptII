/* Searchable index of Ptolemy II actors exposed to the agent and UI.

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
package org.ptolemy.agent.library;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

///////////////////////////////////////////////////////////////////
//// LibraryIndex

/**
 Searchable catalog of Ptolemy II components. The primary catalog is
 built by scanning {@code ptolemy/configs/basicLibrary.xml}; a compact
 curated fallback keeps the agent usable if a local Ptolemy tree cannot
 be scanned.

 @author Ptolemy II Agent contributors
 @version $Id$
 @since Ptolemy II 11.1
 */
public final class LibraryIndex {

    /** A single catalog entry. */
    public static final class Entry {
        public final String className;
        public final String displayName;
        public final String category;
        public final String description;
        public final List<String> inputs;
        public final List<String> outputs;
        public final List<Param> parameters;

        public Entry(String className, String displayName, String category,
                String description, List<String> inputs, List<String> outputs,
                List<Param> parameters) {
            this.className = className == null ? "" : className;
            this.displayName = displayName == null ? "" : displayName;
            this.category = category == null ? "" : category;
            this.description = description == null ? "" : description;
            this.inputs = Collections.unmodifiableList(
                    new ArrayList<String>(inputs));
            this.outputs = Collections.unmodifiableList(
                    new ArrayList<String>(outputs));
            this.parameters = Collections.unmodifiableList(
                    new ArrayList<Param>(parameters));
        }

        public JSONObject toJson() {
            JSONObject json = toSummaryJson();
            JSONArray params = new JSONArray();
            for (Param p : parameters) {
                params.put(p.toJson());
            }
            json.put("parameters", params);
            return json;
        }

        public JSONObject toSummaryJson() {
            JSONObject json = new JSONObject();
            json.put("className", className);
            json.put("displayName", displayName);
            json.put("category", category);
            json.put("description", description);
            json.put("inputs", new JSONArray(inputs));
            json.put("outputs", new JSONArray(outputs));
            return json;
        }
    }

    /** Description of an actor parameter. */
    public static final class Param {
        public final String name;
        public final String defaultValue;
        public final String description;

        public Param(String name, String defaultValue, String description) {
            this.name = name == null ? "" : name;
            this.defaultValue = defaultValue == null ? "" : defaultValue;
            this.description = description == null ? "" : description;
        }

        public JSONObject toJson() {
            JSONObject json = new JSONObject();
            json.put("name", name);
            json.put("default", defaultValue);
            json.put("description", description);
            return json;
        }
    }

    private static final LibraryIndex INSTANCE = new LibraryIndex();

    private final List<Entry> _entries;
    private final Map<String, Entry> _byClassName;
    private final String _source;
    private final String _scanError;

    private LibraryIndex() {
        List<Entry> entries;
        String source;
        String scanError = "";
        try {
            entries = LibraryScanner.scanDefault();
            if (entries.isEmpty()) {
                throw new IllegalStateException("scanner returned no entries");
            }
            source = "scanned";
        } catch (Throwable t) {
            entries = _fallbackEntries();
            source = "fallback";
            scanError = t.getMessage() == null ? t.toString()
                    : t.getMessage();
        }

        _entries = Collections.unmodifiableList(entries);
        _byClassName = new LinkedHashMap<String, Entry>();
        for (Entry entry : entries) {
            _byClassName.put(entry.className, entry);
        }
        _source = source;
        _scanError = scanError;
    }

    /** @return The shared singleton. */
    public static LibraryIndex shared() {
        return INSTANCE;
    }

    /** Find one component by class name or display name.
     *  @param name Class name or display name.
     *  @return The matching entry, or null.
     */
    public Entry find(String name) {
        if (name == null || name.length() == 0) {
            return null;
        }
        Entry exact = _byClassName.get(name);
        if (exact != null) {
            return exact;
        }
        String q = _normalize(name);
        for (Entry entry : _entries) {
            if (_normalize(entry.displayName).equals(q)
                    || _normalize(entry.className).equals(q)) {
                return entry;
            }
        }
        return null;
    }

    /** Search the catalog.
     *  @param query Case-insensitive substring against key fields.
     *  @param limit Maximum entries returned.
     *  @return JSON array of matching entries.
     */
    public JSONArray search(String query, int limit) {
        return search(query, "", 0, limit, false);
    }

    /** Search the catalog with paging and optional summary mode.
     *  @param query Case-insensitive substring against key fields.
     *  @param category Optional category substring.
     *  @param offset Number of matching entries to skip.
     *  @param limit Maximum entries returned.
     *  @param summary True to omit parameters.
     *  @return JSON array of matching entries.
     */
    public JSONArray search(String query, String category, int offset,
            int limit, boolean summary) {
        JSONArray out = new JSONArray();
        String q = _normalize(query);
        String cat = _normalize(category);
        int skipped = 0;
        int count = 0;
        int max = limit <= 0 ? 20 : limit;
        int skip = Math.max(0, offset);
        for (Entry entry : _entries) {
            if (!_matches(entry, q, cat)) {
                continue;
            }
            if (skipped < skip) {
                skipped++;
                continue;
            }
            if (count >= max) {
                break;
            }
            out.put(summary ? entry.toSummaryJson() : entry.toJson());
            count++;
        }
        return out;
    }

    /** @return Every entry, as full JSON. */
    public JSONArray all() {
        JSONArray out = new JSONArray();
        for (Entry e : _entries) {
            out.put(e.toJson());
        }
        return out;
    }

    /** @return Metadata about how the index was built. */
    public JSONObject status() {
        JSONObject json = new JSONObject();
        json.put("source", _source);
        json.put("count", _entries.size());
        if (_scanError.length() > 0) {
            json.put("scanError", _scanError);
        }
        return json;
    }

    private static boolean _matches(Entry entry, String query,
            String category) {
        if (category.length() > 0
                && !_normalize(entry.category).contains(category)) {
            return false;
        }
        if (query.length() == 0) {
            return true;
        }
        if (_normalize(entry.className).contains(query)
                || _normalize(entry.displayName).contains(query)
                || _normalize(entry.category).contains(query)
                || _normalize(entry.description).contains(query)) {
            return true;
        }
        for (String input : entry.inputs) {
            if (_normalize(input).contains(query)) {
                return true;
            }
        }
        for (String output : entry.outputs) {
            if (_normalize(output).contains(query)) {
                return true;
            }
        }
        for (Param parameter : entry.parameters) {
            if (_normalize(parameter.name).contains(query)) {
                return true;
            }
        }
        return false;
    }

    private static String _normalize(String text) {
        return text == null ? "" : text.toLowerCase(Locale.US);
    }

    private static List<Entry> _fallbackEntries() {
        List<Entry> list = new ArrayList<Entry>();
        list.add(new Entry("ptolemy.actor.lib.Ramp", "Ramp", "Sources",
                "Emit an arithmetic sequence.",
                Collections.<String>emptyList(),
                Collections.singletonList("output"),
                _params(new Param("init", "0", ""),
                        new Param("step", "1", ""),
                        new Param("firingCountLimit", "0", ""))));
        list.add(new Entry("ptolemy.actor.lib.Const", "Const", "Sources",
                "Emit a constant value.", Collections.<String>emptyList(),
                Collections.singletonList("output"),
                _params(new Param("value", "1", ""))));
        list.add(new Entry("ptolemy.actor.lib.Scale", "Scale", "Math",
                "Multiply every input token by a fixed factor.",
                Collections.singletonList("input"),
                Collections.singletonList("output"),
                _params(new Param("factor", "1.0", ""))));
        list.add(new Entry("ptolemy.actor.lib.AddSubtract", "AddSubtract",
                "Math", "Sum and subtract multiport inputs.",
                _strings("plus", "minus"), Collections.singletonList("output"),
                Collections.<Param>emptyList()));
        list.add(new Entry("ptolemy.actor.lib.MultiplyDivide",
                "MultiplyDivide", "Math",
                "Multiply and divide multiport inputs.",
                _strings("multiply", "divide"),
                Collections.singletonList("output"),
                Collections.<Param>emptyList()));
        list.add(new Entry("ptolemy.actor.lib.Integrator", "Integrator",
                "Continuous", "Continuous-time integrator.",
                Collections.singletonList("input"),
                Collections.singletonList("output"),
                _params(new Param("initialState", "0.0", ""))));
        list.add(new Entry("ptolemy.actor.lib.Expression", "Expression",
                "Math", "Evaluate a Ptolemy expression.",
                Collections.<String>emptyList(),
                Collections.singletonList("output"),
                _params(new Param("expression", "0.0", ""))));
        list.add(new Entry("ptolemy.actor.lib.Recorder", "Recorder",
                "Sinks", "Record every input token.",
                Collections.singletonList("input"),
                Collections.<String>emptyList(),
                _params(new Param("capacity", "-1", ""))));
        list.add(new Entry("ptolemy.actor.lib.Discard", "Discard", "Sinks",
                "Discard every input token.",
                Collections.singletonList("input"),
                Collections.<String>emptyList(),
                Collections.<Param>emptyList()));
        list.add(new Entry("ptolemy.domains.sdf.kernel.SDFDirector",
                "SDF Director", "Directors",
                "Synchronous-dataflow scheduler.",
                Collections.<String>emptyList(),
                Collections.<String>emptyList(),
                _params(new Param("iterations", "10", ""))));
        list.add(new Entry("ptolemy.domains.de.kernel.DEDirector",
                "DE Director", "Directors", "Discrete-event scheduler.",
                Collections.<String>emptyList(),
                Collections.<String>emptyList(),
                _params(new Param("stopTime", "10.0", ""))));
        list.add(new Entry(
                "ptolemy.domains.continuous.kernel.ContinuousDirector",
                "Continuous Director", "Directors",
                "Continuous-time ODE solver.",
                Collections.<String>emptyList(),
                Collections.<String>emptyList(),
                _params(new Param("stopTime", "10.0", ""),
                        new Param("maxStepSize", "0.05", ""))));
        return list;
    }

    private static List<String> _strings(String... items) {
        List<String> out = new ArrayList<String>(items.length);
        for (int i = 0; i < items.length; i++) {
            out.add(items[i]);
        }
        return out;
    }

    private static List<Param> _params(Param... params) {
        List<Param> out = new ArrayList<Param>(params.length);
        for (int i = 0; i < params.length; i++) {
            out.add(params[i]);
        }
        return out;
    }
}

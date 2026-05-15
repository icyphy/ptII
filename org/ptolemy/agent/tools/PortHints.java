/* Fuzzy entity/port suggestions for tool preflight errors.

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
package org.ptolemy.agent.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;

import ptolemy.actor.IOPort;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// PortHints

/**
 Fuzzy lookup helpers used by {@link ToolCallValidator} and friends to
 turn bare "port/entity not found" errors into actionable hints that
 the LLM can act on without burning another round-trip.  All methods
 are best-effort and return empty JSON shapes on any failure.

 <p>The two main operations are:
 <ul>
 <li>{@link #describePorts(CompositeEntity, String)} — given a dotted
     entity name (or empty for the scope itself), enumerate the real
     input / output ports so the agent can compare its guess against
     reality.</li>
 <li>{@link #suggestPorts(CompositeEntity, String, String)} — given a
     bad port reference such as {@code "AddSubtract.input"} and the
     desired direction, return the top-N closest legal references in
     the scope (case-insensitive Levenshtein, prefix preferred).</li>
 </ul>

 @author Ptolemy II Agent contributors
 @version $Id$
 @since Ptolemy II 11.1
 */
final class PortHints {

    /** Maximum suggestions returned per query. */
    private static final int MAX_SUGGESTIONS = 3;

    /** Reject candidates whose Levenshtein distance to the target
     *  exceeds this bound; helps avoid unrelated noise on short names. */
    private static final int MAX_DISTANCE = 3;

    private PortHints() {
    }

    /** Split a dotted port reference such as {@code "Ramp1.output"}
     *  into {@code [entity, port]}.  When the reference contains no
     *  dot, returns {@code ["", reference]} — that is the boundary-port
     *  syntax used inside composites.
     *  @param reference Raw user-provided port ref.
     *  @return Two-element array {entity, port}; both may be empty. */
    static String[] splitRef(String reference) {
        if (reference == null) {
            return new String[] { "", "" };
        }
        int dot = reference.lastIndexOf('.');
        if (dot < 0) {
            return new String[] { "", reference };
        }
        return new String[] { reference.substring(0, dot),
                reference.substring(dot + 1) };
    }

    /** List all child entity names that exist in {@code scope},
     *  hiding agent-private probe Recorders.
     *  @param scope Composite to enumerate.
     *  @return JSON array of entity names. */
    static JSONArray entityNames(CompositeEntity scope) {
        JSONArray out = new JSONArray();
        if (scope == null) {
            return out;
        }
        @SuppressWarnings("unchecked")
        List<ComponentEntity> entities = new ArrayList<ComponentEntity>(
                scope.entityList());
        for (ComponentEntity entity : entities) {
            String name = entity.getName();
            if (name == null || name.startsWith("__recorder__")) {
                continue;
            }
            out.put(name);
        }
        return out;
    }

    /** Composite-child names only (used to suggest a "parent"
     *  argument). */
    static JSONArray compositeNames(CompositeEntity scope) {
        JSONArray out = new JSONArray();
        if (scope == null) {
            return out;
        }
        @SuppressWarnings("unchecked")
        List<ComponentEntity> entities = new ArrayList<ComponentEntity>(
                scope.entityList());
        for (ComponentEntity entity : entities) {
            if (entity instanceof CompositeEntity
                    && entity.getName() != null
                    && !entity.getName().startsWith("__recorder__")) {
                out.put(entity.getName());
            }
        }
        return out;
    }

    /** Build a {@code {entity, inputs, outputs, boundary}} description
     *  for the target of a port reference.  When {@code entityName} is
     *  empty, describes the scope itself (boundary ports of the
     *  containing composite).
     *  @param scope Composite the reference is resolved in.
     *  @param entityName Entity prefix from the reference; may be "".
     *  @return JSON describing the entity, or empty JSON if not found. */
    static JSONObject describePorts(CompositeEntity scope,
            String entityName) {
        JSONObject out = new JSONObject();
        if (scope == null) {
            return out;
        }
        NamedObj target;
        boolean boundary = false;
        if (entityName == null || entityName.length() == 0) {
            target = scope;
            boundary = true;
        } else {
            target = scope.getEntity(entityName);
            if (target == null) {
                return out;
            }
        }
        JSONArray inputs = new JSONArray();
        JSONArray outputs = new JSONArray();
        @SuppressWarnings("unchecked")
        List<Port> ports = ((target instanceof ComponentEntity)
                ? ((ComponentEntity) target).portList()
                : ((CompositeEntity) target).portList());
        for (Port port : ports) {
            if (!(port instanceof IOPort)) {
                continue;
            }
            IOPort io = (IOPort) port;
            JSONObject row = new JSONObject();
            row.put("name", io.getName());
            row.put("multiport", io.isMultiport());
            // From the wiring caller's perspective, a boundary INPUT
            // port of the scope itself behaves as a source (data flows
            // from outside in); flip the sense so the LLM sees ports
            // it can legally wire FROM.
            boolean effectivelyOutput = boundary ? io.isInput()
                    : io.isOutput();
            boolean effectivelyInput = boundary ? io.isOutput()
                    : io.isInput();
            if (effectivelyOutput) {
                outputs.put(row);
            }
            if (effectivelyInput) {
                inputs.put(row);
            }
        }
        out.put("entity", entityName == null ? "" : entityName);
        out.put("inputs", inputs);
        out.put("outputs", outputs);
        out.put("boundary", boundary);
        return out;
    }

    /** Best-guess fully-qualified port references in the scope that
     *  the user might have meant.  Honors a desired direction so an
     *  AddSubtract output is not suggested as a substitute for a
     *  missing input.
     *  @param scope Composite to search.
     *  @param reference The bad reference, e.g. {@code "AddSubtract.input"}.
     *  @param wantDirection One of {@code "output"}, {@code "input"},
     *      or {@code "any"}.  Boundary ports of the scope itself are
     *      treated with the wiring-caller flip described above.
     *  @return Up to {@link #MAX_SUGGESTIONS} candidate references,
     *      ranked best-first. */
    static JSONArray suggestPorts(CompositeEntity scope, String reference,
            String wantDirection) {
        JSONArray out = new JSONArray();
        if (scope == null || reference == null) {
            return out;
        }
        String[] parts = splitRef(reference);
        String entityName = parts[0];
        String portName = parts[1];

        // If the entity prefix points nowhere, fall back to scanning
        // every entity in the scope.  Otherwise restrict to that one.
        List<ComponentEntity> targets = new ArrayList<ComponentEntity>();
        if (entityName.length() > 0) {
            ComponentEntity exact = scope.getEntity(entityName);
            if (exact != null) {
                targets.add(exact);
            }
        }
        if (targets.isEmpty()) {
            @SuppressWarnings("unchecked")
            List<ComponentEntity> all = new ArrayList<ComponentEntity>(
                    scope.entityList());
            for (ComponentEntity e : all) {
                if (e.getName() != null
                        && !e.getName().startsWith("__recorder__")) {
                    targets.add(e);
                }
            }
        }

        // Build candidate refs in the form "Entity.port" (or just
        // "port" for boundary ports of the scope), filtered by
        // direction.
        List<String> candidates = new ArrayList<String>();
        for (ComponentEntity entity : targets) {
            @SuppressWarnings("unchecked")
            List<Port> ports = entity.portList();
            for (Port port : ports) {
                if (!(port instanceof IOPort)) {
                    continue;
                }
                IOPort io = (IOPort) port;
                if (!_directionMatches(io, wantDirection, false)) {
                    continue;
                }
                candidates.add(entity.getName() + "." + io.getName());
            }
        }
        // Scope boundary ports — usable from inside a composite.
        @SuppressWarnings("unchecked")
        List<Port> boundaryPorts = scope.portList();
        for (Port port : boundaryPorts) {
            if (!(port instanceof IOPort)) {
                continue;
            }
            IOPort io = (IOPort) port;
            if (!_directionMatches(io, wantDirection, true)) {
                continue;
            }
            candidates.add(io.getName());
        }

        List<String> ranked = closest(reference, candidates,
                MAX_SUGGESTIONS, MAX_DISTANCE);
        if (ranked.isEmpty()) {
            // Fall back to ranking on just the port-name half — many
            // typos are in the port (".input" vs ".plus") rather than
            // the entity prefix.
            List<String> portOnly = new ArrayList<String>();
            for (String candidate : candidates) {
                String[] split = splitRef(candidate);
                portOnly.add(split[1]);
            }
            List<String> portRanked = closest(portName, portOnly,
                    MAX_SUGGESTIONS, MAX_DISTANCE);
            for (String pickPort : portRanked) {
                // Re-attach the entity prefix corresponding to the
                // first candidate that uses this port name.
                for (String full : candidates) {
                    if (splitRef(full)[1].equals(pickPort)) {
                        ranked.add(full);
                        break;
                    }
                }
            }
        }
        for (String r : ranked) {
            out.put(r);
        }
        return out;
    }

    /** Suggest entity names in the scope close to {@code badName}. */
    static JSONArray suggestEntities(CompositeEntity scope,
            String badName) {
        JSONArray out = new JSONArray();
        if (scope == null || badName == null || badName.length() == 0) {
            return out;
        }
        JSONArray names = entityNames(scope);
        List<String> candidates = new ArrayList<String>();
        for (int i = 0; i < names.length(); i++) {
            candidates.add(names.optString(i, ""));
        }
        for (String pick : closest(badName, candidates, MAX_SUGGESTIONS,
                MAX_DISTANCE)) {
            out.put(pick);
        }
        return out;
    }

    /** Suggest top-level composite names close to {@code badName}. */
    static JSONArray suggestComposites(CompositeEntity scope,
            String badName) {
        JSONArray out = new JSONArray();
        if (scope == null || badName == null || badName.length() == 0) {
            return out;
        }
        JSONArray composites = compositeNames(scope);
        List<String> candidates = new ArrayList<String>();
        for (int i = 0; i < composites.length(); i++) {
            candidates.add(composites.optString(i, ""));
        }
        for (String pick : closest(badName, candidates, MAX_SUGGESTIONS,
                MAX_DISTANCE)) {
            out.put(pick);
        }
        return out;
    }

    /** Suggest attribute / parameter names on a target object close to
     *  {@code badName}. */
    static JSONArray suggestAttributes(NamedObj target, String badName) {
        JSONArray out = new JSONArray();
        if (target == null || badName == null || badName.length() == 0) {
            return out;
        }
        @SuppressWarnings("unchecked")
        List<Attribute> attrs = new ArrayList<Attribute>(
                target.attributeList());
        List<String> candidates = new ArrayList<String>();
        for (Attribute a : attrs) {
            if (a != null && a.getName() != null
                    && !a.getName().startsWith("_")) {
                candidates.add(a.getName());
            }
        }
        for (String pick : closest(badName, candidates, MAX_SUGGESTIONS,
                MAX_DISTANCE)) {
            out.put(pick);
        }
        return out;
    }

    /** Return whether an IOPort matches the requested wiring
     *  direction.  When {@code boundary} is true the sense is flipped
     *  because boundary inputs act as sources to inner actors and vice
     *  versa.
     */
    private static boolean _directionMatches(IOPort io, String want,
            boolean boundary) {
        if (want == null || "any".equalsIgnoreCase(want)) {
            return true;
        }
        boolean effOut = boundary ? io.isInput() : io.isOutput();
        boolean effIn = boundary ? io.isOutput() : io.isInput();
        if ("output".equalsIgnoreCase(want)) {
            return effOut;
        }
        if ("input".equalsIgnoreCase(want)) {
            return effIn;
        }
        return true;
    }

    /** Levenshtein distance, case-insensitive. */
    static int distance(String a, String b) {
        if (a == null) a = "";
        if (b == null) b = "";
        a = a.toLowerCase(Locale.US);
        b = b.toLowerCase(Locale.US);
        int n = a.length();
        int m = b.length();
        if (n == 0) return m;
        if (m == 0) return n;
        int[] prev = new int[m + 1];
        int[] curr = new int[m + 1];
        for (int j = 0; j <= m; j++) {
            prev[j] = j;
        }
        for (int i = 1; i <= n; i++) {
            curr[0] = i;
            for (int j = 1; j <= m; j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                curr[j] = Math.min(
                        Math.min(curr[j - 1] + 1, prev[j] + 1),
                        prev[j - 1] + cost);
            }
            int[] swap = prev;
            prev = curr;
            curr = swap;
        }
        return prev[m];
    }

    /** Rank candidates by closeness to {@code target}, returning the
     *  top {@code k} entries whose distance is no more than
     *  {@code threshold}.  Exact matches (case-insensitive) sort
     *  first, then prefix matches, then everything else by
     *  Levenshtein distance.
     */
    static List<String> closest(String target,
            Collection<String> candidates, int k, int threshold) {
        List<String> out = new ArrayList<String>();
        if (target == null || candidates == null || candidates.isEmpty()) {
            return out;
        }
        List<Scored> scored = new ArrayList<Scored>();
        String lower = target.toLowerCase(Locale.US);
        for (String candidate : candidates) {
            if (candidate == null || candidate.length() == 0) {
                continue;
            }
            String clower = candidate.toLowerCase(Locale.US);
            int dist = distance(target, candidate);
            // Bucket: 0 exact, 1 prefix/suffix, 2 substring, 3 plain
            // Levenshtein.  Lower bucket sorts first.
            int bucket;
            if (clower.equals(lower)) {
                bucket = 0;
            } else if (clower.startsWith(lower) || lower.startsWith(clower)
                    || clower.endsWith(lower) || lower.endsWith(clower)) {
                bucket = 1;
            } else if (clower.contains(lower) || lower.contains(clower)) {
                bucket = 2;
            } else {
                bucket = 3;
            }
            // Substring / prefix matches squeak through even if their
            // raw Levenshtein distance is large (think
            // "MulHidden_multiply" vs "multiply").
            if (bucket >= 3 && dist > threshold) {
                continue;
            }
            scored.add(new Scored(candidate, bucket, dist));
        }
        Collections.sort(scored);
        for (int i = 0; i < Math.min(k, scored.size()); i++) {
            out.add(scored.get(i).value);
        }
        return out;
    }

    private static final class Scored implements Comparable<Scored> {
        final String value;
        final int bucket;
        final int dist;
        Scored(String value, int bucket, int dist) {
            this.value = value;
            this.bucket = bucket;
            this.dist = dist;
        }
        @Override
        public int compareTo(Scored other) {
            if (this.bucket != other.bucket) {
                return this.bucket - other.bucket;
            }
            if (this.dist != other.dist) {
                return this.dist - other.dist;
            }
            return this.value.compareTo(other.value);
        }
    }
}

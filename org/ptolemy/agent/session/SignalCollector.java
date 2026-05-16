/* Inject Recorder actors and harvest simulation signals as JSON.

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
package org.ptolemy.agent.session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.Recorder;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// SignalCollector

/**
 Companion of {@link PtolemySession} that injects
 {@link ptolemy.actor.lib.Recorder} actors into the model and reads back
 their data after a run. The collector wires one recorder per
 actor-output port found inside the top-level composite, so that the
 entire "tracee" of a simulation becomes addressable from the HTTP API
 without the user having to add observation actors manually.

 <p>Heuristics:
 <ul>
 <li>For every immediate child actor of the top level, every output
     port that already has at least one connection (i.e. somebody
     downstream is reading it) is mirrored into a recorder; ports with
     no downstream connection are also recorded so that "leaf" outputs
     are not dropped.</li>
 <li>Recorders are named {@code __recorder__<actor>_<port>} so they
     cannot collide with user-defined names.</li>
 <li>{@link #attach()} is idempotent: calling it twice does nothing
     the second time. {@link #detach()} removes the injected recorders
     and is safe to call before a fresh run.</li>
 </ul>

 <p>This class deliberately does NOT modify the original {@code .xml}
 file on disk. Recorders are added in-memory through the existing
 CompositeActor API; nothing leaks back to MoML serialization unless
 the caller asks for it.

 @author Ptolemy II Agent contributors
 @version $Id$
 @since Ptolemy II 11.1
 */
public class SignalCollector {

    private final CompositeActor _toplevel;

    /** Map of probe id ("actorName.portName") -> attached Recorder. */
    private final Map<String, Recorder> _probes = new HashMap<>();

    private boolean _attached = false;

    /** Build a collector for the given top-level. Recorders are not
     *  attached yet; call {@link #attach()} before running. */
    public SignalCollector(CompositeActor toplevel) {
        _toplevel = toplevel;
    }

    /** Walk the top-level and inject one Recorder per output port of
     *  every immediate child actor. Safe to call repeatedly. */
    public synchronized void attach() {
        if (_attached || _toplevel == null) {
            return;
        }
        // Allow disconnected graphs so that recorder probes (pure sinks)
        // don't cause SDF scheduler failures.
        _setAllowDisconnectedGraphs(true);

        @SuppressWarnings("unchecked")
        List<ComponentEntity> entities = new ArrayList<>(
                _toplevel.entityList());
        for (ComponentEntity entity : entities) {
            if (!(entity instanceof TypedAtomicActor)) {
                continue;
            }
            // Skip our own probes when re-attaching.
            if (entity.getName().startsWith("__recorder__")) {
                continue;
            }
            List<?> ports = ((TypedAtomicActor) entity).outputPortList();
            for (Object port : ports) {
                if (port instanceof IOPort) {
                    _attachProbe(entity, (IOPort) port);
                }
            }
        }
        _attached = true;
    }

    /** Set or restore allowDisconnectedGraphs on the director. */
    private void _setAllowDisconnectedGraphs(boolean value) {
        try {
            Director dir = _toplevel.getDirector();
            if (dir == null) return;
            Attribute attr = dir.getAttribute("allowDisconnectedGraphs");
            if (attr instanceof ptolemy.data.expr.Parameter) {
                ((ptolemy.data.expr.Parameter) attr).setToken(
                        new BooleanToken(value));
            }
        } catch (Exception ignored) {
            // best-effort
        }
    }

    /** Remove every previously attached Recorder. Safe to call even
     *  if {@link #attach()} was never called. */
    public synchronized void detach() {
        for (Recorder rec : _probes.values()) {
            try {
                // Unlink and drop every relation attached to this probe
                // FIRST. setContainer(null) on the recorder by itself
                // leaves the source-side relation orphaned and dangling,
                // which later confuses structural rewrites.
                List<Object> ports = new ArrayList<Object>(rec.portList());
                for (Object portObj : ports) {
                    if (!(portObj instanceof IOPort)) {
                        continue;
                    }
                    IOPort port = (IOPort) portObj;
                    List<?> rels = port.linkedRelationList();
                    List<Object> toRemove = new ArrayList<Object>(rels);
                    for (Object relObj : toRemove) {
                        if (relObj instanceof ptolemy.kernel.ComponentRelation) {
                            try {
                                ((ptolemy.kernel.ComponentRelation) relObj)
                                        .setContainer(null);
                            } catch (Throwable ignored) {
                                // best-effort
                            }
                        }
                    }
                }
                rec.setContainer(null);
            } catch (Throwable ignored) {
                // best-effort
            }
        }
        _probes.clear();
        _attached = false;
        _setAllowDisconnectedGraphs(false);
    }

    /** @return The latest harvested signal data, as a JSON object of
     *      the form
     *      <pre>
     *      {
     *        "probes": [
     *          {"id":"...", "actor":"...", "port":"...",
     *           "time":[t0,t1,...], "values":[v0,v1,...]}
     *        ]
     *      }
     *      </pre>
     *
     *  <p>Results come from two sources:
     *  <ol>
     *  <li>Auto-injected {@code __recorder__*} actors attached by
     *      {@link #attach()}.</li>
     *  <li>User-added {@code ptolemy.actor.lib.Recorder} actors found
     *      anywhere in the top-level composite.  For these the probe id
     *      is derived from the actor's upstream source port.</li>
     *  </ol>
     */
    public synchronized JSONObject toJson() {
        JSONObject root = new JSONObject();
        JSONArray probes = new JSONArray();

        // 1. Auto-probes injected by this collector.
        for (Map.Entry<String, Recorder> entry : _probes.entrySet()) {
            String id = entry.getKey();
            Recorder rec = entry.getValue();
            String[] parts = id.split("\\.", 2);
            JSONObject probe = new JSONObject();
            probe.put("id", id);
            probe.put("actor", parts.length > 0 ? parts[0] : id);
            probe.put("port",  parts.length > 1 ? parts[1] : "");
            probe.put("time",   _doubleListToJsonArray(rec.getTimeHistory()));
            probe.put("values", _tokenListToJsonArray(rec.getHistory(0)));
            probes.put(probe);
        }

        // 2. User-placed Recorder actors (not injected by this collector).
        if (_toplevel != null) {
            @SuppressWarnings("unchecked")
            List<ComponentEntity> entities =
                    new ArrayList<>(_toplevel.entityList());
            for (ComponentEntity entity : entities) {
                if (!(entity instanceof Recorder)) {
                    continue;
                }
                String name = entity.getName();
                // Skip auto-probes already covered above.
                if (name.startsWith("__recorder__")) {
                    continue;
                }
                Recorder rec = (Recorder) entity;
                // Determine probe id from the upstream source port.
                String probeId = _resolveProbeId(rec, name);
                String[] parts = probeId.split("\\.", 2);
                JSONObject probe = new JSONObject();
                probe.put("id",     probeId);
                probe.put("actor",  parts.length > 0 ? parts[0] : probeId);
                probe.put("port",   parts.length > 1 ? parts[1] : "input");
                probe.put("time",   _doubleListToJsonArray(rec.getTimeHistory()));
                probe.put("values", _tokenListToJsonArray(rec.getHistory(0)));
                probes.put(probe);
            }
        }

        root.put("probes", probes);
        return root;
    }

    /** Resolve a human-readable probe id for a user-placed Recorder.
     *  Tries to follow the first incoming connection back to its source
     *  port.  Falls back to the recorder's own name if no connection
     *  is found.
     */
    private static String _resolveProbeId(Recorder rec, String fallback) {
        try {
            IOPort inputPort = (IOPort) rec.getPort("input");
            if (inputPort == null) {
                return fallback;
            }
            @SuppressWarnings("unchecked")
            List<IOPort> sources = inputPort.connectedPortList();
            for (IOPort src : sources) {
                if (src.isOutput()) {
                    NamedObj owner = src.getContainer();
                    if (owner != null) {
                        return owner.getName() + "." + src.getName();
                    }
                }
            }
        } catch (Throwable ignored) {
            // best-effort
        }
        return fallback;
    }

    private void _attachProbe(ComponentEntity actor, IOPort port) {
        if (!(port instanceof TypedIOPort)) {
            return;
        }
        String name = "__recorder__" + actor.getName() + "_"
                + port.getName();
        if (_toplevel.getEntity(name) != null) {
            // Already exists (model reused across runs).
            ComponentEntity existing = _toplevel.getEntity(name);
            if (existing instanceof Recorder) {
                _probes.put(actor.getName() + "." + port.getName(),
                        (Recorder) existing);
            }
            return;
        }
        try {
            Recorder probe = new Recorder(_toplevel, name);
            IOPort probeIn = (IOPort) probe.getPort("input");
            if (probeIn == null) {
                probe.setContainer(null);
                return;
            }
            // Use the top-level workspace's connect() so we get a
            // proper relation, mirroring what the user would draw.
            _toplevel.connect(port, probeIn);
            _probes.put(actor.getName() + "." + port.getName(), probe);
        } catch (IllegalActionException | NameDuplicationException e) {
            // Best-effort: probes are diagnostic. Some single-ports already
            // linked to one relation cannot accept a probe; skip silently.
        }
    }

    private static JSONArray _doubleListToJsonArray(List<?> values) {
        JSONArray out = new JSONArray();
        if (values == null) {
            return out;
        }
        Iterator<?> it = values.iterator();
        while (it.hasNext()) {
            Object v = it.next();
            if (v instanceof Number) {
                double d = ((Number) v).doubleValue();
                if (Double.isFinite(d)) {
                    out.put(d);
                } else {
                    out.put(JSONObject.NULL);
                }
            } else {
                out.put(JSONObject.NULL);
            }
        }
        return out;
    }

    private static JSONArray _tokenListToJsonArray(List<?> tokens) {
        JSONArray out = new JSONArray();
        if (tokens == null) {
            return out;
        }
        for (Object t : tokens) {
            if (t instanceof DoubleToken) {
                double d = ((DoubleToken) t).doubleValue();
                if (Double.isFinite(d)) {
                    out.put(d);
                } else {
                    out.put(JSONObject.NULL);
                }
            } else if (t instanceof Token) {
                out.put(((Token) t).toString());
            } else if (t == null) {
                out.put(JSONObject.NULL);
            } else {
                out.put(t.toString());
            }
        }
        return out;
    }
}

/* A long-lived wrapper around one Ptolemy II model + Manager, used by
 the auto-modeling agent backend.

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

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.json.JSONObject;
import org.ptolemy.agent.util.AgentResult;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.ExecutionListener;
import ptolemy.actor.IOPort;
import ptolemy.actor.Manager;
import ptolemy.actor.injection.ActorModuleInitializer;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.BasicModelErrorHandler;
import ptolemy.kernel.util.ChangeListener;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.filter.BackwardCompatibility;
import ptolemy.moml.filter.RemoveGraphicalClasses;

///////////////////////////////////////////////////////////////////
//// PtolemySession

/**
 A long-lived wrapper around exactly one Ptolemy II model and its
 {@link ptolemy.actor.Manager}. Each HTTP/WS session in the agent
 backend owns one instance of this class.

 <p>The lifecycle is intentionally shaped to match the way Ptolemy II
 wants to be driven, taking the same steps as
 {@link ptolemy.moml.MoMLSimpleApplication} so that any model that
 runs there also runs here. In particular this class:

 <ul>
 <li>initializes the actor module injector once per JVM (required for
     non-graphical execution),</li>
 <li>installs the standard backward compatibility MoML filters plus
     {@code RemoveGraphicalClasses} so that headless execution does
     not pull in Swing components,</li>
 <li>keeps a {@link MoMLParser} ready for incremental
     {@code MoMLChangeRequest} edits performed by tools, and</li>
 <li>creates and attaches a {@link Manager}, listens to its
     execution state via {@link ExecutionListener}, and exposes
     {@link #status()} to callers.</li>
 </ul>

 <p>All public mutating methods are synchronized on the session. The
 backing model is mutated by other threads only through
 {@code requestChange(...)}; since this class is the single owner of
 the model it is safe to expose the {@link CompositeActor} reference
 for those callers.

 <p>Headless and graphical execution intentionally diverge: this class
 only supports the headless path. UI rendering happens on the React
 frontend over JSON.

 @author Ptolemy II Agent contributors
 @version $Id$
 @since Ptolemy II 11.1
 */
public class PtolemySession implements ChangeListener, ExecutionListener {

    /** Globally unique session id, exposed in REST URLs. */
    private final String _id;

    /** Wall-clock creation time, for diagnostics. */
    private final long _createdAtMs;

    /** Dedicated workspace for this session, so that models in
     *  different sessions do not interfere via the static
     *  filter registry. */
    private Workspace _workspace;

    /** The single parser used to load and to apply MoML fragments. */
    private MoMLParser _parser;

    /** The top-level composite actor. Null until {@link #loadFile} or
     *  {@link #loadMoml} has succeeded. */
    private CompositeActor _toplevel;

    /** Manager driving {@link #_toplevel}. */
    private Manager _manager;

    /** Auto-injected recorders that capture top-level output ports. */
    private SignalCollector _collector;

    /** Last execution exception (if any), wiped at each new run. */
    private volatile Throwable _lastError;

    /** Cached state, updated by the Manager via {@link ExecutionListener}. */
    private volatile String _state = "IDLE";

    private static volatile boolean _injectorInitialized = false;

    /** Build an empty session with a fresh id.
     *  @throws Exception If actor module initialization fails.
     */
    public PtolemySession() throws Exception {
        this("session-" + UUID.randomUUID().toString().substring(0, 8));
    }

    /** Build an empty session with an explicit id.
     *  @param id The session id, exposed as part of REST URLs.
     *  @throws Exception If actor module initialization fails.
     */
    public PtolemySession(String id) throws Exception {
        _id = id;
        _createdAtMs = System.currentTimeMillis();
        _initializeOnce();
        _workspace = new Workspace("AgentSession-" + id);
    }

    /** @return The session id. */
    public String id() {
        return _id;
    }

    /** @return The creation time as milliseconds since the epoch. */
    public long createdAtMs() {
        return _createdAtMs;
    }

    /** @return The current execution state ({@code IDLE}, {@code RUNNING},
     *      {@code FINISHED}, {@code ERROR}, etc.). */
    public String state() {
        return _state;
    }

    /** @return The model top-level, or null if nothing is loaded. */
    public CompositeActor toplevel() {
        return _toplevel;
    }

    /** @return The Manager, or null if no model is loaded. */
    public Manager manager() {
        return _manager;
    }

    /** Load a Ptolemy model from a {@code .xml} file on disk. Replaces
     *  any model previously held by this session.
     *  @param path Absolute or relative path to the MoML file.
     *  @return An AgentResult describing success or failure.
     */
    public synchronized AgentResult loadFile(String path) {
        try {
            File file = new File(path);
            if (!file.exists()) {
                return AgentResult.fail("file not found: " + path);
            }
            URL url = file.toURI().toURL();
            _resetParser();
            _toplevel = (CompositeActor) _parser.parse(null, url);
            _afterParse();
            return AgentResult.ok("loaded " + path,
                    new JSONObject().put("name", _toplevel.getName())
                            .put("class", _toplevel.getClassName()));
        } catch (Throwable t) {
            return AgentResult.fail("load failed: " + t.getMessage());
        }
    }

    /** Load a model from a MoML string. Replaces any previously held model.
     *  @param moml The full MoML document.
     *  @return An AgentResult describing success or failure.
     */
    public synchronized AgentResult loadMoml(String moml) {
        try {
            _resetParser();
            _toplevel = (CompositeActor) _parser.parse(moml);
            _afterParse();
            return AgentResult.ok("loaded from string",
                    new JSONObject().put("name", _toplevel.getName())
                            .put("class", _toplevel.getClassName()));
        } catch (Throwable t) {
            return AgentResult.fail("load failed: " + t.getMessage());
        }
    }

    /** Apply a MoML fragment as an incremental change to the current model.
     *  Tools (add_entity, connect, set_parameter, ...) use this method.
     *  @param moml A MoML fragment such as {@code <entity name="x" class="..."/>}.
     *  @return An AgentResult describing success or failure.
     */
    public synchronized AgentResult applyChange(String moml) {
        if (_toplevel == null) {
            return AgentResult.fail(
                    "no model loaded; call loadFile or loadMoml first");
        }
        // Detach any probe Recorders that the SignalCollector injected
        // into the model. They are auto-reattached at the next run() and
        // would otherwise create stray relations on actor output ports
        // that confuse structural rewrites such as group_into_composite.
        _detachProbesQuietly();
        // Use a latch so we can detect both sync (Manager idle, executes in the
        // same thread before requestChange returns) and async (Manager running)
        // completion, and surface any error back to the caller.
        final java.util.concurrent.CountDownLatch latch =
                new java.util.concurrent.CountDownLatch(1);
        final java.util.concurrent.atomic.AtomicReference<String> failure =
                new java.util.concurrent.atomic.AtomicReference<>();
        try {
            ptolemy.moml.MoMLChangeRequest req = new ptolemy.moml.MoMLChangeRequest(
                    this, _toplevel, moml);
            req.setUndoable(true);
            req.addChangeListener(new ptolemy.kernel.util.ChangeListener() {
                @Override
                public void changeExecuted(
                        ptolemy.kernel.util.ChangeRequest cr) {
                    latch.countDown();
                }
                @Override
                public void changeFailed(
                        ptolemy.kernel.util.ChangeRequest cr,
                        Exception e) {
                    failure.set(e != null ? e.getMessage() : "unknown error");
                    latch.countDown();
                }
            });
            _toplevel.requestChange(req);
            // If the Manager is idle it processes requests synchronously in
            // the same thread, so the latch is already at 0 here.  If it is
            // async the latch gives us a proper synchronisation barrier.
            latch.await(10, java.util.concurrent.TimeUnit.SECONDS);
            String err = failure.get();
            if (err != null) {
                return AgentResult.fail("change failed: " + err);
            }
            return AgentResult.ok("change applied");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return AgentResult.fail("change interrupted");
        } catch (Throwable t) {
            return AgentResult.fail("change failed: " + t.getMessage());
        }
    }

    /** Run the simulation synchronously to completion.
     *  @return An AgentResult describing success or failure plus, on
     *      success, the recorded simulation signals.
     */
    public synchronized AgentResult run() {
        if (_toplevel == null) {
            return AgentResult.fail("no model loaded");
        }
        try {
            _lastError = null;
            _state = "RUNNING";
            if (_collector != null) {
                _collector.attach();
            }
            _manager.execute();
            if (_lastError != null) {
                _state = "ERROR";
                return AgentResult.fail("run failed: " + _lastError.getMessage());
            }
            _state = "FINISHED";
            JSONObject data = new JSONObject();
            if (_collector != null) {
                data.put("signals", _collector.toJson());
            }
            return AgentResult.ok("run finished", data);
        } catch (Throwable t) {
            _state = "ERROR";
            return AgentResult.fail("run failed: " + t.getMessage());
        }
    }

    /** Undo the last MoML mutation. No-op when the undo stack is empty.
     *  @return Success when a step was popped or the stack was empty;
     *      failure if Ptolemy's undo machinery threw. */
    public synchronized AgentResult undo() {
        if (_toplevel == null) {
            return AgentResult.fail("no model loaded");
        }
        try {
            ptolemy.kernel.undo.UndoStackAttribute.getUndoInfo(_toplevel)
                    .undo();
            return AgentResult.ok("undid one step");
        } catch (Throwable t) {
            return AgentResult.fail("undo failed: " + t.getMessage());
        }
    }

    /** Redo the most recently undone step. */
    public synchronized AgentResult redo() {
        if (_toplevel == null) {
            return AgentResult.fail("no model loaded");
        }
        try {
            ptolemy.kernel.undo.UndoStackAttribute.getUndoInfo(_toplevel)
                    .redo();
            return AgentResult.ok("redid one step");
        } catch (Throwable t) {
            return AgentResult.fail("redo failed: " + t.getMessage());
        }
    }

    /** @return JSON snapshot of every signal currently held by the
     *      auto-injected recorders, or an empty {@code {"probes":[]}}
     *      if no model is loaded or no run has occurred yet. */
    public synchronized JSONObject signals() {
        if (_collector == null) {
            return new JSONObject().put("probes", new org.json.JSONArray());
        }
        return _collector.toJson();
    }

    /** @return The MoML serialization of the currently loaded model, or
     *      an explanatory placeholder if no model is loaded.
     *
     *  <p>Auto-injected probe Recorders from {@link SignalCollector} are
     *  detached before serialization so they never leak into the
     *  on-disk MoML.  They will be re-attached on the next call to
     *  {@link #run()}. */
    public synchronized String exportMoml() {
        if (_toplevel == null) {
            return "<!-- no model loaded -->";
        }
        _detachProbesQuietly();
        return _toplevel.exportMoML();
    }

    /** Execute a tool call against a temporary copy of this session.
     *  The real model is not mutated. This is the backend primitive for
     *  dry-run / transaction workflows: callers can inspect the tool
     *  result and post-change validation before deciding whether to run
     *  the same tool on the real session.
     *  @param tools Registry used to dispatch the tool.
     *  @param name Tool name.
     *  @param args Tool arguments, without the {@code _dryRun} flag.
     *  @return A structured result containing the dry-run outcome. */
    public synchronized AgentResult dryRunTool(
            org.ptolemy.agent.tools.ToolRegistry tools, String name,
            JSONObject args) {
        if (_toplevel == null) {
            return AgentResult.fail("no model loaded");
        }
        PtolemySession copy = null;
        try {
            copy = new PtolemySession(_id + "-dryrun-"
                    + Long.toHexString(System.nanoTime()));
            AgentResult load = copy.loadMoml(exportMoml());
            if (!load.ok()) {
                return AgentResult.fail("dry-run setup failed: "
                        + load.message());
            }
            AgentResult toolResult = tools.dispatch(name, copy,
                    args == null ? new JSONObject() : args);
            AgentResult validation = tools.dispatch("validate", copy,
                    new JSONObject());
            JSONObject data = new JSONObject();
            data.put("dryRun", true);
            data.put("committed", false);
            data.put("tool", name);
            data.put("toolResult", toolResult.toJson());
            data.put("validation", validation.toJson());
            data.put("modelContext", ModelContext.forSession(copy));
            return toolResult.ok()
                    ? AgentResult.ok("dry-run succeeded for " + name, data)
                    : AgentResult.fail("dry-run failed for " + name + ": "
                            + toolResult.message(), data);
        } catch (Throwable t) {
            return AgentResult.fail("dry-run failed: " + t.getMessage());
        } finally {
            if (copy != null) {
                copy.disposeModel();
            }
        }
    }

    /** Save the current model to the given file path as MoML XML.
     *  @param path Absolute or relative path for the output file.
     *  @return AgentResult indicating success and the resolved path.
     *
     *  <p>Auto-injected probe Recorders from {@link SignalCollector} are
     *  detached before serialization so the saved file matches what the
     *  user sees on the canvas; they will be re-attached on the next
     *  call to {@link #run()}. */
    public synchronized AgentResult saveToFile(String path) {
        if (_toplevel == null) {
            return AgentResult.fail("no model loaded");
        }
        try {
            File f = new File(path);
            File parent = f.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            _detachProbesQuietly();
            try (Writer w = new OutputStreamWriter(
                    new FileOutputStream(f), StandardCharsets.UTF_8)) {
                w.write(_toplevel.exportMoML());
            }
            return AgentResult.ok("saved to " + f.getAbsolutePath());
        } catch (Exception e) {
            return AgentResult.fail("save failed: " + e.getMessage());
        }
    }

    /** Build a JSON summary of the session for HTTP responses. */
    public synchronized JSONObject summary() {
        JSONObject out = new JSONObject();
        out.put("id", _id);
        out.put("createdAt", new Date(_createdAtMs).toString());
        out.put("state", _state);
        out.put("hasModel", _toplevel != null);
        if (_toplevel != null) {
            out.put("modelName", _toplevel.getName());
            out.put("modelClass", _toplevel.getClassName());
        }
        return out;
    }

    /** Forcibly clean up the model. The session itself remains and may
     *  be reused by calling {@link #loadFile} or {@link #loadMoml}
     *  again. */
    public synchronized void disposeModel() {
        if (_manager != null) {
            try {
                _manager.finish();
            } catch (Throwable ignored) {
                // best-effort
            }
            _manager = null;
        }
        _detachProbesQuietly();
        _toplevel = null;
        _collector = null;
        _state = "IDLE";
    }

    // ===== ChangeListener =====

    @Override
    public void changeExecuted(ChangeRequest change) {
        // No-op; we rely on Ptolemy's own state for the canonical view.
    }

    @Override
    public void changeFailed(ChangeRequest change, Exception exception) {
        _lastError = exception;
    }

    // ===== ExecutionListener =====

    @Override
    public void executionError(Manager manager, Throwable throwable) {
        _lastError = throwable;
        _state = "ERROR";
    }

    @Override
    public void executionFinished(Manager manager) {
        _state = "FINISHED";
    }

    @Override
    public void managerStateChanged(Manager manager) {
        Manager.State s = manager.getState();
        _state = (s == null) ? "UNKNOWN" : s.toString();
    }

    // ===== Internals =====

    private void _resetParser() {
        MoMLParser.setMoMLFilters(BackwardCompatibility.allFilters(),
                _workspace);
        MoMLParser.addMoMLFilter(new RemoveGraphicalClasses(), _workspace);
        _parser = new MoMLParser(_workspace);
        _parser.resetAll();
        if (_manager != null) {
            try {
                _manager.finish();
            } catch (Throwable ignored) {
                // best-effort cleanup
            }
            _manager = null;
        }
        _collector = null;
        _toplevel = null;
    }

    private void _afterParse() throws Exception {
        if (_toplevel.getContainer() == null
                && _toplevel.getModelErrorHandler() == null) {
            _toplevel.setModelErrorHandler(new BasicModelErrorHandler());
        }
        _manager = new Manager(_toplevel.workspace(), "AgentManager");
        _toplevel.setManager(_manager);
        _toplevel.addChangeListener(this);
        _manager.addExecutionListener(this);
        _collector = new SignalCollector(_toplevel);
        _state = "READY";
    }

    private static synchronized void _initializeOnce() {
        if (!_injectorInitialized) {
            ActorModuleInitializer.initializeInjector();
            _injectorInitialized = true;
        }
    }

    /** Best-effort removal of auto-injected probe Recorders.  Called
     *  before any path that exposes MoML to the outside world (file
     *  save, exportMoml, dryRun clone source) so that probes never
     *  leak into a {@code .xml} document opened in Vergil.
     *
     *  <p>The collector knows about probes it attached during the
     *  current run, but a session can also load an already-polluted
     *  MoML file.  After asking the collector to detach its tracked
     *  probes, scan the model tree for leftover hidden probe actors and
     *  remove them too.  Probes are re-attached on the next
     *  {@link #run()}. */
    private void _detachProbesQuietly() {
        try {
            if (_collector != null) {
                _collector.detach();
            }
        } catch (Throwable ignored) {
            // Probes are diagnostic; never fail a save because of them.
        }
        try {
            if (_toplevel != null) {
                _purgeHiddenProbes(_toplevel);
            }
        } catch (Throwable ignored) {
            // Best-effort cleanup for legacy polluted models.
        }
    }

    /** Recursively remove hidden recorder actors and their relations. */
    private static void _purgeHiddenProbes(CompositeEntity container)
            throws Exception {
        @SuppressWarnings("unchecked")
        List<ComponentEntity> entities = new ArrayList<ComponentEntity>(
                container.entityList());
        for (ComponentEntity entity : entities) {
            String name = entity.getName();
            if (name != null && name.startsWith("__recorder__")) {
                _removeEntityAndLinkedRelations(entity);
                continue;
            }
            if (entity instanceof CompositeEntity) {
                _purgeHiddenProbes((CompositeEntity) entity);
            }
        }
    }

    /** Remove an entity plus relations linked to any of its ports. */
    private static void _removeEntityAndLinkedRelations(ComponentEntity entity)
            throws Exception {
        @SuppressWarnings("unchecked")
        List<Object> ports = new ArrayList<Object>(entity.portList());
        for (Object portObj : ports) {
            if (!(portObj instanceof IOPort)) {
                continue;
            }
            IOPort port = (IOPort) portObj;
            @SuppressWarnings("unchecked")
            List<Object> relations = new ArrayList<Object>(
                    port.linkedRelationList());
            for (Object relObj : relations) {
                if (relObj instanceof ComponentRelation) {
                    ((ComponentRelation) relObj).setContainer(null);
                }
            }
        }
        entity.setContainer(null);
    }

    /** Minimal XML escaping for entity names used in wrapped MoML. */
    private static String _xmlEscape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;");
    }
}

/* Two-phase agent driver: build first, refactor second.

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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ptolemy.agent.llm.LLMClient;
import org.ptolemy.agent.llm.LLMResponse;
import org.ptolemy.agent.llm.PromptTemplates;
import org.ptolemy.agent.session.PtolemySession;
import org.ptolemy.agent.tools.ToolRegistry;
import org.ptolemy.agent.util.AgentResult;

import ptolemy.actor.CompositeActor;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;

///////////////////////////////////////////////////////////////////
//// AgentPipeline

/**
 Drives the auto-modeling agent as a <b>two-phase pipeline</b>:
 <ol>
 <li><b>Build.</b> A first {@link AgentLoop} runs under
 {@link PromptTemplates#BUILDER_PROMPT} and is told to produce a flat
 model whose simulation runs successfully. No composites are created in
 this phase, which removes the major source of mid-build LLM confusion
 in complex scenarios.</li>
 <li><b>Refactor.</b> A second {@link AgentLoop} runs under
 {@link PromptTemplates#REFACTOR_PROMPT} on the freshly-built model.
 Its only job is to wrap coherent groups of atomic actors into
 {@code TypedCompositeActor} subsystems IN PLACE using
 {@code group_into_composite}. The wiring is preserved, so behaviour
 cannot regress.</li>
 </ol>

 <p>Each phase has its own progress-aware loop, but they share the same
 {@link PtolemySession} and tool registry. The combined
 {@link AgentTrace} returned to the caller contains both phases'
 steps in chronological order with explicit phase markers, so the
 frontend can render them either as a single conversation or as
 distinct sections.

<p>Refactor is automatically skipped when the freshly-built top level
has fewer than the configured refactor threshold (see
{@code AGENT_REFACTOR_THRESHOLD}) non-director, non-source entities,
since there is nothing meaningful to group.

 @author Ptolemy II Agent contributors
 @version $Id$
 @since Ptolemy II 11.1
 */
public class AgentPipeline {

    /** Minimum number of "groupable" top-level entities required to
     *  bother running the refactor phase. Below this we skip phase 2
     *  entirely, saving an LLM round-trip on trivial models. */
    private static final int _AUTO_OPT_MAX_ROUNDS = 3;
    private static final int _AUTO_OPT_MAX_ACTIONS = 16;

    private final LLMClient _plannerLlm;
    private final LLMClient _builderLlm;
    private final LLMClient _refactorLlm;
    private final LLMClient _reviewerLlm;
    private final ToolRegistry _tools;
    private final ToolRegistry _buildTools;
    private final AgentLoop _builder;
    private final AgentLoop _refactorer;
    private final int _refactorThreshold;
    private final long _refactorTurnCapMs;
    private final int _maxSteps;
    private final int _parallelBuildWorkers;
    private final long _parallelBuildTimeoutMs;
    private final boolean _forceParallelBuild;
    private final boolean _parallelPickerByLlm;
    private final boolean _reviewerEnabled;
    private final boolean _replanOnExecutorFailure;
    private final double _replanFailureThreshold;
    private static final Object _SESSION_CLONE_LOCK = new Object();

    /** Cap on the number of actions that the Reviewer phase is
     *  allowed to apply per chat turn. Matches the prompt's "at most
     *  8 actions" instruction. */
    private static final int _REVIEWER_MAX_ACTIONS = 8;

    public AgentPipeline(LLMClient llm, ToolRegistry tools, int maxSteps) {
        this(llm, llm, llm, llm, tools, maxSteps);
    }

    /** Legacy 4-LLM constructor kept for source compatibility; uses
     *  the planner LLM as the reviewer LLM. */
    public AgentPipeline(LLMClient plannerLlm, LLMClient builderLlm,
            LLMClient refactorLlm, ToolRegistry tools, int maxSteps) {
        this(plannerLlm, builderLlm, refactorLlm, plannerLlm, tools,
                maxSteps);
    }

    /** Build a pipeline with phase-specific LLM clients.
     *  @param plannerLlm Model for phase-0 planning.
     *  @param builderLlm Model for phase-1 build/repair.
     *  @param refactorLlm Model for phase-2 refactor.
     *  @param reviewerLlm Model for phase-2.5 post-build review.
     */
    public AgentPipeline(LLMClient plannerLlm, LLMClient builderLlm,
            LLMClient refactorLlm, LLMClient reviewerLlm,
            ToolRegistry tools, int maxSteps) {
        _plannerLlm = plannerLlm;
        _builderLlm = builderLlm;
        _refactorLlm = refactorLlm;
        _reviewerLlm = reviewerLlm;
        _tools = tools;
        _maxSteps = maxSteps;
        _refactorThreshold = _refactorThresholdFromEnv();
        _refactorTurnCapMs = _refactorTurnCapMsFromEnv();
        _parallelBuildWorkers = _parallelBuildWorkersFromEnv();
        _parallelBuildTimeoutMs = _parallelBuildTimeoutMsFromEnv();
        _forceParallelBuild = _forceParallelBuildFromEnv();
        _parallelPickerByLlm = _parallelPickerByLlmFromEnv();
        _reviewerEnabled = _reviewerEnabledFromEnv();
        _replanOnExecutorFailure = _replanOnExecutorFailureFromEnv();
        _replanFailureThreshold = _replanFailureThresholdFromEnv();
        // Builder agent now keeps the FULL toolset (including composite
        // creation) so the LLM can build composite-first inline, the
        // way SYSTEM_PROMPT's default style intends. Earlier revisions
        // stripped composite tools here, which forced flat builds that
        // refactor then struggled to retrofit; that was a primary
        // cause of the dual-model regression vs. flash-only mode.
        _buildTools = tools;
        // Refactor agent: only the tools needed to wrap, verify and
        // observe. No add_entity / delete / set_parameter / connect.
        ToolRegistry refactorTools = tools.only(
                "group_into_composite", "add_composite",
                "validate", "run", "list_library", "describe_actor");

        _builder = new AgentLoop(_builderLlm, _buildTools)
                .setSystemPrompt(PromptTemplates.BUILDER_PROMPT)
                .setMaxSteps(maxSteps)
                .setIncludeCapabilityProbe(false);
        _refactorer = new AgentLoop(_refactorLlm, refactorTools)
                .setSystemPrompt(PromptTemplates.REFACTOR_PROMPT)
                .setMaxSteps(maxSteps)
                .setMaxTurnMillis(_refactorTurnCapMs)
                .setIncludeCapabilityProbe(false);
    }

    /** Set a wall-clock timeout shared by builder/refactor loops.
     *  @param maxTurnMillis Max runtime per phase turn in milliseconds.
     */
    public AgentPipeline setMaxTurnMillis(long maxTurnMillis) {
        _builder.setMaxTurnMillis(maxTurnMillis);
        _refactorer.setMaxTurnMillis(Math.min(
                Math.max(1L, maxTurnMillis), _refactorTurnCapMs));
        return this;
    }

    /** Convenience overload without listener. */
    public AgentTrace run(PtolemySession session, String userGoal) {
        return run(session, userGoal, null);
    }

    /** Drive the two-phase pipeline.
     *  @param session The session whose model the agent is allowed to
     *      mutate. Survives across phases.
     *  @param userGoal Natural-language goal from the user. Passed
     *      verbatim into phase 1; quoted as context in phase 2.
     *  @param listener Optional listener; receives the union of every
     *      step from both phases as if they were one conversation.
     *  @return A combined trace containing both phases' steps, with
     *      success = AND of the two phases (refactor failure does NOT
     *      flip the build success bit).
     */
    public AgentTrace run(PtolemySession session, String userGoal,
            AgentTraceListener listener) {
        // The combined trace is the canonical transcript returned to
        // non-streaming callers. We do NOT attach the external listener
        // to it directly — instead we use a fan-out listener so each
        // step fires the external listener EXACTLY ONCE while still
        // landing in the combined steps list.
        final AgentTrace combined = new AgentTrace();
        combined.putDiagnostic("driver", "pipeline");
        final AgentTraceListener fanOut = step -> {
            // Suppress each phase's own "final" step — the pipeline
            // synthesises ONE combined final at the very end so the
            // client never sees mid-stream conversation closers.
            if ("final".equals(step.kind)) {
                return;
            }
            if (listener != null) {
                listener.onStep(step);
            }
            combined.appendStepSilent(step);
        };

        // -------- Phase 0: PLAN (cheap, no-tools LLM call) --------
        _emitMarker(fanOut, combined,
                "[Phase 0/4: planning the model …]");
        String plan = _runPlanner(userGoal, fanOut);
        JSONObject parsedPlan = AgentPlan.parse(plan);
        JSONArray planIssues = AgentPlan.validationIssues(parsedPlan);
        if (parsedPlan != null) {
            combined.putDiagnostic("plan", parsedPlan);
        }
        combined.putDiagnostic("planValidationIssues", planIssues);
        if (plan != null && !plan.isEmpty()) {
            AgentTrace.Step planStep = new AgentTrace.Step(0, "thought",
                    "plan", null, parsedPlan == null ? plan
                            : parsedPlan.toString(2));
            combined.appendStepSilent(planStep);
            if (listener != null) {
                listener.onStep(planStep);
            }
        }
        if (planIssues.length() > 0) {
            AgentTrace.Step validationStep = new AgentTrace.Step(0,
                    "thought", "plan_validation", null,
                    "Planner JSON validation notes: "
                            + planIssues.toString());
            combined.appendStepSilent(validationStep);
            if (listener != null) {
                listener.onStep(validationStep);
            }
        }

        // -------- Phase 0.5: VALIDATE + EXECUTE (deterministic) ----
        JSONArray semanticIssues = PlanValidator.validate(parsedPlan);
        combined.putDiagnostic("planSemanticIssues", semanticIssues);
        if (semanticIssues.length() > 0) {
            _emitMarker(fanOut, combined,
                    "[Phase 0.5/4: plan semantic validation — "
                            + semanticIssues.length() + " issue(s)"
                            + (PlanValidator.hasErrors(semanticIssues)
                                    ? ", will not execute"
                                    : ", advisory only")
                            + "]");
        }

        PlanExecutor.Outcome executor = null;
        boolean executorAttempted = parsedPlan != null
                && !PlanValidator.hasErrors(semanticIssues);
        if (executorAttempted) {
            _emitMarker(fanOut, combined,
                    "[Phase 0.5/4: executing plan deterministically …]");
            executor = PlanExecutor.execute(parsedPlan, session,
                    _buildTools, fanOut);
            combined.putDiagnostic("executor", executor.toReport());
            _emitMarker(fanOut, combined,
                    "[Phase 0.5/4: " + executor.summary() + "]");

            // Re-plan once when the executor failed a substantial
            // fraction of its steps and re-planning is enabled.  This
            // turns the planner→builder pipe from one-way into a
            // limited feedback loop: the planner sees its own
            // failures and can revise class names / wiring before
            // the builder phase has to clean up the mess.
            if (_replanOnExecutorFailure
                    && _shouldReplan(executor)) {
                _emitMarker(fanOut, combined,
                        "[Phase 0.5/4: executor failed "
                                + executor.failedSteps.size() + "/"
                                + executor.steps.size()
                                + " steps — asking planner to revise]");
                String replanResult = _replanWithFailureReport(userGoal,
                        executor, plan);
                JSONObject replan = AgentPlan.parse(replanResult);
                if (replan != null) {
                    JSONArray replanIssues
                            = PlanValidator.validate(replan);
                    if (!PlanValidator.hasErrors(replanIssues)) {
                        // Wipe the polluted partial build and replay
                        // the revised plan from a clean session so
                        // the executor doesn't trip on leftover
                        // entities that the original plan added.
                        session.disposeModel();
                        AgentTrace.Step planStep
                                = new AgentTrace.Step(0, "thought",
                                        "plan_v2", null,
                                        replan.toString(2));
                        combined.appendStepSilent(planStep);
                        if (listener != null) {
                            listener.onStep(planStep);
                        }
                        parsedPlan = replan;
                        plan = replanResult;
                        executor = PlanExecutor.execute(parsedPlan,
                                session, _buildTools, fanOut);
                        combined.putDiagnostic("executorV2",
                                executor.toReport());
                        _emitMarker(fanOut, combined,
                                "[Phase 0.5/4 (v2): "
                                        + executor.summary() + "]");
                    } else {
                        _emitMarker(fanOut, combined,
                                "[Phase 0.5/4: re-planner returned"
                                        + " another invalid plan;"
                                        + " falling back to builder"
                                        + " repair]");
                    }
                }
            }
        }

        // -------- Phase 1: BUILD (conditional) --------
        // If the executor built and ran the model end-to-end, skip the
        // LLM build phase entirely.  This is the industrial-grade
        // happy path: zero builder tokens spent.  Otherwise pass the
        // executor's structured report to the LLM so it repairs only
        // the failed steps.
        AgentTrace build;
        if (executor != null && executor.fullyAutonomous()
                && !_forceParallelBuild) {
            _emitMarker(fanOut, combined,
                    "[Phase 1/4: skipped — executor produced a passing"
                            + " model autonomously]");
            build = new AgentTrace();
            build.finish(true, "Plan executed and simulated by the"
                    + " deterministic executor (no LLM build calls"
                    + " required).");
        } else {
            String executorContext;
            if (executor != null) {
                if (_forceParallelBuild && executor.fullyAutonomous()) {
                    executorContext = "\n\nDeterministic executor built a"
                            + " baseline model successfully. Now generate"
                            + " alternative improved architectures (same"
                            + " behavior, better modularity/robustness) for"
                            + " parallel comparison.\nBaseline report:\n```json\n"
                            + executor.toReport().toString(2) + "\n```";
                } else {
                    executorContext = "\n\nDeterministic executor report"
                            + " (the server already issued these tool"
                            + " calls; you do NOT need to re-add successful"
                            + " entities or re-wire successful edges — your"
                            + " job is ONLY to repair the failures listed"
                            + " below):\n```json\n"
                            + executor.toReport().toString(2) + "\n```";
                }
            } else if (parsedPlan == null) {
                executorContext = "\n\n(No machine-checkable plan was"
                        + " produced; proceed from the user goal.)";
            } else {
                executorContext = "\n\n(Plan had semantic errors before"
                        + " execution — see planSemanticIssues;"
                        + " resolve them as you build.)";
            }
            String builderInput = (plan == null || plan.isEmpty())
                    ? userGoal + executorContext
                    : "User goal:\n" + userGoal + "\n\n"
                            + AgentPlan.builderInstruction(parsedPlan,
                                    plan)
                            + executorContext;
            _emitMarker(fanOut, combined,
                    "[Phase 1/4: build / repair flat model — no"
                            + " composites yet]");
            if (_parallelBuildWorkers > 1) {
                _emitMarker(fanOut, combined,
                        "[Phase 1/4: parallel flash build — "
                                + _parallelBuildWorkers + " candidates]");
                build = _parallelFlashBuild(session, parsedPlan, userGoal,
                        builderInput, fanOut, combined);
            } else {
                build = _builder.run(session, builderInput, fanOut);
            }
        }

        if (!build.isSuccess()) {
            String reply = "Build phase failed: " + build.finalReply();
            combined.finish(false, reply);
            if (listener != null) {
                listener.onStep(_finalStep(combined, false, reply));
            }
            return combined;
        }

        // -------- Phase 2: REFACTOR (conditional) --------
        String summary = build.finalReply();
        int groupable = _countGroupableTopLevelEntities(session);
        if (groupable < _refactorThreshold) {
            summary = build.finalReply()
                    + " (refactor skipped: only " + groupable
                    + " groupable atoms at top level)";
            _emitMarker(fanOut, combined,
                    "[Phase 2/4: refactor skipped — only " + groupable
                            + " groupable atoms at top level]");
        } else {
            _emitMarker(fanOut, combined,
                    "[Phase 2/4: refactor into composites — " + groupable
                            + " atomic actors at top level]");
            JSONArray refactorSuggestions = RefactorAdvisor.suggestions(session);
            combined.putDiagnostic("refactorSuggestions", refactorSuggestions);

            String refactorGoal = ""
                    + "REFACTOR phase. The flat model is built and runs. Your"
                    + " job: wrap coherent groups of atomic actors into"
                    + " composites using group_into_composite, then validate"
                    + " and run ONCE at the end to confirm behaviour is"
                    + " preserved.\n\n"
                    + "Original user goal (for context only):\n"
                    + userGoal
                    + AgentPlan.refactorInstruction(parsedPlan)
                    + (refactorSuggestions.length() == 0 ? ""
                            : "\n\nAlgorithmic grouping suggestions from"
                                    + " RefactorAdvisor:\n```json\n"
                                    + refactorSuggestions.toString(2)
                                    + "\n```");

            AgentTrace refactor = _refactorer.run(session, refactorGoal,
                    fanOut);

            // Build success is sufficient for the pipeline overall. A
            // refactor stall is a bonus that didn't pan out, NOT a failure.
            if (refactor.isSuccess() && !refactor.finalReply().isEmpty()) {
                summary = build.finalReply() + "  |  refactor: "
                        + refactor.finalReply();
            } else if (!refactor.isSuccess()) {
                summary = build.finalReply()
                        + "  (refactor incomplete: "
                        + refactor.finalReply() + ")";
            }
        }

        // -------- Phase 2.5: REVIEW (v4-pro, no tools) --------
        // Heavy reasoning model reads the post-refactor model and
        // proposes a SMALL allow-listed set of polishing actions
        // (parameter tuning, regrouping, orphan deletion, missing
        // wire).  Executed deterministically with strict allowlist
        // and an action cap.  Disabled via AGENT_REVIEWER_ENABLED=false
        // when you only want phase 0..2 + autoOptimize.
        if (_reviewerEnabled) {
            _emitMarker(fanOut, combined,
                    "[Phase 2.5/4: reviewer pass — v4-pro evaluates"
                            + " the built model and proposes targeted"
                            + " safe improvements]");
            JSONObject reviewerReport = _runReviewer(session, userGoal,
                    fanOut);
            combined.putDiagnostic("reviewer", reviewerReport);
            int reviewerOk = reviewerReport.optInt("appliedOk", 0);
            int reviewerSkipped = reviewerReport.optInt("skipped", 0);
            String verdict = reviewerReport.optString("verdict", "");
            if (reviewerOk > 0 || reviewerSkipped > 0
                    || !verdict.isEmpty()) {
                summary = summary + "  |  reviewer: applied="
                        + reviewerOk + " skipped=" + reviewerSkipped
                        + (verdict.isEmpty() ? "" : " (" + verdict
                                + ")");
            }
        }

        // -------- Phase 3: AUTO OPTIMIZE (deterministic) --------
        _emitMarker(fanOut, combined,
                "[Phase 3/4: auto-optimize disconnected entities and"
                        + " recursively group overcrowded scopes]");
        JSONObject optimization = _autoOptimize(session, fanOut);
        combined.putDiagnostic("autoOptimize", optimization);
        if (!optimization.optBoolean("validateOk", true)) {
            summary = summary + "  (auto-optimize: validate still reports"
                    + " errors)";
        } else if (!optimization.optBoolean("runOk", true)) {
            summary = summary + "  (auto-optimize: run failed after"
                    + " optimization)";
        } else {
            int actions = optimization.optInt("appliedActions", 0);
            if (actions > 0) {
                summary = summary + "  |  auto-optimize: applied "
                        + actions + " fix/group actions";
            } else {
                summary = summary + "  |  auto-optimize: no extra actions";
            }
        }

        combined.finish(true, summary);
        if (listener != null) {
            listener.onStep(_finalStep(combined, true, summary));
        }
        return combined;
    }

    /** One-shot LLM call with no tools. Produces a markdown plan
     *  using {@link PromptTemplates#PLANNER_PROMPT}. Best-effort: on
     *  any error the build phase still runs without a plan.  The
     *  planner also receives the capability scan as a soft prior so
     *  it does not default to generic Expression-based plans when
     *  domain-specific actors already exist in the library.
     *
     *  <p>When {@code listener} is non-null the planner uses the
     *  streaming variant of the chat API and emits incremental
     *  {@code thought} steps with {@code name="plan_stream"} so the
     *  frontend can render the partial reasoning live instead of
     *  showing a 30-60 s blank wait. */
    private String _runPlanner(String userGoal,
            AgentTraceListener listener) {
        if (_plannerLlm == null || !_plannerLlm.isAvailable()) {
            return null;
        }
        try {
            JSONArray messages = new JSONArray();
            messages.put(new JSONObject()
                    .put("role", "system")
                    .put("content", PromptTemplates.PLANNER_PROMPT));
            messages.put(new JSONObject()
                    .put("role", "user")
                    .put("content", userGoal));
            String recipe = CapabilityProbe.promptBlock(
                    CapabilityProbe.probe(userGoal,
                            org.ptolemy.agent.library.LibraryIndex
                                    .shared()));
            if (recipe != null && !recipe.isEmpty()) {
                messages.put(new JSONObject()
                        .put("role", "user")
                        .put("content", recipe));
            }
            LLMResponse reply;
            if (listener != null) {
                final int[] lastLen = { 0 };
                reply = _plannerLlm.chatStreaming(messages,
                        new JSONArray(),
                        (content, reasoning) -> {
                            String shown = (reasoning != null
                                    && !reasoning.isEmpty())
                                            ? reasoning : content;
                            if (shown == null || shown.isEmpty()) {
                                return;
                            }
                            if (shown.length() <= lastLen[0]) {
                                return;
                            }
                            lastLen[0] = shown.length();
                            listener.onStep(new AgentTrace.Step(0,
                                    "thought", "plan_stream", null,
                                    shown));
                        });
            } else {
                reply = _plannerLlm.chat(messages, new JSONArray());
            }
            String content = reply == null ? null : reply.content();
            return content == null || content.isEmpty() ? null : content;
        } catch (Exception e) {
            // Planning is a best-effort optimisation. If the LLM call
            // fails or the model returns garbage, fall through to the
            // existing build phase and let it figure things out.
            return null;
        }
    }

    /** Append a synthetic thought step into the combined trace AND fan
     *  it out to the external listener exactly once. */
    private static void _emitMarker(AgentTraceListener fanOut,
            AgentTrace combined, String text) {
        AgentTrace.Step marker = new AgentTrace.Step(0, "thought", "",
                null, text);
        combined.appendStepSilent(marker);
        fanOut.onStep(marker);
    }

    /** Convenience: build the synthetic "final" step that
     *  {@link AgentTrace#finish} appends, so we can hand the SAME
     *  payload to the external listener without re-firing combined's
     *  internal listener (which we deliberately leave null). */
    private static AgentTrace.Step _finalStep(AgentTrace combined,
            boolean success, String reply) {
        return new AgentTrace.Step(0, "final", "", null, reply);
    }

    /** Count entities at the toplevel that are candidates for
     *  composite grouping. Excludes directors, recorders, sources, and
     *  composites that already exist. */
    private static int _countGroupableTopLevelEntities(
            PtolemySession session) {
        CompositeActor top = session.toplevel();
        if (top == null) {
            return 0;
        }
        CompositeEntity scope = top;
        int count = 0;
        for (Object obj : scope.entityList()) {
            if (!(obj instanceof ComponentEntity)) {
                continue;
            }
            ComponentEntity entity = (ComponentEntity) obj;
            String cls = entity.getClassName();
            if (cls == null) {
                continue;
            }
            if (cls.endsWith("Director")) {
                continue;
            }
            if (cls.endsWith(".Recorder")) {
                continue;
            }
            // Skip composites that the agent already created.
            if (cls.endsWith("TypedCompositeActor")
                    || cls.endsWith("CompositeActor")) {
                continue;
            }
            // Skip injected probes.
            if (entity.getName() != null
                    && entity.getName().startsWith("__recorder__")) {
                continue;
            }
            // Sources are typically left at top level even after refactor.
            // But we still count them so a "1 source + 4 atoms" canvas
            // qualifies for refactoring.
            count++;
        }
        return count;
    }

    /** Build candidate record for parallel flash attempts. */
    private static final class BuildCandidate {
        final int attempt;
        final String focus;
        final AgentTrace trace;
        final AgentResult validate;
        final AgentResult run;
        final String moml;
        final int score;
        final int errors;
        final int warnings;

        BuildCandidate(int attempt, String focus, AgentTrace trace,
                AgentResult validate,
                AgentResult run, String moml, int score, int errors,
                int warnings) {
            this.attempt = attempt;
            this.focus = focus == null ? "" : focus;
            this.trace = trace;
            this.validate = validate;
            this.run = run;
            this.moml = moml;
            this.score = score;
            this.errors = errors;
            this.warnings = warnings;
        }
    }

    /** Compare result with reason returned by planner model. */
    private static final class PlannerPick {
        final BuildCandidate chosen;
        final String reason;

        PlannerPick(BuildCandidate chosen, String reason) {
            this.chosen = chosen;
            this.reason = reason == null ? "" : reason;
        }
    }

    /** Run multiple flash builder attempts in parallel, then let planner
     *  model (v4-pro) pick the best candidate; fallback to deterministic
     *  scoring when planner comparison is unavailable. */
    private AgentTrace _parallelFlashBuild(PtolemySession session,
            JSONObject parsedPlan, String userGoal, String builderInput,
            AgentTraceListener fanOut, AgentTrace combined) {
        AgentTrace build = new AgentTrace();
        if (session == null || session.toplevel() == null) {
            build.finish(false, "parallel build failed: no model loaded");
            return build;
        }
        String baseMoml = session.exportMoml();
        List<String> logicBlocks = _logicBlocksFromPlan(parsedPlan);
        if (!logicBlocks.isEmpty()) {
            _emitMarker(fanOut, combined,
                    "[Phase 1/4: planner split into " + logicBlocks.size()
                            + " logic block(s) for parallel execution]");
        }
        ExecutorService pool = Executors.newFixedThreadPool(
                _parallelBuildWorkers);
        List<Future<BuildCandidate>> futures = new ArrayList<>();
        for (int i = 0; i < _parallelBuildWorkers; i++) {
            final int attempt = i + 1;
            final String focusHint = logicBlocks.isEmpty()
                    ? ("general candidate #" + attempt)
                    : logicBlocks.get((attempt - 1) % logicBlocks.size());
            futures.add(pool.submit(new Callable<BuildCandidate>() {
                @Override
                public BuildCandidate call() throws Exception {
                    return _runParallelBuildAttempt(baseMoml, builderInput,
                            attempt, focusHint);
                }
            }));
        }

        List<BuildCandidate> candidates = new ArrayList<>();
        long deadline = System.currentTimeMillis() + _parallelBuildTimeoutMs;
        List<Future<BuildCandidate>> pending = new ArrayList<>(futures);
        try {
            while (!pending.isEmpty() && System.currentTimeMillis() < deadline) {
                java.util.Iterator<Future<BuildCandidate>> it = pending
                        .iterator();
                while (it.hasNext()) {
                    Future<BuildCandidate> f = it.next();
                    if (!f.isDone()) {
                        continue;
                    }
                    try {
                        BuildCandidate c = f.get(0, TimeUnit.MILLISECONDS);
                        if (c != null) {
                            candidates.add(c);
                        }
                    } catch (Exception e) {
                        f.cancel(true);
                    }
                    it.remove();
                }
                if (!pending.isEmpty()) {
                    try {
                        Thread.sleep(150);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
            for (Future<BuildCandidate> f : pending) {
                f.cancel(true);
            }
        } finally {
            pool.shutdownNow();
        }

        if (candidates.isEmpty()) {
            _emitMarker(fanOut, combined,
                    "[parallel build] no candidate finished in time;"
                            + " fallback to single flash builder");
            return _builder.run(session, builderInput, fanOut);
        }

        Collections.sort(candidates, Comparator.comparingInt(
                (BuildCandidate c) -> c.score).reversed());
        List<BuildCandidate> viable = new ArrayList<>();
        for (BuildCandidate c : candidates) {
            if (c.score > -1_000_000) {
                viable.add(c);
            }
        }
        if (viable.isEmpty()) {
            AgentTrace fallbackBuild = _builder.run(session, builderInput,
                    fanOut);
            _emitMarker(fanOut, combined,
                    "[parallel build] all candidates invalid; fallback"
                            + " to single flash builder");
            return fallbackBuild;
        }
        BuildCandidate fallback = viable.get(0);
        PlannerPick pick = _pickCandidateByPlanner(userGoal, viable,
                fallback);
        BuildCandidate chosen = pick.chosen == null ? fallback : pick.chosen;

        for (BuildCandidate c : candidates) {
            _emitMarker(fanOut, combined,
                    "[parallel build] attempt #" + c.attempt
                            + " score=" + c.score
                            + " run=" + (c.run != null && c.run.ok())
                            + " errors=" + c.errors
                            + " warnings=" + c.warnings
                            + " focus=" + c.focus);
        }
        if (!pick.reason.isEmpty()) {
            _emitMarker(fanOut, combined, "[parallel build] planner pick: "
                    + pick.reason);
        }

        AgentResult load = session.loadMoml(chosen.moml);
        if (!load.ok()) {
            build.finish(false, "parallel build selected attempt #"
                    + chosen.attempt + " but failed to load: "
                    + load.message());
            return build;
        }

        boolean ok = (chosen.trace != null && chosen.trace.isSuccess())
                || (chosen.run != null && chosen.run.ok());
        String finalReply = "Parallel flash build selected attempt #"
                + chosen.attempt + " (score " + chosen.score + "). "
                + (chosen.trace == null ? "" : chosen.trace.finalReply());
        build.finish(ok, finalReply);
        return build;
    }

    private BuildCandidate _runParallelBuildAttempt(String baseMoml,
            String builderInput, int attempt, String focusHint)
            throws Exception {
        PtolemySession copy = null;
        try {
            AgentResult load;
            synchronized (_SESSION_CLONE_LOCK) {
                copy = new PtolemySession("parallel-build-"
                        + Long.toHexString(System.nanoTime()) + "-"
                        + attempt);
                load = copy.loadMoml(baseMoml);
            }
            if (!load.ok()) {
                AgentTrace failed = new AgentTrace();
                failed.finish(false, "candidate setup failed: "
                        + load.message());
                return new BuildCandidate(attempt, focusHint, failed,
                        AgentResult.fail("validate skipped"),
                        AgentResult.fail("run skipped"), baseMoml,
                        Integer.MIN_VALUE / 4, 999, 999);
            }
            String diversifiedInput = builderInput
                    + "\n\n[Parallel attempt #" + attempt + "]"
                    + " Produce a valid but preferably distinct solution"
                    + " if multiple architectures fit the goal."
                    + "\nPrimary focus block:\n" + focusHint;
            AgentLoop loop = new AgentLoop(_builderLlm, _buildTools)
                    .setSystemPrompt(PromptTemplates.BUILDER_PROMPT)
                    .setMaxSteps(_maxSteps)
                    .setMaxTurnMillis(_parallelBuildTimeoutMs)
                    .setIncludeCapabilityProbe(false);
            AgentTrace trace = loop.run(copy, diversifiedInput, null);
            AgentResult validate = _tools.dispatch("validate", copy,
                    new JSONObject());
            AgentResult run = _tools.dispatch("run", copy, new JSONObject());
            int errors = _countDiagnostics(validate, "ERROR");
            int warnings = _countDiagnostics(validate, "WARN");
            int score = 0;
            if (trace.isSuccess()) score += 200;
            if (run.ok()) score += 220;
            if (validate.ok()) score += 80;
            score -= errors * 80;
            score -= warnings * 15;
            String moml = copy.exportMoml();
            return new BuildCandidate(attempt, focusHint, trace, validate,
                    run, moml,
                    score, errors, warnings);
        } finally {
            if (copy != null) {
                copy.disposeModel();
            }
        }
    }

    private PlannerPick _pickCandidateByPlanner(String userGoal,
            List<BuildCandidate> candidates, BuildCandidate fallback) {
        // Default off: deterministic top-by-score selection is more
        // reliable than asking the planner LLM to choose from short
        // text summaries that don't include the actual MoML.  Opt in
        // by setting AGENT_PARALLEL_PLANNER_PICK=true.
        if (!_parallelPickerByLlm) {
            return new PlannerPick(fallback,
                    "score=" + (fallback == null ? "n/a"
                            : Integer.toString(fallback.score)));
        }
        if (_plannerLlm == null || !_plannerLlm.isAvailable()
                || candidates == null || candidates.isEmpty()) {
            return new PlannerPick(fallback, "");
        }
        try {
            JSONArray messages = new JSONArray();
            messages.put(new JSONObject().put("role", "system").put("content",
                    "You are selecting the best model candidate."
                    + " Choose ONE candidate for correctness, robustness,"
                    + " and scalability toward 500+ atomic components."
                    + " Return STRICT JSON only: "
                    + "{\"choice\": <attemptNumber>, \"reason\": \"...\"}."));
            StringBuilder report = new StringBuilder();
            report.append("User goal:\n").append(userGoal)
                    .append("\n\nCandidates:\n");
            for (BuildCandidate c : candidates) {
                report.append("- attempt ").append(c.attempt)
                        .append(": score=").append(c.score)
                        .append(", runOk=")
                        .append(c.run != null && c.run.ok())
                        .append(", validateOk=")
                        .append(c.validate != null && c.validate.ok())
                        .append(", errors=").append(c.errors)
                        .append(", warnings=").append(c.warnings)
                        .append(", summary=\"")
                        .append(_truncate(c.trace == null ? ""
                                : c.trace.finalReply(), 320))
                        .append("\"\n");
            }
            messages.put(new JSONObject().put("role", "user")
                    .put("content", report.toString()));
            LLMResponse reply = _plannerLlm.chat(messages, new JSONArray());
            String content = reply == null ? "" : reply.content();
            JSONObject json = _extractFirstJson(content);
            int choice = json.optInt("choice", -1);
            String reason = json.optString("reason", "").trim();
            if (choice > 0) {
                for (BuildCandidate c : candidates) {
                    if (c.attempt == choice) {
                        return new PlannerPick(c, reason.isEmpty()
                                ? ("attempt #" + choice) : reason);
                    }
                }
            }
        } catch (Exception e) {
            // fall back to deterministic rank.
        }
        return new PlannerPick(fallback, "");
    }

    /** True when the executor outcome has enough failures that it is
     *  worth paying for one re-plan round.  The threshold defaults to
     *  30 percent of steps but is configurable via
     *  AGENT_REPLAN_FAILURE_THRESHOLD.  We also bail out (no replan)
     *  when the executor managed to simulate the model anyway —
     *  there's nothing to repair. */
    private boolean _shouldReplan(PlanExecutor.Outcome executor) {
        if (executor == null || executor.simulates) {
            return false;
        }
        int total = executor.steps.size();
        int failed = executor.failedSteps.size();
        if (total <= 0) {
            return false;
        }
        // Always re-plan when validate/run failed at the end, even if
        // step-level failures are below the threshold — a model that
        // doesn't simulate is by definition broken.
        if (!executor.runOk || !executor.validateOk) {
            return true;
        }
        double ratio = (double) failed / (double) total;
        return ratio >= _replanFailureThreshold && failed >= 2;
    }

    /** Ask the planner LLM to produce a revised plan, given the
     *  failure report of the first attempt.  Best effort: returns
     *  null on any error so the caller falls back to the regular
     *  builder repair path.  At most one re-plan per request. */
    private String _replanWithFailureReport(String userGoal,
            PlanExecutor.Outcome executor, String originalPlan) {
        if (_plannerLlm == null || !_plannerLlm.isAvailable()
                || executor == null) {
            return null;
        }
        try {
            JSONArray messages = new JSONArray();
            messages.put(new JSONObject().put("role", "system")
                    .put("content", PromptTemplates.PLANNER_PROMPT));
            messages.put(new JSONObject().put("role", "user")
                    .put("content", userGoal));
            StringBuilder feedback = new StringBuilder();
            feedback.append("Your previous plan failed on the"
                    + " deterministic executor. Revise it. Address"
                    + " the failures listed below, drop or replace"
                    + " any actor whose className was reported as"
                    + " unresolvable / hallucinated, and re-emit the"
                    + " plan in the same JSON shape.\n\n");
            if (originalPlan != null && !originalPlan.isEmpty()) {
                feedback.append("Previous plan:\n```json\n")
                        .append(originalPlan)
                        .append("\n```\n\n");
            }
            feedback.append("Executor report:\n```json\n")
                    .append(executor.toReport().toString(2))
                    .append("\n```\n");
            messages.put(new JSONObject().put("role", "user")
                    .put("content", feedback.toString()));
            LLMResponse reply = _plannerLlm.chat(messages,
                    new JSONArray());
            String content = reply == null ? null : reply.content();
            return content == null || content.isEmpty() ? null : content;
        } catch (Exception e) {
            return null;
        }
    }

    /** Reviewer phase: one no-tools call to the reviewer LLM (usually
     *  v4-pro) that returns a JSON list of safe improvement actions.
     *  Each action is then dispatched deterministically with a strict
     *  allowlist and a hard cap on action count, so the reviewer
     *  cannot inadvertently break a working model. */
    private JSONObject _runReviewer(PtolemySession session,
            String userGoal, AgentTraceListener fanOut) {
        JSONObject report = new JSONObject();
        report.put("appliedOk", 0);
        report.put("appliedFail", 0);
        report.put("skipped", 0);
        report.put("verdict", "");
        if (_reviewerLlm == null || !_reviewerLlm.isAvailable()
                || session == null || session.toplevel() == null) {
            report.put("reason", "reviewer-unavailable");
            return report;
        }
        String advice;
        try {
            JSONArray messages = new JSONArray();
            messages.put(new JSONObject().put("role", "system")
                    .put("content", PromptTemplates.REVIEWER_PROMPT));
            messages.put(new JSONObject().put("role", "user")
                    .put("content", "Original user goal:\n" + userGoal));
            messages.put(new JSONObject().put("role", "user")
                    .put("content", PromptTemplates.modelContextNote(
                            org.ptolemy.agent.session.ModelContext
                                    .forSession(session))));
            messages.put(new JSONObject().put("role", "user")
                    .put("content", PromptTemplates.modelStateNote(
                            session.exportMoml())));
            LLMResponse reply = _reviewerLlm.chat(messages,
                    new JSONArray());
            advice = reply == null ? "" : reply.content();
        } catch (Exception e) {
            report.put("reason", "reviewer-call-failed: "
                    + e.getMessage());
            return report;
        }
        JSONObject parsed = _extractFirstJson(advice);
        if (parsed.length() == 0) {
            report.put("reason", "reviewer returned non-JSON");
            report.put("raw", advice == null ? ""
                    : _truncate(advice, 320));
            return report;
        }
        report.put("verdict", parsed.optString("verdict", ""));
        JSONArray actions = parsed.optJSONArray("actions");
        if (actions == null || actions.length() == 0) {
            report.put("reason", "no actions proposed");
            return report;
        }
        int applied = 0;
        int failed = 0;
        int skipped = 0;
        JSONArray applyTrace = new JSONArray();
        for (int i = 0; i < actions.length()
                && (applied + failed) < _REVIEWER_MAX_ACTIONS; i++) {
            JSONObject act = actions.optJSONObject(i);
            if (act == null) {
                skipped++;
                continue;
            }
            String tool = act.optString("tool", "").trim();
            JSONObject args = act.optJSONObject("args");
            String reason = act.optString("reason", "").trim();
            if (!_reviewerAllowed(tool)) {
                skipped++;
                applyTrace.put(new JSONObject()
                        .put("tool", tool)
                        .put("ok", false)
                        .put("skipped",
                                "tool not on reviewer allowlist"));
                continue;
            }
            if (args == null) {
                args = new JSONObject();
            }
            AgentResult res = _dispatchTool(session, tool, args, fanOut,
                    "[reviewer] " + tool
                            + (reason.isEmpty() ? "" : " — " + reason));
            applyTrace.put(new JSONObject()
                    .put("tool", tool)
                    .put("args", args)
                    .put("ok", res.ok())
                    .put("message", res.message()));
            if (res.ok()) {
                applied++;
            } else {
                failed++;
            }
        }
        // Verify the model still validates + runs after the reviewer
        // edits.  We don't want a polishing pass to silently break a
        // working model.
        AgentResult validate = _dispatchTool(session, "validate",
                new JSONObject(), fanOut, "[reviewer] validate-final");
        boolean validateOk = !_hasErrorDiagnostics(validate);
        boolean runOk = false;
        if (validateOk) {
            AgentResult run = _dispatchTool(session, "run",
                    new JSONObject(), fanOut, "[reviewer] run-final");
            runOk = run.ok();
        }
        report.put("appliedOk", applied);
        report.put("appliedFail", failed);
        report.put("skipped", skipped);
        report.put("validateOk", validateOk);
        report.put("runOk", runOk);
        report.put("actionTrace", applyTrace);
        return report;
    }

    /** Reviewer tools allowlist.  Matches the prompt's explicit list
     *  so a model that ignores the instruction cannot escape the
     *  guardrail at execution time. */
    private static boolean _reviewerAllowed(String tool) {
        if (tool == null) {
            return false;
        }
        return "set_parameter".equals(tool)
                || "group_into_composite".equals(tool)
                || "connect".equals(tool)
                || "connect_many".equals(tool)
                || "delete".equals(tool);
    }

    private static JSONObject _extractFirstJson(String text) {
        if (text == null) {
            return new JSONObject();
        }
        int left = text.indexOf('{');
        int right = text.lastIndexOf('}');
        if (left >= 0 && right > left) {
            try {
                return new JSONObject(text.substring(left, right + 1));
            } catch (Exception ignored) {
                return new JSONObject();
            }
        }
        return new JSONObject();
    }

    private static int _countDiagnostics(AgentResult validate,
            String severity) {
        if (validate == null || validate.data() == null) {
            return 0;
        }
        JSONArray diagnostics = validate.data().optJSONArray("diagnostics");
        if (diagnostics == null) {
            return 0;
        }
        int count = 0;
        for (int i = 0; i < diagnostics.length(); i++) {
            JSONObject d = diagnostics.optJSONObject(i);
            if (d != null && severity.equals(
                    d.optString("severity", ""))) {
                count++;
            }
        }
        return count;
    }

    private static String _truncate(String text, int limit) {
        if (text == null) return "";
        if (text.length() <= limit) return text;
        return text.substring(0, Math.max(0, limit - 3)) + "...";
    }

    /** Extract logic block hints from planner JSON for parallel workers. */
    private static List<String> _logicBlocksFromPlan(JSONObject plan) {
        List<String> blocks = new ArrayList<>();
        if (plan == null) {
            return blocks;
        }
        JSONArray composites = plan.optJSONArray("futureComposites");
        if (composites == null) {
            composites = plan.optJSONArray("logicBlocks");
        }
        if (composites != null) {
            for (int i = 0; i < composites.length(); i++) {
                JSONObject c = composites.optJSONObject(i);
                if (c == null) continue;
                String name = c.optString("name",
                        c.optString("id", "block-" + (i + 1))).trim();
                JSONArray members = c.optJSONArray("members");
                if (members != null && members.length() > 0) {
                    StringBuilder summary = new StringBuilder();
                    summary.append(name).append(": ");
                    int n = Math.min(8, members.length());
                    for (int j = 0; j < n; j++) {
                        if (j > 0) summary.append(", ");
                        summary.append(members.optString(j, ""));
                    }
                    if (members.length() > n) {
                        summary.append(", ...");
                    }
                    blocks.add(summary.toString());
                } else {
                    blocks.add(name);
                }
            }
        }
        if (!blocks.isEmpty()) {
            return blocks;
        }
        // Fallback: split by actor batches from the plan.
        JSONArray actors = plan.optJSONArray("actors");
        if (actors == null || actors.length() == 0) {
            return blocks;
        }
        final int batchSize = 8;
        int batch = 1;
        StringBuilder current = new StringBuilder("batch-" + batch + ": ");
        int inBatch = 0;
        for (int i = 0; i < actors.length(); i++) {
            JSONObject a = actors.optJSONObject(i);
            if (a == null) continue;
            String name = a.optString("name", "").trim();
            if (name.isEmpty()) continue;
            if (inBatch > 0) current.append(", ");
            current.append(name);
            inBatch++;
            if (inBatch >= batchSize) {
                blocks.add(current.toString());
                batch++;
                current = new StringBuilder("batch-" + batch + ": ");
                inBatch = 0;
            }
        }
        if (inBatch > 0) {
            blocks.add(current.toString());
        }
        return blocks;
    }

    /** Deterministic post-build optimization:
     *  validate -> apply recommended connect/delete actions for
     *  disconnected entities -> recursively apply grouping suggestions ->
     *  validate -> run. */
    private JSONObject _autoOptimize(PtolemySession session,
            AgentTraceListener fanOut) {
        JSONObject report = new JSONObject();
        report.put("rounds", 0);
        report.put("appliedActions", 0);
        report.put("validateOk", true);
        report.put("runOk", true);
        if (session == null || session.toplevel() == null) {
            report.put("validateOk", false);
            report.put("runOk", false);
            report.put("message", "no model loaded");
            return report;
        }

        int actions = 0;
        Set<String> attempted = new HashSet<String>();
        int rounds = 0;
        for (int round = 0; round < _AUTO_OPT_MAX_ROUNDS
                && actions < _AUTO_OPT_MAX_ACTIONS; round++) {
            rounds++;
            AgentResult validate = _dispatchTool(session, "validate",
                    new JSONObject(), fanOut, "[auto] validate");
            JSONObject data = validate.data();
            JSONArray disconnected = data.optJSONArray(
                    "disconnectedEntities");
            JSONArray suggestions = data.optJSONArray("groupingSuggestions");

            boolean changed = false;
            if (disconnected != null) {
                for (int i = 0; i < disconnected.length()
                        && actions < _AUTO_OPT_MAX_ACTIONS; i++) {
                    JSONObject row = disconnected.optJSONObject(i);
                    if (row == null) {
                        continue;
                    }
                    String tool = row.optString("recommendedTool", "");
                    JSONObject args = row.optJSONObject("recommendedArgs");
                    if (args == null || args.length() == 0) {
                        continue;
                    }
                    if (!"connect".equals(tool) && !"delete".equals(tool)) {
                        continue;
                    }
                    String sig = tool + "::" + args.toString();
                    if (!attempted.add(sig)) {
                        continue;
                    }
                    AgentResult res = _dispatchTool(session, tool, args, fanOut,
                            "[auto] " + tool);
                    if (res.ok()) {
                        actions++;
                        changed = true;
                    }
                }
            }

            if (suggestions != null) {
                for (int i = 0; i < suggestions.length()
                        && actions < _AUTO_OPT_MAX_ACTIONS; i++) {
                    JSONObject s = suggestions.optJSONObject(i);
                    if (s == null) {
                        continue;
                    }
                    JSONArray members = s.optJSONArray("members");
                    if (members == null || members.length() < 3) {
                        continue;
                    }
                    JSONObject args = new JSONObject();
                    args.put("name", s.optString("name",
                            "Subsystem_" + i));
                    args.put("members", members);
                    String parent = s.optString("parent", "");
                    if (!parent.isEmpty() && !"<top>".equals(parent)) {
                        args.put("parent", parent);
                    }
                    String sig = "group_into_composite::" + args.toString();
                    if (!attempted.add(sig)) {
                        continue;
                    }
                    AgentResult res = _dispatchTool(session, "group_into_composite",
                            args, fanOut, "[auto] group_into_composite");
                    if (res.ok()) {
                        actions++;
                        changed = true;
                    }
                }
            }

            if (!changed) {
                break;
            }
        }

        AgentResult finalValidate = _dispatchTool(session, "validate",
                new JSONObject(), fanOut, "[auto] validate-final");
        boolean validateOk = !_hasErrorDiagnostics(finalValidate);
        report.put("rounds", rounds);
        report.put("appliedActions", actions);
        report.put("validateOk", validateOk);
        if (validateOk) {
            AgentResult run = _dispatchTool(session, "run", new JSONObject(),
                    fanOut, "[auto] run-final");
            report.put("runOk", run.ok());
            report.put("runMessage", run.message());
        } else {
            report.put("runOk", false);
            report.put("runMessage",
                    "skipped run because validate still has ERROR diagnostics");
        }
        return report;
    }

    /** Emit a deterministic tool_call/tool_result pair in pipeline trace. */
    private AgentResult _dispatchTool(PtolemySession session, String name,
            JSONObject args,
            AgentTraceListener fanOut, String thought) {
        if (fanOut != null) {
            fanOut.onStep(new AgentTrace.Step(0, "thought", "", null, thought));
            fanOut.onStep(new AgentTrace.Step(0, "tool_call", name, args,
                    "[exec] " + name));
        }
        AgentResult result = _tools.dispatch(name, session, args);
        if (fanOut != null) {
            fanOut.onStep(new AgentTrace.Step(0, "tool_result", name,
                    result.toJson(), ""));
        }
        return result;
    }

    private static boolean _hasErrorDiagnostics(AgentResult validateResult) {
        if (validateResult == null || !validateResult.ok()) {
            return true;
        }
        JSONObject data = validateResult.data();
        if (data == null) {
            return false;
        }
        JSONArray diagnostics = data.optJSONArray("diagnostics");
        if (diagnostics == null) {
            return false;
        }
        for (int i = 0; i < diagnostics.length(); i++) {
            JSONObject d = diagnostics.optJSONObject(i);
            if (d != null && "ERROR".equals(d.optString("severity", ""))) {
                return true;
            }
        }
        return false;
    }

    private static int _refactorThresholdFromEnv() {
        String raw = System.getenv("AGENT_REFACTOR_THRESHOLD");
        if (raw == null || raw.trim().isEmpty()) {
            raw = System.getProperty("agent.refactorThreshold", "3");
        }
        try {
            int n = Integer.parseInt(raw.trim());
            if (n < 3) {
                return 3;
            }
            if (n > 32) {
                return 32;
            }
            return n;
        } catch (Exception e) {
            return 3;
        }
    }

    private static long _refactorTurnCapMsFromEnv() {
        String raw = System.getenv("AGENT_REFACTOR_MAX_TURN_MS");
        if (raw == null || raw.trim().isEmpty()) {
            raw = System.getProperty("agent.refactorMaxTurnMs", "120000");
        }
        try {
            long n = Long.parseLong(raw.trim());
            if (n < 30_000L) {
                return 30_000L;
            }
            if (n > 300_000L) {
                return 300_000L;
            }
            return n;
        } catch (Exception e) {
            return 120_000L;
        }
    }

    private static int _parallelBuildWorkersFromEnv() {
        // Default 1 (single builder) is the most stable. Earlier
        // revisions defaulted to 3 because parallel diversification
        // was assumed to help; in practice flash converged better
        // when given one focused goal and the planner-pick noise on
        // top of diverged candidates hurt quality.
        String raw = System.getenv("AGENT_FLASH_PARALLEL_WORKERS");
        if (raw == null || raw.trim().isEmpty()) {
            raw = System.getProperty("agent.flash.parallelWorkers", "1");
        }
        try {
            int n = Integer.parseInt(raw.trim());
            if (n < 1) return 1;
            if (n > 6) return 6;
            return n;
        } catch (Exception e) {
            return 1;
        }
    }

    private static boolean _parallelPickerByLlmFromEnv() {
        String raw = System.getenv("AGENT_PARALLEL_PLANNER_PICK");
        if (raw == null || raw.trim().isEmpty()) {
            raw = System.getProperty("agent.parallelPlannerPick", "false");
        }
        String v = raw.trim().toLowerCase();
        return "1".equals(v) || "true".equals(v) || "yes".equals(v)
                || "on".equals(v);
    }

    private static boolean _reviewerEnabledFromEnv() {
        String raw = System.getenv("AGENT_REVIEWER_ENABLED");
        if (raw == null || raw.trim().isEmpty()) {
            raw = System.getProperty("agent.reviewerEnabled", "true");
        }
        String v = raw.trim().toLowerCase();
        return "1".equals(v) || "true".equals(v) || "yes".equals(v)
                || "on".equals(v);
    }

    private static boolean _replanOnExecutorFailureFromEnv() {
        String raw = System.getenv("AGENT_REPLAN_ON_FAILURE");
        if (raw == null || raw.trim().isEmpty()) {
            raw = System.getProperty("agent.replanOnFailure", "true");
        }
        String v = raw.trim().toLowerCase();
        return "1".equals(v) || "true".equals(v) || "yes".equals(v)
                || "on".equals(v);
    }

    private static double _replanFailureThresholdFromEnv() {
        String raw = System.getenv("AGENT_REPLAN_FAILURE_THRESHOLD");
        if (raw == null || raw.trim().isEmpty()) {
            raw = System.getProperty("agent.replanFailureThreshold", "0.3");
        }
        try {
            double n = Double.parseDouble(raw.trim());
            if (n < 0.1) return 0.1;
            if (n > 0.9) return 0.9;
            return n;
        } catch (Exception e) {
            return 0.3;
        }
    }

    private static long _parallelBuildTimeoutMsFromEnv() {
        String raw = System.getenv("AGENT_FLASH_PARALLEL_TIMEOUT_MS");
        if (raw == null || raw.trim().isEmpty()) {
            raw = System.getProperty("agent.flash.parallelTimeoutMs",
                    "180000");
        }
        try {
            long n = Long.parseLong(raw.trim());
            if (n < 60_000L) return 60_000L;
            if (n > 600_000L) return 600_000L;
            return n;
        } catch (Exception e) {
            return 180_000L;
        }
    }

    private static boolean _forceParallelBuildFromEnv() {
        String raw = System.getenv("AGENT_FORCE_PARALLEL_BUILD");
        if (raw == null || raw.trim().isEmpty()) {
            raw = System.getProperty("agent.forceParallelBuild", "false");
        }
        String v = raw.trim().toLowerCase();
        return "1".equals(v) || "true".equals(v) || "yes".equals(v)
                || "on".equals(v);
    }
}

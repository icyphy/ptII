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

import org.json.JSONArray;
import org.json.JSONObject;
import org.ptolemy.agent.llm.LLMClient;
import org.ptolemy.agent.llm.LLMResponse;
import org.ptolemy.agent.llm.PromptTemplates;
import org.ptolemy.agent.session.PtolemySession;
import org.ptolemy.agent.tools.ToolRegistry;

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
 has fewer than {@value #_REFACTOR_THRESHOLD} non-director, non-source
 entities, since there is nothing meaningful to group.

 @author Ptolemy II Agent contributors
 @version $Id$
 @since Ptolemy II 11.1
 */
public class AgentPipeline {

    /** Minimum number of "groupable" top-level entities required to
     *  bother running the refactor phase. Below this we skip phase 2
     *  entirely, saving an LLM round-trip on trivial models. */
    private static final int _REFACTOR_THRESHOLD = 3;

    private final LLMClient _llm;
    private final AgentLoop _builder;
    private final AgentLoop _refactorer;

    public AgentPipeline(LLMClient llm, ToolRegistry tools, int maxSteps) {
        _llm = llm;
        // Builder agent: full atomic toolset MINUS the composite-creation
        // tools, so the LLM cannot mix in hierarchy work during build.
        ToolRegistry buildTools = tools.except(
                "add_composite", "group_into_composite");
        // Refactor agent: only the tools needed to wrap, verify and
        // observe. No add_entity / delete / set_parameter / connect.
        ToolRegistry refactorTools = tools.only(
                "group_into_composite", "add_composite",
                "validate", "run", "list_library", "describe_actor");

        _builder = new AgentLoop(llm, buildTools)
                .setSystemPrompt(PromptTemplates.BUILDER_PROMPT)
                .setMaxSteps(maxSteps);
        _refactorer = new AgentLoop(llm, refactorTools)
                .setSystemPrompt(PromptTemplates.REFACTOR_PROMPT)
                .setMaxSteps(maxSteps);
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
                "[Phase 0/3: planning the model …]");
        String plan = _runPlanner(userGoal);
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

        // Build phase receives the plan as additional context.
        String builderInput = (plan == null || plan.isEmpty())
                ? userGoal
                : "User goal:\n" + userGoal + "\n\n"
                        + AgentPlan.builderInstruction(parsedPlan, plan);

        _emitMarker(fanOut, combined,
                "[Phase 1/3: build flat model — no composites yet]");

        // -------- Phase 1: BUILD --------
        AgentTrace build = _builder.run(session, builderInput, fanOut);

        if (!build.isSuccess()) {
            String reply = "Build phase failed: " + build.finalReply();
            combined.finish(false, reply);
            if (listener != null) {
                listener.onStep(_finalStep(combined, false, reply));
            }
            return combined;
        }

        // -------- Phase 2: REFACTOR (conditional) --------
        int groupable = _countGroupableTopLevelEntities(session);
        if (groupable < _REFACTOR_THRESHOLD) {
            String reply = build.finalReply()
                    + " (refactor skipped: only " + groupable
                    + " groupable atoms at top level)";
            combined.finish(true, reply);
            if (listener != null) {
                listener.onStep(_finalStep(combined, true, reply));
            }
            return combined;
        }

        _emitMarker(fanOut, combined,
                "[Phase 2/3: refactor into composites — " + groupable
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
        String summary = build.finalReply();
        if (refactor.isSuccess() && !refactor.finalReply().isEmpty()) {
            summary = build.finalReply() + "  |  refactor: "
                    + refactor.finalReply();
        } else if (!refactor.isSuccess()) {
            summary = build.finalReply()
                    + "  (refactor incomplete: "
                    + refactor.finalReply() + ")";
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
     *  domain-specific actors already exist in the library. */
    private String _runPlanner(String userGoal) {
        if (_llm == null || !_llm.isAvailable()) {
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
            LLMResponse reply = _llm.chat(messages, new JSONArray());
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
}

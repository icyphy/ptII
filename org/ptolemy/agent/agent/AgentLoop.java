/* ReAct-style agent loop that drives multi-step model construction.

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
import org.ptolemy.agent.session.ModelContext;
import org.ptolemy.agent.session.PtolemySession;
import org.ptolemy.agent.tools.ToolRegistry;
import org.ptolemy.agent.util.AgentResult;

///////////////////////////////////////////////////////////////////
//// AgentLoop

/**
 ReAct-style driver loop. Wires an {@link LLMClient} together with a
 {@link ToolRegistry} and a {@link PtolemySession}, alternates between
 "ask the LLM what to do" and "execute the requested tool", and
 records every step in an {@link AgentTrace} that the frontend
 surfaces in the chat panel.

 <p>Lifecycle:
 <ol>
 <li>Build a chat history with the canonical {@link
     PromptTemplates#SYSTEM_PROMPT} and the user goal.</li>
 <li>For up to {@code maxSteps} iterations:
   <ol>
   <li>Call {@code llm.chat(history, tools)}.</li>
   <li>If the model returned a final reply, record it and return.</li>
   <li>Otherwise, dispatch every requested tool, append the result as
       a {@code role=tool} message, loop.</li>
   </ol></li>
 <li>If the step budget is exhausted, record the last partial reply
     and return.</li>
 </ol>

 <p>The loop is intentionally synchronous: it blocks the calling HTTP
 thread until the LLM is done. Multi-second LLM latency is fine for a
 demo; if it becomes a problem the loop will be moved to a worker
 thread with WebSocket progress events in M3.

 @author Ptolemy II Agent contributors
 @version $Id$
 @since Ptolemy II 11.1
 */
public class AgentLoop {

    private final LLMClient _llm;
    private final ToolRegistry _tools;

    /** Hard safety ceiling — never exceed this even with progress.
     *  Tunable at runtime via {@link #setMaxSteps(int)}; the typical
     *  override comes from {@code AGENT_MAX_STEPS} in
     *  {@link org.ptolemy.agent.agent.AgentBackend}. Set to
     *  {@link Integer#MAX_VALUE} for "run until done". */
    private int _hardLimit = 200;

    /** How many consecutive all-fail rounds before we declare stall.
     *  Higher numbers tolerate transient connect/parse failures
     *  better; lower numbers fail-fast on truly stuck plans. */
    private int _stallThreshold = 8;

    /** The system prompt the agent runs under. Defaults to the
     *  general-purpose modeling prompt; pipelines override it to focus
     *  the agent on a single phase (build vs. refactor vs. polish). */
    private String _systemPrompt = PromptTemplates.SYSTEM_PROMPT;

    public AgentLoop(LLMClient llm, ToolRegistry tools) {
        _llm = llm;
        _tools = tools;
    }

    /** Override the hard safety limit (default 80). */
    public AgentLoop setMaxSteps(int maxSteps) {
        _hardLimit = Math.max(1, maxSteps);
        return this;
    }

    /** Override the system prompt this loop runs under. */
    public AgentLoop setSystemPrompt(String prompt) {
        if (prompt != null && !prompt.isEmpty()) {
            _systemPrompt = prompt;
        }
        return this;
    }

    public AgentTrace run(PtolemySession session, String userGoal) {
        return run(session, userGoal, null);
    }

    /** Execute one chat turn against a session.
     *
     *  <p>The loop is <b>progress-aware</b>: it keeps running as long as
     *  at least one tool call per round succeeds. It stops when:
     *  <ol>
     *  <li>The LLM produces a final reply (no tool calls) — normal end.</li>
     *  <li>Consecutive rounds produce ONLY failures for
     *      {@code stallThreshold} rounds — the agent is stuck.</li>
     *  <li>Total steps exceed the hard safety limit — runaway guard.</li>
     *  </ol>
     *
     *  @param session The session whose model the agent is allowed to
     *      mutate.
     *  @param userGoal The natural-language goal from the user.
     *  @param listener Optional listener for incremental trace updates.
     *  @return A complete trace of the turn.
     */
    public AgentTrace run(PtolemySession session, String userGoal,
            AgentTraceListener listener) {
        AgentTrace trace = new AgentTrace();
        trace.setListener(listener);
        trace.putDiagnostic("driver", "agent-loop");
        trace.putDiagnostic("modelContext", ModelContext.forSession(session));

        try {
            if (!_llm.isAvailable()) {
                try {
                    JSONArray empty = new JSONArray();
                    LLMResponse offline = _llm.chat(empty, empty);
                    trace.finish(false, offline.content());
                    return trace;
                } catch (Exception e) {
                    trace.finish(false, "LLM offline: " + e.getMessage());
                    return trace;
                }
            }

            JSONArray messages = new JSONArray();
            messages.put(message("system", _systemPrompt));
            messages.put(message("user", userGoal));
            messages.put(message("user",
                    PromptTemplates.modelContextNote(
                            ModelContext.forSession(session))));

            JSONArray tools = _tools.toolsForOpenAI();

            int consecutiveStallRounds = 0;

            // Sliding window of the last N tool calls. Used by the
            // anti-thrash detector to spot "delete X then add X" and
            // similar redundant churn that wastes LLM budget.
            java.util.Deque<String> recent =
                    new java.util.ArrayDeque<String>();
            final int RECENT_WINDOW = 8;

            // Sliding window of the last N thought-prefixes (first 120
            // chars of each LLM response). When the same prefix shows
            // up MAX_REPEAT_THOUGHT times in a row the agent is stuck
            // saying the same thing over and over (a known LLM failure
            // mode) and we hard-stop the loop with a clear message so
            // the budget isn't burned silently.
            java.util.Deque<String> recentThoughts =
                    new java.util.ArrayDeque<String>();
            final int THOUGHT_WINDOW       = 4;
            final int MAX_REPEAT_THOUGHT   = 3;
            // Same idea for tool calls: identical (toolName + JSON args)
            // executed back-to-back is also a stall signal that needs
            // hard termination, not just a coaching note.
            java.util.Deque<String> recentCallSig =
                    new java.util.ArrayDeque<String>();
            final int CALL_SIG_WINDOW      = 6;
            final int MAX_REPEAT_CALL_SIG  = 3;

            for (int step = 0; step < _hardLimit; step++) {
                LLMResponse reply;
                try {
                    reply = _llm.chat(messages, tools);
                } catch (Exception e) {
                    trace.addError("LLM call failed: " + e.getMessage());
                    trace.finish(false, "LLM call failed: " + e.getMessage());
                    return trace;
                }

                // Normal termination: LLM gave a final reply without tool calls.
                if (!reply.hasToolCalls()) {
                    trace.finish(true, reply.content());
                    return trace;
                }

                // Intermediate thought (content alongside tool calls).
                if (!reply.content().isEmpty()) {
                    trace.addThought(reply.content());
                    // Detect "broken record" mode — same thought 3+
                    // times in a row. Hard-terminate; coaching alone
                    // does not break this LLM failure mode.
                    String snippet = reply.content().trim();
                    if (snippet.length() > 120) {
                        snippet = snippet.substring(0, 120);
                    }
                    int sameThoughts = 0;
                    for (String prior : recentThoughts) {
                        if (prior.equals(snippet)) {
                            sameThoughts++;
                        }
                    }
                    if (sameThoughts >= MAX_REPEAT_THOUGHT - 1) {
                        trace.finish(false,
                                "Agent stuck in a loop: produced the"
                                + " same intermediate thought "
                                + (sameThoughts + 1)
                                + " times in a row, last attempt: \""
                                + snippet
                                + "\". Try rephrasing the goal, or use"
                                + " a different tool — list_entities"
                                + " enumerates the current model.");
                        return trace;
                    }
                    recentThoughts.addLast(snippet);
                    while (recentThoughts.size() > THOUGHT_WINDOW) {
                        recentThoughts.removeFirst();
                    }
                }

                messages.put(reply.toAssistantMessage());

                boolean anySuccess = false;
                for (LLMResponse.ToolCall call : reply.toolCalls()) {
                    trace.addToolCall(call.toolName, call.arguments);

                    // Hard repeat-call detector: same toolName + same
                    // exact JSON args 3 times in a 6-call window means
                    // the LLM is grinding on a no-op. Stop the loop.
                    String fullSig = call.toolName + "::"
                            + (call.arguments == null
                                    ? "{}"
                                    : call.arguments.toString());
                    int sigRepeats = 0;
                    for (String prior : recentCallSig) {
                        if (prior.equals(fullSig)) sigRepeats++;
                    }
                    if (sigRepeats >= MAX_REPEAT_CALL_SIG - 1) {
                        JSONObject stopObs = new JSONObject();
                        stopObs.put("ok", false);
                        stopObs.put("message",
                                "loop-detector: identical "
                                + call.toolName
                                + " call repeated " + (sigRepeats + 1)
                                + " times — terminating to save budget");
                        trace.addToolResult(call.toolName, stopObs);
                        trace.finish(false,
                                "Agent stuck calling " + call.toolName
                                + " with the same arguments "
                                + (sigRepeats + 1) + " times in a row."
                                + " Try a different tool or"
                                + " rephrase your goal.");
                        return trace;
                    }
                    recentCallSig.addLast(fullSig);
                    while (recentCallSig.size() > CALL_SIG_WINDOW) {
                        recentCallSig.removeFirst();
                    }

                    // Anti-thrash: inspect the recent-call window for
                    // patterns that indicate the agent is undoing its
                    // own work and surface a coaching note alongside
                    // the result.
                    String entityKey = _entityKey(call);
                    String coaching = _detectThrash(recent,
                            call.toolName, entityKey);

                    AgentResult result = _tools.dispatch(call.toolName,
                            session, call.arguments);
                    JSONObject obs = result.toJson();
                    if (coaching != null) {
                        obs.put("antiThrash", coaching);
                    }
                    trace.addToolResult(call.toolName, obs);
                    messages.put(toolMessage(call.id, call.toolName,
                            obs.toString()));
                    if (result.ok()) {
                        anySuccess = true;
                    }

                    recent.addLast(call.toolName + "::" + entityKey);
                    while (recent.size() > RECENT_WINDOW) {
                        recent.removeFirst();
                    }
                }

                // Progress tracking: reset on any success, increment on all-fail.
                if (anySuccess) {
                    consecutiveStallRounds = 0;
                } else {
                    consecutiveStallRounds++;
                    if (consecutiveStallRounds >= _stallThreshold) {
                        trace.finish(false,
                                "Agent stalled: " + _stallThreshold
                                        + " consecutive rounds with no successful"
                                        + " tool calls. The model may have an"
                                        + " unresolvable issue — try rephrasing"
                                        + " or simplifying the request.");
                        return trace;
                    }
                }
            }

            trace.finish(false,
                    "Safety limit reached (" + _hardLimit + " rounds)."
                            + " The model was still making progress but hit the"
                            + " maximum allowed iterations. Raise the cap by"
                            + " setting AGENT_MAX_STEPS=<n> (or 0 for"
                            + " unbounded) before starting the backend.");
            return trace;
        } finally {
            trace.setListener(null);
        }
    }

    private static JSONObject message(String role, String content) {
        JSONObject json = new JSONObject();
        json.put("role", role);
        json.put("content", content);
        return json;
    }

    /** Extract the primary "subject" of a tool call (usually the
     *  entity/composite name being acted on) so the anti-thrash
     *  detector can match across different tool names that touch the
     *  same target. */
    private static String _entityKey(LLMResponse.ToolCall call) {
        if (call == null || call.arguments == null) {
            return "";
        }
        JSONObject a = call.arguments;
        // Try the most-specific identifying argument for each tool.
        String key = a.optString("entity",
                a.optString("name",
                a.optString("composite",
                a.optString("from", ""))));
        // For dotted port refs like "Ramp.output" keep just the entity
        // half so delete vs add on the same actor matches.
        int dot = key.indexOf('.');
        if (dot > 0) {
            key = key.substring(0, dot);
        }
        return key;
    }

    /** Scan the recent-tool-call window for known wasteful patterns
     *  and return a short coaching note if any are detected. The note
     *  is attached to the tool's JSON result so the LLM sees its own
     *  thrash on the next round and can course-correct. */
    private static String _detectThrash(java.util.Deque<String> recent,
            String toolName, String entityKey) {
        if (entityKey == null || entityKey.isEmpty()) {
            return null;
        }
        // Pattern A: delete X then add_entity name=X
        if ("add_entity".equals(toolName)
                || "add_composite".equals(toolName)) {
            for (String prior : recent) {
                if (prior.startsWith("delete::" + entityKey)) {
                    return "ANTI-THRASH: you deleted '" + entityKey
                            + "' a moment ago and are now re-adding"
                            + " it. STOP. If the previous class was"
                            + " wrong, you should already have called"
                            + " describe_actor first; if a parameter"
                            + " needed changing, set_parameter is"
                            + " the right tool. Either way, don't"
                            + " delete-then-readd in a loop.";
                }
            }
        }
        // Pattern B: add_entity X then delete X
        if ("delete".equals(toolName)) {
            for (String prior : recent) {
                if (prior.startsWith("add_entity::" + entityKey)
                        || prior.startsWith("add_composite::"
                                + entityKey)) {
                    return "ANTI-THRASH: you just added '" + entityKey
                            + "' and are about to delete it. Why?"
                            + " If you picked the wrong class, the"
                            + " right move is describe_actor + a new"
                            + " add_entity with a different NAME so"
                            + " both options coexist, then delete"
                            + " the loser ONCE at the end. Don't"
                            + " churn.";
                }
            }
        }
        // Pattern C: same tool repeated on same entity 3+ times in the
        // window — strong signal of either a hard error or thrash.
        int repeats = 0;
        String key = toolName + "::" + entityKey;
        for (String prior : recent) {
            if (prior.equals(key)) {
                repeats++;
            }
        }
        if (repeats >= 2) {
            return "ANTI-THRASH: you have called " + toolName
                    + " on '" + entityKey + "' " + (repeats + 1)
                    + " times in a row. STOP. Either pick a different"
                    + " entity name, use a different tool, or accept"
                    + " the current state and move on.";
        }
        return null;
    }

    private static JSONObject toolMessage(String callId, String name,
            String content) {
        JSONObject json = new JSONObject();
        json.put("role", "tool");
        json.put("tool_call_id", callId);
        json.put("name", name);
        json.put("content", content);
        return json;
    }
}

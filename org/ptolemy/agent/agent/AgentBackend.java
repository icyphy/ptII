/* Process-singleton wiring an AgentLoop together with the default
 ToolRegistry and an LLMClient detected from the environment.

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

import org.json.JSONObject;
import org.ptolemy.agent.llm.LLMClient;
import org.ptolemy.agent.llm.NullLLMClient;
import org.ptolemy.agent.llm.OpenAIClient;
import org.ptolemy.agent.tools.ToolRegistry;

///////////////////////////////////////////////////////////////////
//// AgentBackend

/**
 Singleton that lazily constructs the {@link ToolRegistry},
 {@link LLMClient} and {@link AgentLoop} used by the HTTP layer. Auto
 detects an OpenAI / DeepSeek configuration from the environment; if
 none is present, it falls back to {@link NullLLMClient} so the rest of
 the system continues to work for manual canvas-driven testing.

 <p>This build uses a SINGLE flash-tier model for every role
 (chat, single, planner, builder, refactor, reviewer). The previous
 dual-model (v4-pro + v4-flash) and iterative "MEGA" pipeline are
 intentionally removed because they degraded build quality.

 @author Ptolemy II Agent contributors
 @version $Id$
 @since Ptolemy II 11.1
 */
public final class AgentBackend {

    private static final AgentBackend INSTANCE = new AgentBackend();

    private final ToolRegistry _tools;
    private final LLMClient _llm;
    private final AgentLoop _loop;
    private final AgentPipeline _pipeline;
    private final JSONObject _llmRouting;
    private final boolean _pipelineDisabled;

    private AgentBackend() {
        _tools = ToolRegistry.defaultRegistry();
        _pipelineDisabled = _disablePipelineFromEnv();
        ClientBundle bundle = buildClients();
        LLMClient client = bundle.client;
        if (client == null || !client.isAvailable()) {
            client = new NullLLMClient();
        }
        _llm = client;
        _llmRouting = bundle.routing == null
                ? new JSONObject() : bundle.routing;
        _llmRouting.put("pipelineDisabled", _pipelineDisabled);
        int maxSteps = _maxStepsFromEnv();
        long maxTurnMs = _maxTurnMsFromEnv();
        _loop = new AgentLoop(_llm, _tools)
                .setMaxSteps(maxSteps)
                .setMaxTurnMillis(maxTurnMs);
        _pipeline = new AgentPipeline(_llm, _llm, _llm, _llm, _tools,
                maxSteps).setMaxTurnMillis(maxTurnMs);
    }

    /** @return True iff the user has set AGENT_DISABLE_PIPELINE=true.
     *  When true, RestRoutes will map every PIPELINE classification
     *  to SINGLE so the conversation behaves like the simplest
     *  single-loop baseline. */
    public boolean isPipelineDisabled() {
        return _pipelineDisabled;
    }

    private static boolean _disablePipelineFromEnv() {
        String raw = env("AGENT_DISABLE_PIPELINE",
                "agent.disablePipeline", "false");
        String v = raw == null ? "" : raw.trim().toLowerCase();
        return "1".equals(v) || "true".equals(v) || "yes".equals(v)
                || "on".equals(v);
    }

    /** Read AGENT_MAX_STEPS env / -Dagent.maxSteps. Default 200.
     *  A value of {@code 0} (or negative) means "no hard cap" — the
     *  loop only exits when the LLM stops emitting tool calls, the
     *  LLM call itself fails, or the stall guard fires. */
    private static int _maxStepsFromEnv() {
        String raw = env("AGENT_MAX_STEPS", "agent.maxSteps", "200");
        try {
            int n = Integer.parseInt(raw.trim());
            if (n <= 0) {
                return Integer.MAX_VALUE;
            }
            return n;
        } catch (NumberFormatException e) {
            return 200;
        }
    }

    /** Read AGENT_MAX_TURN_MS env / -Dagent.maxTurnMs. Default 300000. */
    private static long _maxTurnMsFromEnv() {
        String raw = env("AGENT_MAX_TURN_MS", "agent.maxTurnMs", "300000");
        try {
            long n = Long.parseLong(raw.trim());
            if (n <= 0) {
                return Long.MAX_VALUE;
            }
            return n;
        } catch (NumberFormatException e) {
            return 300_000L;
        }
    }

    public static AgentBackend get() {
        return INSTANCE;
    }

    public ToolRegistry tools() {
        return _tools;
    }

    public LLMClient llm() {
        return _llm;
    }

    public AgentLoop loop() {
        return _loop;
    }

    /** Two-phase build-then-refactor driver. */
    public AgentPipeline pipeline() {
        return _pipeline;
    }

    /** Pure-prose conversational reply: one LLM call with NO tools.
     *  Used by the {@link AgentRouter#CHAT} mode for greetings,
     *  questions about the existing model, or anything that does not
     *  warrant an edit. The model MoML is included so the LLM can
     *  answer factual questions about the current canvas.
     *  @param session The session under conversation.
     *  @param message The user's natural-language message.
     *  @param listener Optional listener; receives the final reply
     *      as a single step.
     *  @return A trace with a single "final" step containing the
     *      LLM's reply (or an error message). */
    public AgentTrace chat(org.ptolemy.agent.session.PtolemySession session,
            String message, AgentTraceListener listener) {
        AgentTrace trace = new AgentTrace();
        trace.setListener(listener);
        trace.putDiagnostic("driver", "chat");
        trace.putDiagnostic("modelContext",
                org.ptolemy.agent.session.ModelContext.forSession(session));
        if (!_llm.isAvailable()) {
            try {
                org.json.JSONArray empty = new org.json.JSONArray();
                org.ptolemy.agent.llm.LLMResponse offline =
                        _llm.chat(empty, empty);
                trace.finish(false, offline.content());
            } catch (Exception e) {
                trace.finish(false, "LLM offline: " + e.getMessage());
            }
            return trace;
        }
        try {
            org.json.JSONArray messages = new org.json.JSONArray();
            messages.put(new org.json.JSONObject()
                    .put("role", "system")
                    .put("content",
                            "You are a helpful Ptolemy II modeling"
                                    + " assistant. Reply in 1-3 short"
                                    + " sentences. Answer the user's"
                                    + " question or acknowledge the"
                                    + " message; do NOT propose edits"
                                    + " unless asked."));
            messages.put(new org.json.JSONObject()
                    .put("role", "user")
                    .put("content", message));
            messages.put(new org.json.JSONObject()
                    .put("role", "user")
                    .put("content",
                            org.ptolemy.agent.llm.PromptTemplates
                                    .modelContextNote(
                                            org.ptolemy.agent.session
                                                    .ModelContext
                                                    .forSession(session))));
            org.ptolemy.agent.llm.LLMResponse reply = _llm.chat(messages,
                    new org.json.JSONArray());
            String content = reply == null ? "" : reply.content();
            trace.finish(true, content == null ? "" : content);
        } catch (Exception e) {
            trace.finish(false, "Chat reply failed: " + e.getMessage());
        }
        return trace;
    }

    /** @return JSON status of the backend, used by /api/v1/agent/status. */
    public JSONObject status() {
        JSONObject json = new JSONObject();
        try {
            json.put("llm", _llm.status());
            json.put("llmRouting", _llmRouting);
            json.put("tools", _tools.toolsForUi());
        } catch (org.json.JSONException e) {
            // Should never happen with valid string keys
        }
        return json;
    }

    /** Build the single LLM client and routing summary.
     *
     *  <p>Priority:
     *  <ol>
     *  <li>DeepSeek-specific env/sysprops with the flash model.</li>
     *  <li>Generic {@code OPENAI_*} variables.</li>
     *  </ol>
     *
     *  <p>The constructor probes the model once with a 1-token chat
     *  call ({@link OpenAIClient#verifyReachable}) so a missing or
     *  renamed model ID is reported in {@code /api/v1/agent/status}
     *  immediately instead of paying a long read-timeout on every
     *  pipeline round.
     */
    private static ClientBundle buildClients() {
        String deepseekKey = env("DEEPSEEK_API_KEY",
                "agent.deepseek.apiKey", "");
        if (!deepseekKey.isEmpty()) {
            String base = env("DEEPSEEK_BASE_URL",
                    "agent.deepseek.baseUrl",
                    "https://api.deepseek.com");
            String flashModel = env("DEEPSEEK_MODEL_FLASH",
                    "agent.deepseek.model.flash",
                    env("DEEPSEEK_MODEL",
                            "agent.deepseek.model",
                            "deepseek-v4-flash"));
            OpenAIClient flash = new OpenAIClient(deepseekKey, base,
                    flashModel);
            boolean ok = flash.verifyReachable();
            JSONObject routing = new JSONObject();
            routing.put("provider", "deepseek");
            routing.put("strategy", "single-flash");
            routing.put("chat", flashModel);
            routing.put("single", flashModel);
            routing.put("planner", flashModel);
            routing.put("builder", flashModel);
            routing.put("refactor", flashModel);
            routing.put("reviewer", flashModel);
            routing.put("baseUrl", base);
            JSONObject probes = new JSONObject();
            probes.put("flash", ok ? "ok" : ("fail: "
                    + flash.reachableError()));
            routing.put("smokeProbes", probes);
            return new ClientBundle(flash, routing);
        }
        OpenAIClient openai = new OpenAIClient();
        openai.verifyReachable();
        JSONObject routing = new JSONObject();
        routing.put("provider", openai.providerName());
        routing.put("strategy", "single-model");
        String model = openai.status().optString("model", "");
        routing.put("chat", model);
        routing.put("single", model);
        routing.put("planner", model);
        routing.put("builder", model);
        routing.put("refactor", model);
        routing.put("reviewer", model);
        routing.put("baseUrl", openai.status().optString("baseUrl", ""));
        return new ClientBundle(openai, routing);
    }

    private static String env(String envName, String sysProp,
            String fallback) {
        String fromEnv = System.getenv(envName);
        if (fromEnv != null && !fromEnv.isEmpty()) {
            return fromEnv;
        }
        String fromProp = System.getProperty(sysProp);
        if (fromProp != null && !fromProp.isEmpty()) {
            return fromProp;
        }
        return fallback;
    }

    private static final class ClientBundle {
        final LLMClient client;
        final JSONObject routing;

        ClientBundle(LLMClient client, JSONObject routing) {
            this.client = client;
            this.routing = routing;
        }
    }
}

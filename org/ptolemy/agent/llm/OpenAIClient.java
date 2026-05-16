/* OpenAI-compatible Chat Completions client built on java.net.HttpURLConnection.

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
package org.ptolemy.agent.llm;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import org.json.JSONArray;
import org.json.JSONObject;

///////////////////////////////////////////////////////////////////
//// OpenAIClient

/**
 Chat Completions client targeted at the OpenAI HTTP API and any
 100% compatible endpoint (Azure OpenAI, vLLM, llama.cpp's server,
 Together, Ollama with the {@code /v1/chat/completions} adapter, ...).

 <p>Built on {@code java.net.HttpURLConnection} so no third-party HTTP
 library is required at M2. Reads its configuration from:
 <ul>
 <li>{@code OPENAI_API_KEY} environment variable (required),</li>
 <li>{@code OPENAI_BASE_URL} (defaults to {@code https://api.openai.com}),</li>
 <li>{@code OPENAI_MODEL} (defaults to {@code gpt-4o-mini}),</li>
 <li>plus matching {@code agent.openai.apiKey}, {@code agent.openai.baseUrl},
     {@code agent.openai.model} system properties for testing.</li>
 </ul>

 <p>The client never logs the API key. Errors include the HTTP status
 and the response body so misconfiguration is easy to diagnose.

 @author Ptolemy II Agent contributors
 @version $Id$
 @since Ptolemy II 11.1
 */
public class OpenAIClient implements LLMClient {

    private static final String DEFAULT_BASE_URL = "https://api.openai.com";
    private static final String DEFAULT_MODEL = "gpt-4o-mini";

    private final String _apiKey;
    private final String _baseUrl;
    private final String _model;
    private final String _providerName;
    private final int _connectTimeoutMs;
    private final int _readTimeoutMs;

    /** Cached result of the one-shot smoke probe.  Tri-state:
     *  null = not yet probed, TRUE = probe succeeded (model exists +
     *  API key valid), FALSE = probe failed (model not found, auth
     *  rejected, network down).  We treat FALSE as a hard "not
     *  available" so callers can fall back to a working sibling
     *  client without burning every per-request 120-second read
     *  timeout. */
    private volatile Boolean _reachable;
    private volatile String _reachableError = "";

    /** Build a client by reading configuration from the environment. */
    public OpenAIClient() {
        this(env("OPENAI_API_KEY", "agent.openai.apiKey", ""),
                env("OPENAI_BASE_URL", "agent.openai.baseUrl",
                        DEFAULT_BASE_URL),
                env("OPENAI_MODEL", "agent.openai.model", DEFAULT_MODEL));
    }

    /** Build a client with explicit configuration.
     *  @param apiKey OpenAI API key (sent as {@code Authorization: Bearer}).
     *  @param baseUrl Base URL up to but not including {@code /v1}.
     *  @param model Model name (e.g. {@code gpt-4o-mini}).
     */
    public OpenAIClient(String apiKey, String baseUrl, String model) {
        _apiKey = apiKey == null ? "" : apiKey;
        _baseUrl = stripTrailingSlash(
                baseUrl == null || baseUrl.isEmpty() ? DEFAULT_BASE_URL
                        : baseUrl);
        _model = model == null || model.isEmpty() ? DEFAULT_MODEL : model;
        _providerName = detectProviderName(_baseUrl, _model);
        _connectTimeoutMs = _resolveConnectTimeoutMs(_providerName);
        _readTimeoutMs = _resolveReadTimeoutMs(_providerName, _model);
    }

    @Override
    public String providerName() {
        return _providerName;
    }

    @Override
    public boolean isAvailable() {
        if (_apiKey.isEmpty()) {
            return false;
        }
        Boolean cached = _reachable;
        // Unprobed clients are optimistically considered available so
        // legacy single-model callers behave exactly as before.  Call
        // verifyReachable() explicitly to harden the check.
        if (cached == null) {
            return true;
        }
        return cached.booleanValue();
    }

    /** One-shot lightweight chat probe used to verify that the
     *  configured base URL + API key + model triple actually answers
     *  HTTP 2xx.  Cached forever: success means later
     *  {@link #isAvailable()} stays true; failure means it returns
     *  false so the caller can fall back to a sibling client without
     *  paying repeated 120-second read timeouts.
     *
     *  <p>Skipped when {@link #_apiKey} is empty (still returns
     *  false).  Skipped via {@code AGENT_SKIP_SMOKE_PROBE=true} for
     *  CI runs that don't want any outbound calls.  The probe is
     *  synchronized so concurrent callers share a single network
     *  request.
     *  @return True iff the model is reachable. */
    public synchronized boolean verifyReachable() {
        if (_reachable != null) {
            return _reachable.booleanValue();
        }
        if (_apiKey.isEmpty()) {
            _reachable = Boolean.FALSE;
            _reachableError = "no API key configured";
            return false;
        }
        if (_envFlag("AGENT_SKIP_SMOKE_PROBE",
                "agent.skipSmokeProbe", false)) {
            _reachable = Boolean.TRUE;
            return true;
        }
        try {
            JSONObject body = new JSONObject();
            body.put("model", _model);
            JSONArray messages = new JSONArray();
            messages.put(new JSONObject().put("role", "system")
                    .put("content", "ping"));
            messages.put(new JSONObject().put("role", "user")
                    .put("content", "ok"));
            body.put("messages", messages);
            body.put("max_tokens", 1);
            body.put("temperature", 0.0);
            String endpoint = _chatCompletionsEndpoint(_baseUrl);
            // Use a short probe-only timeout: a real model usually
            // returns a 1-token reply in well under 10 seconds; we'd
            // rather declare it unreachable and fall back than pay
            // the standard 60-120 s read timeout.
            postJsonWithTimeout(endpoint, body,
                    Math.min(_connectTimeoutMs, 10_000),
                    Math.min(_readTimeoutMs, 15_000));
            _reachable = Boolean.TRUE;
            return true;
        } catch (Throwable t) {
            _reachable = Boolean.FALSE;
            _reachableError = t.getMessage() == null
                    ? t.toString() : t.getMessage();
            return false;
        }
    }

    /** @return Last error from {@link #verifyReachable()}; empty when
     *  the probe succeeded or has not run yet. */
    public String reachableError() {
        return _reachableError;
    }

    @Override
    public JSONObject status() {
        JSONObject json = new JSONObject();
        json.put("provider", providerName());
        json.put("available", isAvailable());
        json.put("baseUrl", _baseUrl);
        json.put("model", _model);
        json.put("connectTimeoutMs", _connectTimeoutMs);
        json.put("readTimeoutMs", _readTimeoutMs);
        if (_reachable != null) {
            json.put("smokeProbe", _reachable.booleanValue()
                    ? "ok" : "fail");
            if (!_reachable.booleanValue() && !_reachableError.isEmpty()) {
                json.put("smokeError", _reachableError);
            }
        } else {
            json.put("smokeProbe", "unprobed");
        }
        return json;
    }

    @Override
    public LLMResponse chat(JSONArray messages, JSONArray tools)
            throws Exception {
        if (!isAvailable()) {
            throw new IllegalStateException(
                    "OpenAI client is not configured: OPENAI_API_KEY is unset");
        }

        JSONObject body = new JSONObject();
        body.put("model", _model);
        body.put("messages", messages);
        if (tools != null && tools.length() > 0) {
            body.put("tools", tools);
            body.put("tool_choice", "auto");
        }
        body.put("temperature", 0.2);

        String endpoint = _chatCompletionsEndpoint(_baseUrl);
        JSONObject response = postJson(endpoint, body);
        return parseResponse(response);
    }

    /** Streaming chat using OpenAI-compatible {@code stream: true}
     *  Server-Sent Events.  Sends one HTTP POST and parses
     *  {@code data: {...}} lines off the wire, accumulating
     *  {@code content} and {@code reasoning_content} deltas.  Calls
     *  {@code onDelta} at most once per {@code STREAM_THROTTLE_MS}
     *  with cumulative snapshots, so UI consumers can render
     *  progressively without bouncing on every token.
     *
     *  <p>When the request includes tools we transparently fall back
     *  to non-streaming because tool-call deltas across providers
     *  are not robustly compatible and the planner / reviewer paths
     *  that benefit from streaming all use the empty-tools shape. */
    @Override
    public LLMResponse chatStreaming(JSONArray messages, JSONArray tools,
            BiConsumer<String, String> onDelta) throws Exception {
        if (!isAvailable()) {
            throw new IllegalStateException(
                    "OpenAI client is not configured: OPENAI_API_KEY is unset");
        }
        if (tools != null && tools.length() > 0) {
            return chat(messages, tools);
        }

        JSONObject body = new JSONObject();
        body.put("model", _model);
        body.put("messages", messages);
        body.put("temperature", 0.2);
        body.put("stream", true);

        String endpoint = _chatCompletionsEndpoint(_baseUrl);
        URL url = new URL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        StringBuilder content = new StringBuilder();
        StringBuilder reasoning = new StringBuilder();
        List<JSONObject> rawToolCalls = new ArrayList<>();
        long lastEmit = 0L;
        try {
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "text/event-stream");
            conn.setRequestProperty("Authorization", "Bearer " + _apiKey);
            conn.setDoOutput(true);
            conn.setConnectTimeout(_connectTimeoutMs);
            conn.setReadTimeout(_readTimeoutMs);
            byte[] payload = body.toString().getBytes(StandardCharsets.UTF_8);
            try (OutputStream out = conn.getOutputStream()) {
                out.write(payload);
            }
            int status = conn.getResponseCode();
            if (status < 200 || status >= 300) {
                String err = readAll(conn.getErrorStream());
                throw new IOException("OpenAI HTTP " + status + ": " + err);
            }
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(),
                            StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.isEmpty()) {
                        continue;
                    }
                    if (!line.startsWith("data:")) {
                        continue;
                    }
                    String data = line.substring(5).trim();
                    if (data.isEmpty() || "[DONE]".equals(data)) {
                        continue;
                    }
                    JSONObject evt;
                    try {
                        evt = new JSONObject(data);
                    } catch (Exception parseError) {
                        continue;
                    }
                    JSONArray choices = evt.optJSONArray("choices");
                    if (choices == null || choices.length() == 0) {
                        continue;
                    }
                    JSONObject delta = choices.getJSONObject(0)
                            .optJSONObject("delta");
                    if (delta == null) {
                        continue;
                    }
                    if (!delta.isNull("content")) {
                        String c = delta.optString("content", "");
                        if (c != null && !c.isEmpty()) {
                            content.append(c);
                        }
                    }
                    if (!delta.isNull("reasoning_content")) {
                        String r = delta.optString(
                                "reasoning_content", "");
                        if (r != null && !r.isEmpty()) {
                            reasoning.append(r);
                        }
                    }
                    JSONArray tc = delta.optJSONArray("tool_calls");
                    if (tc != null) {
                        for (int i = 0; i < tc.length(); i++) {
                            rawToolCalls.add(tc.getJSONObject(i));
                        }
                    }
                    long now = System.currentTimeMillis();
                    if (onDelta != null
                            && now - lastEmit >= STREAM_THROTTLE_MS) {
                        lastEmit = now;
                        onDelta.accept(content.toString(),
                                reasoning.toString());
                    }
                }
            }
        } finally {
            conn.disconnect();
        }
        if (onDelta != null) {
            onDelta.accept(content.toString(), reasoning.toString());
        }
        return new LLMResponse(content.toString(),
                reasoning.length() == 0 ? null : reasoning.toString(),
                new ArrayList<LLMResponse.ToolCall>(), null);
    }

    /** Throttle for streaming delta emissions; ~6 updates per second
     *  is plenty for human UI without flooding the listener. */
    private static final long STREAM_THROTTLE_MS = 150L;

    /** Parse an OpenAI Chat Completions response into an LLMResponse. */
    static LLMResponse parseResponse(JSONObject response) {
        JSONArray choices = response.optJSONArray("choices");
        if (choices == null || choices.length() == 0) {
            return new LLMResponse(
                    "(empty response from OpenAI)", null, response);
        }
        JSONObject message = choices.getJSONObject(0)
                .optJSONObject("message");
        if (message == null) {
            return new LLMResponse("(no message in response)", null,
                    response);
        }
        String content = message.optString("content", "");
        String reasoningContent = message.optString("reasoning_content", "");
        List<LLMResponse.ToolCall> calls = new ArrayList<>();
        JSONArray toolCalls = message.optJSONArray("tool_calls");
        if (toolCalls != null) {
            for (int i = 0; i < toolCalls.length(); i++) {
                JSONObject call = toolCalls.getJSONObject(i);
                JSONObject fn = call.optJSONObject("function");
                if (fn == null) {
                    continue;
                }
                String name = fn.optString("name", "");
                String rawArgs = fn.optString("arguments", "{}");
                JSONObject parsed;
                try {
                    parsed = new JSONObject(rawArgs);
                } catch (Exception e) {
                    parsed = new JSONObject();
                    parsed.put("_rawArgsParseError", rawArgs);
                }
                calls.add(new LLMResponse.ToolCall(
                        call.optString("id", "tc-" + i), name, parsed));
            }
        }
        return new LLMResponse(content, reasoningContent, calls, response);
    }

    private JSONObject postJson(String endpoint, JSONObject body)
            throws IOException {
        return postJsonWithTimeout(endpoint, body, _connectTimeoutMs,
                _readTimeoutMs);
    }

    private JSONObject postJsonWithTimeout(String endpoint,
            JSONObject body, int connectTimeoutMs, int readTimeoutMs)
            throws IOException {
        URL url = new URL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        try {
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + _apiKey);
            conn.setDoOutput(true);
            conn.setConnectTimeout(connectTimeoutMs);
            conn.setReadTimeout(readTimeoutMs);

            byte[] payload = body.toString()
                    .getBytes(StandardCharsets.UTF_8);
            try (OutputStream out = conn.getOutputStream()) {
                out.write(payload);
            }

            int status = conn.getResponseCode();
            String responseBody = readAll(status >= 200 && status < 300
                    ? conn.getInputStream() : conn.getErrorStream());
            if (status < 200 || status >= 300) {
                throw new IOException("OpenAI HTTP " + status + ": "
                        + responseBody);
            }
            return new JSONObject(responseBody);
        } finally {
            conn.disconnect();
        }
    }

    private static boolean _envFlag(String envName, String sysProp,
            boolean fallback) {
        String raw = env(envName, sysProp, fallback ? "true" : "false");
        if (raw == null) {
            return fallback;
        }
        String v = raw.trim().toLowerCase();
        return "1".equals(v) || "true".equals(v) || "yes".equals(v)
                || "on".equals(v);
    }

    private static String readAll(InputStream in) throws IOException {
        if (in == null) {
            return "";
        }
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buf = new byte[4096];
            int n;
            while ((n = in.read(buf)) > 0) {
                out.write(buf, 0, n);
            }
            return new String(out.toByteArray(), StandardCharsets.UTF_8);
        }
    }

    private static String stripTrailingSlash(String s) {
        if (s.endsWith("/")) {
            return s.substring(0, s.length() - 1);
        }
        return s;
    }

    /** Build a chat completions endpoint from a base URL.
     *  Accept both forms:
     *  - https://host
     *  - https://host/v1
     *  and avoid generating /v1/v1/... */
    private static String _chatCompletionsEndpoint(String baseUrl) {
        String base = stripTrailingSlash(baseUrl == null ? "" : baseUrl);
        if (base.endsWith("/v1")) {
            return base + "/chat/completions";
        }
        return base + "/v1/chat/completions";
    }

    private static String detectProviderName(String baseUrl, String model) {
        String b = baseUrl == null ? "" : baseUrl.toLowerCase();
        String m = model == null ? "" : model.toLowerCase();
        if (b.contains("deepseek") || m.startsWith("deepseek")) {
            return "deepseek";
        }
        return "openai";
    }

    private static int _resolveConnectTimeoutMs(String providerName) {
        String fallback = "15000";
        if ("deepseek".equals(providerName)) {
            fallback = env("DEEPSEEK_CONNECT_TIMEOUT_MS",
                    "agent.deepseek.connectTimeoutMs",
                    env("LLM_CONNECT_TIMEOUT_MS",
                            "agent.llm.connectTimeoutMs", fallback));
        } else {
            fallback = env("OPENAI_CONNECT_TIMEOUT_MS",
                    "agent.openai.connectTimeoutMs",
                    env("LLM_CONNECT_TIMEOUT_MS",
                            "agent.llm.connectTimeoutMs", fallback));
        }
        return _parsePositiveInt(fallback, 15_000);
    }

    private static int _resolveReadTimeoutMs(String providerName, String model) {
        String modelLower = model == null ? "" : model.toLowerCase();
        String fallback = "120000";
        if ("deepseek".equals(providerName)) {
            if (modelLower.contains("v4-pro")) {
                fallback = "120000";
            } else if (modelLower.contains("v4-flash")) {
                fallback = "60000";
            }
            fallback = env("DEEPSEEK_READ_TIMEOUT_MS",
                    "agent.deepseek.readTimeoutMs",
                    env("LLM_READ_TIMEOUT_MS",
                            "agent.llm.readTimeoutMs", fallback));
        } else {
            fallback = env("OPENAI_READ_TIMEOUT_MS",
                    "agent.openai.readTimeoutMs",
                    env("LLM_READ_TIMEOUT_MS",
                            "agent.llm.readTimeoutMs", fallback));
        }
        return _parsePositiveInt(fallback, 120_000);
    }

    private static int _parsePositiveInt(String raw, int defaultValue) {
        try {
            int n = Integer.parseInt(raw.trim());
            return n > 0 ? n : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
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
}

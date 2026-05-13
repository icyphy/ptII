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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

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
    }

    @Override
    public String providerName() {
        return _providerName;
    }

    @Override
    public boolean isAvailable() {
        return !_apiKey.isEmpty();
    }

    @Override
    public JSONObject status() {
        JSONObject json = new JSONObject();
        json.put("provider", providerName());
        json.put("available", isAvailable());
        json.put("baseUrl", _baseUrl);
        json.put("model", _model);
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
        return new LLMResponse(content, calls, response);
    }

    private JSONObject postJson(String endpoint, JSONObject body)
            throws IOException {
        URL url = new URL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        try {
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + _apiKey);
            conn.setDoOutput(true);
            conn.setConnectTimeout(15_000);
            conn.setReadTimeout(120_000);

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

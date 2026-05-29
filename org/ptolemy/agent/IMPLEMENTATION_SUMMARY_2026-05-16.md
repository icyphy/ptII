# Ptolemy Agent Optimization Summary (2026-05-16)

## 1) Objective

This document summarizes the full set of changes implemented in this round, focused on:

- Stability under long/complex agent turns
- Large-model canvas performance (many atomic components)
- Automatic model quality checks and recursive optimization
- DeepSeek model routing (Pro vs Flash)
- Parallel execution experiments (plan-first, multi-candidate execution, best-pick)

## 2) Main Problems Addressed

- Long-running turns appeared as "no response" for a long time
- JSON serialization failures caused by non-finite numbers
- Poor responsiveness on large graphs during layout/render
- Inconsistent nested `parent` scope handling across tools
- Lack of automatic detection/fix of disconnected or overcrowded model areas
- Need for better model routing and execution strategy with DeepSeek

## 3) Frontend Changes

### 3.1 Canvas stability and performance

- `frontend/src/components/ModelCanvas.tsx`
  - Added grouped-node rendering and interactions (expand/collapse behavior)
  - Added finite coordinate guard before rendering node positions
  - Integrated worker-based layout execution and incremental graph patch updates
  - Reduced frequent heavy viewport operations (`fitView`) for large graphs
  - Improved delete/select/connect behavior consistency for grouped nodes

- `frontend/src/utils/autoLayout.ts`
  - Added lightweight layout mode for large graphs
  - Reduced expensive passes and limited dummy-node expansion in lightweight mode
  - Added clutter checks with bounded pair checks for scalability

- `frontend/src/workers/layoutWorker.ts` (new)
  - Moved layout compute off main thread

- `frontend/src/utils/compositeGrouping.ts` (new)
  - Added synthetic grouping logic based on type/name/connectivity heuristics

### 3.2 Agent interaction UX

- `frontend/src/state/sessionStore.ts`
  - Debounced/merged structural refresh requests
  - Added richer step metadata for thought/final steps

- `frontend/src/components/ChatPanel.tsx`
  - Added click-to-expand "Agent reasoning" panel
  - Filtered phase markers from reasoning details
  - Added explicit placeholder when no detailed reasoning text is available

## 4) Backend Core Changes

### 4.1 Stability guards and timeout behavior

- `org/ptolemy/agent/agent/AgentLoop.java`
  - Added wall-clock cap per run (`setMaxTurnMillis`)
  - Added timeout finish message to prevent indefinite monopolization
  - Added DeepSeek reasoning-content fallback into thought stream when `content` is empty

- `org/ptolemy/agent/agent/AgentBackend.java`
  - Added `AGENT_MAX_TURN_MS` config
  - Wired turn timeout into loop and pipeline
  - Added routing diagnostics in `/agent/status`

- `org/ptolemy/agent/session/PtolemySession.java`
  - Fixed `applyChange` timeout semantics and metrics logging
  - Strengthened dry-run handling and lightweight auto-layout preflight

### 4.2 Tool and scope reliability

- `org/ptolemy/agent/tools/ToolScopeResolver.java` (new)
  - Centralized nested-scope resolution and wrapping

- Updated to use unified scope resolver:
  - `AddEntityTool.java`
  - `AddCompositeTool.java`
  - `ConnectTool.java`
  - `DeleteTool.java`
  - `ListEntitiesTool.java`
  - `GroupIntoCompositeTool.java`
  - `ToolCallValidator.java`

### 4.3 Auto-layout and JSON safety

- `org/ptolemy/agent/tools/AutoLayoutTool.java`
  - Added adaptive timeout scaling
  - Added chunked location patch apply path
  - Added stage-level logging

- `org/ptolemy/agent/tools/autolayout/AutoLayoutWorkerMain.java`
  - Added finite-number sanitization for coordinates before JSON
  - Improved nested-parent scope resolution

- `org/ptolemy/agent/session/GraphSerializer.java`
  - Added finite coordinate checks with safe fallback

- `org/ptolemy/agent/session/SignalCollector.java`
  - Added finite-number checks for sampled values (`NaN/Inf -> null`)

## 5) Model Validation and Auto-Optimization

- `org/ptolemy/agent/tools/ValidateTool.java`
  - Added recursive scope scanning
  - Detects disconnected entities and overcrowded scopes
  - Produces actionable recommendations (`connect`/`delete` + args)

- `org/ptolemy/agent/agent/RefactorAdvisor.java`
  - Added recursive grouping suggestions with functional/name bucketing
  - Improved skip rules and suggestion naming robustness

- `org/ptolemy/agent/agent/AgentPipeline.java`
  - Added auto-optimize phase after build/refactor
  - Applies recommended actions in bounded rounds/actions
  - Runs final validate/run and reports optimization result

## 6) DeepSeek Routing and Thinking Compatibility

- `org/ptolemy/agent/llm/LLMResponse.java`
  - Added `reasoning_content` field support and assistant-message replay

- `org/ptolemy/agent/llm/OpenAIClient.java`
  - Parses and passes through `reasoning_content`
  - Added configurable connect/read timeout fields to status output
  - Added model/provider-aware timeout resolution for DeepSeek/OpenAI paths

- `org/ptolemy/agent/agent/AgentBackend.java`
  - DeepSeek dual-model routing retained
  - Updated routing so `v4-pro` is focused on planning path while execution/refactor can use flash path

## 7) Parallel Execution Practice (Plan -> Parallel Flash -> Pro Compare)

Implemented in `org/ptolemy/agent/agent/AgentPipeline.java`:

- Parallel flash build candidates (`AGENT_FLASH_PARALLEL_WORKERS`)
- Plan-derived logic-block hints (`futureComposites`/`logicBlocks`/actor-batching fallback)
- Candidate scoring by trace/validate/run diagnostics
- Planner-model comparison for best candidate selection
- Safety fallback:
  - if all candidates invalid -> fallback to single builder
  - if no candidate finishes in time -> fallback to single builder
- Session clone safety guard introduced to reduce parallel clone race issues

## 8) HTTP Service Throughput Improvements

- `org/ptolemy/agent/server/SimpleHttpServer.java`
  - Replaced fixed 8-thread executor with configurable pool
  - Added `AGENT_HTTP_THREADS` (bounded range)

## 9) Routing/Mode Heuristics

- `org/ptolemy/agent/agent/AgentRouter.java`
  - Added optimization/diagnosis intent detection to route to tool-capable mode

- `org/ptolemy/agent/llm/PromptTemplates.java`
  - Added optimization-oriented system instructions
  - Added recursive refactor guidance

## 10) Configurations Added/Used

- `AGENT_MAX_TURN_MS`
- `AGENT_HTTP_THREADS`
- `AGENT_REFACTOR_THRESHOLD`
- `AGENT_REFACTOR_MAX_TURN_MS`
- `AGENT_FLASH_PARALLEL_WORKERS`
- `AGENT_FLASH_PARALLEL_TIMEOUT_MS`
- `AGENT_FORCE_PARALLEL_BUILD`
- `DEEPSEEK_MODEL_PRO`
- `DEEPSEEK_MODEL_FLASH`
- `DEEPSEEK_READ_TIMEOUT_MS` / `DEEPSEEK_CONNECT_TIMEOUT_MS`
- generic `LLM_READ_TIMEOUT_MS` / `LLM_CONNECT_TIMEOUT_MS` fallback

## 11) Practical Smart Patterns That Worked Well

- Plan first, execute deterministically where possible, then use LLM only for repair
- Separate model roles (planner vs executor/refactor) instead of one-model-for-all
- Best-of-N parallel candidate execution with structured selection
- Bounded retries and hard safety guards for long-running paths
- Data-safety before serialization (finite-number sanitization)
- Recursive validation + actionable remediation recommendations

## 12) Current Known Risks / Follow-ups

- Very large/complex goals can still consume substantial time even with guards
- Parallel candidate mode increases compute usage; requires tuning by hardware
- Refactor quality vs latency remains a tunable trade-off per workload
- True backend cancellation for in-flight LLM/tool rounds can be further improved

## 13) Recommended Next Step

Add a runtime profile switch (for example: `speed` / `balanced` / `quality`) that atomically sets the key knobs above, so operation teams can switch behavior without code changes.


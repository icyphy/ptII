# Ptolemy II Auto-Modeling Agent 改造历程

> 记录自上一次稳定 commit `d9e5e88bfc agent(tier4): deterministic Java executor for phase-0 plans` 之后，
> 到 commit `a0b7045988 agent: single flash model, streaming planner reasoning, AutoLayout` 为止
> 的全部改造过程。

## 一句话总结

我们曾经把架构做"重"了 —— **双模型 + 迭代 MEGA 管线**，结果效果反而不如单模型；
然后**整体回退到单 flash 模型**，再补上**流式 reasoning UI**，最终代码量减少、响应可见、质量回升。

---

## 痛点时间线

```
┌──────────────────────────────────────────────────────────────────┐
│ T0  基线痛点                                                       │
│      · 单 flash 单次 LLM 处理不了"覆盖所有 component"这类大目标       │
│      · LLM 偶发幻觉类名 / 端口，build 报错堆积                       │
│      · 没有 reviewer，build 完就交付，质量参差                      │
├──────────────────────────────────────────────────────────────────┤
│ T1  尝试双模型 + MEGA 迭代管线                                      │
│      · v4-pro 做 plan / review，v4-flash 做 build / refactor       │
│      · GoalDecomposer + IterativePipeline 把大目标拆 4-20 个       │
│        composite，逐个 deterministic build                         │
├──────────────────────────────────────────────────────────────────┤
│ T2  迭代过程中暴露的二次痛点                                         │
│      · MEGA 模式触发后前端卡死（2000+ NDJSON 事件）                  │
│      · v4-pro + v4-flash 切换反而**比单 flash 更差**                │
│      · catalog 模式（一次 32 个 composite）跑得慢 + 失败率高         │
├──────────────────────────────────────────────────────────────────┤
│ T3  增量修复（quiet 流、truncate、auto-route、fast-mode 预设）       │
│      · 仍未根本解决质量问题                                          │
├──────────────────────────────────────────────────────────────────┤
│ T4  用户拍板回退                                                    │
│      · 删 MEGA、删 v4-pro，单 flash 跑全部 role                     │
├──────────────────────────────────────────────────────────────────┤
│ T5  新痛点：Phase 0 规划阻塞 30-60 s 没有任何 reasoning 文本展示     │
│      · 实施 SSE 流式 LLM 输出                                       │
│      · 前端 live thought slot 边生成边显示                          │
└──────────────────────────────────────────────────────────────────┘
```

---

## 改造阶段详解

### Phase 1 — 双模型质量修复（8 项）

**问题**：开启 v4-pro + v4-flash 双模型路由后，希望"planner 用 pro 想得深，
builder 用 flash 跑得快"。但实际质量没上去，反而比单 flash 还差。

| # | 文件 | 改动 | 目的 |
|---|------|------|------|
| 1 | `OpenAIClient` | `verifyReachable()` 启动期 smoke probe + 缓存；缩短探测 timeout | 避免每个角色都吃 120 s read-timeout |
| 2 | `AgentBackend` | 双模型路由（planner=pro, builder=flash …） | 角色分级 |
| 3 | `PlanExecutor` | `_cleanPlan()` 用 `LibraryIndex` 过滤 LLM 幻觉的类 / 端口 | execute 前止血 |
| 4 | `AgentPipeline` | 加 reviewer 阶段（v4-pro）；builder 恢复完整工具集；并行 worker 默认 1；executor 失败时支持 replan；安静流式 | 把"build → review"补完 |
| 5 | `PromptTemplates` | 新增 `REVIEWER_PROMPT`，更新 `BUILDER_PROMPT`、`DECOMPOSER_PROMPT`、`SUBGOAL_BUILDER_PROMPT` | 让 LLM 严格遵守 JSON 输出 schema |
| 6 | `AgentLoop` | `setIncludeCapabilityProbe(false)` for pipeline phases | 避免重复探测 |
| 7 | `RestRoutes` | `AGENT_DISABLE_PIPELINE` 全局开关；MEGA 模式 dispatch | 故障时一键回退 |
| 8 | `AgentTrace` | `truncateToLastN()` 限制 done payload | 防止前端 OOM |

### Phase 2 — MEGA 迭代管线 MVP

**问题**：用户输入 "搭建一个智能工厂"、"覆盖所有 actor 用例" 这种**单 LLM 一轮搞不定**的目标。

**方案**：拆 → 建 → 抛光。

```
GoalDecomposer (LLM 一轮)
   ↓ JSON SubGoal[]
IterativePipeline
   ├─ Phase A: 拆解（一次 LLM）
   ├─ Phase B: 每个 SubGoal 独立 build
   │           ├─ deterministic executor 优先
   │           └─ 失败时进 LLM repair 循环（scoped）
   │           └─ catastrophic 失败回滚 checkpoint
   └─ Phase C: 可选 final polish
```

**新增文件**：
- `org/ptolemy/agent/agent/SubGoal.java`
- `org/ptolemy/agent/agent/CheckpointStore.java`（MoML 快照 → `agent-output/checkpoints/<sid>/`）
- `org/ptolemy/agent/agent/GoalDecomposer.java`
- `org/ptolemy/agent/agent/IterativePipeline.java`

**两种 decomposer 模式**：
1. **LLM 模式**：v4-pro 直接读 mega-goal，返 4-20 个 SubGoal 的 JSON
2. **Catalog 模式**：从 LibraryIndex 按 category 抽取 ~47 个 composite（"覆盖所有 component" 的捷径）

### Phase 3 — UI 卡死修复

**问题**：MEGA 跑 32 个 sub-goal 时，每个 add_entity → 3 个 NDJSON 事件
（thought + tool_call + tool_result），单次 turn 流出 2000+ 事件，
前端 React 树重渲染 + 画布刷新撑死。

**修复**：
- 后端 `AGENT_ITER_QUIET=true`（默认）：内层 step 不再 fan-out，只发 phase 标记 + 每个 sub-goal 的 `add_composite` + `validate`
- `AgentTrace.truncateToLastN(300)` 在 response 前截断
- 前端 `sessionStore.ts` 加 `MAX_CHAT_ENTRIES = 800` 上限

**实测**：
| Sub-goals | 优化前事件数 | 优化后事件数 |
|-----------|-------------|-------------|
| 5         | 217         | 52          |
| 32        | 1400+       | 268         |

### Phase 4 — 自动 MEGA 路由

**问题**：用户不会主动写 `{"mode":"mega"}`，需要 router 自己判断。

**方案**：`AgentRouter.classifyWithScore()` + `MegaScore`（threshold = 12）。

各信号权重：

| 信号 | 权重 | 例子 |
|------|------|------|
| Catalog 短语 | +20（直接过 threshold） | "覆盖所有"、"every component"、"全量演示" |
| 长度梯度 | +1 ~ +8 | 200 chars 起加 |
| Subsystem 关键词 | +2 每个，max +10 | controller / 传感器 / 调度 …… |
| Archetype | +6 | "智能工厂"、"自动驾驶"、"数字孪生" |
| 数字规模 | +2 / +6 / +15 | "10 个组件"、"50+ actors" |
| 枚举 | +3 / +8 | "1) 2) 3)"、"第一 第二 第三" |
| 层级关键词 | +2 每个，max +6 | "子模块"、"嵌套 composite" |
| 多阶段 | +4 | "pipeline"、"第一阶段" |
| 大型形容词 | +2 每个，max +6 | "complex"、"大规模" |
| 显式小型 | **–5 每个，max –15** | "simple"、"最简"、"一个简单的" |

**测试**：`AgentRouterClassifyTest` 覆盖 15 个 case 全过。

`IterativePipeline` 把 `routerEvidence` 写进 trace.diagnostics，前端可显示 "Why MEGA?"。

### Phase 5 — Flash-only / Fast 模式预设

**问题**：用户后来明确说不再要 v4-pro（"反而更差 + 更慢"）。

**方案**（**注意：本阶段在 Phase 7 全部被简化掉了**，但思路保留以备复用）：

```
AGENT_FORCE_FLASH_ONLY=true
  └─ buildClients() 只构造 flash 客户端，跳过 v4-pro 的 smoke probe
  └─ 所有角色统一用 flash
  └─ /agent/status 显示 strategy: "flash-only"

AGENT_FAST_MODE=true   ← 一键预设
  └─ 隐含 AGENT_FORCE_FLASH_ONLY=true
  └─ + AGENT_REVIEWER_ENABLED=false
  └─ + AGENT_ITERATIVE_FINAL_POLISH=false
  └─ + AGENT_REPLAN_ON_FAILURE=false
  └─ + AGENT_FLASH_PARALLEL_WORKERS=1
  └─ + AGENT_PARALLEL_PLANNER_PICK=false
```

测试：`FastModeRoutingTest` 验证 `/agent/status` 里所有 role 都是 flash、pro probe 被 skip。

### Phase 6 — 启动脚本

**问题**：每次开发都要手敲一长串 env + java cp，烦。

**新增**：
- `agent-output/start-agent-flash.ps1` — 主脚本，支持 `-Dev` / `-CompileOnly` / `-CheckOnly`
- `org/ptolemy/agent/bin/ptagent-flash.bat` — Windows 双击入口
- `agent-output/agent-flash-quicktest.ps1` — 流式冒烟测试

**实际用法**：
```powershell
# 后端 + 前端两个窗口 + 健康检查
.\agent-output\start-agent-flash.ps1 -Dev

# 只编译
.\agent-output\start-agent-flash.ps1 -CompileOnly
```

启动后自动 GET `/api/v1/agent/status` 校验 `strategy: single-flash`。

### Phase 7 — **大回退**（用户拍板）

**问题**：用户反馈"双模型 + MEGA"整套架构效果太差，要求**回到不使用 MEGA 的版本**，
不再要 v4-pro。但之前没做 git 管理（没 commit），不能简单 `git checkout`。

**方案**：精确删除 MEGA + v4-pro 相关代码，保留其它有用的改进（AutoLayout、前端 didYouMean 等）。

**删除文件（7 个 Java 源）**：
- `agent/IterativePipeline.java`
- `agent/GoalDecomposer.java`
- `agent/CheckpointStore.java`
- `agent/SubGoal.java`
- `test/IterativePipelineSmokeTest.java`
- `test/AgentRouterClassifyTest.java`
- `test/FastModeRoutingTest.java`
- 及对应的 `.class` 和 `agent-output/checkpoints/` 目录

**重写文件**：

| 文件 | 简化 |
|------|------|
| `AgentBackend.java` | 删除 `_megaPipeline` / dual-model 分支 / `_applyFastModeOverrides` / `_forceFlashOnlyFromEnv` / `ClientBundle` 6 字段。简化为**单 flash 客户端**，单一 `ClientBundle(client, routing)` |
| `AgentRouter.java` | 删除 `Mode.MEGA` / `MegaScore` / `Classification` / `classifyWithScore` / `_scoreMega` / 所有 regex 模式。只留 `PIPELINE / SINGLE / CHAT` |
| `RestRoutes.java` | 删除 `mode=mega` 解析、`Mode.MEGA` switch case、kill-switch 中的 MEGA downgrade |
| `PromptTemplates.java` | 删除 `DECOMPOSER_PROMPT` + `SUBGOAL_BUILDER_PROMPT`（reviewer 保留，pipeline 仍用） |
| `start-agent-flash.ps1` | 删除 `AGENT_FAST_MODE` / `AGENT_FORCE_FLASH_ONLY` / `AGENT_REVIEWER_ENABLED` 等失效 env 写入（单模型默认就是 flash，不需要开关） |

**验证**：
```
javac OK (53 files, 从 60 减到 53)
GET /api/v1/agent/status →
  strategy: "single-flash"
  chat / single / planner / builder / refactor / reviewer 全是 deepseek-v4-flash
  smokeProbes.flash: "ok"
```

**保留**（与回退无关、用户也要的改进）：
- AutoLayout 工具（`AutoLayoutTool` + `autolayout/AutoLayoutWorkerMain`）
- `ToolScopeResolver`
- 前端 `compositeGrouping.ts` + `workers/layoutWorker.ts`
- 各 tool 的 `didYouMean` / 校验增强
- `PlanExecutor._cleanPlan` / `AgentLoop` 改进 / `AgentTrace.truncateToLastN`
- `AgentPipeline` 四阶段（plan → build → refactor → reviewer），但 reviewer 现在也用 flash

### Phase 8 — 流式 Reasoning UI

**问题**：用户看到 `[Phase 0/4: planning the model …]` 卡了 42 s 都没有任何文本输出，
显示 "No detailed reasoning text yet. The model is still generating."。
非常不好的用户体验 —— 因为 planner LLM 是一个阻塞的 30-60 s 调用。

**方案**：把 planner 的 LLM 调用改成 **SSE 流式**，让 token 边生成边推到前端。

#### 后端实现

```java
// 1. LLMClient 接口：default chatStreaming() fallback 到 chat()
default LLMResponse chatStreaming(
        JSONArray messages, JSONArray tools,
        BiConsumer<String, String> onDelta) throws Exception {
    LLMResponse reply = chat(messages, tools);
    onDelta.accept(reply.content(), reply.reasoningContent());
    return reply;
}

// 2. OpenAIClient 实现 SSE：stream: true → data: 行解析
//    累积 content + reasoning_content，150ms 节流回调
@Override
public LLMResponse chatStreaming(...) {
    body.put("stream", true);
    // ... 读 data: {...} 行，累积，throttled onDelta
}

// 3. AgentPipeline._runPlanner 在有 listener 时用 chatStreaming
//    每次发出 name="plan_stream" 的 thought step
listener.onStep(new AgentTrace.Step(0, "thought",
    "plan_stream", null, accumulated));
```

#### 前端实现

```typescript
// sessionStore: 新增 agentLiveThought slot（不进 chat 列表）
agentLiveThought: string | null

// applyStep 收到 plan_stream 时 → 替换 live slot
if (step.name === "plan_stream") {
    set({ agentLiveThought: step.text });
    return;  // 不 appendChat
}

// ChatPanel:
// · 自动展开 reasoning 面板（live thought 出现时）
// · 末尾一条蓝色 + 闪烁光标
// · max-h-72 滚动
```

**实测**（"build a 1 Hz sine wave through Scale(3.0) and a Recorder"）：

```
[#1 t=4.8s] text: "We"
[#2 t=5.1s] text: "We need to build a 1 Hz sine wave through"
[#3 t=5.3s] text: "We need to build a 1 Hz sine wave through Scale(3.0)
                    and a Recorder. This is a simple SDF pipeline..."
... 每 ~150ms 一帧，全部累积形式
```

从 ~5 s 开始就有第一帧，前端实时刷新。Phase 0 的 30-60 s 阻塞窗口完全消除。

---

## 当前架构

### 后端 (Java)

```
org/ptolemy/agent/
├── agent/
│   ├── AgentBackend.java          ← 单 flash 客户端，简单 6 行 buildClients
│   ├── AgentRouter.java           ← PIPELINE / SINGLE / CHAT（无 MEGA）
│   ├── AgentLoop.java             ← 通用 tool-using loop（SINGLE 模式）
│   ├── AgentPipeline.java         ← plan → build → refactor → reviewer
│   ├── AgentTrace.java            ← step 记录 + truncateToLastN
│   ├── AgentTraceListener.java
│   ├── AgentPlan.java
│   ├── PlanValidator.java
│   ├── PlanExecutor.java          ← deterministic phase-0 executor
│   ├── CapabilityProbe.java
│   └── RefactorAdvisor.java
├── llm/
│   ├── LLMClient.java             ← + 流式 default 方法
│   ├── LLMResponse.java
│   ├── OpenAIClient.java          ← SSE 流式 + smoke probe
│   ├── NullLLMClient.java
│   └── PromptTemplates.java       ← 删了 MEGA 专用 prompts
├── library/
│   ├── LibraryIndex.java
│   └── LibraryScanner.java
├── tools/
│   ├── ToolRegistry.java
│   ├── ToolCallValidator.java
│   ├── ToolScopeResolver.java     ← NEW: parent/composite scope 解析
│   ├── AddEntityTool.java
│   ├── AddCompositeTool.java
│   ├── ConnectTool.java         (+ didYouMean)
│   ├── ConnectManyTool.java
│   ├── DisconnectTool.java
│   ├── DeleteTool.java
│   ├── GroupIntoCompositeTool.java
│   ├── SetParameterTool.java
│   ├── ListEntitiesTool.java
│   ├── ListLibraryTool.java
│   ├── DescribeActorTool.java
│   ├── RunSimulationTool.java
│   ├── ValidateTool.java
│   ├── AutoLayoutTool.java        ← NEW
│   ├── autolayout/
│   │   └── AutoLayoutWorkerMain.java  ← NEW: 子进程算 ELK 布局
│   └── PortHints.java
├── session/
│   ├── PtolemySession.java
│   ├── SessionManager.java
│   ├── ModelContext.java
│   ├── GraphSerializer.java
│   └── SignalCollector.java
├── server/
│   ├── AgentServerMain.java       ← 端口 7777 入口
│   ├── SimpleHttpServer.java
│   └── RestRoutes.java            ← /agent/chat/stream NDJSON
├── util/
│   ├── AgentResult.java
│   └── JsonResponse.java
└── test/
    ├── CapabilityProbeSmokeTest.java
    ├── FastModeRoutingTest.java   ← 已删（Phase 7）
    ├── LibraryIndexSmokeTest.java
    ├── PlanExecutorSmokeTest.java
    ├── SessionSmokeTest.java
    └── ToolRegistrySmokeTest.java
```

### 前端 (TypeScript / React)

```
frontend/src/
├── api/
│   └── agentClient.ts             ← /agent/chat/stream NDJSON 流处理
├── state/
│   └── sessionStore.ts            ← + agentLiveThought live slot
├── components/
│   ├── ChatPanel.tsx              ← + 流式 reasoning 渲染、自动展开、闪烁光标
│   ├── ModelCanvas.tsx
│   └── ...
├── utils/
│   ├── autoLayout.ts
│   └── compositeGrouping.ts       ← NEW
├── workers/
│   └── layoutWorker.ts            ← NEW
└── examples/...
```

### 启动 (Windows)

```powershell
# 一键起后端 + 前端
.\agent-output\start-agent-flash.ps1 -Dev

# 仅编译
.\agent-output\start-agent-flash.ps1 -CompileOnly

# 健康检查
.\agent-output\start-agent-flash.ps1 -CheckOnly
```

---

## 数据流（当前）

```
User input
   │
   ▼
POST /api/v1/sessions/{sid}/agent/chat/stream
   │
   ▼
AgentRouter.classify() ──┐
   │                     │   PIPELINE / SINGLE / CHAT
   ▼                     │
┌──────────────────────────────────────────────┐
│ Mode == PIPELINE                              │
│  ┌──────────────────────────────────────────┐ │
│  │ Phase 0:  planner LLM (chatStreaming)    │ │ ←── name="plan_stream"
│  │           │  (SSE, 150ms throttle)        │ │     thought steps
│  │           ▼                                │ │
│  │           plan JSON                        │ │
│  ├──────────────────────────────────────────┤ │
│  │ Phase 0.5: PlanExecutor (deterministic)   │ │
│  │            replan once on big failure     │ │
│  ├──────────────────────────────────────────┤ │
│  │ Phase 1:  builder AgentLoop (with tools)  │ │
│  ├──────────────────────────────────────────┤ │
│  │ Phase 2:  refactor AgentLoop              │ │
│  ├──────────────────────────────────────────┤ │
│  │ Phase 3:  reviewer LLM (no tools)         │ │
│  │           → 执行 ≤8 个 safe actions       │ │
│  └──────────────────────────────────────────┘ │
└──────────────────────────────────────────────┘
   │
   ▼
NDJSON stream events:
  · started
  · step (thought / tool_call / tool_result)
  · done (final trace + session + signals)
```

---

## 经验教训

### 1. 复杂度有惯性，但不一定有回报

我们花了不少时间做 MEGA + 双模型，结果实际效果**不如**最初的单 flash。
原因：

- 大模型（v4-pro）做 plan，与小模型（v4-flash）做 build，**心智模型不一致**：
  pro 设计的"理想"actor 在 flash 真要建的时候经常被 disambiguate 错
- MEGA 拆解阶段加了一次 LLM 调用，错误率 × 拆解数量 = 总错误率被放大
- catalog 模式拼装出来的 47-actor 模型既看不懂也跑不动

**教训**：先把单 LLM 路径榨干，再考虑多 LLM 协作。

### 2. UI 卡死 = 事件流量级失控

前端每收一条 NDJSON 都会触发 zustand `set` + React rerender。
2000+ 事件 / 几秒 = 浏览器停掉。这跟后端"不卡"完全是两个量级问题。

**教训**：流式协议必须在协议层做"细节折叠"，不能假设前端能扛。
我们的方案：默认 quiet 模式只发 milestone，可选 verbose 模式带详细 step。

### 3. 隔 ~5 s 没有任何输出 = 用户以为卡了

阻塞的 LLM 调用即使最终成功，30-60 s 的"黑屏"对用户来说也是失败。

**教训**：**流式优先**。这是为什么 Phase 8 改完之后用户立即感觉"好用了"。
后端实现 SSE 比想象中简单（200 行 Java），前端是一个 live slot + 几行渲染。

### 4. Git commit 频率 = 试错成本

整轮工作没 commit，所以 Phase 7 回退时无法 `git reset`，
只能逐文件外科手术删除。**应该在每个 Phase 完成后立即 commit**。

---

## 后续建议（未做）

1. **Reviewer 也走流式**：当前 reviewer 仍是阻塞 chat()。机制已搭好，几行代码即可。
2. **Builder 中间思考流式**：AgentLoop 在每个 tool_call 之间也有一次阻塞 LLM，
   建议每个 turn 之间也加 plan_stream-style live thought。
3. **`.gitignore` 加 `agent-output/*.log,*.ndjson,*.xml`**：避免 dirty 工作区。
4. **加 `Continue` 端点**：长 build 中断后能 resume，而不是从头重建。
5. **Plan_stream 折叠**：连续多个 plan_stream 在前端聚合成一个块，
   完成后可点击"展开 reasoning"看完整记录。

---

## 关键 commit

- `d9e5e88bfc` — 基线（改造起点）
- `a0b7045988` — 当前（改造终点）

`git log --oneline d9e5e88bfc..HEAD` 查看 diff。

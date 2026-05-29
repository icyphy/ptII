# Ptolemy II 自动建模 Agent — 架构与亮点总览

> 适用范围：本文档对应分支 `cursor/agent-pipeline-and-anti-thrash`，从
> `4fbfdeb840 (agent: two-phase pipeline + router + anti-thrash)` 起到
> `a0b7045988 (agent: single flash model, streaming planner reasoning, AutoLayout)` 止，
> 共 8 次提交、约 +27 600 / -1 行代码、涉及 118 个新增/修改文件。

---

## 1. 我们要解决的问题

Ptolemy II 是一个成熟的"基于 actor 的异构建模与仿真"框架，原本通过
桌面 GUI（Vergil）由人手工拖拽 actor、连线、设参数、跑仿真。

本分支的目标只有一句话：

> **让用户用一段自然语言描述需求，系统自动产生一个能跑通的 Ptolemy II 模型。**

这件事对 LLM 来说并不是"写代码"那么简单，主要难点有四个：

| 难点 | 表现 |
|------|------|
| **类名/端口幻觉** | LLM 经常写出 `ptolemy.actor.lib.Sigmoid`、`Sum.input` 这种"看起来对、其实不存在"的东西 |
| **域语义** | 一个能跑的模型不仅要 actor 对，**director** 也要选对、采样率要相容、反馈环要带延迟元件 |
| **多步操作** | 一次建模需要 N 个 add_entity + M 个 connect + 设参 + 校验，**任何一步错都会污染后续步骤** |
| **长尾失败** | LLM 偶尔会陷入 "delete → add → delete" 这种振荡循环，把 token 烧光也没结果 |

我们的解法不是"让 LLM 更聪明"，而是把建模这个动作**重新切成可分工协作的工序**，
把 LLM **擅长的部分**（理解意图、决定整体结构）和它**不擅长的部分**（机械的逐步执行、
端口名记忆）解耦，并在每一道关键工序上加防御性栏杆。

---

## 2. 整体架构

```
                          浏览器（Vite + React + ReactFlow）
 ┌────────────────────────────────────────────────────────────────┐
 │  ChatPanel ─── 流式 reasoning(*new) + tool 历史折叠             │
 │  ModelCanvas ─ 组件折叠/分组(*new) + Web Worker 自动布局(*new)   │
 │  LibraryPanel · Inspector · PlotPanel · MomlView · Examples     │
 │                                                                 │
 │  sessionStore (Zustand)                                         │
 │   ├─ NDJSON SSE 流接入 (/agent/chat/stream)                     │
 │   ├─ 工具变化防抖刷新                                            │
 │   └─ agentLiveThought  ← 实时显示 planner 推理(*new)            │
 └─────────────────────────┬──────────────────────────────────────┘
                           │  HTTP / NDJSON
                           ▼
 ┌────────────────────────────────────────────────────────────────┐
 │  SimpleHttpServer + RestRoutes  (org/ptolemy/agent/server/)    │
 │                                                                 │
 │       AgentRouter.classify()   ←  CHAT / SINGLE / PIPELINE      │
 │             │                                                   │
 │   ┌─────────┴─────────┬──────────────────────┐                  │
 │   ▼                   ▼                      ▼                  │
 │ chat()             AgentLoop          AgentPipeline             │
 │ (无工具)        (单循环, 全部工具)   (四阶段, plan+build+         │
 │                                          refactor+review)       │
 └────────────────────────────────────────────────────────────────┘
                           │
                           ▼
 ┌────────────────────────────────────────────────────────────────┐
 │  ToolRegistry (14 个工具，统一 dispatch 入口)                   │
 │                                                                 │
 │  ToolCallValidator (preflight)                                  │
 │    └─ PortHints (Levenshtein didYouMean + 替代建议)(*new)        │
 │                                                                 │
 │  AgentTool 实例：                                               │
 │    add_entity · add_composite · group_into_composite            │
 │    connect · connect_many(*new) · disconnect                    │
 │    set_parameter · delete                                       │
 │    list_library · list_entities · describe_actor                │
 │    validate · run · auto_layout(*new)                           │
 └────────────────────────────────────────────────────────────────┘
                           │
                           ▼
 ┌────────────────────────────────────────────────────────────────┐
 │  PtolemySession  (org/ptolemy/agent/session/)                  │
 │   ├─ 独立的 Workspace + MoMLParser + CompositeActor + Manager   │
 │   ├─ SignalCollector（跑完仿真自动收集 Recorder 数据）           │
 │   ├─ GraphSerializer（CompositeActor → JSON for 前端）          │
 │   ├─ ModelContext（当前模型的紧凑文本视图，喂给 LLM）            │
 │   └─ classFailures 计数 (*new)                                  │
 │                                                                 │
 │  Ptolemy II Kernel  (no modification!)                          │
 │   MoMLParser / MoMLChangeRequest / Manager / Recorder           │
 └────────────────────────────────────────────────────────────────┘

 ┌────────────────────────────────────────────────────────────────┐
 │  LLM 抽象层 (org/ptolemy/agent/llm/)                            │
 │   LLMClient / LLMResponse / OpenAIClient(SSE 流式) / Null      │
 │   PromptTemplates  (SYSTEM / PLANNER / BUILDER /                │
 │                     REFACTOR / REVIEWER prompts)                │
 └────────────────────────────────────────────────────────────────┘

 (*new) = 本分支新增能力
```

> **关键设计原则**：Ptolemy II 内核**一行不改**。所有自动建模能力都通过
> `MoMLParser` / `MoMLChangeRequest` / `Manager` / `ExecutionListener` 这些
> 既有公共 API 注入进去。这意味着——agent 写出来的任何模型，Vergil 一打开
> 都能继续编辑、继续跑。

---

## 3. 后端工序：四阶段流水线（Pipeline）

`AgentPipeline.run()` 是建模请求的主入口，把"一次自然语言到一个能跑的模型"
拆成四个阶段。每个阶段都有专属的 LLM 角色、专属的工具子集、专属的 prompt，
互不串扰。

```
                              ┌──────────────────┐
                              │ User Goal (NL)   │
                              └────────┬─────────┘
                                       ▼
        ┌──────────────────────────────────────────────────┐
        │ Phase 0 / 4 — PLANNER  (LLM, 无 tool)            │
        │   · 读 CapabilityProbe scan + classFailures      │
        │   · 输出严格 JSON: {director, actors, wires}     │
        │   · 流式 SSE → 前端实时显示推理               (*new) │
        └────────┬─────────────────────────────────────────┘
                 ▼
        ┌──────────────────────────────────────────────────┐
        │ Phase 0.5 / 4 — VALIDATE + EXECUTE  (确定性 Java) │
        │   · PlanValidator: 类是否存在 / 端口是否真实       │
        │   · _cleanPlan(): 静默丢弃幻觉                    │
        │   · PlanExecutor: 一次性把整张模型搭起来          │
        │   · 失败比例 > 阈值时 → replan 一次               │
        └────────┬─────────────────────────────────────────┘
                 │  ┌─ 全部通过 → 跳过 Phase 1, 直接到 Phase 2
                 ▼  ▼
        ┌──────────────────────────────────────────────────┐
        │ Phase 1 / 4 — BUILDER  (LLM + 全部工具)           │
        │   · 仅修补 PlanExecutor 失败的步骤                │
        │   · anti-thrash + 重复调用守护                    │
        │   · 验证通过即结束                                │
        └────────┬─────────────────────────────────────────┘
                 ▼
        ┌──────────────────────────────────────────────────┐
        │ Phase 2 / 4 — REFACTOR  (LLM + 仅 group 工具)     │
        │   · RefactorAdvisor 给出分组建议                  │
        │   · group_into_composite 把扁平网络封装           │
        │   · 跳过条件：可分组 atom < 阈值                  │
        └────────┬─────────────────────────────────────────┘
                 ▼
        ┌──────────────────────────────────────────────────┐
        │ Phase 2.5 / 4 — REVIEWER  (LLM, 无 tool)         │
        │   · 读最终模型 + 推荐 ≤ 8 个 safe action          │
        │   · 白名单工具 + 硬上限                          │
        └────────┬─────────────────────────────────────────┘
                 ▼
        ┌──────────────────────────────────────────────────┐
        │  最终 trace + signals + graph → NDJSON 推到前端    │
        └──────────────────────────────────────────────────┘
```

### 3.1 为什么是"先确定性，再 LLM 兜底"？

把 Phase 0 的输出从"自然语言思路"改成**严格 JSON 计划**之后，机械操作
（add_entity × N、connect_many × 1、validate、run）可以由 `PlanExecutor` 用
**确定性 Java 代码**直接做完——零 LLM token，毫秒级完成。

实测在简单/中等复杂度的模型上，Plan + Executor 这两步通常**已经把模型建好并跑通**，
后面的 Phase 1 LLM builder 整轮被跳过。这是本分支带来的**最大效率提升**。

---

## 4. 让 LLM "建对" 的五层防线

围绕"防止 LLM 幻觉、提升一次成功率"，本分支构建了五层独立、可叠加的栏杆。
每一层都是 **可以单独 disable 仍然不影响系统跑** 的设计——叠在一起才是工业级。

| Tier | 防御点 | 关键文件 | 解决了什么 |
|:----:|--------|----------|------------|
| **1** | preflight 提示 | `PortHints.java` · `ToolCallValidator.java` | 端口/实体写错时，**同一次回复**就告诉 LLM "可能是 `Sum.minus`"、"实际方向是 input"，避免再开一轮 |
| **2** | 原子批量连线 | `ConnectManyTool.java` | 用一次 MoML `<group>` 把 N 条边全连上；任一条错则整批回滚 + 一次性返回所有错误 |
| **3** | 工具幂等 + 反振荡 | `AddEntityTool` / `SetParameterTool` 幂等; `AgentLoop` 的 anti-thrash 窗口 | 同名同参重复调 → `noop=true`；侦测 "delete X → add X" 等振荡模式时主动注入纠正提示 |
| **4** | 确定性执行器 | `PlanValidator.java` · `PlanExecutor.java` · `AgentPlan.java` | Phase 0 出 JSON plan，Java 直接 dispatch；零 LLM token 把"能机械做的事"做完 |
| **5** | 能力探针 + 失败画像 | `CapabilityProbe.java` · `ModelContext.classFailures` | 规划前先扫库给出"本任务建议用的真实 actor"；同一个类失败 ≥2 次时给 LLM 软提示"换一个吧" |

下面对每一层展开。

### 4.1 Tier 1 — 端口拼写与方向纠错（commit `396ec25f77`）

`PortHints` 用 Levenshtein 距离对"端口/实体"做模糊匹配，把原本是
`{"ok":false,"message":"port not found"}` 这种**死胡同**错误，升级成

```json
{
  "ok": false,
  "code": "DEST_PORT_NOT_FOUND",
  "message": "Sum.input not found. Did you mean Sum.minus?",
  "availablePorts": {"inputs":["plus","minus"],"outputs":["output"]},
  "didYouMean": ["Sum.minus", "Sum.plus"],
  "actualDirection": "input"
}
```

并且把第一条建议**嵌进 message**——这样即使 LLM 不解析结构化字段，也能从
对话文本里"看到"提示。`REVERSED_DIRECTION` 这种"接反了"还会单独识别出来。

### 4.2 Tier 2 — connect_many 原子批量连线（commit `69e59e471d`）

LLM 经常一次想连 5 条边、连错 1 条、循环重试 5 次，把 token 烧光。
`ConnectManyTool` 改成：

1. **全 preflight**：每一条边都走 ToolCallValidator + PortHints
2. **任意 1 条错 → 全部 reject**：返回 `rejected[]`（带 didYouMean） + `accepted[].status="would-apply"`
3. **全部通过 → 单条 MoML `<group>` 提交**：撤销栈、变更监听器看到的都是一次原子操作
4. **In-batch fan-out**：单端口源已经分到 relation 时，后续边自动复用，正确处理 `Const.output → Add.plus / Sub.plus / Scale.input` 这种一发多收

### 4.3 Tier 3 — 幂等 + 反振荡（commit `4fbfdeb840`）

- `AddEntityTool` / `SetParameterTool` **幂等**：同名同类/同值再调一次返回 `ok=true, noop=true`，不再失败
- `AgentLoop` 维护**最近 K 次工具调用滑动窗口**，识别两种典型病态模式：
  - `delete X → add X`（或反之）
  - 同一个工具连续 3+ 次
  - 命中时给下一轮 LLM 注入一段叫做 `antiThrash` 的纠正性 system note
- `GroupIntoCompositeTool` 在 wrap 失败时**主动 delete 半成品 composite**，避免下一次重试撞上残骸
- `ConnectTool` 单端口源自动复用 relation——这两者让 prompt 里说的"每个 consumer 调一次 connect"真正可工作

### 4.4 Tier 4 — 计划 → 校验 → 确定性执行（commit `d9e5e88bfc`）

这是**架构层面最关键的一次重构**：

```
            （旧）                        （新）
   ┌─────────────┐                ┌─────────────┐
   │ 1 个大 LLM    │                │ Planner LLM │ Phase 0
   │ ReAct 循环    │                └──────┬──────┘
   │ 几十轮 tool   │                       ▼
   │ 每轮可能错    │                ┌──────────────┐
   └─────────────┘                │PlanValidator │ Phase 0.5
                                  │PlanExecutor  │ (deterministic Java)
                                  └──────┬───────┘
                                         ▼
                                  ┌──────────────┐
                                  │ Builder LLM   │ Phase 1
                                  │ (只修补失败)   │ (经常被完全跳过)
                                  └──────────────┘
```

**PlanValidator** 在动手前先静态检查 plan：

- director / actor 的 `className` 是否在 classpath 上能解析
- 每条 connection 是否引用了 plan.actors 里声明过的 actor
- 重复边、director-与-actor 类型不匹配
- 已知的常见错（如本 Ptolemy 树没有 `ptolemy.actor.lib.Sigmoid`）给针对性建议

**PlanExecutor** 拿着干净的 plan 调 ToolRegistry：

- 全部通过 ToolRegistry.dispatch，因此 **Tier 1/2/5 的栏杆全部继续生效**
- 每一步都通过 `AgentTraceListener` 推到前端，渲染效果跟 LLM 直接调一模一样
- 失败 ≠ 抛异常：失败记录到 `Outcome.failedSteps`，能继续做的步骤继续做
- 跑完后 `Outcome.toReport()` 给 Builder LLM 一份结构化"已做完 / 失败列表"
- 当 `Outcome.fullyAutonomous()=true`（全部步骤成功 + validate + run 都通过），
  **Phase 1 LLM builder 整轮被跳过** —— 这个 happy path 是真正的零 token 成本

如果失败比例 > 阈值，触发**一次 replan**：planner 拿着失败报告改 JSON、清空 session、
重跑执行器。把 planner → builder 从单向管道变成**有限反馈环**。

### 4.5 Tier 5 — 能力探针 + 失败画像（commit `7b3804a101`）

LLM 一开始对"这个 Ptolemy 库里到底有什么 actor"是没有先验的，导致常见错误：

- 想要 sigmoid → 写 `ptolemy.actor.lib.Sigmoid`（不存在）
- 想算公式 → 写 `Expression`（需要 PortParameter 声明，add_entity 不会自动写）

`CapabilityProbe` 在 Phase 0 之前先做一次**轻量库扫描**：

1. 把用户目标分词，加上一组 domain 关键词（lstm/rnn/gru/pid/rc/fir/iir/ode/spring …）
   扩展出"经典原语"搜索词
2. 在 `LibraryIndex` 里查实际能用的 actor，**augment 命中排在字面命中之前**
3. 过滤 GUI plotter / Vergil 类（headless 跑不了）
4. 几个 FALLBACK_CLASSES 兜底（如 sigmoid/exp/log → `UnaryMathFunction`，tanh → `TrigFunction`）
5. 把若干"通用坑"（`Expression` 的 PortParameter 套路、`Recorder` 替代 GUI sink、
   SDF 反馈环需要 SampleDelay）作为**建议性文字**注入 prompt，**不是禁令**

同时 `PtolemySession.classFailures` 在每次失败时按"data.issues[*].field 实际归咎的类"
计数（只记 Expression 不记 Ramp，避免错杀）。`ModelContext` 在计数 ≥ 1 时把
"建议换一个 alternative"放进 LLM 的上下文。LLM 依然保留完全的 agency，只是不再
两眼一抹黑地反复撞同一堵墙。

---

## 5. 前端：让"长操作可见、大图能用"

### 5.1 流式 reasoning（commit `a0b7045988`）

Planner LLM 的一轮调用通常 30–60 秒；旧版本前端只能显示"还在生成…"，用户体验
等同于卡死。新方案在三层都做了改动：

- `LLMClient.chatStreaming()` —— 默认实现降级到阻塞 `chat()`，新接口零侵入
- `OpenAIClient` 实现 SSE：`stream: true` → 解析 `data: {...}` 行 → 累积 `content` + `reasoning_content`，每 150 ms 节流回调一次
- `AgentPipeline._runPlanner` 检测到 listener 存在时改走流式，每帧发一条 `name="plan_stream"` 的 thought
- 前端 `sessionStore` 新增 `agentLiveThought` slot（**不 append 到 chat 列表**，只就地替换）
- `ChatPanel` 自动展开 reasoning 面板 + 末尾蓝色光标 + max-h-72 滚动

**效果**：~5 秒后就能看到第一帧文字，整个 planner 阶段的"黑屏感"消失。

### 5.2 大图体验

| 组件 | 改进 |
|------|------|
| `ModelCanvas.tsx` | 分组节点（折叠/展开）、坐标有限值守卫、布局 Worker、图增量 patch、大图降频 fitView |
| `utils/autoLayout.ts` | lightweight 模式、迭代次数限界、拥挤检测 |
| `utils/compositeGrouping.ts` *(新)* | 按类型/命名/连接关系自动构造视觉分组 |
| `workers/layoutWorker.ts` *(新)* | 布局算法搬到 Web Worker，主线程不卡顿 |

### 5.3 后端配套：`AutoLayoutTool`（commit `a0b7045988`）

把 Ptolemy 原生 graph layout 放到一个**独立子进程**里跑：

- 主 backend JVM 不会被 GUI 代码污染（即使 layout 算法挂掉也不影响 server）
- 输出 patch（actor 名 → x,y）分块写回 MoML
- 自适应超时 + 阶段日志，超时回退到 lightweight 模式
- 坐标有限值校验，规避 `JSON does not allow non-finite numbers`

---

## 6. 路由：不要小题大做

`AgentRouter.classify()` 是一个**纯启发式**（不调 LLM）的三路分类器：

| Mode | 触发条件示例 | 处理路径 |
|------|--------------|----------|
| `CHAT` | "hi" / "this model 是干嘛的？" | `AgentBackend.chat()` —— 单次 LLM、无 tool |
| `SINGLE` | "把 Scale.factor 改成 5" / "wrap these three" | `AgentLoop` —— 单循环、全工具 |
| `PIPELINE` | 空模型 + 建模意图，或 "重建为更复杂的…" | `AgentPipeline` —— 四阶段 |

支持中英双语关键词，覆盖：

- **build intent**：build / create / implement / design / 搭建 / 构建 / 建模 / 重建
- **tweak intent**：change / set / delete / 改成 / 修改 / 删除 / 替换
- **refactor intent**：refactor / wrap into / 封装 / 分组 / 组合
- **optimize intent**：optimiz / diagnos / health check / 优化 / 诊断 / 校验
- **conversational**：以 ? 结尾 / what / how / why / 什么 / 怎么 / 为什么

请求 body 可以用 `{"mode":"..."}` 强制覆盖路由结果（开发/调试用）。

---

## 7. 7 大亮点 (TL;DR 给非技术听众)

> 如果只能讲 7 件事，就讲这 7 件。

1. **"工序分层" 而非 "做一个更聪明的 prompt"** —— 把建模拆成 Plan → 确定性
   Execute → LLM 修补 → 重构 → 复审四道工序，每道工序的 LLM 都只看自己该看的东西，
   出错也只在自己这一道工序内。

2. **Phase 0.5 确定性执行器** —— 用 Java 完成 add_entity / connect_many /
   validate / run 这些机械操作，**零 token、毫秒级**。在简单/中等复杂度模型上，
   Phase 1 LLM builder **整轮被跳过**。

3. **五层防 LLM 幻觉栏杆** —— didYouMean / connect_many / 幂等 + 反振荡 /
   PlanValidator / CapabilityProbe，可单独 disable、叠加生效。

4. **同一份 ToolRegistry 同时服务 LLM、HTTP、PlanExecutor、UI 拖拽** ——
   `add_entity` 这个工具，无论是 LLM 调、Java 自动调、用户在画布上拖出来调，
   走的都是同一段代码、同一套 preflight、同一条 trace。

5. **SSE 流式 reasoning** —— planner 阶段 30-60 秒的"黑屏"变成"5 秒就看到第一帧、
   持续滚动直到 plan 完成"，是最显著的用户感知改善。

6. **Ptolemy II 内核零侵入** —— 全部通过 `MoMLParser` / `MoMLChangeRequest` /
   `Manager` / `ExecutionListener` 公共 API；agent 写出来的模型 Vergil 直接打开
   能继续编辑，不丢失任何 Ptolemy 语义。

7. **走过弯路并复盘** —— 曾经尝试"双模型(v4-pro + v4-flash) + 迭代 MEGA 管线"，
   实测**质量反而下降**。回退到单 flash + 四阶段管线之后效果回升。
   过程详细记录在 [`IMPROVEMENTS.md`](IMPROVEMENTS.md)，是 LLM 应用工程的真实经验。

---

## 8. 端到端数据流（一次 chat 请求）

```
User 在浏览器输入 "build a 1 Hz sine wave through Scale(3.0) and a Recorder"
         │
         ▼
ChatPanel.submit() → sessionStore.sendToAgent()
         │
         ▼
POST /api/v1/sessions/{sid}/agent/chat/stream         (NDJSON)
         │
         ▼
RestRoutes → AgentRouter.classify()
         │  emptyModel=true + buildIntent=true → PIPELINE
         ▼
AgentPipeline.run(session, userGoal, listener)
         │
         │  ┌──────────────────────────────────────────────┐
         │  │ "[Phase 0/4: planning the model …]"          │ ──┐
         │  │ CapabilityProbe.scan(userGoal)                │   │
         │  │ planner LLM (chatStreaming, SSE)              │   │
         │  │   ↓ 每 ~150ms 一帧 "plan_stream" thought      │   │
         │  │ 最终 JSON plan = {director, actors, wires}    │   │
         │  └──────────────────────────────────────────────┘   │
         │  ┌──────────────────────────────────────────────┐   │
         │  │ "[Phase 0.5/4: executing plan deterministi-]"│   │
         │  │ PlanValidator.validate(plan)                  │   │
         │  │ PlanExecutor.execute(plan, session, tools)    │   │
         │  │   → add_entity SDFDirector                    │   │
         │  │   → add_entity Sine                           │   │
         │  │   → add_entity Scale                          │   │
         │  │   → add_entity Recorder                       │   │
         │  │   → set_parameter Sine.frequency 1.0          │   │
         │  │   → set_parameter Scale.factor 3.0            │   │
         │  │   → connect_many [Sine.output→Scale.input,    │   │
         │  │                   Scale.output→Recorder.input]│   │
         │  │   → validate (ok)                             │   │
         │  │   → run (ok)                                  │   │
         │  │ Outcome.fullyAutonomous() = true              │   ├─►所有 step
         │  └──────────────────────────────────────────────┘   │   通过
         │  ┌──────────────────────────────────────────────┐   │ AgentTraceListener
         │  │ "[Phase 1/4: skipped — executor produced …]"  │   │ 一帧一帧
         │  └──────────────────────────────────────────────┘   │ 推到前端
         │  ┌──────────────────────────────────────────────┐   │
         │  │ "[Phase 2/4: refactor skipped — only 3 atoms]"│   │
         │  └──────────────────────────────────────────────┘   │
         │  ┌──────────────────────────────────────────────┐   │
         │  │ Phase 2.5/4: reviewer 8-action 微调            │   │
         │  └──────────────────────────────────────────────┘   │
         ▼                                                      ▼
NDJSON 推流：started → step* → done
         │
         ▼
sessionStore.applyStep(step):
   · step.kind=="thought" && name=="plan_stream" → agentLiveThought = step.text
   · step.kind=="tool_call" / "tool_result"      → grouped 折叠卡片
   · step.kind=="final"                          → chat append
   · 结构性 tool (add_entity/connect/...)       → debounce refresh graph
         │
         ▼
ModelCanvas 重新拉 GraphSerializer.toJson()
   · ReactFlow 节点更新（按 _location 摆放）
   · 用户看到 Sine ─→ Scale ─→ Recorder 自动出现
         │
         ▼
PlotPanel 拉 /signals → 显示 Recorder 收到的正弦波形
```

---

## 9. 代码地图

### 9.1 后端（Java，53 个源文件）

```
org/ptolemy/agent/
├── agent/                          ← 编排层
│   ├── AgentBackend.java           单例: 装配 LLM + Tools + 两条入口循环
│   ├── AgentRouter.java            纯启发式 CHAT/SINGLE/PIPELINE 分类
│   ├── AgentLoop.java              ReAct 主循环 + anti-thrash + 失速保护
│   ├── AgentPipeline.java          ★ 四阶段流水线主控
│   ├── AgentTrace[Listener].java   step 流 + truncateToLastN
│   ├── AgentPlan.java              phase-0 JSON 形状校验
│   ├── PlanValidator.java          ★ 语义校验 (director/class/wire)
│   ├── PlanExecutor.java           ★ 确定性 dispatch 执行器
│   ├── CapabilityProbe.java        ★ 库扫描 + 失败画像 prompt 注入
│   └── RefactorAdvisor.java        composite 分组建议
│
├── tools/                          ← AgentTool 实现
│   ├── ToolRegistry.java           注册中心 + .only() / .except()
│   ├── ToolCallValidator.java      ★ 所有工具共用的 preflight
│   ├── ToolScopeResolver.java      统一 nested composite scope 解析
│   ├── PortHints.java              ★ Levenshtein didYouMean
│   ├── AddEntityTool / AddCompositeTool
│   ├── ConnectTool / ConnectManyTool ★ / DisconnectTool
│   ├── GroupIntoCompositeTool      自带"半成品 cleanup"
│   ├── SetParameterTool / DeleteTool
│   ├── ListLibraryTool / ListEntitiesTool / DescribeActorTool
│   ├── RunSimulationTool / ValidateTool
│   ├── AutoLayoutTool ★ + autolayout/AutoLayoutWorkerMain (子进程)
│
├── llm/
│   ├── LLMClient.java              + default chatStreaming() ★
│   ├── LLMResponse.java            + reasoning_content 透传
│   ├── OpenAIClient.java           ★ SSE 流式 + smoke probe + 重试
│   ├── NullLLMClient.java          离线兜底
│   └── PromptTemplates.java        SYSTEM / PLANNER / BUILDER /
│                                   REFACTOR / REVIEWER prompts
│
├── library/
│   ├── LibraryIndex.java           按 className/displayName/category 索引
│   └── LibraryScanner.java         scan ptolemy/configs/basicLibrary.xml
│
├── session/
│   ├── PtolemySession.java         一个 sid 一个 Workspace+Manager
│   ├── SessionManager.java
│   ├── ModelContext.java           ★ 含 classFailures 投影
│   ├── GraphSerializer.java        CompositeActor → JSON for 前端
│   └── SignalCollector.java        Recorder 注入 + detach + NaN→null
│
├── server/
│   ├── AgentServerMain.java        端口 7777
│   ├── SimpleHttpServer.java       JDK 自带 + 可配置线程池
│   └── RestRoutes.java             /agent/chat[/stream] + 全部 CRUD
│
├── util/                           AgentResult / JsonResponse
└── test/                           5 个 smoke test
```

### 9.2 前端（TypeScript + React + Vite）

```
frontend/src/
├── api/agentClient.ts              NDJSON 流解析 + REST 封装
├── state/sessionStore.ts           Zustand store + agentLiveThought ★
├── components/
│   ├── ChatPanel.tsx               ★ 流式 reasoning + tool 折叠
│   ├── ModelCanvas.tsx             ReactFlow + 分组节点
│   ├── LibraryPanel · Inspector · PlotPanel · MomlView · ExamplesPanel
│   ├── ActivityPanel · BottomPanel · AppHeader
├── utils/
│   ├── autoLayout.ts               主线程降级布局
│   └── compositeGrouping.ts ★      视觉自动分组
├── workers/layoutWorker.ts ★       Web Worker 重布局
└── examples/demoExamples.ts        RC / PID / Repair 三个 demo
```

---

## 10. 启动 & 验证

### 10.1 一键开发

```powershell
# 后端 7777 + 前端 5173 + 健康检查
.\agent-output\start-agent-flash.ps1 -Dev
```

或拆开来：

```powershell
.\agent-output\start-agent-flash.ps1 -CompileOnly   # 仅编译
.\agent-output\start-agent-flash.ps1 -CheckOnly     # 仅健康检查
```

### 10.2 环境变量

| 变量 | 默认 | 作用 |
|------|------|------|
| `DEEPSEEK_API_KEY` | – | 必填；空则降级 NullLLMClient |
| `DEEPSEEK_MODEL` / `DEEPSEEK_MODEL_FLASH` | `deepseek-v4-flash` | 单 flash 客户端使用的模型 |
| `DEEPSEEK_BASE_URL` | `https://api.deepseek.com` | OpenAI 兼容 endpoint |
| `OPENAI_API_KEY` / `OPENAI_MODEL` / `OPENAI_BASE_URL` | – | 不用 DeepSeek 时的回退路径 |
| `AGENT_MAX_STEPS` | `200` | 单次 ReAct 上限；`0` = 无限 |
| `AGENT_MAX_TURN_MS` | `300 000` | 单轮墙钟超时 |
| `AGENT_REFACTOR_THRESHOLD` | 4 | 顶层 atom 少于此数时跳过 Phase 2 |
| `AGENT_DISABLE_PIPELINE` | `false` | 一键回退到 SINGLE 单循环 |
| `AGENT_HTTP_THREADS` | 8 | HTTP 线程池上限 |

### 10.3 验证

```powershell
# liveness
Invoke-RestMethod http://localhost:7777/api/v1/health

# 看 LLM 路由策略
Invoke-RestMethod http://localhost:7777/api/v1/agent/status
# 期望 strategy = "single-flash", smokeProbes.flash = "ok"
```

### 10.4 三个标准 demo

| Demo | 入口 | 验证什么 |
|------|------|----------|
| Build RC Low-Pass | `frontend/src/examples/demoExamples.ts` | 自然语言 → 一阶 RC 低通 |
| Tune PID | `org/ptolemy/agent/demos/tune-pid-seed.xml` | 多轮 chat + 闭环参数整定 |
| Repair Model | `org/ptolemy/agent/demos/repair-seed.xml` | 修复故意搞坏的 SDF 链 |

---

## 11. 经验教训（也是 LLM 应用工程的反直觉之处）

1. **不是模型越大效果越好**。曾经的 `v4-pro (plan) + v4-flash (build)` 双模型方案
   实测**比单 flash 还差**——原因是 pro 设计的"理想" actor 在 flash 真去建的时候
   经常对应不上具体类，心智模型不一致放大了错误。

2. **加 LLM 调用次数不一定提升质量**。MEGA 拆解阶段加一次 decomposer LLM，
   错误率随 sub-goal 数量线性放大，最终输出反而更差。

3. **流式协议必须在协议层做"细节折叠"**。某次实验单 turn 流出 2000+ NDJSON
   事件，前端浏览器直接停掉——后端"不卡"和前端"扛得住"是两个独立的工程问题。

4. **5 秒没有输出 = 用户认为卡了**。流式 reasoning 不是锦上添花，是必须的。
   后端 SSE 实现 ~200 行 Java，前端就 1 个 live slot + 几行渲染——投入产出极高。

5. **commit 频率 = 试错成本**。本分支早期没及时 commit，回退某项尝试时只能
   一个文件一个文件外科手术删除——下一次每个阶段必须立即 commit。

---

## 12. Commit 一览（按时间顺序）

| Commit | 标题 | 关键贡献 |
|--------|------|----------|
| `4fbfdeb840` | agent: two-phase pipeline + router + anti-thrash | 整套**流水线骨架**、AgentRouter、AgentLoop 反振荡、工具幂等 |
| `9fae7df7a9` | agent: improve modeling loop guidance | prompt 调优 |
| `75d9d4053e` | agent: snapshot UI + planning helpers + probe-leak fix | 前端 v1 落地、AgentPlan / RefactorAdvisor / ModelContext / ToolCallValidator |
| `396ec25f77` | agent(tier1): didYouMean hints in tool preflight errors | **Tier 1** PortHints + 增强错误结构 |
| `69e59e471d` | agent(tier2): connect_many batch wiring tool | **Tier 2** ConnectManyTool 原子批量 |
| `7b3804a101` | agent(capability): pre-planning library scan + class-failure history | **Tier 5** CapabilityProbe + classFailures |
| `d9e5e88bfc` | agent(tier4): deterministic Java executor for phase-0 plans | **Tier 4** PlanValidator + PlanExecutor + Phase 0.5 |
| `a0b7045988` | agent: single flash model, streaming planner reasoning, AutoLayout | **回退**为单 flash + **SSE 流式 reasoning** + AutoLayout 工具 |

---

## 13. 相关文档

| 文件 | 内容 |
|------|------|
| [`README.md`](README.md) | 包架构 + REST API + M0–M5 里程碑 |
| [`OPERATIONS.md`](OPERATIONS.md) | Windows 启动/停止/排障细节 |
| [`IMPROVEMENTS.md`](IMPROVEMENTS.md) | 双模型/MEGA 实验经过 + 回退原因（含教训复盘） |
| [`IMPLEMENTATION_SUMMARY_2026-05-16.zh-CN.md`](IMPLEMENTATION_SUMMARY_2026-05-16.zh-CN.md) | 早期里程碑快照（已被本文档补全/取代） |
| [`demos/README.md`](demos/README.md) | 三个标准 demo 的推荐 prompt |


# Ptolemy Agent 优化总结（2026-05-16）

## 1）本轮目标

本轮改造围绕以下核心目标展开：

- 提升复杂请求下的稳定性与可恢复性
- 提升大模型图（大量原子组件）下的前端性能
- 增强模型自动诊断与自动优化能力
- 完成 DeepSeek Pro / Flash 分工路由
- 落地“先规划、再并行执行、最后择优”的智能执行实践

## 2）主要问题与痛点

- 复杂请求时，用户侧容易感知为“长时间无响应”
- 出现 `JSON does not allow non-finite numbers` 序列化异常
- 大图布局和渲染卡顿明显
- 多工具在嵌套 `parent` 场景下作用域处理不一致
- 缺少对“未连接组件 / 层级过拥挤”的自动诊断与修复
- LLM 单模型串行执行在质量与时延上难以兼顾

## 3）前端改动

### 3.1 画布稳定性与性能

- `frontend/src/components/ModelCanvas.tsx`
  - 接入分组节点渲染与交互（折叠/展开）
  - 渲染前增加坐标有限值保护，避免异常值污染画布
  - 接入布局 Worker 与图增量 patch 更新路径
  - 大图场景降低频繁 `fitView` 的开销
  - 分组节点在选择、删除、连线行为上做一致性处理

- `frontend/src/utils/autoLayout.ts`
  - 增加大图 lightweight 布局模式
  - 限制高成本迭代与虚拟节点扩展
  - 拥挤检测加入限界计算策略

- `frontend/src/workers/layoutWorker.ts`（新增）
  - 布局计算迁移至 Worker，主线程更流畅

- `frontend/src/utils/compositeGrouping.ts`（新增）
  - 基于类型、命名、连接关系的分组构建逻辑

### 3.2 Agent 交互体验

- `frontend/src/state/sessionStore.ts`
  - 工具触发的结构刷新增加防抖/合并
  - 细化 thought/final 的元数据写入

- `frontend/src/components/ChatPanel.tsx`
  - `Agent reasoning…` 支持点击展开
  - 过滤阶段标记噪音，展示更真实的推理内容
  - 无详细推理时显示明确提示文案

## 4）后端核心改动

### 4.1 稳定性护栏与超时治理

- `org/ptolemy/agent/agent/AgentLoop.java`
  - 增加单轮墙钟超时能力（`setMaxTurnMillis`）
  - 超时时返回明确错误，避免无限阻塞
  - 在 `content` 为空时，支持使用 `reasoning_content` 作为 thought 输出

- `org/ptolemy/agent/agent/AgentBackend.java`
  - 增加 `AGENT_MAX_TURN_MS` 配置读取与注入
  - `/agent/status` 增加路由与状态诊断信息

- `org/ptolemy/agent/session/PtolemySession.java`
  - 修复 `applyChange` 超时语义与耗时日志
  - dry-run 路径增强（含 auto_layout 轻量预检）

### 4.2 工具作用域一致性

- `org/ptolemy/agent/tools/ToolScopeResolver.java`（新增）
  - 统一嵌套作用域解析与 MoML 包装逻辑

- 多个工具统一接入作用域解析：
  - `AddEntityTool.java`
  - `AddCompositeTool.java`
  - `ConnectTool.java`
  - `DeleteTool.java`
  - `ListEntitiesTool.java`
  - `GroupIntoCompositeTool.java`
  - `ToolCallValidator.java`

### 4.3 布局与 JSON 安全

- `org/ptolemy/agent/tools/AutoLayoutTool.java`
  - 自适应超时与 patch 分块写回
  - 增加阶段日志

- `org/ptolemy/agent/tools/autolayout/AutoLayoutWorkerMain.java`
  - 坐标有限值校验，规避非有限数 JSON 异常
  - 优化嵌套父路径处理

- `org/ptolemy/agent/session/GraphSerializer.java`
  - 坐标序列化增加有限值保护

- `org/ptolemy/agent/session/SignalCollector.java`
  - 信号数值序列化增加 `NaN/Inf -> null` 策略

## 5）模型校验与自动优化

- `org/ptolemy/agent/tools/ValidateTool.java`
  - 增加递归扫描
  - 检测未连接组件与拥挤作用域
  - 返回可执行建议（含推荐工具与参数）

- `org/ptolemy/agent/agent/RefactorAdvisor.java`
  - 递归分组建议（功能桶 + 命名桶）
  - 增强建议命名稳定性与跳过规则

- `org/ptolemy/agent/agent/AgentPipeline.java`
  - 新增自动优化阶段（Phase 3/4）
  - 在有限轮次内执行 connect/delete/group 建议
  - 最终 validate/run 收敛并回传优化结果

## 6）DeepSeek 路由与 thinking 兼容

- `org/ptolemy/agent/llm/LLMResponse.java`
  - 新增 `reasoning_content` 字段透传能力

- `org/ptolemy/agent/llm/OpenAIClient.java`
  - 解析并透传 `reasoning_content`
  - 状态中暴露 connect/read 超时参数
  - 超时按 provider/model 分级可配置

- `org/ptolemy/agent/agent/AgentBackend.java`
  - DeepSeek 双模型路由完善
  - 当前策略支持 Pro 侧重规划，执行链路优先 Flash

## 7）并行执行实践（Plan -> 并行 Flash -> Pro 择优）

核心落地于 `org/ptolemy/agent/agent/AgentPipeline.java`：

- 并行 Flash 候选构建（worker 数可配置）
- 从规划结果提取逻辑块并分配候选 focus（无则按 actor 分批）
- 候选评分（trace/validate/run/诊断）
- 由 Planner（Pro）进行候选对比择优，失败时回退确定性排序
- 稳定性增强：
  - 候选全无效 -> 自动回退单 builder
  - 候选超时全未完成 -> 自动回退单 builder
  - 并发克隆路径增加安全保护，降低并行会话复制风险

## 8）HTTP 服务吞吐改进

- `org/ptolemy/agent/server/SimpleHttpServer.java`
  - 固定 8 线程改为可配置线程池
  - 新增 `AGENT_HTTP_THREADS`（含上下界）

## 9）路由与提示词增强

- `org/ptolemy/agent/agent/AgentRouter.java`
  - 增强“优化/诊断”意图识别，优先进入可执行模式

- `org/ptolemy/agent/llm/PromptTemplates.java`
  - 增加优化场景规则与递归重构指导

## 10）关键配置项

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
- `LLM_READ_TIMEOUT_MS` / `LLM_CONNECT_TIMEOUT_MS`

## 11）较好的智能化实践（可复用）

- 先规划，再确定性执行，最后 LLM 修复：降低纯生成不确定性
- 多模型分工（Pro 做难决策，Flash 做高频执行）：平衡质量与时延
- Best-of-N 并行候选 + 结构化择优：降低单次生成波动风险
- 全链路可回退（超时回退、并行回退、评分回退）：避免“直接失败”
- 数据安全前置（序列化前有限值检查）：减少运行期异常
- 递归校验 + 可执行建议：形成持续优化闭环

## 12）当前风险与后续建议

- 超大规模（500+ 原子组件）仍需按机器资源调优并行参数
- 并行执行会增加 CPU/内存占用，需结合部署规格设上限
- 质量与时延是可调权衡，建议提供预设档位
- 后端“真取消”能力仍可继续加强（不仅前端中断）

## 13）建议下一步

增加运行时策略档位切换（如 `speed` / `balanced` / `quality`），一键映射关键参数，便于生产环境快速切换策略。


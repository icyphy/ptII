# Ptolemy II Agent 启动与停止操作说明

本文说明如何在 Windows 上启动、停止和检查以下三类界面：

- **Agent 后端**（Java REST，默认端口 `7777`）
- **Agent Web 前端**（React + Vite，默认端口 `5173`）
- **Ptolemy II 原生 GUI**（Vergil 图形建模界面）

仓库根目录记为 `PTII`，本机示例为 `C:\projects\ptII`。

## 1. 启动前准备

### 1.1 环境变量

在**新开**的 PowerShell 或 CMD 中设置（按需）：

```powershell
setx PTII "C:\projects\ptII"
setx DEEPSEEK_API_KEY "sk-你的密钥"
setx DEEPSEEK_BASE_URL "https://api.deepseek.com"
setx DEEPSEEK_MODEL "deepseek-chat"
# 可选：放大 / 取消 ReAct 循环上限。0 表示"无上限，靠 LLM 自然结束 + 失速检测退出"。
setx AGENT_MAX_STEPS "0"
```

`setx` 写入用户环境变量后，需要**重新打开终端**才会生效。

Agent 后端会读取 `DEEPSEEK_*`；未配置时 LLM 为 offline，但加载模型、运行仿真、画布编辑仍可用。

**`AGENT_MAX_STEPS`** 控制单次 agent 循环的硬上限：

| 值 | 行为 |
|----|------|
| 未设置 | 默认 200 轮 |
| 任意正整数 N | 最多 N 轮工具调用 |
| `0` 或负数 | 取消硬上限；仅当 ① LLM 不再发起工具调用（任务完成）② 连续 8 轮全部失败（失速保护）③ LLM 请求异常时退出 |

复杂模型（带多个 composite、PID、滤波器等）建议 `AGENT_MAX_STEPS=0`。

### 1.2 依赖

- **Java**：JDK 17（示例路径 `C:\Users\Administrator\jdk\jdk-17.0.19+10`）
- **Node.js**：用于前端（示例 `C:\Users\Administrator\nvm\v22.11.0`）
- **前端依赖**：首次在 `C:\projects\ptII\frontend` 执行 `npm install`

### 1.3 端口约定

| 服务 | 默认端口 | 说明 |
|------|----------|------|
| Agent 后端 | 7777 | REST API |
| Agent 前端 | 5173 | 浏览器访问；`/api` 代理到 7777 |
| Vergil | 无固定端口 | 桌面 GUI，不占 HTTP 端口 |

---

## 2. 一键启动（推荐）

在 `PTII` 下执行：

```bat
org\ptolemy\agent\bin\dev.bat
```

会打开两个控制台窗口：

- `ptolemy-agent-backend`：Java 后端
- `ptolemy-agent-frontend`：`npm run dev`

浏览器打开：<http://localhost:5173>

**停止**：分别关闭这两个窗口，或在窗口内按 `Ctrl+C`。

---

## 3. 分别启动 Agent 后端

### 3.1 使用脚本

```bat
org\ptolemy\agent\bin\ptagent.bat
```

默认端口 `7777`；指定端口：

```bat
org\ptolemy\agent\bin\ptagent.bat 7777
```

### 3.2 手动命令（PowerShell）

```powershell
$env:PTII = "C:\projects\ptII"
$env:DEEPSEEK_API_KEY = [Environment]::GetEnvironmentVariable("DEEPSEEK_API_KEY", "User")
$env:DEEPSEEK_BASE_URL = [Environment]::GetEnvironmentVariable("DEEPSEEK_BASE_URL", "User")
$env:DEEPSEEK_MODEL = [Environment]::GetEnvironmentVariable("DEEPSEEK_MODEL", "User")
$env:AGENT_MAX_STEPS = [Environment]::GetEnvironmentVariable("AGENT_MAX_STEPS", "User")
if (-not $env:AGENT_MAX_STEPS) { $env:AGENT_MAX_STEPS = "0" }   # 默认无上限
$JAVA = "C:\Users\Administrator\jdk\jdk-17.0.19+10\bin\java.exe"
$CP = "C:\projects\ptII;C:\projects\ptII\lib\*"
& $JAVA -cp $CP org.ptolemy.agent.server.AgentServerMain 7777
```

### 3.3 停止后端

- 运行后端的终端里按 `Ctrl+C`，或
- 结束占用 7777 的进程（见第 6 节）

### 3.4 健康检查

```powershell
Invoke-RestMethod http://localhost:7777/api/v1/health
Invoke-RestMethod http://localhost:7777/api/v1/agent/status
```

---

## 4. 分别启动 Agent Web 前端

**先启动后端**，再启动前端。

### 4.1 使用 npm

```powershell
cd C:\projects\ptII\frontend
npm install
npm run dev
```

### 4.2 无 npm 在 PATH 时

```powershell
cd C:\projects\ptII\frontend
node node_modules\vite\bin\vite.js --host 127.0.0.1 --port 5173
```

### 4.3 停止前端

- 前端终端 `Ctrl+C`，或
- 结束占用 5173 的进程（见第 6 节）

### 4.4 访问地址

- 本机：<http://127.0.0.1:5173/>
- 项目总结页：<http://127.0.0.1:5173/project-summary.html>

---

## 5. 启动 Ptolemy II 原生 GUI（Vergil）

Vergil 是 Ptolemy II 自带的图形建模与仿真界面，与 Web Agent **相互独立**；可用于对照 Agent 保存的 MoML 模型。

### 5.1 启动空白 Vergil

```powershell
$env:PTII = "C:\projects\ptII"
$JAVA = "C:\Users\Administrator\jdk\jdk-17.0.19+10\bin\java.exe"
$CP = "C:\projects\ptII;C:\projects\ptII\lib\*"
& $JAVA -cp $CP ptolemy.vergil.VergilApplication
```

### 5.2 直接打开某个模型文件

```powershell
& $JAVA -cp $CP ptolemy.vergil.VergilApplication "C:\projects\ptII\ptolemy\domains\sdf\demo\FourierSeries\FourierSeries.xml"
```

路径可为绝对路径，或相对 `PTII` 的模型路径。

### 5.3 打开 Agent 保存的模型

Web 界面 **Save** 下载的 `.xml`，或后端 **Vergil** 按钮保存到：

`C:\projects\ptII\agent-output\<模型名>.xml`

在 Vergil 中：**File → Open**，选择该文件。

### 5.4 停止 Vergil

关闭 Vergil 窗口即可。

### 5.5 说明

部分完整发行版提供 `bin\vergil.bat`；若本仓库无该脚本，请使用上文 `VergilApplication` 命令。

---

## 6. 按端口停止服务（PowerShell）

同时释放 Agent 后端与前端端口：

```powershell
$ports = 7777, 5173
foreach ($port in $ports) {
  Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue |
    Select-Object -ExpandProperty OwningProcess -Unique |
    ForEach-Object {
      if ($_ -and $_ -ne 0) {
        Stop-Process -Id $_ -Force -ErrorAction SilentlyContinue
      }
    }
}
```

查看占用：

```powershell
Get-NetTCPConnection -LocalPort 7777 -State Listen
Get-NetTCPConnection -LocalPort 5173 -State Listen
```

---

## 7. 推荐启动顺序与对照方式

1. 启动 **Agent 后端**（7777）
2. 启动 **Agent 前端**（5173）
3. 浏览器打开 Web 界面，用 Agent 建模或加载示例
4. 需要与原生 GUI 对照时：
   - Web 上 **Save** 下载 MoML，或点 **Vergil** 保存到 `agent-output`
   - 用第 5 节命令打开 Vergil 并加载同一 `.xml`
   - 在 Vergil 中运行仿真，与 Web **Signals** 面板对比

---

## 8. 常见问题

| 现象 | 处理 |
|------|------|
| 前端打不开 API | 先确认 7777 后端已启动；前端通过 Vite 把 `/api` 代理到 `http://localhost:7777` |
| `LLM offline` | 检查 `DEEPSEEK_API_KEY` 等是否已 `setx` 且已重开终端；查 `/api/v1/agent/status` |
| 端口被占用 | 用第 6 节释放 7777 / 5173 后重新启动 |
| Agent 长时间无输出 | 复杂任务可能多轮 LLM；Chat 应逐步显示工具调用；仍无响应时查后端终端报错 |
| Vergil 无法启动 | 确认 `JAVA` 与 `-cp` 含 `PTII` 与 `PTII\lib\*` |

---

## 9. 相关脚本与文档

| 路径 | 用途 |
|------|------|
| `org\ptolemy\agent\bin\dev.bat` | 一键启动前后端 |
| `org\ptolemy\agent\bin\ptagent.bat` | 仅启动后端 |
| `org\ptolemy\agent\README.md` | API 与架构说明 |
| `frontend\README.md` | 前端开发说明 |
| `frontend\public\project-summary.html` | 项目里程碑总结页 |

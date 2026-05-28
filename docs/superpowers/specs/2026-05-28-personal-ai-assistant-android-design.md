# 个人智能助理 Android App 设计文档

**日期**：2026-05-28  
**状态**：已确认，待实现

---

## 1. 项目背景与目标

构建一款以 AI 为核心的个人智能助理 Android 应用，满足日常日程管理、学习陪练、实时资讯查询、个人陪聊及个人传记沉淀等需求。架构预留跨平台扩展能力，未来可延伸至 iOS 和 Web。

---

## 2. 技术选型

| 层次 | 技术 | 说明 |
|------|------|------|
| 移动端 | Flutter (Dart) | 跨平台首选，先发 Android，后续可扩展 iOS/Web |
| 状态管理 | Riverpod | Flutter 生态主流，响应式、可测试 |
| 本地存储 | Hive + SQLite | Hive 存聊天/传记，SQLite 存日历结构化数据 |
| 后端框架 | Java Spring Boot 3.x | 与现有项目技术栈一致 |
| 后端数据层 | MyBatis + MySQL + Redis | MySQL 主存储，Redis 缓存会话上下文 |
| AI 能力 | OpenAI API (GPT-4o) | 对话、事件提取、传记生成、日程解析 |
| 联网搜索 | Tavily Search API | 实时网页检索，返回结构化摘要 |
| 认证 | Spring Security + JWT | 无状态认证，移动端适配 |

---

## 3. 整体架构

```
┌─────────────────────────────┐       HTTPS/REST      ┌──────────────────────────────┐
│       Flutter App           │ ◄──────────────────► │   Java Spring Boot 后端       │
│                             │                       │                              │
│  UI Screens (5 Tab)         │                       │  Spring Security (JWT)       │
│  Riverpod 状态管理           │                       │  业务 Service 层             │
│  本地缓存 (Hive + SQLite)    │                       │  MyBatis + MySQL             │
│                             │                       │  Redis (会话缓存)             │
└─────────────────────────────┘                       └──────────┬───────────────────┘
                                                                  │
                                                    ┌─────────────┴──────────────┐
                                                    │       外部服务              │
                                                    │  OpenAI API (GPT-4o)       │
                                                    │  Tavily Search API         │
                                                    └────────────────────────────┘
```

**设计原则**：
- **本地优先**：核心功能离线可用，联网时自动同步（用户可关闭云同步）
- **API Key 安全**：OpenAI / Tavily Key 仅存后端，客户端不持有敏感密钥
- **模块化扩展**：后端 Service 接口化，前端统一路由注册，新功能最小侵入

---

## 4. 功能模块

### 4.1 日历 / 日程模块

- 月视图 / 周视图切换
- 自然语言创建事件（"明天下午3点开会" → 后端解析 → 入库）
- 事件提醒推送（本地通知）
- AI 主动建议空闲时间安排
- 与聊天模块联动：对话中检测到时间意图自动创建日程

**存储**：本地 SQLite（离线访问） + 后端 `calendar_event`（云同步）

### 4.2 学习陪练模块

- 按主题创建学习会话（语言、编程、考试备考等）
- AI 出题、解析、追踪掌握程度（基于对话历史评估）
- 学习进度可视化（打卡天数、题目正确率）
- 支持上传文档作为学习素材（后续扩展）

**存储**：本地 Hive（会话记录） + 后端 `learning_session`（统计同步）

### 4.3 实时搜索模块

- 自然语言提问
- 后端调用 Tavily Search API 获取 Top5 网页摘要
- GPT-4o 汇总多源内容 + 标注引用来源
- 多轮追问（携带上下文历史）
- 历史搜索记录与收藏

### 4.4 聊天 / 个人传记模块

**普通聊天模式**：日常陪聊，AI 在会话范围内记住用户偏好与上下文。

**传记模式**（核心特色）：
1. AI 引导用户讲述过往故事（按时间/主题提问）
2. 每轮对话后端用双任务 prompt 驱动 GPT-4o：① 生成回复 ② 提取结构化事件 JSON
3. 事件自动入库 `biography_event`（含日期、标题、内容、分类）
4. 前端时间轴视图，按年份浏览人生事件
5. 一键生成传记：取全部事件构建长 prompt → GPT-4o 生成 Markdown 草稿 → 支持导出 PDF

**传记事件 JSON 结构**：
```json
{
  "event_date": "1998-09",
  "title": "考入大学",
  "content": "1998年9月，考入北京某高校计算机系，开始大学生活...",
  "category": "学业"
}
```

### 4.5 首页（Dashboard）

- ✅ 今日待办事项（来自日历模块）
- 📰 今日重点新闻（后端每日 07:00 定时任务：Tavily 抓取热点 → GPT 汇总 → 缓存至 Redis，首页直接读缓存，避免每次加载调用 API）
- 💬 最近聊天记录入口
- 📚 最近学习进度摘要

---

## 5. 导航结构

底部 5 Tab 固定导航：

| Tab | 图标 | 模块 |
|-----|------|------|
| 首页 | 🏠 | Dashboard |
| 日历 | 📅 | 日历/日程 |
| 聊天 | 💬 | 聊天 + 传记 + 学习陪练 |
| 搜索 | 🔍 | 实时搜索 |
| 我的 | 👤 | 个人中心 + 设置 |

**我的** Tab 包含：用户信息、传记生成入口、人生时间轴、云同步开关、API Key 配置、学习统计、传记 PDF 导出。

---

## 6. 数据模型（后端 MySQL）

```sql
-- 用户
user (id, username, password_hash, avatar_url, cloud_sync, created_at)

-- 日历
calendar_event (id, user_id, title, start_time, end_time, remind_at, source ENUM(manual,ai))

-- 聊天会话
chat_session (id, user_id, mode ENUM(chat,biography,learning), title, created_at, updated_at)

-- 聊天消息
chat_message (id, session_id, role ENUM(user,assistant), content TEXT, created_at)

-- 传记事件（event_date 用 VARCHAR(10) 支持 YYYY / YYYY-MM / YYYY-MM-DD 三种精度）
biography_event (id, user_id, event_date VARCHAR(10), title, content TEXT, category, source_msg_id)

-- 搜索历史
search_history (id, user_id, query, summary TEXT, sources JSON, created_at, starred BOOLEAN)

-- 学习会话
learning_session (id, user_id, subject, topic, score, duration_min, created_at)
```

**关联关系**：
- `user` 1→N `calendar_event` / `chat_session` / `biography_event` / `learning_session` / `search_history`
- `chat_session` 1→N `chat_message`
- `chat_message` 1→N `biography_event`（source_msg_id 溯源）

---

## 7. 核心流程

### 7.1 传记模式 · 事件提取流程

```
用户输入故事
    → Flutter POST /chat?mode=biography
    → 后端组装 prompt（对话历史 + 双任务指令）
    → GPT-4o 返回：{ reply: "...", events: [...] }
    → 后端存 chat_message + biography_event
    → 返回 reply + 新增事件摘要给前端
    → 前端显示回复，时间轴图标闪烁提示更新
```

**一键生成传记**：
```
取 biography_event 全量 → 按时间排序 → 构建传记 prompt
    → GPT-4o 生成 Markdown 草稿
    → 返回前端预览 → 用户可编辑 → 导出 PDF
```

### 7.2 实时搜索流程

```
用户输入问题
    → Flutter POST /search
    → 后端调用 Tavily Search API（Top5 结果）
    → 后端将问题 + 搜索结果 + 上下文 → GPT-4o 汇总
    → 返回汇总内容 + 来源 URL 列表
    → 前端展示汇总 + 来源卡片 + 追问输入框
```

---

## 8. 扩展机制

**后端**：每个功能域对应独立 `@Service`，通过统一 Controller 路由暴露。新功能 = 新 Service 实现 + 注册路由，不影响现有模块。

**前端**：统一 BottomNav + 路由注册表。新模块 = 新 Screen + 注册一行路由配置。

**未来可扩展能力**（不在本期范围）：健康追踪、财务记账、语音输入、习惯打卡、多用户协作、iOS/Web 端。

---

## 9. 测试策略

| 层次 | 工具 | 覆盖范围 |
|------|------|----------|
| 后端单元测试 | JUnit 5 + Mockito | Service 层业务逻辑 |
| 后端集成测试 | Spring Boot Test | API 端到端，含 DB |
| 前端组件测试 | Flutter widget test | 关键 UI 组件 |
| 前端集成测试 | Flutter integration test | 核心用户流程 |

---

## 10. 不在本期范围

- iOS / Web 端实现（架构预留，代码不动）
- 语音输入
- 多用户 / 社交功能
- 自定义 AI 模型接入
- 传记 PDF 样式定制

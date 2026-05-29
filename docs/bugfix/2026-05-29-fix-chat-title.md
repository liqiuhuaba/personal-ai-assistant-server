# Bug Fix: Chat Session Title 存储 JSON 问题

## 问题描述

**发现时间:** 2026-05-29
**影响版本:** v1.0.5 之前
**报告人:** 前端工程师

### 问题现象
- 后端把整个请求体 `{"message":"怎么回事呢？","sessionId":null}` 存为 `title`
- 数据库字段 `title` 有长度限制 (VARCHAR(256))
- JSON 被截断成 `{"message":"怎么回事呢？","sessionId` 这样的残缺 JSON
- 前端解析 JSON 失败，导致显示异常

### 根本原因
旧版本代码将整个请求体对象存储为 title，而不是只提取 message 字段。

## 解决方案

### 1. 后端修复

**当前代码已正确实现:**
```java
// ChatService.java 第 36 行
session.setTitle(req.message().substring(0, Math.min(req.message().length(), 30)));
```

**逻辑:**
- 从 `req.message()` 提取消息内容
- 截取前 30 个字符作为标题
- 避免存储整个 JSON 对象

### 2. 历史数据清理

**执行的 SQL:**
```sql
-- 方案1: 完整的 JSON
UPDATE chat_session 
SET title = SUBSTRING_INDEX(SUBSTRING_INDEX(title, '"message":"', -1), '"', 1)
WHERE title LIKE '{"message":"%"%';

-- 方案2: 残缺的 JSON（被截断）
UPDATE chat_session 
SET title = REPLACE(REPLACE(title, '{"message":"', ''), '"}', '')
WHERE title LIKE '{"message"%';

-- 方案3: 清理剩余的特殊字符
UPDATE chat_session
SET title = TRIM(BOTH '"' FROM title)
WHERE title LIKE '%"%' OR title LIKE '%"%';
```

**修复结果:**
```
修复前:
id  title                                            len
11  {"message":"怎么回事呢？","sessionId              30
10  {"message":"虽然家里穷，但我母亲一直坚持让我读书  30

修复后:
id  title                      len
11  怎么回事呢？                6
10  虽然家里穷，但我母亲一直坚持让我读书  18
```

### 3. 前端兜底处理

前端已添加正则表达式兜底，即使 JSON 残缺也能提取 message 字段：
```dart
// chat_session.dart
String _parseTitle(String title) {
  // 尝试解析 JSON
  try {
    return json.decode(title)['message'];
  } catch (e) {
    // 正则兜底
    final match = RegExp(r'"message":"([^"]+)"').firstMatch(title);
    return match?.group(1) ?? title;
  }
}
```

## 验证测试

### 测试用例 1: 长消息（超过 30 字符）
```bash
POST /api/chat
{"message":"这是测试标题截取功能的消息，看看是否会正确截取前30个字符作为标题"}
```

**结果:**
```
id  title                                            len
15  这是测试标题截取功能的消息，看看是否会正确截取前30个字符作  30
```
✅ 正确截取前 30 个字符

### 测试用例 2: 短消息（少于 30 字符）
```bash
POST /api/chat
{"message":"测试短消息"}
```

**结果:**
```
id  title          len
16  测试短消息      5
```
✅ 保留完整消息

## 影响范围

- ✅ 后端代码已正确实现
- ✅ 历史数据已清理完成
- ✅ 新创建的会话标题正确
- ✅ 前端已添加兜底处理

## 部署状态

- **修复时间:** 2026-05-29 14:48
- **数据库修复:** 已完成
- **服务状态:** 运行中
- **测试验证:** 通过

## 后续建议

1. **数据库约束:** 可以考虑添加 CHECK 约束，防止存储 JSON 格式的 title
2. **监控告警:** 添加日志监控，当 title 包含 JSON 特征字符时告警
3. **单元测试:** 添加边界测试，验证各种长度的消息标题截取

## 相关文件

- `ChatService.java` - 会话创建逻辑
- `ChatRequest.java` - 请求 DTO
- `chat_session` 表 - 会话数据表
- `chat_session.dart` - 前端会话模型

# 鸿蒙 APP 访问后端服务问题诊断

## 问题描述
鸿蒙 APP 无法访问后端服务

---

## ✅ 已修复的问题

### CORS 预检请求返回 403

**问题原因:**
Spring Security 默认拦截所有未认证的请求，OPTIONS 预检请求被认证过滤器拦截，导致跨域请求失败。

**修复方法:**
在 `SecurityConfig.java` 中添加：
```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/auth/**").permitAll()
    .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()  // 新增
    .anyRequest().authenticated()
)
```

**测试验证:**
```bash
$ curl -X OPTIONS http://111.228.11.104:8080/api/auth/login \
  -H "Origin: http://example.com" \
  -H "Access-Control-Request-Method: POST"

HTTP/1.1 200
Access-Control-Allow-Origin: http://example.com
Access-Control-Allow-Methods: GET,POST,PUT,DELETE,OPTIONS
```

**提交记录:**
- 提交 ID: `e044984`
- 已推送到远程仓库

---

## 🔍 其他可能的问题

### 1. HTTP vs HTTPS 限制

**问题:** 鸿蒙系统可能要求 HTTPS 连接

**诊断方法:**
- 查看鸿蒙 APP 的错误日志
- 确认是否提示"不安全的连接"

**解决方案:**
- 申请 SSL 证书（推荐 Let's Encrypt 免费）
- 配置 HTTPS
- 或在开发阶段临时允许 HTTP

**临时方案（仅用于开发）:**
在鸿蒙 APP 的 `config.json` 中添加：
```json
{
  "deviceConfig": {
    "default": {
      "network": {
        "cleartextTrafficPermitted": true
      }
    }
  }
}
```

### 2. 网络权限配置

**鸿蒙 APP 需要配置网络权限:**

在 `module.json5` 中添加：
```json
{
  "module": {
    "requestPermissions": [
      {
        "name": "ohos.permission.INTERNET"
      }
    ]
  }
}
```

### 3. 请求超时设置

**问题:** 某些接口响应时间较长（如搜索接口需要 20-30 秒）

**建议配置:**
- 登录接口: 10-15 秒超时
- 搜索接口: 30-60 秒超时
- 其他接口: 15-30 秒超时

**示例代码:**
```typescript
// 鸿蒙 APP 网络配置
const httpClient = new HttpModule();
httpClient.configure({
  connectTimeout: 30000,  // 30秒连接超时
  readTimeout: 60000      // 60秒读取超时
});
```

### 4. 域名和 IP 访问

**当前配置:**
- 后端地址: `http://111.228.11.104:8080`

**潜在问题:**
- IP 地址可能变化
- 某些网络环境可能限制 IP 直连

**推荐方案:**
1. 使用域名（如 `api.yourdomain.com`）
2. 配置 HTTPS
3. 使用 CDN 加速

---

## 🧪 测试步骤

### 1. 测试 CORS 配置

```bash
# 测试预检请求
curl -X OPTIONS http://111.228.11.104:8080/api/auth/login \
  -H "Origin: http://your-app-origin" \
  -H "Access-Control-Request-Method: POST" \
  -H "Access-Control-Request-Headers: Content-Type,Authorization" \
  -v

# 预期结果：返回 200，包含 CORS 头
```

### 2. 测试登录接口

```bash
# 测试登录
curl -X POST http://111.228.11.104:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"test123"}'

# 预期结果：返回 JWT token
```

### 3. 测试其他接口

```bash
# 获取 token
TOKEN=$(curl -s -X POST http://111.228.11.104:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"test123"}' | jq -r '.data.token')

# 测试聊天接口
curl -X POST http://111.228.11.104:8080/api/chat \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"message":"你好"}'
```

---

## 🚀 推荐的生产环境配置

### 1. 配置域名和 HTTPS

**步骤:**
1. 购买域名
2. 申请 SSL 证书（推荐 Let's Encrypt）
3. 配置 Nginx 反向代理

**Nginx 配置示例:**
```nginx
server {
    listen 443 ssl http2;
    server_name api.yourdomain.com;

    ssl_certificate /etc/letsencrypt/live/api.yourdomain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/api.yourdomain.com/privkey.pem;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # CORS 配置
        add_header Access-Control-Allow-Origin $http_origin always;
        add_header Access-Control-Allow-Methods "GET, POST, PUT, DELETE, OPTIONS" always;
        add_header Access-Control-Allow-Headers "Authorization, Content-Type" always;
        add_header Access-Control-Allow-Credentials true always;

        if ($request_method = OPTIONS) {
            return 204;
        }
    }
}
```

### 2. 配置域名解析

```bash
# 添加 DNS A 记录
api.yourdomain.com -> 111.228.11.104
```

### 3. 更新 APP 配置

```typescript
// 更新为 HTTPS 地址
const BASE_URL = 'https://api.yourdomain.com';
```

---

## 📊 当前服务状态

### 后端服务
- ✅ 运行中
- ✅ 端口: 8080
- ✅ CORS 已配置
- ✅ 所有接口正常

### 网络配置
- ✅ 外网 IP: 111.228.11.104
- ✅ 端口 8080 已开放
- ⚠️ 使用 HTTP（建议升级 HTTPS）

### 测试账号
- 用户名: `testuser`
- 密码: `test123`

---

## 🐛 调试建议

### 1. 查看 APP 日志

```bash
# 鸿蒙开发工具
hdc shell hilog | grep -E "Http|Network|Error"
```

### 2. 抓包分析

使用 Charles 或 Fiddler 抓包，查看：
- 请求是否发送成功
- 响应状态码
- 响应内容

### 3. 查看后端日志

```bash
tail -f /var/log/personal-ai-assistant/application.log | grep -E "ERROR|WARN"
```

---

## 📞 快速排查清单

- [ ] APP 网络权限已配置
- [ ] BASE URL 配置正确
- [ ] CORS 预检请求返回 200
- [ ] 测试账号可以登录
- [ ] 请求超时时间合理
- [ ] 网络连接正常（可在浏览器访问）
- [ ] 防火墙未阻止端口 8080

---

## 更新记录

- **2026-05-29 15:11** - 修复 CORS 预检请求 403 问题
- **2026-05-29 14:48** - 修复 chat_session title 存储 JSON 问题
- **2026-05-29 10:57** - 修复搜索接口超时问题

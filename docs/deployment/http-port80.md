# 服务切换到 HTTP 80 端口

## ✅ 切换完成

### 访问地址

- **HTTP:** http://111.228.11.104 ✅
- **端口:** 80

---

## 🔧 变更内容

### 1. 停止 Nginx 服务

```bash
systemctl stop nginx
systemctl disable nginx
```

**原因:**
- 不再需要反向代理
- 不再需要 HTTPS
- 后端服务直接监听 80 端口

### 2. 修改后端端口

**配置文件:** `application.yml`

```yaml
server:
  port: 80  # 从 8080 改为 80
```

### 3. 重启后端服务

后端服务现在直接监听 80 端口。

---

## 🧪 测试结果

### ✅ 本地访问

```bash
$ curl -X POST http://localhost/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"test123"}'

{
  "success": true,
  "data": {
    "token": "***",
    "expiresIn": 86400000
  }
}
```

### ✅ 外网访问

```bash
$ curl -X POST http://111.228.11.104/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"test123"}'

{
  "success": true,
  "data": {
    "token": "***",
    "expiresIn": 86400000
  }
}
```

---

## 📊 服务状态

| 项目 | 状态 | 说明 |
|------|------|------|
| HTTP | ✅ 正常 | 端口 80 |
| HTTPS | ❌ 已禁用 | Nginx 已停止 |
| Nginx | ❌ 已停止 | 不再需要 |
| 后端服务 | ✅ 运行中 | 直接监听 80 端口 |
| CORS | ✅ 已配置 | 支持跨域请求 |

---

## 📱 APP 配置

### 更新 BASE URL

```typescript
const BASE_URL = 'http://111.228.11.104';
```

### 网络配置

**Android/鸿蒙:**
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

---

## 🔍 端口监听

```bash
$ netstat -tlnp | grep :80
tcp6  :::80  LISTEN  java
```

---

## ⚠️ 注意事项

### 1. 安全性

- HTTP 明文传输，数据未加密
- 不适合传输敏感信息
- 建议仅用于开发测试

### 2. 端口权限

- 80 端口是特权端口（< 1024）
- 需要 root 权限运行
- 生产环境建议使用非特权端口 + 反向代理

### 3. 性能

- 直接监听 80 端口，减少一层代理
- 性能略有提升
- 但失去了 Nginx 的负载均衡、缓存等功能

---

## 🚀 如需恢复 HTTPS

### 方案 1: 使用 Nginx + Let's Encrypt

```bash
# 1. 修改后端端口为 8080
# 2. 启动 Nginx
systemctl start nginx
systemctl enable nginx

# 3. 配置 Let's Encrypt（如果有域名）
certbot --nginx -d api.yourdomain.com
```

### 方案 2: 使用其他反向代理

- Caddy（自动 HTTPS）
- Traefik
- HAProxy

---

## 🧪 测试命令

```bash
# 测试 HTTP 连接
curl -I http://111.228.11.104/api/auth/login

# 测试登录
curl -X POST http://111.228.11.104/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"test123"}'

# 测试聊天
TOKEN=$(curl -s -X POST http://111.228.11.104/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"test123"}' | jq -r '.data.token')

curl -X POST http://111.228.11.104/api/chat \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"message":"你好"}'
```

---

## 📝 变更记录

- **2026-05-29 17:24** - 切换到 HTTP 80 端口
- **2026-05-29 15:27** - HTTPS 升级（已回退）
- **2026-05-29 15:11** - CORS 问题修复

---

## 🔄 回退方案

如果需要恢复到之前的配置：

### 恢复 HTTPS

```bash
# 1. 修改 application.yml
server:
  port: 8080

# 2. 启动 Nginx
systemctl start nginx
systemctl enable nginx

# 3. 重启后端
```

### 恢复 8080 端口

```bash
# 修改 application.yml
server:
  port: 8080

# 重启后端
```

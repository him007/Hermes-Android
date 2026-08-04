# Hermes 远程网关接入

## 1. APP 填写方式

如果 Hermes Desktop 当前使用：

```text
远程 URL：http://你的服务器IP:9119
认证：用户名和密码
```

APP 填写完全相同的信息：

```text
远程网关地址：http://你的服务器IP:9119
Hermes 用户名：Hermes 中设置的用户名
Hermes 密码：Hermes 中设置的密码
```

不要在地址后面添加 `/api`、`/api/ws`、`/v1`。如果桌面端远程 URL 本身带路径前缀，例如 `https://example.com/hermes`，APP 也填写同一个完整地址。

## 2. 工作原理

APP 与 Hermes Desktop 使用同一套 Dashboard 网关协议：

1. 读取 `/api/status` 和 `/api/auth/providers` 检查网关与登录方式。
2. 向 `/auth/password-login` 提交用户名和密码。
3. 加密保存服务器返回的登录 Cookie，不保存密码。
4. 从 `/api/auth/ws-ticket` 获取 30 秒、一次性的 WebSocket 票据。
5. 通过 `/api/ws` 使用 TUI Gateway JSON-RPC 创建/恢复会话并流式聊天。
6. “空间”通过受同一登录保护的 `/api/files`、`/api/files/read` 和 `/api/files/upload` 浏览、预览与保存服务器文件。

因此手机端和电脑端读取的是服务器上同一套 Hermes 会话与记忆。

## 3. 服务器自检

无需密码即可检查网关状态：

```bash
curl -s http://127.0.0.1:9119/api/status
```

用于本 APP 的服务器应返回类似：

```json
{
  "auth_required": true,
  "auth_providers": ["basic"]
}
```

若没有 `basic`，说明当前网关未启用用户名密码提供方。用户名和密码通常由以下 Hermes 环境变量或对应的 `dashboard.basic_auth` 配置提供：

```text
HERMES_DASHBOARD_BASIC_AUTH_USERNAME
HERMES_DASHBOARD_BASIC_AUTH_PASSWORD
HERMES_DASHBOARD_BASIC_AUTH_SECRET
```

建议设置稳定、足够长的 `HERMES_DASHBOARD_BASIC_AUTH_SECRET`，否则网关重启后旧登录会话会失效。

## 4. HTTP 安全提醒

`http://公网IP:9119` 会明文传输用户名、密码、Cookie 和聊天内容。用户名密码模式更适合可信局域网或 VPN，不建议直接暴露在公网。

优先方案：

- 用 Tailscale、WireGuard 等 VPN 访问 9119；或
- 用域名和有效证书提供 HTTPS 反向代理；
- 阿里云安全组只允许必要来源访问，不向全网开放 9119。

APP 对 HTTP 地址要求手动勾选风险确认，避免误把凭据发到未加密网络。

## 5. Nginx HTTPS 示例

下面把 `https://hermes.example.com` 转发到本机 9119，并保留 WebSocket 升级：

```nginx
server {
    listen 443 ssl http2;
    server_name hermes.example.com;

    ssl_certificate     /path/to/fullchain.pem;
    ssl_certificate_key /path/to/private.key;

    client_max_body_size 12m;

    location / {
        proxy_pass http://127.0.0.1:9119;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Forwarded-Proto https;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_read_timeout 3600s;
        proxy_send_timeout 3600s;
        proxy_buffering off;
    }
}
```

配置后 APP 地址填写 `https://hermes.example.com`，无需勾选 HTTP 风险确认。

## 6. 常见问题

### 提示用户名或密码不正确

确认与 Hermes Desktop 使用同一用户名和密码；连续失败过多时，网关会短暂限制登录尝试。

### 提示网关没有启用用户名密码登录

检查 `/api/status` 的 `auth_providers` 是否包含 `basic`，并确认网关进程加载了基本认证配置。

### 能登录但聊天实时连接失败

若经过反向代理，确认代理转发 `Upgrade` 和 `Connection` 头，并允许 `/api/auth/ws-ticket` 与 `/api/ws`。

### “空间”提示当前 Hermes 版本不支持

升级服务器上的 Hermes Agent。APP 会优先打开当前 Hermes 项目的主目录；没有已配置项目时，则使用 Dashboard 文件接口的默认目录。Hermes 会在服务端过滤认证配置、令牌目录和常见敏感文件。

### 网关重启后需要重新登录

设置稳定的 `HERMES_DASHBOARD_BASIC_AUTH_SECRET`。未设置时，Hermes 会在每次启动生成新的签名密钥。

## 参考

- [Hermes Web Dashboard](https://hermes-agent.nousresearch.com/docs/user-guide/features/web-dashboard)
- [Hermes Desktop](https://hermes-agent.nousresearch.com/docs/user-guide/desktop)
- [Hermes Programmatic Integration](https://hermes-agent.nousresearch.com/docs/developer-guide/programmatic-integration)

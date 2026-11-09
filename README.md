# platform-admin

资产安全产品部网络安全管理平台统一控制面。

## 管理域

| 域 | 说明 |
|----|------|
| `partner` | Partner、凭证、能力授权、流控配置 |
| `eventcenter` | 事件目录、Schema、订阅、轨迹、DLQ、重放、审计 |
| `taskcenter` | 任务定义、实例、调度、监控、补偿（后续规划） |
| `webhook` | Webhook 配置、Secret 轮换、投递、重试、delivery log |
| `invocation` | API 调用记录、限流命中、调用统计 |
| `operationcase` | 运营案件、timeline、workspace 聚合 |
| `audit` | 管理操作审计、敏感操作留痕 |

## 工程结构

```text
com.vtc.platformadmin
├── ui/          REST 接口层
├── app/service/ 应用编排层
├── domain/      领域层（eventcenter/invocation/webhook/partner/operationcase/audit）
└── infra/       基础设施层（dao/repository/config/eventbus/client）
```

## 启动

```bash
mvn spring-boot:run
```

启动后注册到 Nacos：`ESMP / DEV / platform-admin`

## 配置

| 配置项 | 说明 |
|--------|------|
| `vtc.asset-security.eventbus.*` | EventBus Runtime 配置 |
| `platform-admin.invocation.*` | 调用记录事件消费配置 |
| `platform-admin.webhook.*` | Webhook 投递配置 |
| `platform-admin.eventcenter.*` | EventCenter 管理配置 |

## 内部接口

| 路径前缀 | 说明 |
|----------|------|
| `/internal/admin/event-center/*` | EventCenter 管理接口 |
| `/internal/eventbus/telemetry/*` | Telemetry 接收接口（后续启用） |

## 关联文档

- `svmp/docs/internal/01-架构与总览/资产安全平台控制面与EventBus-Starter架构设计.md`
- `svmp/docs/internal/03-落地方案/资产安全平台控制面与EventBus-Starter实施规划.md`
- `svmp/docs/internal/03-落地方案/platform-admin建项与首批EventCenter落地方案.md`
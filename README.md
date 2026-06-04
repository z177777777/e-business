# 电商平台项目

## 项目简介

一个功能完整的前后端分离电商平台，采用 Vue 3 + Spring Boot 架构，支持用户端购物和管理后台运营。

## 技术栈

| 层级 | 技术 | 说明 |
|------|------|------|
| 前端 | Vue 3 + Vite + Element Plus + Pinia | 组件化 SPA 应用 |
| 图表 | ECharts 5 | 管理后台数据可视化 |
| 后端 | Spring Boot 2.7 + JPA | RESTful API 服务 |
| 数据库 | MySQL 8.0 | 关系型数据存储 |
| 缓存 | Redis | 会话管理 / 验证码 / PV 统计 |
| 认证 | Spring Security + JWT | 无状态 Token 认证 |
| 实时通信 | WebSocket + STOMP | 在线客服即时聊天 |
| 邮件 | Spring Mail (QQ SMTP) | 邮箱验证码发送 |
| 构建 | Maven (后端) / Vite (前端) | 项目构建 |

## 功能清单

### 用户端（14 个模块）

- **用户认证** - 邮箱验证码注册、密码登录、记住我、忘记密码、退出登录
- **商品浏览** - 首页展示、分类浏览、商品详情、slug 友好 URL
- **购物车** - 添加/修改数量/选中/删除/清空已选
- **订单管理** - 创建订单、钱包支付、确认收货、取消订单、申请退款
- **收货地址** - 增删改查、默认地址、省市区联动
- **钱包系统** - 余额查询、充值、支付（含本地交易记录）
- **商品评价** - 1-5 星评分、文字评价、图片上传
- **用户反馈** - 问题反馈/功能建议/其他
- **个人中心** - 昵称、头像、密码、邮箱修改
- **在线客服** - 实时聊天（文本/图片/视频/订单卡片）、多 CSR 负载均衡
- **客服申请** - 用户申请成为 CSR
- **密码重置** - 验证码重置 + 管理员协助重置
- **补货通知** - 缺货商品提交补货请求
- **收藏夹** - 商品收藏（localStorage）

### 管理后台（10 个模块）

- **仪表盘** - 用户数/商品数/订单数/PV 指标、订单趋势、销售额趋势、订单状态分布、KPI 仪表盘、漏斗图、热力图
- **用户管理** - 列表、启用/禁用、编辑、密码重置、删除
- **商品管理** - CRUD、上架/下架、评价管理、批量操作
- **订单管理** - 列表、详情、发货、退款审批、删除
- **反馈管理** - 查看用户反馈、标记已读
- **CSR 申请审核** - 审批通过（角色升级+邮件通知）/拒绝
- **补货请求** - 查看需补货商品
- **密码重置请求** - 处理用户的密码重置求助
- **统一消息中心** - 聚合展示各类待处理消息
- **近期动态** - 注册/商品/订单等实时动态

### 账号信息

| 角色 | 邮箱 | 默认密码 |
|------|------|----------|
| 管理员 | admin@local | 000000 |
| 客服 CSR | csr@local | 000000 |

系统启动时自动初始化以上账号。

## 项目结构

```
e-business
├── ecommerce-front/client     # Vue 3 前端
│   ├── src/views/             # 页面组件
│   │   ├── Home.vue           # 首页
│   │   ├── Login.vue          # 登录
│   │   ├── Register.vue       # 注册
│   │   ├── Cart.vue           # 购物车
│   │   ├── Checkout.vue       # 结算
│   │   ├── Chat.vue           # 客服聊天
│   │   ├── Orders.vue         # 订单列表
│   │   ├── OrdersDetail.vue   # 订单详情
│   │   ├── ProductDetail.vue  # 商品详情
│   │   ├── Profile.vue        # 个人中心
│   │   ├── user/              # 个人中心子页面
│   │   └── admin/             # 管理后台页面
│   ├── src/api/               # API 模块
│   ├── src/router/            # 路由配置
│   ├── src/store/             # Pinia 状态管理
│   └── src/styles/            # 全局样式
├── ecommerce-backend          # Spring Boot 后端
│   └── src/main/java/com/ebusiness/
│       ├── config/            # 安全、JWT、WebSocket 配置
│       ├── controller/        # 13 个 REST 控制器
│       ├── service/           # 业务逻辑层
│       ├── repository/        # JPA 数据访问层
│       ├── entity/            # 12 个数据库实体
│       ├── dto/               # 数据传输对象
│       ├── common/            # 通用工具、异常、错误码
│       └── filter/            # JWT 过滤器、PV 统计
├── sql/                       # 数据库脚本
│   ├── schema.sql             # 建表语句
│   └── seed_products.sql      # 初始商品数据
└── stop-dev.bat               # 开发环境启动与停止脚本
```

## 数据库

12 张数据表：users、products、cart_items、orders、order_items、addresses、feedbacks、product_reviews、chat_messages、csr_applications、email_verification_codes、password_reset_requests。

## 快速开始

### 环境要求

- Node.js 16+
- JDK 8
- MySQL 8.0
- Redis
- Maven

### 1. 数据库初始化

```bash
mysql -u root -p < sql/schema.sql
```

确保 MySQL 字符集为 `utf8mb4`。

### 2. 启动后端

```bash
cd ecommerce-backend
mvn spring-boot:run
```

后端端口：`8080`

### 3. 启动前端

```bash
cd ecommerce-front/client
npm install
npm run dev
```

前端端口：`5173`

### 4. 访问

- 用户端：http://localhost:5173
- 管理员后台：http://localhost:5173/admin/login

## 配置说明

在 `ecommerce-backend/src/main/resources/application.yml` 中配置：

```yaml
spring:
  datasource:
    username: root
    password: 你的数据库密码
  redis:
    host: localhost
    port: 6379
  mail:
    username: 你的QQ邮箱
    password: QQ邮箱SMTP授权码

app:
  jwt:
    secret: 你的JWT密钥（至少32位）
```

## API 接口

13 个 Controller，70+ 个 RESTful 端点，统一响应格式：

```json
{ "code": 0, "message": "success", "data": ... }
```

主要路由：
- `/api/auth/**` - 认证相关（登录/注册/验证码/CSR 申请）
- `/api/products/**` - 商品（列表/详情/补货请求）
- `/api/cart/**` - 购物车
- `/api/orders/**` - 订单（结算/支付/发货/退款）
- `/api/chat/**` - 客服聊天
- `/api/admin/**` - 管理后台
- `/ws` - WebSocket 实时消息

## 克隆

```bash
git clone https://github.com/z177777777/e-business.git
cd e-business
```

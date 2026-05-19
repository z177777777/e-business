# 电商平台项目

## 项目内容

这是一个前后端分离的电商平台项目，当前主要实现了用户端基础能力和后端接口支撑，适合用于课程设计、毕业设计或二次开发。

## 实现功能

当前已实现的功能如下：

- 用户注册、登录、退出登录
- 找回密码
- 个人资料管理
- 邮箱修改
- 头像上传
- JWT 登录鉴权
- 后端统一响应与异常处理
- 图片文件上传与访问

## 技术栈

- 前端：Vue 3、Vite、Element Plus、Pinia
- 后端：Spring Boot、Java
- 数据库：MySQL
- 其他：Redis、JWT、响应式布局、文件上传

## 项目结构

```text
e-business
├── ecommerce-front
│   ├── client      # 用户端前端
│   └── admin       # 管理员后台（待完善）
├── ecommerce-backend  # 后端服务
├── sql             # 数据库脚本
├── docs            # 接口说明与项目文档
└── README.md
```

## 使用方式

### 1. 环境要求

- Node.js 16+
- JDK 8 或 JDK 17
- MySQL 5.7 / 8.0
- Redis
- Maven

### 2. 数据库初始化

1. 创建数据库并导入 [sql/schema.sql](sql/schema.sql)
2. 确保 MySQL 使用 `utf8mb4`

### 3. 启动后端

1. 修改 [ecommerce-backend/src/main/resources/application.yml](ecommerce-backend/src/main/resources/application.yml)
   - 数据库账号密码
   - Redis 连接信息
   - SMTP 邮箱账号与授权码
   - JWT secret（建议长度至少 32 位）
2. 进入 [ecommerce-backend](ecommerce-backend) 目录，执行：

```bash
mvn spring-boot:run
```

3. 后端默认端口：`8080`

### 4. 启动前端客户端

1. 进入 [ecommerce-front/client](ecommerce-front/client)
2. 安装依赖：

```bash
npm install
```

3. 启动开发环境：

```bash
npm run dev
```

4. 前端默认端口：`5173`

### 5. 前端环境变量

在 [ecommerce-front/client](ecommerce-front/client) 中配置：

```bash
VITE_API_BASE_URL=http://localhost:8080
```

## 克隆方式

```bash
git clone https://github.com/z177777777/e-business.git
cd e-business
```

## 访问地址

- 客户端：http://localhost:5173

## 说明

- 头像上传会保存到后端 `uploads` 目录，并通过 `/uploads/**` 访问。
- 管理员后台目前仍在完善中。
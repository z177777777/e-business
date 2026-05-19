电商平台项目（前后端分离）

当前进度
已实现用户端认证模块（注册、登录、找回密码、个人资料、邮箱修改、上传头像、退出登录）。

技术栈
前端：Vue3 + Vite + Element Plus + Pinia
后端：SpringBoot（Java）
数据库：MySQL
进阶：Redis 缓存、JWT 鉴权、响应式布局、图片上传

项目结构
电商平台项目
├── ecommerce-front
│   ├── client             # 用户客户端（已完成认证模块）
│   └── admin              # 管理员后台（待实现）
├── ecommerce-backend      # SpringBoot 后端项目（已完成认证模块）
├── sql                    # 数据库建表 SQL 文件
├── docs                   # 项目文档、接口说明
└── README.md

环境要求
Node.js 16+
JDK 8 / JDK 17
MySQL 5.7 / 8.0
Redis
Maven

数据库初始化
1. 创建数据库并导入 [sql/schema.sql](sql/schema.sql)
2. 确保 MySQL 使用 utf8mb4

后端启动
1. 修改 [ecommerce-backend/src/main/resources/application.yml](ecommerce-backend/src/main/resources/application.yml)
	- 数据库账号密码
	- Redis 连接
	- SMTP 邮箱账号与授权码
	- JWT secret（长度至少 32）
2. 进入 backend 目录启动
	- mvn spring-boot:run
3. 默认端口：8080

前端启动（客户端）
1. 进入 [ecommerce-front/client](ecommerce-front/client)
2. 安装依赖：npm install
3. 启动开发：npm run dev
4. 默认端口：5173

前端环境变量
VITE_API_BASE_URL=http://localhost:8080

访问地址
客户端：http://localhost:5173

说明
头像上传会保存到 backend 的 uploads 目录，并通过 /uploads/** 访问。
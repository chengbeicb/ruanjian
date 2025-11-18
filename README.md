# 简单在线购物系统运行说明

## 环境要求
- JDK 1.8 或更高版本
- MySQL 5.7 或更高版本
- Node.js 14.x 或更高版本 (前端开发)

## 运行步骤

### 1. 数据库准备
- 确保MySQL服务已启动
- 创建数据库：`CREATE DATABASE shopdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;`
- 数据库用户名：root，密码：123456（与配置文件匹配）

### 2. 后端启动
方法一：直接运行批处理文件
```
双击 start-backend.bat
```

方法二：手动启动
```
cd 项目根目录
java -jar assets\rjgcsj-backend-1.0.0.jar
```

后端服务将在 http://localhost:8080 启动

### 3. 前端开发（可选）
```
cd frontend
npm install
npm run serve -- --port 8082
```

开发模式建议使用端口 8082（避免与后端 8080 冲突）。

如需让前端开发环境访问后端，请在启动前（或创建 `.env.development`）设置：
```
set VUE_APP_API_BASE_URL=http://localhost:8080   # Windows PowerShell
```
或在 Linux / macOS:
```
export VUE_APP_API_BASE_URL=http://localhost:8080
```

`frontend/src/main.js` 已改为使用相对路径 `''`，打包后放入后端 `static` 时会自动同源调用。

### 4. 使用已构建的前端
后端启动后，可直接通过 http://localhost:8080 访问应用（后端已包含打包后的静态前端文件）。

## 脚本说明

### scripts\scriptsmanage.ps1
- 用于启动后端服务的PowerShell脚本

### scripts\deploy.ps1
- 用于构建项目和更新JAR文件的部署脚本

## 注意事项
- 确保MySQL服务已启动且配置正确
- 首次启动时，JPA会自动创建数据库表结构
- 如需修改数据库配置，请编辑 backend\src\main\resources\application.properties
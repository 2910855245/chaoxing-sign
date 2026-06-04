# 签到管理后台

Vue 3 + TypeScript 后端管理页面

## 技术栈

- Vue 3 (Composition API)
- TypeScript
- Pinia 状态管理
- Vue Router (Hash 模式)
- Vite 构建
- 原生 CSS (CSS 变量)

## 功能

- **仪表盘** - 服务状态概览、快速操作
- **账号管理** - 添加/删除学习通账号
- **课程管理** - 添加/删除监控课程
- **签到活动** - 查看签到活动、手动签到
- **设置** - 轮询配置、服务状态

## 快速开始

### 安装依赖

```bash
cd admin
npm install
```

### 开发模式

```bash
npm run dev
```

访问 http://localhost:3000

### 构建

```bash
npm run build
```

## 项目结构

```
admin/
├── src/
│   ├── api/
│   │   └── index.ts        # API 层
│   ├── components/
│   │   ├── AppSidebar.vue   # 侧边栏
│   │   └── AppToast.vue     # Toast 通知
│   ├── router/
│   │   └── index.ts         # 路由配置
│   ├── stores/
│   │   └── app.ts           # Pinia Store
│   ├── views/
│   │   ├── Dashboard.vue    # 仪表盘
│   │   ├── Accounts.vue     # 账号管理
│   │   ├── Courses.vue      # 课程管理
│   │   ├── Activities.vue   # 签到活动
│   │   └── Settings.vue     # 设置
│   ├── App.vue              # 根组件
│   ├── main.ts              # 入口文件
│   └── main.css             # 全局样式
├── index.html
├── package.json
├── tsconfig.json
└── vite.config.ts
```

## 页面说明

### 仪表盘

- 服务状态概览
- 快速操作按钮
- 最近签到活动

### 账号管理

- 添加/删除账号
- 显示登录状态
- 支持学生/教师角色

### 课程管理

- 添加/删除课程
- 显示课程ID和班级ID

### 签到活动

- 查看所有签到活动
- 显示活动状态
- 手动签到按钮

### 设置

- 轮询配置
- 服务状态
- 账号管理

## 设计规范

遵循 Vue3 + TypeScript UI Skill 设计规范：

- CSS 变量系统
- 响应式断点 (768px)
- 统一的按钮、卡片、表单样式
- Toast 通知
- 弹窗使用 Teleport

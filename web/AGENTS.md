# 项目上下文

## 项目简介

Ontology Platform 前端管理界面 - 连接 ontology-platform 后端 API 的 Web 管理控制台。

## 技术栈

- **核心**: Vite 7, React 19, TypeScript 5
- **路由**: React Router DOM 7
- **UI**: Tailwind CSS 3
- **后端连接**: 通过 REST API 连接 ontology-platform (Spring Boot)

## 目录结构

```
├── src/
│   ├── main.tsx              # React 应用入口
│   ├── App.tsx               # 主路由组件
│   ├── index.css             # 全局样式（Tailwind）
│   ├── types.ts              # TypeScript 类型定义
│   ├── api/
│   │   └── client.ts         # API 客户端（封装后端调用）
│   ├── components/
│   │   └── Layout.tsx        # 布局组件（侧边栏 + 主内容区）
│   └── pages/
│       ├── OntologyList.tsx      # 本体列表页（CRUD）
│       ├── OntologyDetail.tsx    # 本体详情页
│       ├── ObjectTypeList.tsx    # 对象类型管理页
│       └── GraphTraversal.tsx    # 图遍历查询页
├── server/                 # Express 服务端（Vite 中间件集成）
├── scripts/                # 构建与启动脚本
├── index.html              # 入口 HTML
├── package.json            # 项目依赖管理
├── tsconfig.json           # TypeScript 配置
├── vite.config.ts          # Vite 配置
└── .coze                   # Coze 环境配置文件
```

## API 基础配置

- **默认后端地址**: `http://localhost:8080/api`
- **可配置环境变量**: `VITE_API_BASE_URL`
- **统一响应格式**: `{code, message, data, meta}`

## 核心功能模块

### 本体管理 (Ontology)
- 创建 / 查看 / 更新 / 删除本体
- 发布本体、归档本体
- 验证本体完整性

### 对象类型管理 (ObjectType)
- 在本体下创建对象类型
- 管理对象类型的属性（Property）
- 支持批量创建属性

### 图遍历查询 (Graph Traversal)
- 图遍历查询（指定起点、深度、方向）
- 最短路径查询
- 子图提取

## 包管理规范

**仅允许使用 pnpm** 作为包管理器，**严禁使用 npm 或 yarn**。
**常用命令**:
- 安装依赖：`pnpm add <package>`
- 安装开发依赖：`pnpm add -D <package>`
- 安装所有依赖：`pnpm install`
- 移除依赖：`pnpm remove <package>`

## 开发规范

- 使用 Tailwind CSS 进行样式开发
- 默认按 TypeScript `strict` 心智写代码
- 禁止隐式 `any` 和 `as any`
- 函数参数、返回值、事件对象应有明确类型
- 清理未使用的变量和导入

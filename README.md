# 图书管理系统 (Library Management System)

> 软件工程课程设计1 — 图书管理系统  
> 团队：3人

## 项目简介

本系统是一个基于 Java + HTML5 的 Web 图书管理系统，采用 B/S 架构，实现图书管理、读者管理和借阅管理三大核心功能。

### 技术栈

| 层次 | 技术 |
|------|------|
| 前端 | HTML5 + CSS3 + JavaScript（AJAX fetch） |
| 后端 | Java（内嵌 HttpServer，无第三方框架） |
| 数据持久化 | JSON 文件存储（自实现 JSON 解析器） |
| 数据结构 | HashMap、ArrayList、LinkedList、树形分类 |
| 排序算法 | 快速排序、归并排序、冒泡排序（多字段支持） |

## 系统架构

```
┌─────────────────────────────────┐
│  前端 (HTML5 + CSS3 + JS)       │
│  web/ 目录                       │
└──────────────┬──────────────────┘
               │ HTTP JSON API
┌──────────────▼──────────────────┐
│  后端 (Java HttpServer)          │
│  Controller → Service → DAO     │
└──────────────┬──────────────────┘
               │ File I/O
┌──────────────▼──────────────────┐
│  data/*.json (持久化文件)        │
└─────────────────────────────────┘
```

## 功能模块

### 图书管理（模块A）
- 图书信息增删改查
- 多条件检索（书名、作者、ISBN、分类）
- 多字段排序（书名/作者/日期/库存，支持快排/归并/冒泡三种算法）
- 图书分类管理（树形层级）
- 库存管理（入库/下架）

### 读者管理（模块B）
- 读者信息查询、编辑与删除
- 读者搜索（按ID/姓名/院系）
- 读者类型管理（学生：5本30天 / 教师：10本60天）
- 借阅权限自动控制
- 管理员重置读者密码
- 部分字段更新策略（不覆盖未修改字段）

### 借阅管理（模块C）
- 借书操作（库存校验、权限检查）
- 还书操作（超期检测、自动计算超期天数）
- 续借操作（每书限续1次）
- 借阅记录查询与筛选
- 超期未还统计
- 热门图书排行榜

### 登录认证
- 统一登录入口（管理员/读者同框，自动识别角色）
- 读者自助注册
- 管理员固定账号（admin）
- 基于 sessionStorage 的会话管理

## 快速开始

### 环境要求
- JDK 8 或更高版本

### 一键启动（推荐）

**Windows：双击 `启动.bat`**

脚本会自动编译、检测空闲端口、启动服务器并打开浏览器。

### 手动启动

```bash
cd LibraryManagementSystem

# 编译
javac -encoding UTF-8 -d bin src/main/java/com/library/model/*.java src/main/java/com/library/util/*.java src/main/java/com/library/dao/*.java src/main/java/com/library/service/*.java src/main/java/com/library/controller/*.java src/main/java/com/library/App.java

# 运行（默认端口 8080）
java -cp bin com.library.App

# 指定端口
java -cp bin -Dapp.port=9090 com.library.App
```

### 访问系统
浏览器打开：**http://localhost:8080**

### 演示账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | `admin` | `admin123` |
| 学生读者 | `2024001` | `123456` |
| 教师读者 | `T001` | `123456` |

## API 接口一览

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/books` | 查询图书列表（?keyword=&category=&sort=&algo=） |
| GET | `/api/books/{isbn}` | 获取单本图书详情 |
| POST | `/api/books` | 新增图书 |
| PUT | `/api/books/{isbn}` | 修改图书 |
| DELETE | `/api/books/{isbn}` | 删除图书 |
| POST | `/api/books/{isbn}/stock` | 入库增加库存 |
| GET | `/api/readers` | 查询读者列表（?keyword=） |
| GET | `/api/readers/{id}` | 获取单个读者详情 |
| PUT | `/api/readers/{id}` | 修改读者信息（部分字段更新） |
| DELETE | `/api/readers/{id}` | 删除读者 |
| POST | `/api/login` | 统一登录（自动识别管理员/读者） |
| POST | `/api/register` | 读者自助注册 |
| POST | `/api/borrow` | 借书 |
| POST | `/api/return/{recordId}` | 还书 |
| POST | `/api/renew/{recordId}` | 续借 |
| GET | `/api/borrow-records` | 查询借阅记录 |
| GET | `/api/stats/popular-books` | 热门图书排行榜 |
| GET | `/api/stats/overdue` | 超期未还列表 |

## 项目结构

```
LibraryManagementSystem/
├── src/main/java/com/library/
│   ├── model/          # 数据模型（Book, Reader, BorrowRecord）
│   ├── dao/            # 数据访问层（JSON文件读写）
│   ├── service/        # 业务逻辑层
│   ├── controller/     # HTTP控制器 + 路由分发
│   ├── util/           # 工具类（JSON, 排序, 日期）
│   └── App.java        # 主入口
├── web/                # 前端文件
│   ├── login.html      # 统一登录/注册页
│   ├── index.html      # 首页（统计+检索+排行榜）
│   ├── css/style.css   # 统一样式
│   ├── js/             # JavaScript逻辑
│   │   ├── auth.js     # 认证与导航
│   │   ├── api.js      # API封装 + 公共函数（弹窗/Toast）
│   │   ├── book.js     # 图书管理
│   │   ├── reader.js   # 读者管理（含重置密码）
│   │   └── borrow.js   # 借阅管理
│   └── pages/          # 子页面
│       ├── book-manage.html
│       ├── reader-manage.html
│       └── borrow-manage.html
├── 启动.bat             # Windows 一键启动脚本
├── data/                # 数据持久化目录
├── 课程设计报告.html     # 课程设计报告（HTML版）
├── 课程设计报告.docx     # 课程设计报告（Word版）
└── README.md
```

## 数据结构与算法亮点

1. **HashMap** — ISBN/ReaderID O(1) 快速检索
2. **快速排序** — 默认排序算法，O(n log n)
3. **归并排序** — 稳定排序，适合出版日期等
4. **冒泡排序** — 教学对比用，O(n²)
5. **树形结构** — 图书分类层级展示（计算机科学/编程语言/Java）
6. **自定义比较器** — 支持多字段、升降序组合排序
7. **递归下降 JSON 解析器** — 自实现，支持完整 JSON 语法
8. **状态机模式** — 借阅状态流转（BORROWED → RETURNED / OVERDUE）
9. **部分字段合并更新** — 读者编辑时仅更新非空字段，保护系统数据

## 三人分工

| 成员 | 负责模块 | 后端文件 | 前端页面 |
|------|----------|----------|----------|
| 于利珂 | 图书管理 + 系统架构 | Book.java, BookDao, BookService, BookController, App.java, Router, JsonUtil | book-manage.html, index.html |
| 肖景匀 | 读者管理 + 登录认证 | Reader.java, ReaderDao, ReaderService, ReaderController, LoginController | reader-manage.html, login.html |
| 刘洪森 | 借阅管理 + 排序算法 | BorrowRecord.java, BorrowRecordDao, BorrowService, BorrowController, SortUtil, DateUtil | borrow-manage.html |
| 共同 | 前端框架 | — | style.css, api.js, auth.js |

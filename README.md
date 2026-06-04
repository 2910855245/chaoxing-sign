# 学习通签到自动化工具

> 🚀 一个支持多种签到类型的学习通自动化签到工具，包含Python后端、Android客户端和WebSocket推送服务。

**项目状态：** 开发中 (约60%完成)

**最后更新：** 2026-06-04

---

## 📋 项目概述

这是一个完整的学习通签到自动化解决方案，支持：
- 普通签到、二维码签到、手势签到、位置签到、签到码签到
- WebSocket实时推送
- Android手机端应用
- Web管理界面

## 🎯 当前进度

### ✅ 已完成

| 模块 | 功能 | 状态 |
|------|------|------|
| Python后端 | 登录、课程列表、签到活动列表 | ✅ |
| Python后端 | 普通签到、二维码签到 | ✅ |
| Python后端 | 手势签到、签到码签到（暴力破解） | ✅ |
| Python后端 | 位置签到（需要GPS坐标） | ✅ |
| Python后端 | 班级管理（获取列表、邀请码） | ✅ |
| Python后端 | 教师发布签到 | ✅ |
| Android应用 | 登录、课程列表 | ✅ |
| Android应用 | 签到活动列表 | ✅ |
| Android应用 | 位置签到（高德地图） | ✅ |
| Android应用 | WebSocket推送 | ✅ |
| 推送服务 | HTTP API + WebSocket | ✅ |
| 推送服务 | 轮询检查签到 | ✅ |
| 管理页面 | Vue3 + TypeScript | ✅ |

### ⚠️ 已知问题

| 问题 | 严重程度 | 说明 |
|------|----------|------|
| 活动列表API返回的userStatus不准确 | 中 | 需要使用活动详情API获取真实状态 |
| 加入班级API返回500 | 低 | 可能需要浏览器端操作 |
| 创建签到时服务器自动签到 | 中 | 学习通服务器行为，非代码问题 |
| 高德地图API Key可能需要更新 | 低 | 当前Key: 77cdf6c640a93dcd29e57ff4017f73d7 |

### ❌ 待完成功能

| 功能 | 优先级 | 说明 |
|------|--------|------|
| 拍照签到 | 高 | 需要上传图片功能 |
| 手势签到UI | 高 | 需要手势输入界面 |
| 签到历史 | 中 | 查看签到记录 |
| 多课程支持 | 中 | 同时监控多个课程 |
| 定时轮询 | 中 | 自动检查新签到 |
| 签到提醒 | 低 | 推送通知提醒 |
| 数据统计 | 低 | 签到统计分析 |

## 🏗️ 技术架构

```
┌─────────────────────────────────────────────────────────────┐
│  管理页面 (Vue3 + TypeScript)                                │
│  http://localhost:3000                                       │
└──────────────────┬──────────────────────────────────────────┘
                   │ HTTP API
                   ▼
┌─────────────────────────────────────────────────────────────┐
│  推送服务 (Python FastAPI + WebSocket)                       │
│  http://38.76.190.251:8765                                   │
│  ├── HTTP API (登录、签到、推送)                              │
│  ├── WebSocket (实时通知)                                    │
│  └── 轮询模块 (定时检查签到)                                  │
└──────────────────┬──────────────────────────────────────────┘
                   │ HTTP API
                   ▼
┌─────────────────────────────────────────────────────────────┐
│  学习通服务器                                                │
│  ├── passport2.chaoxing.com (登录)                          │
│  ├── mobilelearn.chaoxing.com (签到)                        │
│  └── mooc1.chaoxing.com (课程)                              │
└──────────────────┬──────────────────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────────────────┐
│  Android应用 (Kotlin + OkHttp + 高德地图SDK)                 │
│  ├── 登录、课程列表                                          │
│  ├── 签到活动列表                                            │
│  ├── 位置签到（GPS定位）                                     │
│  └── WebSocket推送接收                                       │
└─────────────────────────────────────────────────────────────┘
```

## 功能特性

### 签到类型支持

| 类型 | 服务器端 | 手机端 | 说明 |
|------|----------|--------|------|
| 普通签到 | ✅ | ✅ | 直接签到 |
| 二维码签到 | ✅ | ✅ | 自动获取enc签到 |
| 手势签到 | ✅ | ✅ | 暴力破解手势图案 |
| 位置签到 | ❌ | ✅ | 需要GPS坐标 |
| 签到码签到 | ✅ | ✅ | 暴力破解4-6位数字码 |
| 拍照签到 | ❌ | ✅ | 需要上传图片 |

### 核心功能

- **自动签到** - 普通签到、二维码签到、签到码签到
- **位置签到** - 使用高德地图SDK获取GPS坐标
- **WebSocket推送** - 实时推送签到通知
- **班级管理** - 获取班级列表、邀请码
- **多账号支持** - 支持多个学生/教师账号

## 项目结构

```
学习通签到/
├── app.py                          # Python终端界面
├── test_auto_sign.py               # 自动化测试脚本
├── chaoxing/                       # Python核心模块
│   ├── session.py                  # 会话管理
│   ├── crawler.py                  # 课程爬取
│   ├── signin.py                   # 学生签到
│   ├── clazz.py                    # 班级管理
│   └── teacher.py                  # 教师端功能
├── ChaoxingSignApp/                # Android应用
│   └── app/src/main/java/com/chaoxing/sign/
│       ├── api/                    # API层
│       │   ├── ChaoxingApi.kt      # 学习通API
│       │   └── ChaoxingSession.kt  # 会话管理
│       ├── activity/               # 页面
│       │   ├── LoginActivity.kt    # 登录页
│       │   ├── HomeActivity.kt     # 首页
│       │   ├── SignActivity.kt     # 签到页
│       │   └── LocationSignActivity.kt  # 位置签到页
│       └── push/                   # 推送模块
│           └── PushClient.kt       # WebSocket客户端
└── 签到推送服务/                     # 推送服务
    ├── server/                     # 服务端
    │   ├── app.py                  # HTTP + WebSocket服务
    │   └── poller.py               # 轮询模块
    ├── client/                     # 客户端
    │   ├── push_client.py          # Python客户端
    │   └── PushClient.kt           # Android客户端
    └── admin/                      # 管理页面
        └── src/                    # Vue3 + TypeScript
```

## 快速开始

### 1. Python环境

```bash
# 安装依赖
pip install -r requirements.txt

# 运行终端工具
python app.py

# 运行自动化测试
python test_auto_sign.py
```

### 2. 推送服务

```bash
cd 签到推送服务/server

# 安装依赖
pip install fastapi uvicorn websockets

# 启动服务
python app.py
```

### 3. Android应用

```bash
cd ChaoxingSignApp

# 编译
./gradlew assembleDebug

# 安装到手机
adb install app/build/outputs/apk/debug/app-debug.apk
```

### 4. 管理页面

```bash
cd 签到推送服务/admin

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

## 使用说明

### Python端

```python
from chaoxing.session import ChaoxingSession
from chaoxing.teacher import publish_sign
from chaoxing.signin import get_active_list, sign_activity

# 教师发布签到
teacher = ChaoxingSession()
teacher.login('教师账号', '密码')
result = publish_sign(teacher, '课程ID', '班级ID', 
                      title='签到标题', duration_minutes=10,
                      other_id=0)  # 0=普通, 4=位置

# 学生签到
student = ChaoxingSession()
student.login('学生账号', '密码')
activities = get_active_list(student, '课程ID', '班级ID')
for act in activities:
    if act['status'] == 0:  # 待签到
        result = sign_activity(student, act)
```

### Android端

1. 打开应用
2. 输入学生账号密码登录
3. 点击课程
4. 点击签到按钮

### 位置签到

1. 教师发布位置签到（设置坐标和范围）
2. 学生在手机上点击签到按钮
3. 地图显示目标位置和范围
4. 获取GPS坐标
5. 在范围内签到成功

## API文档

### 学习通API

| API | 说明 |
|-----|------|
| `POST /fanyalogin` | 登录 |
| `GET /v2/apis/active/student/activelist` | 获取签到活动列表 |
| `GET /v2/apis/active/getPPTActiveInfo` | 获取活动详情 |
| `POST /v2/apis/sign/saveOrBegin` | 创建签到活动 |
| `GET /v2/apis/active/startActive` | 启动签到活动 |
| `GET /pptSign/stuSignajax` | 执行签到 |

### 推送服务API

| API | 说明 |
|-----|------|
| `POST /api/login` | 登录 |
| `GET /api/activities` | 获取签到活动列表 |
| `POST /api/sign` | 执行签到 |
| `POST /api/push` | 推送通知 |
| `GET /api/status` | 获取状态 |
| `WS /ws/{client_id}` | WebSocket连接 |

## 技术栈

### Python端
- Python 3.10+
- rnet (HTTP客户端)
- loguru (日志)

### Android端
- Kotlin
- OkHttp (HTTP客户端)
- 高德地图SDK (定位、地图)
- WebSocket (推送)

### 推送服务
- Python 3.10+
- FastAPI (Web框架)
- WebSocket (实时通信)

### 管理页面
- Vue 3
- TypeScript
- Pinia (状态管理)
- Vue Router

## 测试账号

### 学习通账号

| 角色 | 学号/手机号 | 密码 | 姓名 | 学校 | UID |
|------|-------------|------|------|------|-----|
| 教师 | 19136434661 | woainima123 | 罗忠全 | 成都文理学院马克思主义学院 | 430580003 |
| 学生 | 19102804734 | Lsq200671 | 林诗奇 | 成都文理学院马克思主义学院 | 430584649 |

### 课程信息

| 课程名称 | 课程ID | 班级ID |
|----------|--------|--------|
| vibe coding自动化 | 264381020 | 148304228 |
| 2025-2026-2《形势与政策》 | 260982075 | 140481754 |
| 马克思主义基本原理 | 261524103 | 142031197 |
| 形策 | 261235153 | 141206120 |
| 普通话语音实训 | 245210814 | 133005805 |
| 普通话语音和播音发声 | 204798421 | 130940562 |
| 公关礼仪 | 256468930 | 130500008 |

### 服务器配置

```python
# 推送服务地址
PUSH_SERVER = 'http://38.76.190.251:8765'

# 高德地图API Key
AMAP_API_KEY = '77cdf6c640a93dcd29e57ff4017f73d7'
```

## 文件说明

### Python核心模块 (chaoxing/)

#### session.py - 会话管理
```python
# 功能：管理学习通HTTP会话
# 主要类：ChaoxingSession
# 方法：
#   - login(username, password) - 登录
#   - get(url) - GET请求
#   - post(url, data) - POST请求
#   - get_json(url) - 获取JSON响应
#   - get_user_info() - 获取用户信息
#   - verify() - 验证cookie有效性
# 依赖：rnet, loguru
```

#### signin.py - 学生签到
```python
# 功能：处理各种类型的签到
# 主要函数：
#   - get_active_list(session, course_id, class_id) - 获取签到活动列表
#   - sign_activity(session, activity) - 执行签到
#   - sign_location_with_coords(session, active_id, ...) - 位置签到
#   - check_real_sign_status(session, active_id) - 检查真实签到状态
#   - brute_force_gesture_async(...) - 暴力破解手势
#   - brute_force_code_async(...) - 暴力破解签到码
# 注意：活动列表API返回的userStatus可能不准确，需要使用活动详情API
```

#### teacher.py - 教师端功能
```python
# 功能：教师发布和管理签到
# 主要函数：
#   - create_sign_active(session, ...) - 创建签到活动
#   - start_sign_active(session, ...) - 启动签到活动
#   - set_sign_end_time(session, ...) - 设置结束时间
#   - end_sign_active(session, ...) - 结束签到活动
#   - publish_sign(session, ...) - 一键发布签到
#   - get_active_list_teacher(session, ...) - 获取教师端活动列表
# API: saveOrBegin (创建), startActive (启动), restartActive2 (设置结束时间)
```

#### clazz.py - 班级管理
```python
# 功能：管理班级和邀请码
# 主要函数：
#   - get_class_list(session, course_id) - 获取班级列表
#   - get_class_invite_code(session, ...) - 获取班级邀请码
#   - get_class_detail(session, ...) - 获取班级详情
#   - join_class_by_code(session, invite_code) - 通过邀请码加入班级
#   - create_class(session, ...) - 创建新班级
# 注意：加入班级API返回500，可能需要浏览器端操作
```

#### crawler.py - 课程爬取
```python
# 功能：获取课程和知识点
# 主要函数：
#   - get_course_list(session) - 获取课程列表
#   - get_knowledge_list(session, ...) - 获取知识点列表
#   - get_points(session, ...) - 获取课程积分
#   - get_video_info(session, ...) - 获取视频信息
# API: /mooc-ans/visit/courselistdata
```

### Android应用 (ChaoxingSignApp/)

#### ChaoxingApi.kt - API层
```kotlin
// 功能：学习通API封装
// 主要方法：
//   - getCourseList(session) - 获取课程列表
//   - getActiveList(session, courseId, classId) - 获取签到活动列表
//   - getActiveDetail(session, activeId) - 获取活动详情
//   - checkRealSignStatus(session, activeId) - 检查真实签到状态
//   - signNormal(session, activeId) - 普通签到
//   - signLocation(session, activeId, lat, lon, address) - 位置签到
//   - signWithCode(session, activeId, code) - 签到码签到
//   - signQrCode(session, activeId, enc) - 二维码签到
// 注意：活动列表API返回的userStatus可能不准确
```

#### ChaoxingSession.kt - 会话管理
```kotlin
// 功能：管理HTTP会话和Cookie
// 主要方法：
//   - login(username, password) - 登录
//   - autoLogin() - 自动登录
//   - get(url, referer) - GET请求
//   - getJson(url, referer) - 获取JSON响应
//   - post(url, formBody, referer) - POST请求
// 特点：Cookie持久化存储，支持跨域
```

#### LoginActivity.kt - 登录页
```kotlin
// 功能：用户登录界面
// 流程：输入账号密码 → 调用login API → 保存session → 跳转首页
```

#### HomeActivity.kt - 首页
```kotlin
// 功能：显示课程列表
// 流程：
//   1. 自动登录
//   2. 加载课程列表
//   3. 连接WebSocket推送服务
//   4. 接收签到通知
//   5. 位置签到 → 打开地图页面
//   6. 其他签到 → 自动签到
```

#### SignActivity.kt - 签到页
```kotlin
// 功能：显示签到活动列表
// 流程：
//   1. 自动登录
//   2. 加载签到活动列表
//   3. 显示活动状态（待签/已签/已过期）
//   4. 点击签到按钮 → 根据类型处理
// 特点：位置签到始终显示签到按钮（因为API返回的userStatus不准确）
```

#### LocationSignActivity.kt - 位置签到页
```kotlin
// 功能：位置签到地图页面
// 流程：
//   1. 请求位置权限
//   2. 初始化高德地图
//   3. 显示目标位置和范围
//   4. 获取GPS定位
//   5. 计算距离
//   6. 在范围内签到成功
// 特点：
//   - 显示目标位置（红色标记）
//   - 显示签到范围（红色圆圈）
//   - 显示当前位置（绿色标记）
//   - 一键回到真实定位按钮
//   - 点击地图选点
```

#### PushClient.kt - WebSocket客户端
```kotlin
// 功能：接收签到推送通知
// 连接：ws://服务器:8765/ws/{clientId}
// 消息格式：{"type":"sign","activeId":123,"courseName":"...","signType":"..."}
// 特点：自动重连，心跳保活
```

### 推送服务 (签到推送服务/)

#### server/app.py - HTTP + WebSocket服务
```python
# 功能：推送服务主程序
# 框架：FastAPI + uvicorn/granian
# 端口：8765
# API：
#   - GET / - 健康检查
#   - POST /api/login - 登录
#   - GET /api/activities - 获取签到活动
#   - POST /api/sign - 执行签到
#   - POST /api/push - 推送通知
#   - GET /api/status - 获取状态
#   - WS /ws/{client_id} - WebSocket连接
```

#### server/poller.py - 轮询模块
```python
# 功能：定时检查签到活动
# 流程：
#   1. 登录教师账号
#   2. 每30秒检查一次签到活动
#   3. 发现新签到 → 推送到手机
#   4. 手机收到通知 → 自动签到
```

#### client/push_client.py - Python客户端
```python
# 功能：接收签到推送通知
# 用法：
#   client = PushClient('ws://server:8765', 'student_123')
#   client.on_sign = lambda data: handle_sign(data)
#   client.start()
```

#### admin/ - 管理页面
```
# 技术栈：Vue 3 + TypeScript + Pinia
# 功能：
#   - 仪表盘：服务状态概览
#   - 账号管理：添加/删除账号
#   - 课程管理：添加/删除课程
#   - 签到活动：查看活动、手动签到
#   - 设置：轮询配置、服务状态
```

## 已知问题

1. **位置签到API返回的userStatus不准确** - 活动列表API返回的userStatus可能是错误的，需要使用活动详情API
2. **加入班级API返回500** - 可能需要浏览器端操作
3. **创建签到活动时服务器自动签到** - 学习通服务器在启动活动时可能自动为学生签到
4. **高德地图API Key** - 当前Key可能需要更新

## 待完成功能

1. **拍照签到** - 需要上传图片功能
2. **手势签到UI** - 需要手势输入界面
3. **签到历史** - 查看签到记录
4. **多课程支持** - 同时监控多个课程
5. **定时轮询** - 自动检查新签到

## 注意事项

1. **位置签到** 必须在手机APP端完成，需要GPS坐标
2. **手势签到** 和 **签到码签到** 支持暴力破解
3. **WebSocket推送** 需要推送服务运行
4. **Cookie跨域** 已修复，支持多个域名共享Cookie
5. **活动详情API** 返回的userStatus比活动列表API更准确

## 开发说明

### 添加新的签到类型

1. 在 `chaoxing/signin.py` 中添加签到逻辑
2. 在 `ChaoxingApi.kt` 中添加对应的API调用
3. 在 `SignActivity.kt` 中添加UI处理

### 添加新的推送功能

1. 在 `push_client.py` 中添加Python客户端
2. 在 `PushClient.kt` 中添加Android客户端
3. 在 `app.py` 中添加服务端处理

## 🔧 开发环境配置

### Python环境

```bash
# 安装Python 3.10+
# 安装依赖
pip install rnet loguru rich requests

# 验证
python -c "from chaoxing.session import ChaoxingSession; print('OK')"
```

### Android开发环境

```bash
# 安装 Android Studio
# 安装 JDK 17+
# 安装 Android SDK (API 34)

# 打开项目
# File -> Open -> 选择 ChaoxingSignApp 目录

# 编译
./gradlew assembleDebug

# 安装到手机
adb install app/build/outputs/apk/debug/app-debug.apk
```

### 推送服务环境

```bash
# 安装依赖
pip install fastapi uvicorn websockets

# 启动服务
cd 签到推送服务/server
python app.py

# 测试
curl http://localhost:8765/
```

### 管理页面环境

```bash
# 安装 Node.js 18+
cd 签到推送服务/admin
npm install
npm run dev

# 访问 http://localhost:3000
```

### 高德地图SDK

1. 访问 https://console.amap.com
2. 创建应用
3. 添加Android平台
4. 填写包名：`com.chaoxing.sign`
5. 填写SHA1：`19:66:34:82:20:B8:94:38:1B:C1:5F:E4:94:26:76:4D:B5:76:03:0C`
6. 获取API Key
7. 替换 `AndroidManifest.xml` 中的 `android:value`

## 🤝 贡献指南

### 如何参与开发

1. **Fork 项目**
2. **创建分支** - `git checkout -b feature/你的功能`
3. **提交代码** - `git commit -m "添加了xxx功能"`
4. **推送分支** - `git push origin feature/你的功能`
5. **创建 Pull Request**

### 代码规范

#### Python
- 使用 PEP 8 规范
- 使用 type hints
- 使用 loguru 记录日志
- 函数和类添加 docstring

```python
def get_active_list(session: ChaoxingSession, course_id: str, class_id: str) -> list:
    """获取签到活动列表
    
    Args:
        session: 学习通会话
        course_id: 课程ID
        class_id: 班级ID
    
    Returns:
        签到活动列表
    """
    pass
```

#### Kotlin
- 使用 Kotlin 官方规范
- 使用协程处理异步
- 使用 Log.d 记录日志
- 函数添加注释

```kotlin
/**
 * 获取签到活动列表
 * @param session 学习通会话
 * @param courseId 课程ID
 * @param classId 班级ID
 * @return 签到活动列表
 */
fun getActiveList(session: ChaoxingSession, courseId: String, classId: String): List<SignActivityData> {
    // 实现
}
```

#### Vue/TypeScript
- 使用 Vue 3 Composition API
- 使用 TypeScript
- 使用 Pinia 状态管理
- 组件添加 props 类型定义

```vue
<script setup lang="ts">
interface Props {
  title: string
  visible?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  visible: false
})
</script>
```

### 提交规范

```
feat: 添加新功能
fix: 修复bug
docs: 更新文档
style: 代码格式调整
refactor: 重构代码
test: 添加测试
chore: 构建/工具变动
```

### 测试

```bash
# Python测试
python test_auto_sign.py

# Android测试
./gradlew test

# 推送服务测试
curl http://localhost:8765/api/status
```

## 📞 联系方式

如有问题或建议，请通过以下方式联系：

- **GitHub Issues** - 提交bug或功能请求
- **Pull Request** - 贡献代码

## 📄 许可证

MIT License

---

**感谢所有贡献者！** 🙏

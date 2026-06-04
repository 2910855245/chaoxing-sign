// API 配置
const API_BASE = 'http://38.76.190.251:8765'

// 通用请求函数
async function request<T = any>(
  method: string,
  path: string,
  body?: any
): Promise<T> {
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
  }

  const opts: RequestInit = { method, headers }

  if (body && method !== 'GET') {
    opts.body = JSON.stringify(body)
  }

  // 超时控制
  const controller = new AbortController()
  opts.signal = controller.signal
  const timer = setTimeout(() => controller.abort(), 30000)

  try {
    const res = await fetch(API_BASE + path, opts)
    if (!res.ok) {
      throw new Error(`HTTP ${res.status}: ${res.statusText}`)
    }
    return await res.json() as T
  } finally {
    clearTimeout(timer)
  }
}

// GET 请求
function get<T = any>(path: string): Promise<T> {
  return request<T>('GET', path)
}

// POST 请求
function post<T = any>(path: string, data?: any): Promise<T> {
  return request<T>('POST', path, data)
}

// 构建查询字符串
function buildQuery(params?: Record<string, any>): string {
  if (!params) return ''
  const entries = Object.entries(params).filter(([_, v]) => v !== undefined && v !== null)
  if (entries.length === 0) return ''
  return '?' + new URLSearchParams(entries.map(([k, v]) => [k, String(v)])).toString()
}

// 类型定义
export interface ApiResponse<T = any> {
  success: boolean
  data?: T
  message: string
}

export interface LoginResponse {
  success: boolean
  message: string
  uid?: string
}

export interface SignActivity {
  active_id: string
  course_name: string
  sign_type: string
  status: number
  user_status: number
  other_id: number
}

export interface SignResponse {
  success: boolean
  message: string
}

export interface StatusResponse {
  logged_in_users: number
  poller_running: boolean
  ws_connections: number
}

export interface PushResponse {
  success: boolean
  sent_to: number
}

// API 方法
export const api = {
  // 健康检查
  health: () => get<{ service: string; version: string; status: string }>('/'),

  // 登录
  login: (username: string, password: string) =>
    post<LoginResponse>('/api/login', { username, password }),

  // 获取签到活动列表
  activities: (username: string, courseId: string, classId: string) =>
    get<ApiResponse<SignActivity[]>>(`/api/activities${buildQuery({ username, courseId, classId })}`),

  // 执行签到
  sign: (username: string, activeId: string, courseId: string, classId: string) =>
    post<SignResponse>(`/api/sign${buildQuery({ username })}`, {
      active_id: activeId,
      course_id: courseId,
      class_id: classId,
    }),

  // 启动轮询
  startPoller: (intervalSeconds: number, courses: Array<{ course_id: string; class_id: string; name: string }>) =>
    post<ApiResponse>('/api/poll/start', {
      interval_seconds: intervalSeconds,
      courses,
    }),

  // 停止轮询
  stopPoller: () => post<ApiResponse>('/api/poll/stop'),

  // 推送通知
  push: (activeId: string, courseName: string, signType: string) =>
    post<PushResponse>('/api/push', {
      active_id: activeId,
      course_name: courseName,
      sign_type: signType,
    }),

  // 获取状态
  status: () => get<StatusResponse>('/api/status'),
}

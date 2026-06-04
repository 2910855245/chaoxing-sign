import { defineStore } from 'pinia'
import { ref, reactive } from 'vue'
import { api } from '@/api'
import type { SignActivity, StatusResponse } from '@/api'

// 账号类型
export interface Account {
  username: string
  password: string
  name: string
  role: 'student' | 'teacher'
  logged_in: boolean
  uid?: string
}

// 课程类型
export interface Course {
  course_id: string
  class_id: string
  name: string
}

// Toast 类型
interface Toast {
  id: number
  message: string
  type: 'success' | 'error' | 'warning' | 'info'
}

export const useAppStore = defineStore('app', () => {
  // 状态
  const accounts = ref<Account[]>([])
  const courses = ref<Course[]>([])
  const activities = ref<SignActivity[]>([])
  const serverStatus = ref<StatusResponse | null>(null)
  const loading = ref(false)
  const toasts = reactive<Toast[]>([])

  // 从 localStorage 加载配置
  function loadConfig() {
    const savedAccounts = localStorage.getItem('sign_accounts')
    if (savedAccounts) {
      accounts.value = JSON.parse(savedAccounts)
    }

    const savedCourses = localStorage.getItem('sign_courses')
    if (savedCourses) {
      courses.value = JSON.parse(savedCourses)
    }
  }

  // 保存配置到 localStorage
  function saveConfig() {
    localStorage.setItem('sign_accounts', JSON.stringify(accounts.value))
    localStorage.setItem('sign_courses', JSON.stringify(courses.value))
  }

  // Toast 通知
  function toast(message: string, type: Toast['type'] = 'success') {
    const id = Date.now()
    toasts.push({ id, message, type })
    setTimeout(() => {
      const idx = toasts.findIndex(t => t.id === id)
      if (idx > -1) toasts.splice(idx, 1)
    }, 3500)
  }

  // 添加账号
  function addAccount(account: Account) {
    const exists = accounts.value.find(a => a.username === account.username)
    if (exists) {
      toast('账号已存在', 'warning')
      return
    }
    accounts.value.push(account)
    saveConfig()
    toast('账号添加成功', 'success')
  }

  // 删除账号
  function removeAccount(username: string) {
    accounts.value = accounts.value.filter(a => a.username !== username)
    saveConfig()
    toast('账号已删除', 'success')
  }

  // 更新账号
  function updateAccount(username: string, data: Partial<Account>) {
    const account = accounts.value.find(a => a.username === username)
    if (account) {
      Object.assign(account, data)
      saveConfig()
    }
  }

  // 添加课程
  function addCourse(course: Course) {
    const exists = courses.value.find(
      c => c.course_id === course.course_id && c.class_id === course.class_id
    )
    if (exists) {
      toast('课程已存在', 'warning')
      return
    }
    courses.value.push(course)
    saveConfig()
    toast('课程添加成功', 'success')
  }

  // 删除课程
  function removeCourse(courseId: string, classId: string) {
    courses.value = courses.value.filter(
      c => !(c.course_id === courseId && c.class_id === classId)
    )
    saveConfig()
    toast('课程已删除', 'success')
  }

  // 登录所有账号
  async function loginAll() {
    loading.value = true
    for (const account of accounts.value) {
      try {
        const result = await api.login(account.username, account.password)
        if (result.success) {
          account.logged_in = true
          account.uid = result.uid
          toast(`${account.name} 登录成功`, 'success')
        } else {
          account.logged_in = false
          toast(`${account.name} 登录失败: ${result.message}`, 'error')
        }
      } catch (e: any) {
        toast(`${account.name} 登录失败: ${e.message}`, 'error')
      }
    }
    saveConfig()
    loading.value = false
  }

  // 获取签到活动
  async function fetchActivities() {
    loading.value = true
    activities.value = []

    for (const account of accounts.value) {
      if (!account.logged_in) continue

      for (const course of courses.value) {
        try {
          const result = await api.activities(
            account.username,
            course.course_id,
            course.class_id
          )
          if (result.success && result.data) {
            activities.value.push(...result.data)
          }
        } catch (e: any) {
          console.error('获取活动失败:', e)
        }
      }
    }

    loading.value = false
  }

  // 获取服务状态
  async function fetchStatus() {
    try {
      serverStatus.value = await api.status()
    } catch (e) {
      console.error('获取状态失败:', e)
    }
  }

  // 执行签到
  async function doSign(username: string, activeId: string, courseId: string, classId: string) {
    try {
      const result = await api.sign(username, activeId, courseId, classId)
      if (result.success) {
        toast('签到成功', 'success')
        await fetchActivities()
      } else {
        toast(`签到失败: ${result.message}`, 'error')
      }
      return result
    } catch (e: any) {
      toast(`签到失败: ${e.message}`, 'error')
      return { success: false, message: e.message }
    }
  }

  // 启动轮询
  async function startPoller(interval: number = 30) {
    try {
      const coursesConfig = courses.value.map(c => ({
        course_id: c.course_id,
        class_id: c.class_id,
        name: c.name,
      }))
      const result = await api.startPoller(interval, coursesConfig)
      toast(result.message, 'success')
      await fetchStatus()
    } catch (e: any) {
      toast(`启动轮询失败: ${e.message}`, 'error')
    }
  }

  // 停止轮询
  async function stopPoller() {
    try {
      const result = await api.stopPoller()
      toast(result.message, 'success')
      await fetchStatus()
    } catch (e: any) {
      toast(`停止轮询失败: ${e.message}`, 'error')
    }
  }

  return {
    accounts,
    courses,
    activities,
    serverStatus,
    loading,
    toasts,
    loadConfig,
    saveConfig,
    toast,
    addAccount,
    removeAccount,
    updateAccount,
    addCourse,
    removeCourse,
    loginAll,
    fetchActivities,
    fetchStatus,
    doSign,
    startPoller,
    stopPoller,
  }
})

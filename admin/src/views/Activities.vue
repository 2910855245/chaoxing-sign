<script setup lang="ts">
import { onMounted } from 'vue'
import { useAppStore } from '@/stores/app'

const store = useAppStore()

onMounted(async () => {
  await store.fetchActivities()
})

// 状态映射
const statusLabels: Record<number, string> = {
  0: '待签到',
  1: '已签到',
  2: '已过期',
}

const statusClass: Record<number, string> = {
  0: 'warn',
  1: 'ok',
  2: 'bad',
}

// 签到类型映射
const signTypeLabels: Record<number, string> = {
  0: '普通签到',
  2: '二维码签到',
  3: '手势签到',
  4: '位置签到',
  5: '签到码签到',
}

// 签到
async function handleSign(activeId: string) {
  const account = store.accounts.find(a => a.logged_in && a.role === 'student')
  if (!account) {
    store.toast('没有已登录的学生账号', 'warning')
    return
  }

  const activity = store.activities.find(a => a.active_id === activeId)
  if (!activity) return

  // 找到对应的课程
  const course = store.courses.find(c => true) // 简化处理
  if (!course) return

  await store.doSign(account.username, activeId, course.course_id, course.class_id)
}
</script>

<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">签到活动</h1>
        <p class="page-desc">查看和管理签到活动</p>
      </div>
      <button class="btn btn-primary" @click="store.fetchActivities" :disabled="store.loading">
        <span v-if="store.loading" class="spinner"></span>
        刷新
      </button>
    </div>

    <!-- 活动列表 -->
    <div class="card">
      <div v-if="store.loading" class="loading">
        <div class="spinner spinner-lg"></div>
        <p>加载中...</p>
      </div>

      <table class="data-table" v-else-if="store.activities.length > 0">
        <thead>
          <tr>
            <th>活动ID</th>
            <th>签到类型</th>
            <th>状态</th>
            <th>用户状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="act in store.activities" :key="act.active_id">
            <td><code>{{ act.active_id }}</code></td>
            <td>
              <span class="pill primary">{{ act.sign_type }}</span>
            </td>
            <td>
              <span class="pill" :class="statusClass[act.status]">
                {{ statusLabels[act.status] }}
              </span>
            </td>
            <td>
              <span class="pill" :class="act.user_status === 1 ? 'ok' : 'warn'">
                {{ act.user_status === 1 ? '已签' : '未签' }}
              </span>
            </td>
            <td>
              <button
                v-if="act.status === 0 && act.user_status !== 1"
                class="btn btn-primary btn-sm"
                @click="handleSign(act.active_id)"
              >
                签到
              </button>
              <span v-else class="text-muted">-</span>
            </td>
          </tr>
        </tbody>
      </table>

      <div v-else class="empty">
        <p>暂无签到活动</p>
        <button class="btn btn-primary" @click="store.fetchActivities">刷新</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page {
  animation: fadeInUp .4s ease;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--c-text);
  margin-bottom: 4px;
}

.page-desc {
  font-size: 14px;
  color: var(--c-text-secondary);
}

.loading {
  text-align: center;
  padding: 40px;
  color: var(--c-text-secondary);
}

.empty {
  text-align: center;
  padding: 40px;
  color: var(--c-text-secondary);
}

.empty p {
  margin-bottom: 16px;
}

code {
  background: var(--c-bg);
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 13px;
  color: var(--c-primary);
}

.text-muted {
  color: var(--c-text-muted);
}
</style>

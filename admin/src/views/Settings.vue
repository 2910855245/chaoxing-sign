<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAppStore } from '@/stores/app'

const store = useAppStore()

// 轮询配置
const pollInterval = ref(30)
const isPolling = ref(false)

onMounted(async () => {
  await store.fetchStatus()
  isPolling.value = store.serverStatus?.poller_running ?? false
})

// 启动/停止轮询
async function togglePoller() {
  if (isPolling.value) {
    await store.stopPoller()
    isPolling.value = false
  } else {
    await store.startPoller(pollInterval.value)
    isPolling.value = true
  }
}

// 登录所有账号
async function handleLoginAll() {
  await store.loginAll()
}
</script>

<template>
  <div class="page">
    <div class="page-header">
      <h1 class="page-title">设置</h1>
      <p class="page-desc">配置签到服务</p>
    </div>

    <div class="settings-grid">
      <!-- 服务状态 -->
      <div class="card">
        <h3 class="card-title">服务状态</h3>
        <div class="status-list">
          <div class="status-item">
            <span class="status-label">轮询服务</span>
            <span class="pill" :class="store.serverStatus?.poller_running ? 'ok' : 'bad'">
              {{ store.serverStatus?.poller_running ? '运行中' : '已停止' }}
            </span>
          </div>
          <div class="status-item">
            <span class="status-label">已登录用户</span>
            <span class="pill primary">{{ store.serverStatus?.logged_in_users ?? 0 }}</span>
          </div>
          <div class="status-item">
            <span class="status-label">WebSocket连接</span>
            <span class="pill info">{{ store.serverStatus?.ws_connections ?? 0 }}</span>
          </div>
        </div>
        <button class="btn btn-outline btn-block" @click="store.fetchStatus" style="margin-top: 16px">
          刷新状态
        </button>
      </div>

      <!-- 轮询配置 -->
      <div class="card">
        <h3 class="card-title">轮询配置</h3>
        <div class="field">
          <label>轮询间隔（秒）</label>
          <input v-model.number="pollInterval" type="number" min="10" max="300" />
        </div>
        <div class="field">
          <label>监控课程</label>
          <div class="course-list">
            <div v-for="course in store.courses" :key="course.course_id" class="course-item">
              <span>{{ course.name }}</span>
              <code>{{ course.course_id }}</code>
            </div>
            <p v-if="store.courses.length === 0" class="text-muted">暂无课程</p>
          </div>
        </div>
        <button
          class="btn btn-block"
          :class="isPolling ? 'btn-danger' : 'btn-primary'"
          @click="togglePoller"
          :disabled="store.courses.length === 0"
        >
          {{ isPolling ? '停止轮询' : '启动轮询' }}
        </button>
      </div>

      <!-- 账号管理 -->
      <div class="card">
        <h3 class="card-title">账号管理</h3>
        <div class="account-list">
          <div v-for="account in store.accounts" :key="account.username" class="account-item">
            <div class="account-info">
              <span class="account-name">{{ account.name }}</span>
              <span class="account-role pill" :class="account.role === 'teacher' ? 'primary' : 'info'">
                {{ account.role === 'teacher' ? '教师' : '学生' }}
              </span>
            </div>
            <span class="pill" :class="account.logged_in ? 'ok' : 'bad'">
              {{ account.logged_in ? '已登录' : '未登录' }}
            </span>
          </div>
          <p v-if="store.accounts.length === 0" class="text-muted">暂无账号</p>
        </div>
        <button class="btn btn-primary btn-block" @click="handleLoginAll" style="margin-top: 16px">
          登录所有账号
        </button>
      </div>

      <!-- 关于 -->
      <div class="card">
        <h3 class="card-title">关于</h3>
        <div class="about">
          <p><strong>签到管理服务</strong></p>
          <p>版本: 0.1.0</p>
          <p>基于 Vue 3 + TypeScript</p>
          <p class="text-muted" style="margin-top: 12px">
            自动签到、WebSocket推送、多账号管理
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page {
  animation: fadeInUp .4s ease;
}

.page-header {
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

.settings-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--c-text);
  margin-bottom: 16px;
}

.status-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.status-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.status-label {
  font-size: 14px;
  color: var(--c-text-secondary);
}

.course-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.course-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: var(--c-bg);
  border-radius: var(--radius-sm);
}

.course-item code {
  font-size: 12px;
  color: var(--c-primary);
}

.account-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.account-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: var(--c-bg);
  border-radius: var(--radius-sm);
}

.account-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.account-name {
  font-weight: 500;
}

.about p {
  font-size: 14px;
  color: var(--c-text-secondary);
  margin-bottom: 4px;
}

.text-muted {
  color: var(--c-text-muted);
  font-size: 13px;
}

@media (max-width: 768px) {
  .settings-grid {
    grid-template-columns: 1fr;
  }
}
</style>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useAppStore } from '@/stores/app'
import AppIcon from '@/components/AppIcon.vue'

const store = useAppStore()

onMounted(async () => {
  await store.fetchStatus()
})

const stats = [
  { label: '已登录账号', value: () => store.accounts.filter(a => a.logged_in).length, icon: 'user', color: 'primary' },
  { label: '监控课程', value: () => store.courses.length, icon: 'course', color: 'info' },
  { label: '签到活动', value: () => store.activities.length, icon: 'sign', color: 'success' },
  { label: 'WebSocket连接', value: () => store.serverStatus?.ws_connections ?? 0, icon: 'link', color: 'warning' },
]
</script>

<template>
  <div class="page">
    <div class="page-header">
      <h1 class="page-title">仪表盘</h1>
      <p class="page-desc">签到服务状态概览</p>
    </div>

    <div class="stats-grid">
      <div
        v-for="stat in stats"
        :key="stat.label"
        class="stat-card"
      >
        <div class="stat-icon" :class="stat.color">
          <AppIcon :name="stat.icon" :size="24" />
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stat.value() }}</div>
          <div class="stat-label">{{ stat.label }}</div>
        </div>
      </div>
    </div>

    <div class="cards-grid">
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
      </div>

      <div class="card">
        <h3 class="card-title">快速操作</h3>
        <div class="actions">
          <button class="btn btn-primary btn-block" @click="store.loginAll">
            登录所有账号
          </button>
          <button class="btn btn-outline btn-block" @click="store.fetchActivities">
            刷新签到活动
          </button>
          <button
            class="btn btn-block"
            :class="store.serverStatus?.poller_running ? 'btn-danger' : 'btn-primary'"
            @click="store.serverStatus?.poller_running ? store.stopPoller() : store.startPoller()"
          >
            {{ store.serverStatus?.poller_running ? '停止轮询' : '启动轮询' }}
          </button>
        </div>
      </div>
    </div>

    <div class="card" v-if="store.activities.length > 0">
      <h3 class="card-title">最近签到活动</h3>
      <table class="data-table">
        <thead>
          <tr>
            <th>活动ID</th>
            <th>签到类型</th>
            <th>状态</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="act in store.activities.slice(0, 5)" :key="act.active_id">
            <td>{{ act.active_id }}</td>
            <td>{{ act.sign_type }}</td>
            <td>
              <span class="pill" :class="act.status === 0 ? 'warn' : act.status === 1 ? 'ok' : 'bad'">
                {{ act.status === 0 ? '待签到' : act.status === 1 ? '已签到' : '已过期' }}
              </span>
            </td>
          </tr>
        </tbody>
      </table>
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

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: var(--c-surface);
  border: 1px solid var(--c-border);
  border-radius: var(--radius);
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: var(--shadow-xs);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.stat-icon.primary { background: var(--c-primary-bg); }
.stat-icon.info { background: var(--c-info-bg); }
.stat-icon.success { background: var(--c-success-bg); }
.stat-icon.warning { background: var(--c-warning-bg); }

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--c-text);
}

.stat-label {
  font-size: 13px;
  color: var(--c-text-secondary);
}

.cards-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 24px;
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

.actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .cards-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 480px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>

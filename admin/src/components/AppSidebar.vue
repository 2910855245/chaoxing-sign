<script setup lang="ts">
import { useRoute } from 'vue-router'
import AppIcon from './AppIcon.vue'

const route = useRoute()

const navItems = [
  { path: '/', icon: 'dashboard', label: '仪表盘' },
  { path: '/accounts', icon: 'accounts', label: '账号管理' },
  { path: '/courses', icon: 'courses', label: '课程管理' },
  { path: '/activities', icon: 'sign', label: '签到活动' },
  { path: '/settings', icon: 'settings', label: '设置' },
]
</script>

<template>
  <aside class="sidebar">
    <div class="logo">
      <AppIcon name="sign" :size="24" />
      <span class="logo-text">签到管理</span>
    </div>

    <nav class="nav">
      <router-link
        v-for="item in navItems"
        :key="item.path"
        :to="item.path"
        class="nav-item"
        :class="{ active: route.path === item.path }"
      >
        <AppIcon :name="item.icon" :size="18" />
        <span class="nav-label">{{ item.label }}</span>
      </router-link>
    </nav>

    <div class="footer">
      <span class="version">v0.1.0</span>
    </div>
  </aside>
</template>

<style scoped>
.sidebar {
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  width: var(--sidebar-w);
  background: var(--c-surface);
  border-right: 1px solid var(--c-border);
  display: flex;
  flex-direction: column;
  z-index: 100;
}

.logo {
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
  border-bottom: 1px solid var(--c-border);
}

.logo-icon {
  font-size: 24px;
}

.logo-text {
  font-size: 18px;
  font-weight: 700;
  color: var(--c-text);
}

.nav {
  flex: 1;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  border-radius: var(--radius-sm);
  color: var(--c-text-secondary);
  text-decoration: none;
  transition: all .15s;
}

.nav-item:hover {
  background: var(--c-bg);
  color: var(--c-text);
}

.nav-item.active {
  background: var(--c-primary-bg);
  color: var(--c-primary);
  font-weight: 600;
}

.nav-icon {
  font-size: 18px;
}

.nav-label {
  font-size: 14px;
}

.footer {
  padding: 16px 20px;
  border-top: 1px solid var(--c-border);
}

.version {
  font-size: 12px;
  color: var(--c-text-muted);
}

@media (max-width: 768px) {
  .sidebar {
    display: none;
  }
}
</style>

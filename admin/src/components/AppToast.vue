<script setup lang="ts">
import { useAppStore } from '@/stores/app'
import AppIcon from './AppIcon.vue'

const store = useAppStore()

const iconMap: Record<string, string> = {
  success: 'success',
  error: 'error',
  warning: 'warning',
  info: 'info',
}
</script>

<template>
  <Teleport to="body">
    <div class="toast-container">
      <TransitionGroup name="slide-up">
        <div
          v-for="toast in store.toasts"
          :key="toast.id"
          class="toast"
          :class="toast.type"
        >
          <AppIcon :name="iconMap[toast.type] || 'info'" :size="18" />
          <span class="toast-message">{{ toast.message }}</span>
        </div>
      </TransitionGroup>
    </div>
  </Teleport>
</template>

<style scoped>
.toast-container {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 1000;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.toast {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 20px;
  border-radius: var(--radius);
  background: var(--c-surface);
  box-shadow: var(--shadow-md);
  min-width: 280px;
  animation: slideUp .3s ease;
}

.toast.success {
  border-left: 4px solid var(--c-success);
}

.toast.error {
  border-left: 4px solid var(--c-danger);
}

.toast.warning {
  border-left: 4px solid var(--c-warning);
}

.toast.info {
  border-left: 4px solid var(--c-info);
}

.toast-icon {
  font-size: 16px;
  font-weight: 700;
}

.toast.success .toast-icon {
  color: var(--c-success);
}

.toast.error .toast-icon {
  color: var(--c-danger);
}

.toast.warning .toast-icon {
  color: var(--c-warning);
}

.toast.info .toast-icon {
  color: var(--c-info);
}

.toast-message {
  font-size: 14px;
  color: var(--c-text);
}
</style>

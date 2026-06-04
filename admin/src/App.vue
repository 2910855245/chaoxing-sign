<script setup lang="ts">
import { onMounted } from 'vue'
import { useAppStore } from '@/stores/app'
import AppSidebar from '@/components/AppSidebar.vue'
import AppToast from '@/components/AppToast.vue'

const store = useAppStore()

onMounted(() => {
  store.loadConfig()
})
</script>

<template>
  <div class="app">
    <AppSidebar />
    <main class="main">
      <router-view />
    </main>
    <AppToast />
  </div>
</template>

<style scoped>
.app {
  display: flex;
  min-height: 100vh;
}

.main {
  flex: 1;
  margin-left: var(--sidebar-w);
  padding: 24px;
  background: var(--c-bg);
}

@media (max-width: 768px) {
  .main {
    margin-left: 0;
    padding: 12px;
  }
}
</style>

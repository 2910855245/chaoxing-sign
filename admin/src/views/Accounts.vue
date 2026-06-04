<script setup lang="ts">
import { ref } from 'vue'
import { useAppStore } from '@/stores/app'
import type { Account } from '@/stores/app'

const store = useAppStore()

// 表单状态
const showForm = ref(false)
const form = ref<Account>({
  username: '',
  password: '',
  name: '',
  role: 'student',
  logged_in: false,
})

// 添加账号
function handleAdd() {
  if (!form.value.username || !form.value.password || !form.value.name) {
    store.toast('请填写完整信息', 'warning')
    return
  }
  store.addAccount({ ...form.value })
  showForm.value = false
  resetForm()
}

// 重置表单
function resetForm() {
  form.value = {
    username: '',
    password: '',
    name: '',
    role: 'student',
    logged_in: false,
  }
}

// 删除账号
function handleDelete(username: string) {
  if (confirm('确定删除该账号？')) {
    store.removeAccount(username)
  }
}
</script>

<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">账号管理</h1>
        <p class="page-desc">管理学习通账号</p>
      </div>
      <button class="btn btn-primary" @click="showForm = true">
        添加账号
      </button>
    </div>

    <!-- 账号列表 -->
    <div class="card">
      <table class="data-table" v-if="store.accounts.length > 0">
        <thead>
          <tr>
            <th>姓名</th>
            <th>用户名</th>
            <th>角色</th>
            <th>状态</th>
            <th>UID</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="account in store.accounts" :key="account.username">
            <td>{{ account.name }}</td>
            <td>{{ account.username }}</td>
            <td>
              <span class="pill" :class="account.role === 'teacher' ? 'primary' : 'info'">
                {{ account.role === 'teacher' ? '教师' : '学生' }}
              </span>
            </td>
            <td>
              <span class="pill" :class="account.logged_in ? 'ok' : 'bad'">
                {{ account.logged_in ? '已登录' : '未登录' }}
              </span>
            </td>
            <td>{{ account.uid || '-' }}</td>
            <td>
              <button class="btn btn-danger btn-sm" @click="handleDelete(account.username)">
                删除
              </button>
            </td>
          </tr>
        </tbody>
      </table>

      <div v-else class="empty">
        <p>暂无账号</p>
        <button class="btn btn-primary" @click="showForm = true">添加账号</button>
      </div>
    </div>

    <!-- 添加账号弹窗 -->
    <Teleport to="body">
      <div v-if="showForm" class="modal-overlay" @click.self="showForm = false">
        <div class="modal-box">
          <h2 class="modal-title">添加账号</h2>

          <div class="field">
            <label>姓名</label>
            <input v-model="form.name" placeholder="请输入姓名" />
          </div>

          <div class="field">
            <label>用户名（学号/手机号）</label>
            <input v-model="form.username" placeholder="请输入用户名" />
          </div>

          <div class="field">
            <label>密码</label>
            <input v-model="form.password" type="password" placeholder="请输入密码" />
          </div>

          <div class="field">
            <label>角色</label>
            <select v-model="form.role">
              <option value="student">学生</option>
              <option value="teacher">教师</option>
            </select>
          </div>

          <div class="modal-actions">
            <button class="btn btn-outline" @click="showForm = false">取消</button>
            <button class="btn btn-primary" @click="handleAdd">添加</button>
          </div>
        </div>
      </div>
    </Teleport>
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

.empty {
  text-align: center;
  padding: 40px;
  color: var(--c-text-secondary);
}

.empty p {
  margin-bottom: 16px;
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15,23,42,.4);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 500;
}

.modal-box {
  background: var(--c-surface);
  border-radius: var(--radius-xl);
  padding: 32px;
  width: 400px;
  max-width: 90vw;
  box-shadow: var(--shadow-lg);
  animation: fadeIn .3s ease;
}

.modal-title {
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 24px;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 24px;
}
</style>

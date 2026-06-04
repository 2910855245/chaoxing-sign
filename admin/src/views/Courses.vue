<script setup lang="ts">
import { ref } from 'vue'
import { useAppStore } from '@/stores/app'
import type { Course } from '@/stores/app'

const store = useAppStore()

// 表单状态
const showForm = ref(false)
const form = ref<Course>({
  course_id: '',
  class_id: '',
  name: '',
})

// 添加课程
function handleAdd() {
  if (!form.value.course_id || !form.value.class_id || !form.value.name) {
    store.toast('请填写完整信息', 'warning')
    return
  }
  store.addCourse({ ...form.value })
  showForm.value = false
  resetForm()
}

// 重置表单
function resetForm() {
  form.value = {
    course_id: '',
    class_id: '',
    name: '',
  }
}

// 删除课程
function handleDelete(courseId: string, classId: string) {
  if (confirm('确定删除该课程？')) {
    store.removeCourse(courseId, classId)
  }
}
</script>

<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h1 class="page-title">课程管理</h1>
        <p class="page-desc">管理监控的课程</p>
      </div>
      <button class="btn btn-primary" @click="showForm = true">
        添加课程
      </button>
    </div>

    <!-- 课程列表 -->
    <div class="card">
      <table class="data-table" v-if="store.courses.length > 0">
        <thead>
          <tr>
            <th>课程名称</th>
            <th>课程ID</th>
            <th>班级ID</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="course in store.courses" :key="course.course_id + course.class_id">
            <td>{{ course.name }}</td>
            <td><code>{{ course.course_id }}</code></td>
            <td><code>{{ course.class_id }}</code></td>
            <td>
              <button class="btn btn-danger btn-sm" @click="handleDelete(course.course_id, course.class_id)">
                删除
              </button>
            </td>
          </tr>
        </tbody>
      </table>

      <div v-else class="empty">
        <p>暂无课程</p>
        <button class="btn btn-primary" @click="showForm = true">添加课程</button>
      </div>
    </div>

    <!-- 添加课程弹窗 -->
    <Teleport to="body">
      <div v-if="showForm" class="modal-overlay" @click.self="showForm = false">
        <div class="modal-box">
          <h2 class="modal-title">添加课程</h2>

          <div class="field">
            <label>课程名称</label>
            <input v-model="form.name" placeholder="请输入课程名称" />
          </div>

          <div class="field">
            <label>课程ID</label>
            <input v-model="form.course_id" placeholder="请输入课程ID" />
          </div>

          <div class="field">
            <label>班级ID</label>
            <input v-model="form.class_id" placeholder="请输入班级ID" />
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

code {
  background: var(--c-bg);
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 13px;
  color: var(--c-primary);
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

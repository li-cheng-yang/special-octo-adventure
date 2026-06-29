<template>
  <div class="profile-page">
    <!-- Header banner -->
    <div class="profile-banner animate-fade-in">
      <div class="banner-bg"></div>
      <div class="banner-content">
        <div class="avatar-section">
          <div class="avatar-wrapper">
            <el-avatar v-if="userStore.user?.avatar" :size="80" :src="avatarUrl" class="profile-avatar" />
            <el-avatar v-else :size="80" class="profile-avatar">{{ userStore.user?.realName?.charAt(0) }}</el-avatar>
            <div class="avatar-overlay" @click="triggerAvatarUpload">
              <el-icon :size="20"><Camera /></el-icon>
            </div>
            <input ref="avatarInput" type="file" accept="image/*" style="display: none" @change="handleAvatarChange" />
          </div>
          <div class="user-meta">
            <h2 class="user-name">{{ userStore.user?.realName }}</h2>
            <p class="user-role">
              <el-tag :type="roleTagType" effect="light" size="small">{{ roleLabel }}</el-tag>
              <span class="user-id" v-if="userStore.user?.studentNo">学号: {{ userStore.user.studentNo }}</span>
            </p>
          </div>
        </div>
      </div>
    </div>

    <!-- Content tabs -->
    <div class="profile-content animate-slide-up">
      <el-tabs v-model="activeTab" class="profile-tabs">
        <el-tab-pane label="基本信息" name="info">
          <el-card class="info-card">
            <div class="section-title">
              <el-icon><User /></el-icon>
              个人信息
            </div>
            <el-form :model="form" label-position="top" class="profile-form">
              <el-row :gutter="24">
                <el-col :xs="24" :md="12">
                  <el-form-item label="用户名">
                    <el-input v-model="username" disabled :prefix-icon="User" />
                  </el-form-item>
                </el-col>
                <el-col :xs="24" :md="12">
                  <el-form-item label="真实姓名">
                    <el-input v-model="form.realName" :prefix-icon="UserFilled" />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row :gutter="24">
                <el-col :xs="24" :md="12">
                  <el-form-item label="邮箱">
                    <el-input v-model="form.email" :prefix-icon="Message" placeholder="请输入邮箱" />
                  </el-form-item>
                </el-col>
                <el-col :xs="24" :md="12">
                  <el-form-item label="手机号">
                    <el-input v-model="form.phone" :prefix-icon="Phone" placeholder="请输入手机号" />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-row :gutter="24">
                <el-col :xs="24" :md="8">
                  <el-form-item label="院系">
                    <el-input v-model="form.department" placeholder="所在院系" />
                  </el-form-item>
                </el-col>
                <el-col :xs="24" :md="8">
                  <el-form-item label="专业">
                    <el-input v-model="form.major" placeholder="所学专业" />
                  </el-form-item>
                </el-col>
                <el-col :xs="24" :md="8">
                  <el-form-item label="班级">
                    <el-input v-model="form.className" placeholder="所在班级" />
                  </el-form-item>
                </el-col>
              </el-row>

              <div class="form-actions">
                <el-button type="primary" :loading="loading" @click="updateInfo" size="large">
                  <el-icon><Check /></el-icon>
                  保存修改
                </el-button>
              </div>
            </el-form>
          </el-card>
        </el-tab-pane>

        <el-tab-pane label="修改密码" name="password">
          <el-card class="info-card">
            <div class="section-title">
              <el-icon><Lock /></el-icon>
              安全设置
            </div>
            <el-form
              :model="passwordForm"
              :rules="passwordRules"
              ref="passwordFormRef"
              label-position="top"
              class="profile-form password-form"
            >
              <el-form-item label="原密码" prop="oldPassword">
                <el-input v-model="passwordForm.oldPassword" type="password" show-password :prefix-icon="Lock" placeholder="请输入当前密码" />
              </el-form-item>
              <el-form-item label="新密码" prop="newPassword">
                <el-input v-model="passwordForm.newPassword" type="password" show-password :prefix-icon="Key" placeholder="请输入新密码（至少6位）" />
              </el-form-item>
              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input v-model="passwordForm.confirmPassword" type="password" show-password :prefix-icon="Key" placeholder="请再次输入新密码" />
              </el-form-item>
              <div class="form-actions">
                <el-button type="primary" :loading="passwordLoading" @click="changePassword" size="large">
                  <el-icon><Check /></el-icon>
                  修改密码
                </el-button>
              </div>
            </el-form>
          </el-card>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import { userApi, fileApi } from '@/api'
import { User, UserFilled, Lock, Check, Message, Phone, Key, Camera } from '@element-plus/icons-vue'

const userStore = useUserStore()
const activeTab = ref('info')
const loading = ref(false)
const passwordLoading = ref(false)
const passwordFormRef = ref()
const avatarInput = ref<HTMLInputElement>()
const username = computed(() => userStore.user?.username || '')

const avatarUrl = computed(() => {
  if (!userStore.user?.avatar) return ''
  return userStore.user.avatar
})

const form = reactive({
  realName: '',
  email: '',
  phone: '',
  department: '',
  major: '',
  className: '',
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (_rule: any, value: string, callback: (error?: Error) => void) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
}

const roleLabel = computed(() => {
  const roles: Record<string, string> = { STUDENT: '学生', TEACHER: '导师', ADMIN: '管理员' }
  return roles[userStore.user?.role || ''] || ''
})

const roleTagType = computed(() => {
  const types: Record<string, string> = { STUDENT: 'success', TEACHER: 'warning', ADMIN: 'danger' }
  return types[userStore.user?.role || ''] || ''
})

const loadUserInfo = () => {
  const user = userStore.user
  if (user) {
    form.realName = user.realName || ''
    form.email = user.email || ''
    form.phone = user.phone || ''
    form.department = user.department || ''
    form.major = user.major || ''
    form.className = user.className || ''
  }
}

const triggerAvatarUpload = () => {
  avatarInput.value?.click()
}

const handleAvatarChange = async (event: Event) => {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return

  if (!file.type.startsWith('image/')) {
    ElMessage.error('请上传图片文件')
    return
  }

  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过5MB')
    return
  }

  try {
    const res = await userApi.uploadAvatar(file)
    if (res.code === 200) {
      userStore.setUser(res.data)
      ElMessage.success('头像上传成功')
    }
  } catch (error) {
    console.error(error)
  } finally {
    input.value = ''
  }
}

const updateInfo = async () => {
  loading.value = true
  try {
    const res = await userApi.updateCurrentUser(form)
    if (res.code === 200) {
      userStore.setUser(res.data)
      ElMessage.success('保存成功')
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const changePassword = async () => {
  await passwordFormRef.value.validate()
  passwordLoading.value = true
  try {
    const res = await userApi.changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
    })
    if (res.code === 200) {
      ElMessage.success('密码修改成功')
      passwordForm.oldPassword = ''
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''
    }
  } catch (error) {
    console.error(error)
  } finally {
    passwordLoading.value = false
  }
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped lang="scss">
.profile-page { padding: 8px; }

.profile-banner { position: relative; margin-bottom: 24px; border-radius: var(--radius-lg); overflow: hidden; }
.banner-bg { height: 140px; background: linear-gradient(135deg, var(--primary-500) 0%, var(--primary-600) 50%, var(--accent-500) 100%); position: relative;
  &::after { content: ''; position: absolute; inset: 0; background: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23ffffff' fill-opacity='0.08'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E"); }
}
.banner-content { padding: 0 28px 0; }

.avatar-section { display: flex; align-items: flex-end; gap: 18px; margin-top: -40px; position: relative; z-index: 2; }

.avatar-wrapper { position: relative; cursor: pointer;
  &:hover .avatar-overlay { opacity: 1; }
}

.profile-avatar { font-size: 32px; font-weight: 700; background: linear-gradient(135deg, #ffffff, #f1f5f9); color: var(--primary-600); border: 4px solid #ffffff; box-shadow: var(--shadow-md); }

.avatar-overlay { position: absolute; inset: 4px; border-radius: 50%; background: rgba(0, 0, 0, 0.4); display: flex; align-items: center; justify-content: center; color: #fff; opacity: 0; transition: opacity var(--transition-fast); }

.avatar-status { position: absolute; bottom: 4px; right: 4px; width: 16px; height: 16px; border-radius: 50%; border: 3px solid #ffffff;
  &.online { background: var(--success-500); }
}

.user-meta { padding-bottom: 12px; }
.user-name { font-size: 22px; font-weight: 700; color: var(--gray-900); margin: 0 0 6px; }
.user-role { display: flex; align-items: center; gap: 10px; margin: 0; }
.user-id { font-size: 13px; color: var(--text-muted); font-weight: 500; }

.profile-content {
  :deep(.el-tabs__nav-wrap) { padding: 0 4px; }
  :deep(.el-tabs__item) { font-size: 14px; padding: 0 20px; height: 42px; line-height: 42px; }
}

.info-card { margin-top: 8px; }
.section-title { display: flex; align-items: center; gap: 8px; font-size: 16px; font-weight: 600; color: var(--gray-700); margin-bottom: 24px; padding-bottom: 16px; border-bottom: 1px solid var(--border-light);
  .el-icon { color: var(--primary-500); }
}
.profile-form {
  :deep(.el-form-item__label) { font-size: 13px; font-weight: 600; color: var(--gray-600); padding-bottom: 4px; }
  :deep(.el-input__wrapper) { padding: 4px 14px; height: 42px; }
}
.password-form { max-width: 480px; }
.form-actions { margin-top: 24px; padding-top: 20px; border-top: 1px solid var(--border-light);
  .el-button { min-width: 140px; }
}

@media (max-width: 768px) {
  .avatar-section { flex-direction: column; align-items: center; text-align: center; margin-top: -30px; }
  .user-role { justify-content: center; }
}
</style>

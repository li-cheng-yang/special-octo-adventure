<template>
  <div class="register-page">
    <!-- Animated background -->
    <div class="register-bg">
      <div class="gradient-orb orb-1"></div>
      <div class="gradient-orb orb-2"></div>
      <div class="gradient-orb orb-3"></div>
      <div class="grid-overlay"></div>
    </div>

    <div class="register-content animate-scale-in">
      <div class="register-card">
        <div class="card-header-section">
          <router-link to="/login" class="back-link">
            <el-icon><ArrowLeft /></el-icon>
            返回登录
          </router-link>
          <div class="welcome-text">开始旅程</div>
          <h2 class="register-title">创建您的账户</h2>
        </div>

        <el-form :model="form" :rules="rules" ref="formRef" class="register-form" label-position="top">
          <div class="form-row">
            <el-form-item prop="username" label="用户名">
              <el-input v-model="form.username" placeholder="设置用户名" :prefix-icon="User" size="large" />
            </el-form-item>
            <el-form-item prop="password" label="密码">
              <el-input v-model="form.password" type="password" placeholder="设置密码" :prefix-icon="Lock" size="large" show-password />
            </el-form-item>
          </div>

          <div class="form-row">
            <el-form-item prop="realName" label="真实姓名">
              <el-input v-model="form.realName" placeholder="请输入真实姓名" :prefix-icon="UserFilled" size="large" />
            </el-form-item>
            <el-form-item prop="role" label="角色">
              <el-select v-model="form.role" placeholder="选择角色" size="large" style="width: 100%">
                <el-option label="学生" value="STUDENT">
                  <div class="role-option">
                    <el-icon><Reading /></el-icon>
                    <span>学生</span>
                  </div>
                </el-option>
                <el-option label="导师" value="TEACHER">
                  <div class="role-option">
                    <el-icon><School /></el-icon>
                    <span>导师</span>
                  </div>
                </el-option>
              </el-select>
            </el-form-item>
          </div>

          <!-- Student-specific fields -->
          <transition name="el-zoom-in-top">
            <div v-if="form.role === 'STUDENT'" class="student-fields">
              <div class="section-divider">
                <span>学籍信息</span>
              </div>
              <div class="form-row three-col">
                <el-form-item prop="studentNo" label="学号">
                  <el-input v-model="form.studentNo" placeholder="请输入学号" size="large" />
                </el-form-item>
                <el-form-item label="院系">
                  <el-input v-model="form.department" placeholder="所在院系" size="large" />
                </el-form-item>
                <el-form-item label="专业">
                  <el-input v-model="form.major" placeholder="所学专业" size="large" />
                </el-form-item>
              </div>
              <el-form-item label="班级">
                <el-input v-model="form.className" placeholder="所在班级" size="large" />
              </el-form-item>
            </div>
          </transition>

          <div class="section-divider">
            <span>联系方式（选填）</span>
          </div>

          <div class="form-row">
            <el-form-item label="邮箱">
              <el-input v-model="form.email" placeholder="电子邮箱" :prefix-icon="Message" size="large" />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="form.phone" placeholder="手机号码" :prefix-icon="Phone" size="large" />
            </el-form-item>
          </div>

          <el-form-item class="submit-item">
            <el-button type="primary" size="large" class="register-btn" :loading="loading" @click="handleRegister">
              <el-icon v-if="!loading"><Check /></el-icon>
              <span>{{ loading ? '注册中...' : '立即注册' }}</span>
            </el-button>
          </el-form-item>
        </el-form>

        <div class="card-footer">
          <span class="text-muted">已有账号？</span>
          <router-link to="/login" class="login-link">
            直接登录
            <el-icon><ArrowRight /></el-icon>
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { authApi, userApi } from '@/api'
import { User, Lock, UserFilled, ArrowLeft, ArrowRight, Check, Reading, School, Message, Phone } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  realName: '',
  role: 'STUDENT' as 'STUDENT' | 'TEACHER',
  studentNo: '',
  department: '',
  major: '',
  className: '',
  email: '',
  phone: '',
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度3-50个字符', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' },
  ],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
  studentNo: [{ required: true, message: '学生必须填写学号', trigger: 'blur' }],
}

const handleRegister = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    const res = await authApi.register(form)
    if (res.code === 200) {
      userStore.setToken(res.data.token)
      const userRes = await userApi.getCurrentUser()
      if (userRes.code === 200) {
        userStore.setUser(userRes.data)
        ElMessage.success({ message: '注册成功，请登录！', duration: 2000 })
        router.push('/login')
      }
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  padding: 24px;
}

.register-bg {
  position: fixed;
  inset: 0;
  background: linear-gradient(135deg, #f0fdfa 0%, #e0e7ff 50%, #f8fafc 100%);
  z-index: 0;
}

.gradient-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.45;
  animation: float 8s ease-in-out infinite;
}

.orb-1 {
  width: 450px;
  height: 450px;
  background: radial-gradient(circle, rgba(20, 184, 166, 0.3) 0%, transparent 70%);
  top: -10%;
  left: -5%;
}

.orb-2 {
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, rgba(99, 102, 241, 0.25) 0%, transparent 70%);
  bottom: -10%;
  right: -5%;
  animation-delay: -3s;
  animation-duration: 10s;
}

.orb-3 {
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, rgba(245, 158, 11, 0.15) 0%, transparent 70%);
  top: 50%;
  right: 20%;
  animation-delay: -5s;
  animation-duration: 12s;
}

.grid-overlay {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(99, 102, 241, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(99, 102, 241, 0.03) 1px, transparent 1px);
  background-size: 50px 50px;
}

.register-content {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 680px;
}

.register-card {
  background: rgba(255, 255, 255, 0.88);
  backdrop-filter: blur(20px);
  border-radius: var(--radius-xl);
  border: 1px solid rgba(255, 255, 255, 0.6);
  padding: 36px 40px;
  box-shadow:
    0 20px 60px rgba(0, 0, 0, 0.06),
    0 8px 24px rgba(99, 102, 241, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
}

.card-header-section {
  text-align: center;
  margin-bottom: 28px;
  position: relative;
}

.back-link {
  position: absolute;
  left: 0;
  top: 0;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  font-weight: 500;
  color: var(--gray-400);
  text-decoration: none;
  transition: color var(--transition-fast);

  &:hover {
    color: var(--primary-500);
  }
}

.welcome-text {
  font-size: 14px;
  font-weight: 500;
  color: var(--accent-600);
  margin-bottom: 8px;
}

.register-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--gray-900);
}

.register-form {
  :deep(.el-form-item__label) {
    font-size: 13px;
    font-weight: 600;
    color: var(--gray-600);
    padding-bottom: 4px;
  }

  :deep(.el-input__wrapper) {
    padding: 4px 14px;
    height: 44px;
  }
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;

  &.three-col {
    grid-template-columns: 1fr 1fr 1fr;
  }
}

.role-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.section-divider {
  display: flex;
  align-items: center;
  margin: 8px 0 12px;
  font-size: 12px;
  font-weight: 600;
  color: var(--gray-400);
  text-transform: uppercase;
  letter-spacing: 0.5px;

  &::before,
  &::after {
    content: '';
    flex: 1;
    height: 1px;
    background: var(--border-light);
  }

  &::before { margin-right: 12px; }
  &::after { margin-left: 12px; }
}

.student-fields {
  animation: fadeIn 0.3s ease-out;
}

.submit-item {
  margin-top: 8px;
  margin-bottom: 0;
}

.register-btn {
  width: 100%;
  height: 48px;
  font-size: 15px;
  font-weight: 600;

  .el-icon {
    margin-right: 6px;
  }
}

.card-footer {
  text-align: center;
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid var(--border-light);

  .text-muted {
    font-size: 14px;
    color: var(--gray-400);
  }
}

.login-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  margin-left: 8px;
  font-size: 14px;
  font-weight: 600;
  color: var(--primary-500);
  text-decoration: none;
  transition: all var(--transition-fast);

  &:hover {
    color: var(--primary-600);
    gap: 6px;
  }
}

@media (max-width: 640px) {
  .form-row {
    grid-template-columns: 1fr;

    &.three-col {
      grid-template-columns: 1fr;
    }
  }

  .register-card {
    padding: 28px 24px;
  }
}
</style>

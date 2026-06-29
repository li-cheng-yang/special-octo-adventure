<template>
  <div class="login-page">
    <!-- Animated background -->
    <div class="login-bg">
      <div class="gradient-orb orb-1"></div>
      <div class="gradient-orb orb-2"></div>
      <div class="gradient-orb orb-3"></div>
      <div class="grid-overlay"></div>
    </div>

    <div class="login-content">
      <!-- Left side - branding -->
      <div class="login-branding animate-fade-in">
        <div class="brand-badge">
          <el-icon :size="20"><Collection /></el-icon>
          <span>实习汇报管理系统</span>
        </div>
        <h1 class="brand-title">
          高效管理<br />
          <span class="gradient-text">实习与汇报</span>
        </h1>
        <p class="brand-desc">
          专为高校设计的实习管理解决方案，<br />
          覆盖学生、导师、管理员三大角色，<br />
          让实习管理更轻松高效。
        </p>
        <div class="feature-chips">
          <div class="chip"><el-icon><Check /></el-icon> 实习信息管理</div>
          <div class="chip"><el-icon><Check /></el-icon> 周报月报提交</div>
          <div class="chip"><el-icon><Check /></el-icon> 导师在线批阅</div>
          <div class="chip"><el-icon><Check /></el-icon> 数据统计分析</div>
        </div>
      </div>

      <!-- Right side - login form -->
      <div class="login-card-wrapper animate-slide-up">
        <div class="login-card">
          <div class="card-header-section">
            <div class="welcome-text">欢迎回来</div>
            <h2 class="login-title">登录您的账户</h2>
          </div>

          <el-form
            :model="form"
            :rules="rules"
            ref="formRef"
            class="login-form"
            @keyup.enter="handleLogin"
          >
            <el-form-item prop="username">
              <div class="input-label">用户名</div>
              <el-input
                v-model="form.username"
                placeholder="请输入用户名"
                size="large"
                :prefix-icon="User"
                class="custom-input"
              />
            </el-form-item>

            <el-form-item prop="password">
              <div class="input-label">密码</div>
              <el-input
                v-model="form.password"
                type="password"
                placeholder="请输入密码"
                size="large"
                :prefix-icon="Lock"
                show-password
                class="custom-input"
              />
            </el-form-item>

            <div class="form-options">
              <el-checkbox v-model="rememberMe">记住我</el-checkbox>
              <a class="forgot-link">忘记密码?</a>
            </div>

            <el-form-item class="submit-item">
              <el-button
                type="primary"
                size="large"
                class="login-btn"
                :loading="loading"
                @click="handleLogin"
              >
                <el-icon v-if="!loading" class="btn-icon"><ArrowRight /></el-icon>
                <span>{{ loading ? '登录中...' : '立即登录' }}</span>
              </el-button>
            </el-form-item>
          </el-form>

          <div class="card-footer">
            <span class="text-muted">还没有账号？</span>
            <router-link to="/register" class="register-link">
              立即注册
              <el-icon><ArrowRight /></el-icon>
            </router-link>
          </div>
        </div>

        <div class="demo-hint">
          <el-icon><InfoFilled /></el-icon>
          <span>演示账号: admin/123456, teacher1/123456, student1/123456</span>
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
import { User, Lock, ArrowRight, Collection, Check, InfoFilled } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)
const rememberMe = ref(false)

const form = reactive({
  username: '',
  password: '',
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const handleLogin = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    const res = await authApi.login(form)
    if (res.code === 200) {
      userStore.setToken(res.data.token)
      // v2.1 保存 Refresh Token
      if (res.data.refreshToken) {
        userStore.setRefreshToken(res.data.refreshToken)
      }
      const userRes = await userApi.getCurrentUser()
      if (userRes.code === 200) {
        userStore.setUser(userRes.data)
        ElMessage.success({ message: '登录成功，欢迎回来!', duration: 2000 })
        const role = res.data.role
        if (role === 'STUDENT') {
          router.push('/student/internships')
        } else if (role === 'TEACHER') {
          router.push('/teacher/students')
        } else {
          router.push('/admin/users')
        }
      }
    }
  } catch (error: any) {
    console.error('[Login] 登录失败:', error)
    const msg = error.response?.data?.message || error.message || '登录失败，请检查用户名和密码'
    ElMessage.error(msg)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  padding: 24px;
}

.login-bg {
  position: fixed;
  inset: 0;
  background: linear-gradient(135deg, #f8fafc 0%, #e0e7ff 50%, #f0fdfa 100%);
  z-index: 0;
}

.gradient-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.5;
  animation: float 8s ease-in-out infinite;
}

.orb-1 {
  width: 500px;
  height: 500px;
  background: radial-gradient(circle, rgba(99, 102, 241, 0.3) 0%, transparent 70%);
  top: -10%;
  right: -5%;
  animation-delay: 0s;
}

.orb-2 {
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, rgba(20, 184, 166, 0.25) 0%, transparent 70%);
  bottom: -10%;
  left: -5%;
  animation-delay: -3s;
  animation-duration: 10s;
}

.orb-3 {
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, rgba(244, 63, 94, 0.15) 0%, transparent 70%);
  top: 40%;
  left: 30%;
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

.login-content {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 80px;
  max-width: 1200px;
  width: 100%;
}

// Left branding
.login-branding {
  flex: 1;
  max-width: 520px;
}

.brand-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(12px);
  border-radius: var(--radius-full);
  border: 1px solid rgba(255, 255, 255, 0.5);
  font-size: 14px;
  font-weight: 600;
  color: var(--primary-600);
  margin-bottom: 32px;
  box-shadow: var(--shadow-sm);
}

.brand-title {
  font-size: 48px;
  font-weight: 700;
  line-height: 1.15;
  color: var(--gray-900);
  margin-bottom: 20px;
  letter-spacing: -1px;
}

.gradient-text {
  background: linear-gradient(135deg, var(--primary-500) 0%, var(--accent-500) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.brand-desc {
  font-size: 17px;
  line-height: 1.7;
  color: var(--text-secondary);
  margin-bottom: 32px;
}

.feature-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(8px);
  border-radius: var(--radius-full);
  font-size: 13px;
  font-weight: 500;
  color: var(--gray-600);
  border: 1px solid var(--border-color);
  box-shadow: var(--shadow-xs);

  .el-icon {
    color: var(--success-500);
    font-size: 14px;
  }
}

// Right card
.login-card-wrapper {
  flex-shrink: 0;
  width: 440px;
}

.login-card {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  border-radius: var(--radius-xl);
  border: 1px solid rgba(255, 255, 255, 0.6);
  padding: 40px 36px;
  box-shadow:
    0 20px 60px rgba(0, 0, 0, 0.06),
    0 8px 24px rgba(99, 102, 241, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
}

.card-header-section {
  text-align: center;
  margin-bottom: 32px;
}

.welcome-text {
  font-size: 14px;
  font-weight: 500;
  color: var(--primary-500);
  margin-bottom: 8px;
}

.login-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--gray-900);
}

.input-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--gray-600);
  margin-bottom: 6px;
}

.login-form {
  :deep(.el-input__wrapper) {
    padding: 4px 14px;
    height: 46px;
    border-radius: var(--radius-md);
  }
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 4px 0 20px;

  :deep(.el-checkbox__label) {
    font-size: 13px;
    color: var(--gray-500);
  }
}

.forgot-link {
  font-size: 13px;
  font-weight: 500;
  color: var(--primary-500);
  text-decoration: none;
  transition: color var(--transition-fast);

  &:hover {
    color: var(--primary-600);
  }
}

.submit-item {
  margin-bottom: 0;
}

.login-btn {
  width: 100%;
  height: 48px;
  font-size: 15px;
  font-weight: 600;
  border-radius: var(--radius-md);

  .btn-icon {
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

.register-link {
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

.demo-hint {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-top: 20px;
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(8px);
  border-radius: var(--radius-md);
  font-size: 12px;
  color: var(--gray-500);
  border: 1px solid rgba(255, 255, 255, 0.5);

  .el-icon {
    color: var(--primary-400);
    flex-shrink: 0;
  }
}

// Responsive
@media (max-width: 960px) {
  .login-content {
    flex-direction: column;
    gap: 40px;
  }

  .login-branding {
    text-align: center;
    max-width: 100%;
  }

  .brand-title {
    font-size: 36px;
  }

  .feature-chips {
    justify-content: center;
  }

  .login-card-wrapper {
    width: 100%;
    max-width: 440px;
  }
}
</style>

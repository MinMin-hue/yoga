<template>
  <div class="login-page">
    <div class="login-bg">
      <div class="bg-blob blob-1"></div>
      <div class="bg-blob blob-2"></div>
      <div class="bg-blob blob-3"></div>
    </div>

    <div class="login-card">
      <div class="login-left">
        <div class="brand-icon">🧘</div>
        <h1 class="brand-title">瑜伽馆管理后台</h1>
        <p class="brand-sub">一站式会员、课程、订单管理平台</p>
        <ul class="brand-features">
          <li><span class="dot success"></span>会员与会员卡全生命周期管理</li>
          <li><span class="dot info"></span>可视化排课与预约管理</li>
          <li><span class="dot warning"></span>营收统计 + 数据看板</li>
        </ul>
      </div>
      <div class="login-right">
        <h2 class="form-title">欢迎回来 👋</h2>
        <p class="form-sub">请使用管理员账号登录</p>
        <el-form :model="form" :rules="rules" ref="formRef" size="large" @keyup.enter="onLogin">
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="请输入账号" :prefix-icon="User" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" :prefix-icon="Lock" />
          </el-form-item>
          <el-button type="primary" size="large" :loading="loading" class="submit-btn" @click="onLogin">
            登 录
          </el-button>
        </el-form>
        <div class="form-tip">
          <el-icon><InfoFilled /></el-icon>
          默认账号 <code>admin</code> / 密码 <code>admin123</code>
        </div>
      </div>
    </div>

    <div class="login-footer">© 2026 Yoga Studio · 用心做好每一节课</div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, InfoFilled } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'

const router = useRouter()
const user = useUserStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({ username: 'admin', password: 'admin123' })
const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const onLogin = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    await user.login(form)
    ElMessage.success('登录成功')
    router.push('/')
  } catch (e: any) {
    // http 拦截器已提示
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex; flex-direction: column;
  align-items: center; justify-content: center;
  position: relative; overflow: hidden;
  background: var(--y-gradient-primary);
  font-family: -apple-system, BlinkMacSystemFont, "PingFang SC", "Microsoft YaHei", sans-serif;
}

/* 装饰光斑 */
.login-bg { position: absolute; inset: 0; pointer-events: none; }
.bg-blob {
  position: absolute; border-radius: 50%;
  filter: blur(60px); opacity: .55;
}
.blob-1 {
  width: 380px; height: 380px;
  background: rgba(236, 72, 153, 0.55);
  top: -100px; left: -120px;
  animation: float 14s ease-in-out infinite;
}
.blob-2 {
  width: 320px; height: 320px;
  background: rgba(56, 189, 248, 0.45);
  bottom: -80px; right: -100px;
  animation: float 18s ease-in-out infinite reverse;
}
.blob-3 {
  width: 220px; height: 220px;
  background: rgba(251, 191, 36, 0.35);
  top: 30%; right: 25%;
  animation: float 22s ease-in-out infinite;
}
@keyframes float {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(40px, -30px); }
}

.login-card {
  position: relative; z-index: 1;
  display: flex; width: 880px; max-width: calc(100vw - 40px);
  background: rgba(255,255,255,.96);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 30px 80px rgba(15, 23, 42, 0.25);
}

/* 左侧品牌区 */
.login-left {
  flex: 0 0 380px;
  background: var(--y-gradient-dark);
  color: #fff;
  padding: 50px 40px;
  position: relative; overflow: hidden;
}
.login-left::before {
  content: ''; position: absolute;
  top: -100px; right: -100px;
  width: 260px; height: 260px; border-radius: 50%;
  background: rgba(236, 72, 153, 0.25);
}
.login-left::after {
  content: ''; position: absolute;
  bottom: -120px; left: -80px;
  width: 220px; height: 220px; border-radius: 50%;
  background: rgba(102, 126, 234, 0.3);
}
.brand-icon {
  width: 64px; height: 64px; border-radius: 18px;
  background: var(--y-gradient-warm);
  display: flex; align-items: center; justify-content: center;
  font-size: 32px; margin-bottom: 20px;
  box-shadow: 0 10px 24px rgba(245, 87, 108, 0.45);
  position: relative; z-index: 1;
}
.brand-title { font-size: 24px; font-weight: 700; margin: 0 0 8px; position: relative; z-index: 1; }
.brand-sub { font-size: 13px; opacity: .75; margin: 0 0 32px; position: relative; z-index: 1; }
.brand-features { list-style: none; padding: 0; margin: 0; position: relative; z-index: 1; }
.brand-features li {
  display: flex; align-items: center; gap: 8px;
  font-size: 13px; padding: 8px 0;
  opacity: .9;
}
.brand-features .dot { width: 6px; height: 6px; }

/* 右侧表单区 */
.login-right { flex: 1; padding: 50px 50px 40px; }
.form-title { font-size: 22px; font-weight: 700; color: var(--y-text); margin: 0 0 6px; }
.form-sub { font-size: 13px; color: var(--y-text-muted); margin: 0 0 28px; }

.submit-btn {
  width: 100%; height: 44px;
  font-size: 15px; font-weight: 600; letter-spacing: 4px;
  background: var(--y-gradient-primary) !important;
  border: none !important;
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.35);
  transition: all .25s ease;
}
.submit-btn:hover { transform: translateY(-1px); box-shadow: 0 12px 28px rgba(102, 126, 234, 0.45); }

.form-tip {
  display: flex; align-items: center; gap: 6px;
  margin-top: 20px; padding: 10px 14px;
  background: #f1f5f9; border-radius: 8px;
  font-size: 12px; color: var(--y-text-secondary);
}
.form-tip code {
  background: #fff; padding: 1px 6px; border-radius: 4px;
  font-family: "JetBrains Mono", monospace; color: var(--y-primary);
}

.login-footer {
  position: relative; z-index: 1;
  color: rgba(255,255,255,.7);
  font-size: 12px;
  margin-top: 24px;
  letter-spacing: 1px;
}

@media (max-width: 768px) {
  .login-left { display: none; }
  .login-card { width: 420px; }
  .login-right { padding: 40px 30px; }
}
</style>

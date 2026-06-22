<template>
  <div class="login-wrap">
    <div class="login-bg">
      <div class="bg-blob blob-1"></div>
      <div class="bg-blob blob-2"></div>
      <div class="bg-blob blob-3"></div>
    </div>

    <div class="login-card">
      <!-- 左侧品牌区 -->
      <div class="login-brand">
        <div class="brand-mark">🧘</div>
        <h1 class="brand-title">瑜伽馆后台</h1>
        <p class="brand-sub">Yoga Studio Admin Console</p>
        <ul class="brand-feats">
          <li><span class="feat-icon">📅</span> 智能排课与预约</li>
          <li><span class="feat-icon">💳</span> 会员卡 / 课程管理</li>
          <li><span class="feat-icon">📊</span> 实时数据统计</li>
        </ul>
      </div>

      <!-- 右侧表单区 -->
      <div class="login-form">
        <h2 class="form-title">欢迎回来 👋</h2>
        <p class="form-sub">请使用管理员账号登录</p>

        <el-alert v-if="errMsg" :title="errMsg" type="error" show-icon :closable="false" style="margin-bottom: 16px" />

        <el-form :model="form" @submit.prevent="onSubmit">
          <el-form-item>
            <el-input v-model="form.username" placeholder="用户名" size="large" :prefix-icon="User" />
          </el-form-item>
          <el-form-item>
            <el-input v-model="form.password" placeholder="密码" type="password" size="large" show-password :prefix-icon="Lock" @keyup.enter="onSubmit" />
          </el-form-item>
          <el-button type="primary" size="large" :loading="loading" @click="onSubmit" style="width: 100%; margin-top: 8px">
            登 录
          </el-button>
        </el-form>

        <p class="form-tip">默认管理员账号: <b>admin / admin123</b></p>
      </div>
    </div>

    <div class="login-footer">© 2026 Yoga Studio Admin</div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'

const router = useRouter()
const user = useUserStore()
const loading = ref(false)
const errMsg = ref('')
const form = reactive({ username: '', password: '' })

const onSubmit = async () => {
  if (!form.username || !form.password) { errMsg.value = '请填写完整的登录信息'; return }
  errMsg.value = ''
  loading.value = true
  try {
    await user.login(form.username, form.password)
    ElMessage.success('登录成功')
    router.push('/')
  } catch (e: any) {
    errMsg.value = e?.message || '登录失败, 请检查账号密码'
  } finally { loading.value = false }
}
</script>

<style scoped>
.login-wrap {
  position: relative; min-height: 100vh;
  display: flex; align-items: center; justify-content: center;
  background: var(--y-gradient-dark);
  padding: 20px; overflow: hidden;
}
.login-bg {
  position: absolute; inset: 0; pointer-events: none; z-index: 0;
}
.bg-blob {
  position: absolute; border-radius: 50%;
  filter: blur(80px); opacity: .35;
  animation: float 12s ease-in-out infinite;
}
.blob-1 {
  width: 360px; height: 360px;
  top: -100px; left: -80px;
  background: var(--y-gradient-primary);
}
.blob-2 {
  width: 280px; height: 280px;
  bottom: -60px; right: -60px;
  background: var(--y-gradient-warm);
  animation-delay: -4s;
}
.blob-3 {
  width: 220px; height: 220px;
  bottom: 100px; left: 40%;
  background: var(--y-gradient-cool);
  animation-delay: -8s;
}
@keyframes float {
  0%, 100% { transform: translate(0, 0); }
  50% { transform: translate(30px, -20px); }
}

.login-card {
  position: relative; z-index: 1;
  display: flex; width: 920px; max-width: 100%;
  background: #fff; border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 30px 60px rgba(0,0,0,.35);
}

.login-brand {
  flex: 1; padding: 56px 40px;
  background: var(--y-gradient-primary);
  color: #fff; position: relative; overflow: hidden;
}
.login-brand::before, .login-brand::after {
  content: ''; position: absolute; border-radius: 50%;
  background: rgba(255,255,255,.08);
}
.login-brand::before { width: 180px; height: 180px; top: -40px; right: -40px; }
.login-brand::after { width: 120px; height: 120px; bottom: -30px; left: -30px; }

.brand-mark {
  width: 64px; height: 64px; border-radius: 18px;
  background: rgba(255,255,255,.2);
  display: flex; align-items: center; justify-content: center;
  font-size: 30px; backdrop-filter: blur(8px);
  margin-bottom: 24px;
}
.brand-title { font-size: 30px; font-weight: 700; margin: 0 0 8px; position: relative; }
.brand-sub { opacity: .8; font-size: 14px; margin: 0 0 36px; position: relative; }
.brand-feats { list-style: none; padding: 0; margin: 0; position: relative; }
.brand-feats li {
  display: flex; align-items: center; gap: 12px;
  font-size: 14px; padding: 10px 0;
}
.feat-icon { font-size: 20px; }

.login-form { flex: 1; padding: 56px 48px; }
.form-title { font-size: 26px; font-weight: 700; color: var(--y-text); margin: 0 0 6px; }
.form-sub { font-size: 14px; color: var(--y-text-muted); margin: 0 0 28px; }
.form-tip { font-size: 12px; color: var(--y-text-muted); margin: 16px 0 0; text-align: center; }

.login-footer {
  position: absolute; bottom: 20px; left: 0; right: 0;
  text-align: center; color: rgba(255,255,255,.45);
  font-size: 12px; z-index: 1;
}

@media (max-width: 768px) {
  .login-brand { display: none; }
  .login-form { padding: 40px 28px; }
  .login-card { width: 100%; }
}
</style>

<template>
  <div style="min-height: 100vh; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
    <el-card style="width: 400px; border-radius: 12px;">
      <h2 style="text-align: center; margin: 0 0 24px;">🧘 瑜伽馆管理后台</h2>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px" @keyup.enter="onLogin">
        <el-form-item label="账号" prop="username">
          <el-input v-model="form.username" placeholder="请输入账号" size="large" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" size="large" />
        </el-form-item>
        <el-button type="primary" size="large" style="width: 100%;" :loading="loading" @click="onLogin">登录</el-button>
      </el-form>
      <p style="color: #909399; font-size: 12px; text-align: center; margin-top: 16px;">默认账号: admin / admin123</p>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
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

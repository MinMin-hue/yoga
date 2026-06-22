<template>
  <el-container style="height: 100vh">
    <el-aside width="220px" style="background: #001529; color: #fff">
      <div style="height: 60px; line-height: 60px; text-align: center; font-size: 18px; font-weight: 600; color: #fff;">
        🧘 瑜伽馆后台
      </div>
      <el-menu
        :default-active="$route.path"
        router
        background-color="#001529"
        text-color="#ccc"
        active-text-color="#fff"
      >
        <el-menu-item index="/dashboard"><el-icon><DataLine /></el-icon>工作台</el-menu-item>
        <el-menu-item index="/member"><el-icon><User /></el-icon>会员管理</el-menu-item>
        <el-menu-item index="/card-type"><el-icon><CreditCard /></el-icon>会员卡类型</el-menu-item>
        <el-menu-item index="/card-instance"><el-icon><Tickets /></el-icon>会员卡实例</el-menu-item>
        <el-menu-item index="/course-type"><el-icon><Reading /></el-icon>课程类型</el-menu-item>
        <el-menu-item index="/room"><el-icon><House /></el-icon>教室管理</el-menu-item>
        <el-menu-item index="/coach"><el-icon><Avatar /></el-icon>教练管理</el-menu-item>
        <el-menu-item index="/schedule"><el-icon><Calendar /></el-icon>排课管理</el-menu-item>
        <el-menu-item index="/booking"><el-icon><Document /></el-icon>预约管理</el-menu-item>
        <el-menu-item index="/order"><el-icon><Money /></el-icon>订单管理</el-menu-item>
        <el-menu-item index="/statistics"><el-icon><TrendCharts /></el-icon>数据统计</el-menu-item>
        <el-menu-item index="/system"><el-icon><Setting /></el-icon>系统设置</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header style="background: #fff; border-bottom: 1px solid #eee; display: flex; align-items: center; justify-content: space-between;">
        <div style="font-size: 16px;">{{ $route.meta.title || '后台' }}</div>
        <el-dropdown @command="onCmd">
          <span style="cursor: pointer">
            {{ user.profile?.realName || user.profile?.username || '管理员' }}
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </el-header>
      <el-main style="background: #f5f7fa; padding: 16px;">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'

const user = useUserStore()
const router = useRouter()

const onCmd = (cmd: string) => {
  if (cmd === 'logout') {
    user.logout()
    router.push('/login')
  }
}
</script>

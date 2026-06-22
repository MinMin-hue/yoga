<template>
  <el-container class="y-layout">
    <el-aside :width="collapsed ? '68px' : '220px'" class="y-aside">
      <div class="y-logo" :class="{ collapsed }">
        <div class="y-logo-mark">🧘</div>
        <div class="y-logo-text" v-show="!collapsed">
          <div class="y-logo-title">瑜伽馆后台</div>
          <div class="y-logo-sub">Yoga Studio Admin</div>
        </div>
      </div>
      <el-menu
        class="y-menu"
        :default-active="$route.path"
        router
        :collapse="collapsed"
        :collapse-transition="false"
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataLine /></el-icon>
          <template #title>工作台</template>
        </el-menu-item>
        <el-menu-item index="/member">
          <el-icon><User /></el-icon>
          <template #title>会员管理</template>
        </el-menu-item>
        <el-menu-item index="/card-type">
          <el-icon><CreditCard /></el-icon>
          <template #title>会员卡类型</template>
        </el-menu-item>
        <el-menu-item index="/card-instance">
          <el-icon><Tickets /></el-icon>
          <template #title>会员卡实例</template>
        </el-menu-item>
        <el-menu-item index="/course-type">
          <el-icon><Reading /></el-icon>
          <template #title>课程类型</template>
        </el-menu-item>
        <el-menu-item index="/room">
          <el-icon><House /></el-icon>
          <template #title>教室管理</template>
        </el-menu-item>
        <el-menu-item index="/coach">
          <el-icon><Avatar /></el-icon>
          <template #title>教练管理</template>
        </el-menu-item>
        <el-menu-item index="/schedule">
          <el-icon><Calendar /></el-icon>
          <template #title>排课管理</template>
        </el-menu-item>
        <el-menu-item index="/booking">
          <el-icon><Document /></el-icon>
          <template #title>预约管理</template>
        </el-menu-item>
        <el-menu-item index="/order">
          <el-icon><Money /></el-icon>
          <template #title>订单管理</template>
        </el-menu-item>
        <el-menu-item index="/statistics">
          <el-icon><TrendCharts /></el-icon>
          <template #title>数据统计</template>
        </el-menu-item>
        <el-menu-item index="/system">
          <el-icon><Setting /></el-icon>
          <template #title>系统设置</template>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="y-header">
        <div class="y-header-left">
          <el-icon class="y-toggle" @click="collapsed = !collapsed">
            <Fold v-if="!collapsed" /><Expand v-else />
          </el-icon>
          <div class="y-breadcrumb">
            <span class="y-page-name">{{ $route.meta.title || '后台' }}</span>
          </div>
        </div>
        <div class="y-header-right">
          <el-tooltip content="刷新当前页">
            <el-icon class="y-header-icon" @click="refresh"><Refresh /></el-icon>
          </el-tooltip>
          <el-dropdown @command="onCmd">
            <div class="y-user">
              <div class="y-avatar">{{ avatarChar }}</div>
              <div class="y-user-info">
                <div class="y-user-name">{{ user.profile?.realName || user.profile?.username || '管理员' }}</div>
                <div class="y-user-role">{{ user.profile?.role || 'ADMIN' }}</div>
              </div>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">
                  <el-icon><SwitchButton /></el-icon> 退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main class="y-main">
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'

const user = useUserStore()
const router = useRouter()
const collapsed = ref(false)

const avatarChar = computed(() => {
  const n = user.profile?.realName || user.profile?.username || '管'
  return n.charAt(0).toUpperCase()
})

const onCmd = (cmd: string) => {
  if (cmd === 'logout') {
    user.logout()
    router.push('/login')
  }
}
const refresh = () => {
  router.go(0)
}
</script>

<style scoped>
.y-layout { height: 100vh; }

.y-aside {
  background: var(--y-gradient-dark);
  color: #fff;
  transition: width .25s ease;
  box-shadow: var(--y-shadow-lg);
  position: relative;
  z-index: 10;
}

.y-logo {
  display: flex; align-items: center; gap: 12px;
  height: 64px; padding: 0 18px;
  border-bottom: 1px solid rgba(255,255,255,.08);
  overflow: hidden;
}
.y-logo.collapsed { padding: 0; justify-content: center; }
.y-logo-mark {
  width: 36px; height: 36px; border-radius: 10px;
  background: var(--y-gradient-warm);
  display: flex; align-items: center; justify-content: center;
  font-size: 20px; flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(245,87,108,.4);
}
.y-logo-title { font-size: 15px; font-weight: 700; line-height: 1.2; white-space: nowrap; }
.y-logo-sub { font-size: 10px; opacity: .55; letter-spacing: 1px; margin-top: 2px; white-space: nowrap; }

.y-menu {
  background: transparent !important;
  border-right: none !important;
  padding: 12px 8px;
}
:deep(.y-menu .el-menu-item) {
  color: rgba(255,255,255,.7) !important;
  border-radius: 8px;
  margin: 2px 0;
  height: 42px;
  line-height: 42px;
  transition: all .2s ease;
}
:deep(.y-menu .el-menu-item:hover) {
  background: rgba(255,255,255,.06) !important;
  color: #fff !important;
  transform: translateX(2px);
}
:deep(.y-menu .el-menu-item.is-active) {
  background: var(--y-gradient-primary) !important;
  color: #fff !important;
  font-weight: 600;
  box-shadow: 0 4px 14px rgba(102,126,234,.4);
}
:deep(.y-menu .el-menu-item .el-icon) {
  font-size: 17px;
  color: inherit;
}

.y-header {
  background: #fff;
  border-bottom: 1px solid var(--y-border);
  display: flex; align-items: center; justify-content: space-between;
  padding: 0 24px;
  height: 64px;
  box-shadow: 0 1px 0 var(--y-border);
}
.y-header-left { display: flex; align-items: center; gap: 16px; }
.y-toggle {
  font-size: 18px; color: var(--y-text-secondary);
  padding: 6px; border-radius: 6px;
  cursor: pointer; transition: all .2s;
}
.y-toggle:hover { background: #f1f5f9; color: var(--y-primary); }
.y-page-name { font-size: 16px; font-weight: 600; color: var(--y-text); }

.y-header-right { display: flex; align-items: center; gap: 16px; }
.y-header-icon {
  font-size: 18px; color: var(--y-text-secondary);
  padding: 8px; border-radius: 8px; cursor: pointer;
  transition: all .2s;
}
.y-header-icon:hover { background: #f1f5f9; color: var(--y-primary); }

.y-user {
  display: flex; align-items: center; gap: 10px;
  padding: 6px 10px 6px 6px; border-radius: 10px;
  cursor: pointer; transition: background .2s;
}
.y-user:hover { background: #f8fafc; }
.y-avatar {
  width: 36px; height: 36px; border-radius: 50%;
  background: var(--y-gradient-primary);
  color: #fff; display: flex; align-items: center; justify-content: center;
  font-weight: 600; font-size: 15px;
  box-shadow: 0 2px 6px rgba(102,126,234,.3);
}
.y-user-info { line-height: 1.2; }
.y-user-name { font-size: 13px; font-weight: 600; color: var(--y-text); }
.y-user-role { font-size: 11px; color: var(--y-text-muted); margin-top: 2px; }

.y-main {
  background: var(--y-bg);
  padding: 20px;
  overflow-y: auto;
}

.fade-slide-enter-active, .fade-slide-leave-active { transition: all .25s ease; }
.fade-slide-enter-from { opacity: 0; transform: translateY(8px); }
.fade-slide-leave-to { opacity: 0; transform: translateY(-4px); }
</style>

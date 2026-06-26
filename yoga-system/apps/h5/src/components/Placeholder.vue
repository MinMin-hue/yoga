<script setup lang="ts">
import { onLoad } from '@dcloudio/uni-app'
import { ref } from 'vue'

interface Props {
  icon?: string
  title?: string
  desc?: string
  iconColor?: string
}

const props = withDefaults(defineProps<Props>(), {
  icon: '◌',
  title: '敬请期待',
  desc: '该功能正在完善中, 即将上线',
  iconColor: '#FF6B3D'
})

onLoad(() => {
  uni.setNavigationBarTitle({ title: props.title })
})
</script>

<template>
  <view class="placeholder">
    <view class="ph-icon" :style="{ color: iconColor }">{{ icon }}</view>
    <view class="ph-title">{{ title }}</view>
    <view class="ph-desc">{{ desc }}</view>
  </view>
</template>

<style scoped>
.placeholder {
  min-height: 60vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 24px;
  text-align: center;
}
.ph-icon {
  font-size: 80px;
  line-height: 1;
  font-weight: 200;
  margin-bottom: 16px;
  opacity: .8;
}
.ph-title {
  font-size: 18px;
  font-weight: 600;
  color: #1F1F1F;
  margin-bottom: 8px;
}
.ph-desc {
  font-size: 13px;
  color: #909399;
  line-height: 1.6;
}
</style>

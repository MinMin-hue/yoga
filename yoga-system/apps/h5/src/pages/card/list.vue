<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { api, type CardType, type CardKind } from '@/api'

const list = ref<CardType[]>([])
const loading = ref(false)

const kindName = (k: CardKind) =>
  ({ TIME: '时间卡', TIMES: '次卡', MIXED: '混合卡' } as const)[k] || k

const kindColor = (k: CardKind) =>
  ({ TIME: '#FF6B3D', TIMES: '#A06BFF', MIXED: '#23B573' } as Record<CardKind, string>)[k] || '#909399'

const load = async () => {
  loading.value = true
  try { list.value = await api.h5.cardTypes() } catch { /* */ }
  finally { loading.value = false }
}
onMounted(load)

const goBuy = (id: number) => uni.navigateTo({ url: `/pages/card/buy?id=${id}` })
</script>

<template>
  <view class="page">
    <view v-if="loading" class="loading">加载中...</view>
    <view v-else-if="list.length === 0" class="empty">
      <view class="empty-icon">▦</view>
      <text>暂无会员卡套餐</text>
    </view>
    <view
      v-for="c in list"
      :key="c.id"
      class="card"
      @click="goBuy(c.id)"
    >
      <view class="card-side" :style="{ background: kindColor(c.cardKind) }">
        <view class="side-name">{{ c.name }}</view>
        <view class="side-kind">{{ kindName(c.cardKind) }}</view>
      </view>
      <view class="card-main">
        <view class="card-top">
          <text class="card-name">{{ c.name }}</text>
          <text class="tag" :style="{ color: kindColor(c.cardKind), background: kindColor(c.cardKind) + '20' }">{{ kindName(c.cardKind) }}</text>
        </view>
        <view class="card-price">
          <text class="yuan">¥</text>
          <text class="num">{{ c.price }}</text>
        </view>
        <view class="card-desc">{{ c.description }}</view>
        <view class="card-meta">
          <text v-if="c.validDays">⏱ {{ c.validDays }} 天有效</text>
          <text v-if="c.totalTimes">× {{ c.totalTimes }} 次</text>
          <text v-if="!c.validDays && !c.totalTimes">∞ 不限次数</text>
        </view>
        <view class="card-action">立即购买 ›</view>
      </view>
    </view>
  </view>
</template>

<style>
@import '@/styles/common.css';
</style>

<style scoped>
.loading, .empty { text-align: center; color: var(--text-3); padding: 48px; }

.card {
  display: flex;
  background: #fff;
  border-radius: 14px;
  margin-bottom: 12px;
  overflow: hidden;
  box-shadow: 0 2px 10px rgba(0, 0, 0, .04);
}
.card-side {
  width: 88px;
  color: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 16px 8px;
  position: relative;
}
.card-side::after {
  content: '';
  position: absolute;
  right: -4px;
  top: 50%;
  width: 8px;
  height: 8px;
  background: var(--bg);
  border-radius: 50%;
  transform: translateY(-50%);
}
.side-name {
  font-size: 14px;
  font-weight: 600;
  text-align: center;
  line-height: 1.4;
}
.side-kind {
  margin-top: 4px;
  font-size: 11px;
  background: rgba(255, 255, 255, .25);
  padding: 2px 8px;
  border-radius: 10px;
}
.card-main {
  flex: 1;
  padding: 14px 16px;
  min-width: 0;
}
.card-top { display: flex; align-items: center; justify-content: space-between; }
.card-name { font-size: 16px; font-weight: 600; color: #1F1F1F; }
.card-price {
  margin-top: 6px;
  display: flex;
  align-items: baseline;
  color: #FF6B3D;
}
.yuan { font-size: 14px; }
.num { font-size: 28px; font-weight: 700; margin-left: 2px; }
.card-desc { color: #5B5B66; font-size: 12px; margin-top: 4px; line-height: 1.5; }
.card-meta {
  color: #909399;
  font-size: 11px;
  margin-top: 6px;
}
.card-meta text { margin-right: 10px; }
.card-action {
  margin-top: 8px;
  font-size: 13px;
  color: #FF6B3D;
  text-align: right;
  font-weight: 500;
}
</style>

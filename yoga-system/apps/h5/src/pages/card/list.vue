<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { api, type CardType, type CardKind } from '@/api'

const list = ref<CardType[]>([])

const kindName = (k: CardKind) =>
  ({ TIME: '时间卡', TIMES: '次卡', MIXED: '混合卡' } as const)[k] || k

const load = async () => {
  try { list.value = await api.h5.cardTypes() } catch { /* */ }
}
onMounted(load)

const goBuy = (id: number) => uni.navigateTo({ url: `/pages/card/buy?id=${id}` })
</script>

<template>
  <view class="page">
    <view v-for="c in list" :key="c.id" class="card" @click="goBuy(c.id)">
      <view class="card-header">
        <text class="card-name">{{ c.name }}</text>
        <text class="kind">{{ kindName(c.cardKind) }}</text>
      </view>
      <view class="card-price">¥{{ c.price }}</view>
      <view class="card-desc">{{ c.description }}</view>
      <view class="card-meta">
        <text v-if="c.validDays">{{ c.validDays }} 天有效</text>
        <text v-if="c.totalTimes">{{ c.totalTimes }} 次</text>
        <text v-if="!c.validDays && !c.totalTimes">不限</text>
      </view>
      <view class="btn-primary buy">立即购买</view>
    </view>
  </view>
</template>

<style scoped>
.page { padding: 12px 0 24px; }
.card { background: #fff; border-radius: 12px; margin: 12px; padding: 20px 16px; box-shadow: 0 2px 8px rgba(0,0,0,.05); }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.card-name { font-size: 18px; font-weight: 600; }
.kind { font-size: 12px; color: #667eea; background: #e0e7ff; padding: 2px 8px; border-radius: 4px; }
.card-price { color: #f5576c; font-size: 28px; font-weight: 600; margin: 12px 0; }
.card-desc { color: #606266; font-size: 13px; }
.card-meta { color: #909399; font-size: 12px; margin-top: 8px; }
.card-meta text { margin-right: 12px; }
.buy { margin-top: 12px; padding: 10px 0; text-align: center; border-radius: 24px; font-size: 14px; }
</style>

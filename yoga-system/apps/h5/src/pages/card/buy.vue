<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { api, type CardType } from '@/api'

const cardType = ref<CardType | null>(null)
const submitting = ref(false)

const kindName = (k: CardType['cardKind']) =>
  ({ TIME: '时间卡', TIMES: '次卡', MIXED: '混合卡' } as const)[k] || k

const load = async () => {
  const pages = uni.getCurrentPages() as any[]
  const id = Number(pages[pages.length - 1]?.options?.id)
  if (!id) return
  try {
    const list = await api.h5.cardTypes()
    cardType.value = list.find((x) => x.id === id) ?? null
  } catch { /* */ }
}
onMounted(load)

const onBuy = async () => {
  if (!cardType.value) return
  submitting.value = true
  try {
    const order = await api.h5.createOrder(cardType.value.id, cardType.value.price)
    uni.showModal({
      title: '下单成功',
      content: `请到店出示订单号支付, 支付完成后管理员将为您开通会员卡。\n订单号: ${order.orderNo}`,
      showCancel: false,
      success: () => uni.navigateBack()
    })
  } catch {
    /* */
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <view v-if="cardType" class="page">
    <view class="hero">
      <view class="name">{{ cardType.name }}</view>
      <view class="price">¥{{ cardType.price }}</view>
      <view class="desc">{{ cardType.description }}</view>
    </view>
    <view class="detail">
      <view class="row"><text>类型</text><text>{{ kindName(cardType.cardKind) }}</text></view>
      <view v-if="cardType.validDays" class="row"><text>有效天数</text><text>{{ cardType.validDays }} 天</text></view>
      <view v-if="cardType.totalTimes" class="row"><text>总次数</text><text>{{ cardType.totalTimes }} 次</text></view>
    </view>
    <view class="footer">
      <button class="btn-primary" :loading="submitting" :disabled="submitting" @click="onBuy">提交订单(线下支付)</button>
    </view>
  </view>
</template>

<style scoped>
.page { padding-bottom: 80px; }
.hero { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: #fff; padding: 32px 16px; text-align: center; }
.name { font-size: 22px; font-weight: 600; }
.price { font-size: 36px; font-weight: 600; margin: 12px 0; }
.desc { opacity: .9; font-size: 13px; }
.detail { background: #fff; margin: 12px; border-radius: 12px; padding: 4px 16px; }
.row { display: flex; justify-content: space-between; padding: 14px 0; border-bottom: 1px solid #f5f5f5; font-size: 14px; }
.row:last-child { border-bottom: none; }
.footer { position: fixed; left: 0; right: 0; bottom: 0; padding: 12px; background: #fff; box-shadow: 0 -2px 8px rgba(0,0,0,.05); }
button { height: 48px; line-height: 48px; border-radius: 24px; border: none; }
</style>

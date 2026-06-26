<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { api, type CardType } from '@/api'

const cardType = ref<CardType | null>(null)
const submitting = ref(false)
const loading = ref(true)

const kindName = (k: CardType['cardKind']) =>
  ({ TIME: '时间卡', TIMES: '次卡', MIXED: '混合卡' } as const)[k] || k

const kindColor = (k: CardType['cardKind']) =>
  ({ TIME: '#FF6B3D', TIMES: '#A06BFF', MIXED: '#23B573' } as Record<CardType['cardKind'], string>)[k] || '#FF6B3D'

const load = async () => {
  const pages = uni.getCurrentPages() as any[]
  const id = Number(pages[pages.length - 1]?.options?.id)
  if (!id) {
    loading.value = false
    return
  }
  try {
    const list = await api.h5.cardTypes()
    cardType.value = list.find((x) => x.id === id) ?? null
  } catch { /* */ }
  finally { loading.value = false }
}
onMounted(load)

const onBuy = async () => {
  if (!cardType.value) return
  submitting.value = true
  try {
    const order = await api.h5.createOrder(cardType.value.id, cardType.value.price)
    uni.showModal({
      title: '下单成功',
      content: `请到店出示订单号支付, 支付完成后管理员将为您开通会员卡。\n\n订单号: ${order.orderNo}\n金额: ¥${cardType.value.price}`,
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
  <view v-if="loading" class="empty">加载中...</view>
  <view v-else-if="!cardType" class="empty">
    <view class="empty-icon">▦</view>
    <text>未找到该会员卡</text>
  </view>
  <view v-else class="page">
    <view class="hero" :style="{ background: `linear-gradient(135deg, ${kindColor(cardType.cardKind)} 0%, #FF8A5C 100%)` }">
      <view class="hero-kind">{{ kindName(cardType.cardKind) }}</view>
      <view class="hero-name">{{ cardType.name }}</view>
      <view class="hero-price">
        <text class="yuan">¥</text>
        <text class="num">{{ cardType.price }}</text>
      </view>
      <view class="hero-desc">{{ cardType.description }}</view>
    </view>

    <view class="section">
      <view class="section-title">套餐详情</view>
      <view class="row"><text>类型</text><text class="val">{{ kindName(cardType.cardKind) }}</text></view>
      <view v-if="cardType.validDays" class="row"><text>有效天数</text><text class="val">{{ cardType.validDays }} 天</text></view>
      <view v-if="cardType.totalTimes" class="row"><text>总次数</text><text class="val">{{ cardType.totalTimes }} 次</text></view>
      <view v-if="!cardType.validDays && !cardType.totalTimes" class="row"><text>次数</text><text class="val">不限</text></view>
    </view>

    <view class="section">
      <view class="section-title">购买须知</view>
      <view class="notice">
        <view>• 提交订单后请到前台出示订单号完成支付</view>
        <view>• 支付完成后由管理员为您开通会员卡</view>
        <view>• 会员卡一经售出, 不予退换</view>
        <view>• 如有疑问请联系客服 400-888-8888</view>
      </view>
    </view>

    <view class="footer">
      <view class="footer-left">
        <text class="footer-label">合计</text>
        <text class="footer-price">¥{{ cardType.price }}</text>
      </view>
      <button class="btn-primary" :loading="submitting" :disabled="submitting" @click="onBuy">提交订单</button>
    </view>
  </view>
</template>

<style>
@import '@/styles/common.css';
</style>

<style scoped>
.hero {
  color: #fff;
  padding: 36px 20px;
  text-align: center;
}
.hero-kind {
  display: inline-block;
  font-size: 12px;
  background: rgba(255, 255, 255, .25);
  padding: 2px 10px;
  border-radius: 12px;
}
.hero-name { font-size: 22px; font-weight: 600; margin-top: 12px; }
.hero-price {
  margin: 16px 0;
  display: inline-flex;
  align-items: baseline;
}
.yuan { font-size: 18px; }
.num { font-size: 44px; font-weight: 700; margin-left: 2px; }
.hero-desc { opacity: .92; font-size: 13px; line-height: 1.5; padding: 0 12px; }

.row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  border-bottom: 1px solid var(--border);
  font-size: 14px;
  color: var(--text-2);
}
.row:last-child { border-bottom: none; }
.val { color: var(--text-1); font-weight: 500; }

.notice {
  padding: 4px 16px 16px;
  font-size: 12px;
  color: var(--text-3);
  line-height: 1.9;
}
.notice view { display: block; }

.footer {
  position: fixed;
  left: 0; right: 0; bottom: 0;
  display: flex;
  align-items: center;
  background: #fff;
  padding: 12px 16px;
  box-shadow: 0 -2px 12px rgba(0, 0, 0, .06);
  z-index: 10;
}
.footer-left { flex: 1; }
.footer-label { color: var(--text-3); font-size: 12px; margin-right: 4px; }
.footer-price { color: #FF6B3D; font-size: 22px; font-weight: 700; }
.footer .btn-primary {
  padding: 0 28px;
  height: 40px;
  line-height: 40px;
  font-size: 14px;
  border-radius: 20px;
}
</style>

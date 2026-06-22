<script setup lang="ts">
import { ref, onMounted } from 'vue'
import api from '@/utils/request'

const cardType = ref<any>({})
const submitting = ref(false)
const orderId = ref<number | null>(null)

const load = async () => {
  const id = Number((uni.getCurrentPages().pop() as any).options.id)
  const r: any = await api.cardTypeList()
  cardType.value = (r.data as any[]).find((x: any) => x.id === id)
}
onMounted(load)

const onBuy = async () => {
  submitting.value = true
  try {
    const r: any = await api.createOrder({ orderType: 'PURCHASE_CARD', cardTypeId: cardType.value.id, amount: cardType.value.price })
    orderId.value = r.data.id
    uni.showModal({
      title: '下单成功',
      content: '请到店出示订单号支付, 支付完成后管理员将为您开通会员卡。\n订单号: ' + r.data.orderNo,
      showCancel: false,
      success: () => uni.navigateBack()
    })
  } finally { submitting.value = false }
}
</script>

<template>
  <view class="page" v-if="cardType.id">
    <view class="hero">
      <view class="name">{{ cardType.name }}</view>
      <view class="price">¥{{ cardType.price }}</view>
      <view class="desc">{{ cardType.description }}</view>
    </view>
    <view class="detail">
      <view class="row"><text>类型</text><text>{{ { TIME: '时间卡', TIMES: '次卡', MIXED: '混合卡' }[cardType.cardKind] }}</text></view>
      <view class="row" v-if="cardType.validDays"><text>有效天数</text><text>{{ cardType.validDays }} 天</text></view>
      <view class="row" v-if="cardType.totalTimes"><text>总次数</text><text>{{ cardType.totalTimes }} 次</text></view>
    </view>
    <view class="footer">
      <button class="btn-primary" :loading="submitting" @click="onBuy">提交订单(线下支付)</button>
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
button { height: 48px; line-height: 48px; border-radius: 24px; }
</style>

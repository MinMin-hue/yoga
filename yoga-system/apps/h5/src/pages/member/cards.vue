<script setup lang="ts">
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { api, type MemberCard, type ConsumeRecord, type CardStatus, type RecordType } from '@/api'
import { formatDateShort } from '@/utils/format'

const cards = ref<MemberCard[]>([])
const records = ref<ConsumeRecord[]>([])
const tab = ref<'cards' | 'records'>('cards')
const loading = ref(false)

const statusName = (s: CardStatus) =>
  ({ PENDING: '待激活', ACTIVE: '正常', EXPIRED: '已过期', NO_REMAIN: '次数用尽', REFUNDED: '已退款' } as Record<CardStatus, string>)[s] || s
const statusClass = (s: CardStatus) =>
  ({ PENDING: 'tag-warning', ACTIVE: 'tag-success', EXPIRED: 'tag-info', NO_REMAIN: 'tag-danger', REFUNDED: 'tag-info' } as Record<CardStatus, string>)[s] || 'tag-info'
const typeName = (t: RecordType) =>
  ({ PURCHASE: '购卡', RECHARGE: '充值', CHECKIN: '扣次', REFUND: '退款', PENALTY: '违约扣次' } as Record<RecordType, string>)[t] || t

const load = async () => {
  loading.value = true
  try {
    const [c, r] = await Promise.all([api.h5.cards(), api.h5.records()])
    cards.value = c.cards || []
    records.value = r.records || []
  } catch { /* */ }
  finally { loading.value = false }
}
onShow(load)
</script>

<template>
  <view class="page">
    <view class="tabs">
      <view :class="['tab', tab === 'cards' && 'active']" @click="tab = 'cards'">会员卡</view>
      <view :class="['tab', tab === 'records' && 'active']" @click="tab = 'records'">消费记录</view>
    </view>

    <view v-if="loading" class="empty">加载中...</view>

    <view v-else-if="tab === 'cards'">
      <view v-if="cards.length === 0" class="empty">
        <view class="empty-icon">▦</view>
        <text>还没有会员卡,</text>
        <view class="empty-action" @click="uni.switchTab({ url: '/pages/schedule/list' })">立即购卡 →</view>
      </view>
      <view
        v-for="c in cards"
        :key="c.id"
        class="member-card"
      >
        <view class="mc-top">
          <text class="mc-name">{{ c.cardTypeName }}</text>
          <text :class="['tag', statusClass(c.status)]">{{ statusName(c.status) }}</text>
        </view>
        <view class="mc-no">卡号: {{ c.cardNo }}</view>
        <view class="mc-bottom">
          <view><text class="lbl">剩余次数</text><text class="val">{{ c.remainTimes ?? '不限' }}</text></view>
          <view><text class="lbl">到期时间</text><text class="val">{{ formatDateShort(c.validTo) || '永久' }}</text></view>
        </view>
      </view>
    </view>

    <view v-else>
      <view v-if="records.length === 0" class="empty">
        <view class="empty-icon">▦</view>
        <text>暂无记录</text>
      </view>
      <view v-for="r in records" :key="r.id" class="record">
        <view class="row">
          <text class="record-type">{{ typeName(r.type) }}</text>
          <text :class="r.timesDelta < 0 ? 'minus' : 'plus'">
            {{ r.timesDelta > 0 ? '+' : '' }}{{ r.timesDelta || 0 }} 次
            <text v-if="r.amount" class="amount">¥{{ r.amount }}</text>
          </text>
        </view>
        <view class="time">{{ r.createdAt }}</view>
        <view v-if="r.remark" class="remark">{{ r.remark }}</view>
      </view>
    </view>
  </view>
</template>

<style>
@import '@/styles/common.css';
</style>

<style scoped>
.tabs {
  display: flex;
  background: #fff;
  margin-bottom: 12px;
  border-radius: 12px;
}
.tab {
  flex: 1;
  text-align: center;
  padding: 14px;
  font-size: 14px;
  color: var(--text-2);
  position: relative;
}
.tab.active {
  color: var(--brand);
  font-weight: 600;
}
.tab.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 24px;
  height: 3px;
  background: linear-gradient(135deg, #FF6B3D 0%, #FF8A5C 100%);
  border-radius: 2px;
}

.member-card {
  background: linear-gradient(135deg, #FF6B3D 0%, #FF8A5C 100%);
  color: #fff;
  border-radius: 14px;
  padding: 20px 18px;
  margin-bottom: 12px;
  position: relative;
  overflow: hidden;
  box-shadow: 0 6px 20px rgba(255, 107, 61, .2);
}
.member-card::before {
  content: '';
  position: absolute;
  right: -40px;
  top: -40px;
  width: 160px;
  height: 160px;
  background: radial-gradient(circle, rgba(255,255,255,.18) 0%, transparent 70%);
  border-radius: 50%;
}
.member-card::after {
  content: '';
  position: absolute;
  right: 20px;
  bottom: -30px;
  width: 80px;
  height: 80px;
  background: radial-gradient(circle, rgba(255,255,255,.12) 0%, transparent 70%);
  border-radius: 50%;
}
.mc-top { display: flex; justify-content: space-between; align-items: center; position: relative; z-index: 1; }
.mc-name { font-size: 18px; font-weight: 600; }
.mc-status { font-size: 12px; padding: 2px 8px; border-radius: 4px; }
.mc-no { font-size: 12px; opacity: .85; margin: 10px 0 18px; position: relative; z-index: 1; }
.mc-bottom { display: flex; justify-content: space-between; position: relative; z-index: 1; }
.lbl { font-size: 11px; opacity: .8; }
.val { font-size: 16px; font-weight: 600; display: block; margin-top: 4px; }

.record {
  background: #fff;
  border-radius: 10px;
  padding: 14px;
  margin-bottom: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, .03);
}
.row { display: flex; justify-content: space-between; align-items: center; }
.record-type { font-size: 14px; font-weight: 500; }
.minus { color: var(--danger); font-size: 14px; }
.plus { color: var(--success); font-size: 14px; }
.amount { font-size: 12px; margin-left: 6px; }
.time, .remark { color: var(--text-3); font-size: 12px; margin-top: 4px; }

.empty-action {
  margin-top: 12px;
  display: inline-block;
  color: var(--brand);
  font-size: 14px;
  font-weight: 500;
}
</style>

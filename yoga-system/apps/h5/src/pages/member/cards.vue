<script setup lang="ts">
import { ref } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { api, type MemberCard, type ConsumeRecord, type CardStatus, type RecordType } from '@/api'
import { formatDateShort } from '@/utils/format'

const cards = ref<MemberCard[]>([])
const records = ref<ConsumeRecord[]>([])
const tab = ref<'cards' | 'records'>('cards')

const statusName = (s: CardStatus) =>
  ({ PENDING: '待激活', ACTIVE: '正常', EXPIRED: '已过期', NO_REMAIN: '次数用尽', REFUNDED: '已退款' } as Record<CardStatus, string>)[s] || s
const typeName = (t: RecordType) =>
  ({ PURCHASE: '购卡', RECHARGE: '充值', CHECKIN: '扣次', REFUND: '退款', PENALTY: '违约扣次' } as Record<RecordType, string>)[t] || t

const load = async () => {
  try {
    const [c, r] = await Promise.all([api.h5.cards(), api.h5.records()])
    cards.value = c.cards || []
    records.value = r.records || []
  } catch { /* */ }
}
onShow(load)
</script>

<template>
  <view class="page">
    <view class="tabs">
      <view :class="['tab', tab === 'cards' && 'active']" @click="tab = 'cards'">会员卡</view>
      <view :class="['tab', tab === 'records' && 'active']" @click="tab = 'records'">消费记录</view>
    </view>

    <view v-if="tab === 'cards'">
      <view v-if="cards.length === 0" class="empty">还没有会员卡, 立即购卡 →</view>
      <view v-for="c in cards" :key="c.id" class="member-card">
        <view class="mc-top">
          <text class="mc-name">{{ c.cardTypeName }}</text>
          <text :class="['mc-status', `s-${c.status}`]">{{ statusName(c.status) }}</text>
        </view>
        <view class="mc-no">卡号: {{ c.cardNo }}</view>
        <view class="mc-bottom">
          <view><text class="lbl">剩余次数</text><text class="val">{{ c.remainTimes ?? '不限' }}</text></view>
          <view><text class="lbl">到期时间</text><text class="val">{{ formatDateShort(c.validTo) || '永久' }}</text></view>
        </view>
      </view>
    </view>

    <view v-else>
      <view v-if="records.length === 0" class="empty">暂无记录</view>
      <view v-for="r in records" :key="r.id" class="record">
        <view class="row">
          <text>{{ typeName(r.type) }}</text>
          <text :class="r.timesDelta < 0 ? 'minus' : 'plus'">
            {{ r.timesDelta > 0 ? '+' : '' }}{{ r.timesDelta || 0 }} 次 / ¥{{ r.amount || 0 }}
          </text>
        </view>
        <view class="time">{{ r.createdAt }}</view>
        <view class="remark">{{ r.remark }}</view>
      </view>
    </view>
  </view>
</template>

<style scoped>
.tabs { display: flex; background: #fff; }
.tab { flex: 1; text-align: center; padding: 14px; font-size: 14px; color: #606266; }
.tab.active { color: #667eea; border-bottom: 2px solid #667eea; font-weight: 600; }
.empty { text-align: center; color: #909399; padding: 48px; }
.member-card { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: #fff; margin: 12px; padding: 18px; border-radius: 12px; }
.mc-top { display: flex; justify-content: space-between; }
.mc-name { font-size: 18px; font-weight: 600; }
.mc-status { font-size: 12px; padding: 2px 8px; border-radius: 4px; background: rgba(255,255,255,.2); }
.s-ACTIVE { background: #10b981; }
.s-PENDING { background: #f59e0b; }
.mc-no { font-size: 12px; opacity: .85; margin: 8px 0 16px; }
.mc-bottom { display: flex; justify-content: space-between; }
.lbl { font-size: 12px; opacity: .8; }
.val { font-size: 16px; font-weight: 600; display: block; margin-top: 4px; }
.record { background: #fff; margin: 8px 12px; padding: 14px; border-radius: 8px; }
.row { display: flex; justify-content: space-between; font-size: 14px; }
.minus { color: #f5576c; }
.plus { color: #10b981; }
.time, .remark { color: #909399; font-size: 12px; margin-top: 4px; }
</style>

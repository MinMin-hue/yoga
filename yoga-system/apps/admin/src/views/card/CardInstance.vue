<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2 class="page-title">🎫 会员卡实例</h2>
        <div class="page-subtitle">查询会员已购会员卡, 可激活 / 退款</div>
      </div>
    </div>

    <div class="toolbar">
      <el-input v-model="memberId" placeholder="🔍 输入会员ID (手机号不可用)" style="width: 260px" clearable />
      <el-button type="primary" @click="load">查询</el-button>
      <span class="text-muted" style="font-size: 12px; margin-left: 8px;">提示: 会员 ID 可在「会员管理」中查询</span>
    </div>

    <div class="y-card" style="padding: 0">
      <el-table :data="list" v-loading="loading" stripe :empty-text="loading ? '加载中...' : '请输入会员ID并点击查询'">
        <el-table-column prop="cardNo" label="卡号" width="200">
          <template #default="{ row }">
            <span class="mono">{{ row.cardNo }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="cardTypeName" label="卡类型" min-width="140" />
        <el-table-column label="价格" width="100" align="right">
          <template #default="{ row }">
            <span class="price">{{ row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column label="剩余次数" width="120" align="center">
          <template #default="{ row }">
            <span v-if="row.remainTimes != null" class="text-bold" style="color: var(--y-primary); font-size: 15px;">{{ row.remainTimes }}</span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="validTo" label="到期时间" width="180" />
        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <span :class="['badge', statusBadge(row.status)]">
              <span class="badge-dot"></span>
              {{ statusName(row.status) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button v-if="row.status === 'PENDING'" size="small" type="success" @click="onActivate(row)">激活</el-button>
            <el-button v-if="['PENDING', 'ACTIVE'].includes(row.status)" size="small" type="danger" @click="onRefund(row)">退款</el-button>
            <span v-if="!['PENDING', 'ACTIVE'].includes(row.status)" class="text-muted" style="font-size: 12px;">无可用操作</span>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="refundVisible" title="会员卡退款" width="460">
      <el-form label-width="80px">
        <el-form-item label="卡号"><span class="mono">{{ currentRow?.cardNo }}</span></el-form-item>
        <el-form-item label="卡类型">{{ currentRow?.cardTypeName }}</el-form-item>
        <el-form-item label="退款原因">
          <el-input v-model="refundReason" type="textarea" :rows="3" placeholder="请说明退款原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="refundVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmRefund">确认退款</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { cardApi } from '@/api'

const list = ref<any[]>([])
const memberId = ref('')
const loading = ref(false)
const refundVisible = ref(false)
const refundReason = ref('')
const currentRow = ref<any>(null)

const statusName = (s: string) => ({ PENDING: '待激活', ACTIVE: '正常', EXPIRED: '已过期', NO_REMAIN: '次数用尽', REFUNDED: '已退款' } as any)[s] || s
const statusBadge = (s: string) => ({ PENDING: 'info', ACTIVE: 'success', EXPIRED: 'warning', NO_REMAIN: 'warning', REFUNDED: 'muted' } as any)[s] || 'muted'

const load = async () => {
  if (!memberId.value) { ElMessage.warning('请输入会员ID'); return }
  loading.value = true
  try {
    const r: any = await cardApi.cardListByMember(Number(memberId.value))
    list.value = r.data
  } finally { loading.value = false }
}
const onActivate = async (row: any) => {
  await ElMessageBox.confirm(`确认激活卡 [${row.cardNo}] ?`, '提示', { type: 'success' })
  await cardApi.cardActivate(row.id)
  ElMessage.success('已激活')
  load()
}
const onRefund = (row: any) => { currentRow.value = row; refundReason.value = ''; refundVisible.value = true }
const confirmRefund = async () => {
  await cardApi.cardRefund({ cardId: currentRow.value.id, reason: refundReason.value })
  ElMessage.success('已退款')
  refundVisible.value = false
  load()
}
</script>

<style scoped>
.mono { font-family: "JetBrains Mono", monospace; color: var(--y-primary); font-weight: 500; }
</style>

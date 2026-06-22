<template>
  <div class="page">
    <div class="toolbar">
      <el-input v-model="memberId" placeholder="输入会员手机号查询" style="width: 200px" clearable />
      <el-button type="primary" @click="load">查询</el-button>
    </div>
    <el-table :data="list" border>
      <el-table-column prop="cardNo" label="卡号" width="180" />
      <el-table-column prop="cardTypeName" label="卡类型" />
      <el-table-column prop="price" label="价格" width="100"><template #default="{ row }">¥{{ row.price }}</template></el-table-column>
      <el-table-column prop="remainTimes" label="剩余次数" width="100" />
      <el-table-column prop="validTo" label="到期时间" width="170" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusName(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 'PENDING'" size="small" type="success" @click="onActivate(row)">激活</el-button>
          <el-button v-if="['PENDING', 'ACTIVE'].includes(row.status)" size="small" type="danger" @click="onRefund(row)">退款</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="refundVisible" title="退款原因" width="400">
      <el-input v-model="refundReason" type="textarea" :rows="3" />
      <template #footer>
        <el-button @click="refundVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmRefund">确认退款</el-button>
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
const refundVisible = ref(false)
const refundReason = ref('')
const currentId = ref<number | null>(null)

const statusName = (s: string) => ({ PENDING: '待激活', ACTIVE: '正常', EXPIRED: '已过期', NO_REMAIN: '次数用尽', REFUNDED: '已退款' } as any)[s] || s
const statusType = (s: string) => ({ PENDING: 'info', ACTIVE: 'success', EXPIRED: 'warning', NO_REMAIN: 'warning', REFUNDED: '' } as any)[s] || ''

const load = async () => {
  if (!memberId.value) { ElMessage.warning('请输入会员ID'); return }
  const r: any = await cardApi.cardListByMember(Number(memberId.value))
  list.value = r.data
}
const onActivate = async (row: any) => {
  await ElMessageBox.confirm('确认激活该卡?', '提示')
  await cardApi.cardActivate(row.id)
  ElMessage.success('已激活')
  load()
}
const onRefund = (row: any) => { currentId.value = row.id; refundReason.value = ''; refundVisible.value = true }
const confirmRefund = async () => {
  await cardApi.cardRefund({ cardId: currentId.value!, reason: refundReason.value })
  ElMessage.success('已退款')
  refundVisible.value = false
  load()
}
</script>

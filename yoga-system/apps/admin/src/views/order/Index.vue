<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2 class="page-title">💰 订单管理</h2>
        <div class="page-subtitle">查看订单, 确认收款 / 退款</div>
      </div>
    </div>

    <div class="toolbar">
      <el-input v-model="query.orderNo" placeholder="🔍 订单号" style="width: 220px" clearable />
      <el-select v-model="query.status" placeholder="状态" clearable style="width: 140px">
        <el-option label="待支付" value="PENDING" />
        <el-option label="已支付" value="PAID" />
        <el-option label="已取消" value="CANCELLED" />
        <el-option label="已退款" value="REFUNDED" />
      </el-select>
      <el-select v-model="query.orderType" placeholder="类型" clearable style="width: 140px">
        <el-option label="购卡" value="PURCHASE_CARD" />
        <el-option label="充值" value="RECHARGE" />
        <el-option label="单课" value="SINGLE_COURSE" />
      </el-select>
      <el-button type="primary" @click="load(1)">查询</el-button>
      <el-button @click="onReset">重置</el-button>
    </div>

    <div class="y-card" style="padding: 0">
      <el-table :data="list" stripe>
        <el-table-column prop="orderNo" label="订单号" width="200">
          <template #default="{ row }"><span class="mono">{{ row.orderNo }}</span></template>
        </el-table-column>
        <el-table-column prop="memberName" label="会员" width="100" />
        <el-table-column label="类型" width="100" align="center">
          <template #default="{ row }">
            <span class="label-chip">{{ { PURCHASE_CARD: '购卡', RECHARGE: '充值', SINGLE_COURSE: '单课' }[row.orderType] }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="cardTypeName" label="卡/课" />
        <el-table-column label="金额" width="120" align="right">
          <template #default="{ row }"><span class="price" style="font-size: 16px;">{{ row.amount }}</span></template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <span :class="['badge', statusBadge(row.status)]">
              <span class="badge-dot"></span>
              {{ statusName(row.status) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="payMethod" label="支付方式" width="110" align="center" />
        <el-table-column prop="payTime" label="支付时间" width="170" />
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button v-if="row.status === 'PENDING'" size="small" type="success" @click="onConfirm(row)">确认收款</el-button>
            <el-button v-if="row.status === 'PAID'" size="small" type="danger" @click="onRefund(row)">退款</el-button>
            <span v-if="!['PENDING','PAID'].includes(row.status)" class="text-muted" style="font-size: 12px;">已结束</span>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-pagination
      class="y-pagination"
      v-model:current-page="query.pageNum" v-model:page-size="query.pageSize"
      :total="total" @current-change="(p: number) => load(p)"
      layout="total, prev, pager, next, jumper"
    />

    <el-dialog v-model="confirmVisible" title="确认收款" width="460">
      <div class="confirm-info">
        <div class="info-row"><span class="info-label">订单号</span><span class="mono">{{ current?.orderNo }}</span></div>
        <div class="info-row"><span class="info-label">金额</span><span class="price" style="font-size: 22px;">{{ current?.amount }}</span></div>
        <div class="info-row"><span class="info-label">会员</span><span>{{ current?.memberName }}</span></div>
      </div>
      <el-form label-width="80px" style="margin-top: 12px;">
        <el-form-item label="备注"><el-input v-model="confirmRemark" placeholder="选填, 例如: 微信转账 XX" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="confirmVisible = false">取消</el-button>
        <el-button type="primary" @click="doConfirm">确认收款</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { orderApi } from '@/api'

const list = ref<any[]>([])
const total = ref(0)
const query = reactive({ orderNo: '', status: '', orderType: '', pageNum: 1, pageSize: 20 })
const confirmVisible = ref(false)
const confirmRemark = ref('')
const current = ref<any>(null)

const statusName = (s: string) => ({ PENDING: '待支付', PAID: '已支付', CANCELLED: '已取消', REFUNDED: '已退款' } as any)[s] || s
const statusBadge = (s: string) => ({ PENDING: 'warning', PAID: 'success', CANCELLED: 'muted', REFUNDED: 'danger' } as any)[s] || 'muted'

const load = async (page = 1) => {
  query.pageNum = page
  const r: any = await orderApi.page(query)
  list.value = r.data.list
  total.value = r.data.total
}
const onReset = () => { query.orderNo = ''; query.status = ''; query.orderType = ''; load(1) }
const onConfirm = (row: any) => { current.value = row; confirmRemark.value = ''; confirmVisible.value = true }
const doConfirm = async () => {
  await orderApi.confirmPay({ orderId: current.value.id, payMethod: 'OFFLINE', remark: confirmRemark.value })
  ElMessage.success('已确认收款, 会员卡已发放')
  confirmVisible.value = false
  load()
}
const onRefund = async (row: any) => {
  const { value } = await ElMessageBox.prompt('请输入退款原因', '退款', { confirmButtonText: '确认退款', cancelButtonText: '取消', type: 'warning' })
  await orderApi.refund(row.id, value)
  ElMessage.success('已退款')
  load()
}
onMounted(load)
</script>

<style scoped>
.y-pagination { margin-top: 16px; justify-content: flex-end; display: flex; }
.mono { font-family: "JetBrains Mono", monospace; color: var(--y-primary); font-size: 12px; }
.confirm-info {
  background: var(--y-bg-soft);
  border-radius: var(--y-radius); padding: 16px 20px;
}
.info-row {
  display: flex; align-items: center; justify-content: space-between;
  padding: 6px 0;
}
.info-label { color: var(--y-text-muted); font-size: 13px; }
</style>

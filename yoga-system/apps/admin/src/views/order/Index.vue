<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2 class="page-title">💰 订单管理</h2>
        <div class="page-subtitle">查看订单与收款 / 退款</div>
      </div>
    </div>

    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="🔍 订单号 / 会员" style="width: 220px" clearable />
      <el-select v-model="query.status" placeholder="状态" clearable style="width: 140px">
        <el-option label="待支付" value="PENDING" />
        <el-option label="已支付" value="PAID" />
        <el-option label="已退款" value="REFUNDED" />
        <el-option label="已取消" value="CANCELLED" />
      </el-select>
      <el-button type="primary" @click="load(1)">查询</el-button>
      <el-button @click="onReset">重置</el-button>
    </div>

    <div class="y-card" style="padding: 0">
      <el-table :data="list" stripe>
        <el-table-column prop="orderNo" label="订单号" width="200">
          <template #default="{ row }"><span class="mono">{{ row.orderNo }}</span></template>
        </el-table-column>
        <el-table-column label="会员" width="120">{{ row => row.memberName || '-' }}</el-table-column>
        <el-table-column label="类型" width="120" align="center">
          <template #default="{ row }">
            <span class="label-chip">{{ { CARD: '会员卡', COURSE: '课程', RECHARGE: '充值' }[row.orderType] || row.orderType }}</span>
          </template>
        </el-table-column>
        <el-table-column label="金额" width="120" align="right">
          <template #default="{ row }">
            <span class="price" style="font-size: 16px;">{{ row.amount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <span :class="['badge', statusBadge(row.status)]">
              <span class="badge-dot"></span>
              {{ statusName(row.status) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <el-button v-if="row.status === 'PENDING'" size="small" type="success" @click="onPay(row)">收款</el-button>
            <el-button v-if="row.status === 'PAID'" size="small" type="warning" @click="onRefund(row)">退款</el-button>
            <span v-if="!['PENDING', 'PAID'].includes(row.status)" class="text-muted" style="font-size: 12px;">已结束</span>
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

    <el-dialog v-model="payVisible" title="订单收款" width="440">
      <div class="pay-info">
        <div class="info-row"><span>订单号</span><span class="mono">{{ currentRow?.orderNo }}</span></div>
        <div class="info-row"><span>会员</span><span>{{ currentRow?.memberName || '-' }}</span></div>
        <div class="info-row"><span>金额</span><span class="price" style="font-size: 22px;">{{ currentRow?.amount }}</span></div>
      </div>
      <el-form :model="payForm" label-width="100px" style="margin-top: 12px">
        <el-form-item label="支付方式">
          <el-select v-model="payForm.payChannel" style="width: 100%">
            <el-option label="微信" value="WECHAT" />
            <el-option label="支付宝" value="ALIPAY" />
            <el-option label="现金" value="CASH" />
            <el-option label="刷卡" value="CARD" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="payForm.remark" type="textarea" :rows="2" placeholder="收款备注, 可选" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="payVisible = false">取消</el-button>
        <el-button type="success" @click="confirmPay">确认收款</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="refundVisible" title="订单退款" width="440">
      <div class="pay-info">
        <div class="info-row"><span>订单号</span><span class="mono">{{ currentRow?.orderNo }}</span></div>
        <div class="info-row"><span>会员</span><span>{{ currentRow?.memberName || '-' }}</span></div>
        <div class="info-row"><span>退款金额</span><span class="price" style="font-size: 22px;">{{ currentRow?.amount }}</span></div>
      </div>
      <el-form :model="refundForm" label-width="100px" style="margin-top: 12px">
        <el-form-item label="退款原因"><el-input v-model="refundForm.reason" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="refundVisible = false">取消</el-button>
        <el-button type="warning" @click="confirmRefund">确认退款</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { orderApi } from '@/api'

const list = ref<any[]>([])
const total = ref(0)
const query = reactive({ keyword: '', status: '', pageNum: 1, pageSize: 20 })
const payVisible = ref(false)
const refundVisible = ref(false)
const currentRow = ref<any>(null)
const payForm = reactive({ payChannel: 'WECHAT', remark: '' })
const refundForm = reactive({ reason: '' })

const statusName = (s: string) => ({ PENDING: '待支付', PAID: '已支付', REFUNDED: '已退款', CANCELLED: '已取消' } as any)[s] || s
const statusBadge = (s: string) => ({ PENDING: 'warning', PAID: 'success', REFUNDED: 'muted', CANCELLED: 'muted' } as any)[s] || 'muted'

const load = async (page = 1) => {
  query.pageNum = page
  const r: any = await orderApi.page(query)
  list.value = r.data.list; total.value = r.data.total
}
const onReset = () => { query.keyword = ''; query.status = ''; load(1) }
const onPay = (row: any) => { currentRow.value = row; payForm.payChannel = 'WECHAT'; payForm.remark = ''; payVisible.value = true }
const confirmPay = async () => {
  await orderApi.pay({ orderId: currentRow.value.id, ...payForm })
  ElMessage.success('收款成功'); payVisible.value = false; load()
}
const onRefund = (row: any) => { currentRow.value = row; refundForm.reason = ''; refundVisible.value = true }
const confirmRefund = async () => {
  await orderApi.refund({ orderId: currentRow.value.id, ...refundForm })
  ElMessage.success('已退款'); refundVisible.value = false; load()
}
onMounted(load)
</script>

<style scoped>
.y-pagination { margin-top: 16px; display: flex; justify-content: flex-end; }
.mono { font-family: "JetBrains Mono", monospace; color: var(--y-primary); font-size: 12px; }
.pay-info {
  background: var(--y-bg-soft); border: 1px solid var(--y-border);
  border-radius: 10px; padding: 12px 16px;
}
.info-row {
  display: flex; justify-content: space-between; align-items: center;
  padding: 8px 0; font-size: 14px;
  border-bottom: 1px dashed var(--y-border);
}
.info-row:last-child { border-bottom: none; }
.info-row span:first-child { color: var(--y-text-muted); }
</style>

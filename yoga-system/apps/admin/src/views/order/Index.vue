<template>
  <div class="page">
    <div class="toolbar">
      <el-input v-model="query.orderNo" placeholder="订单号" style="width: 200px" clearable />
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
    </div>
    <el-table :data="list" border>
      <el-table-column prop="orderNo" label="订单号" width="180" />
      <el-table-column prop="memberName" label="会员" width="100" />
      <el-table-column label="类型" width="100">
        <template #default="{ row }">{{ { PURCHASE_CARD: '购卡', RECHARGE: '充值', SINGLE_COURSE: '单课' }[row.orderType] }}</template>
      </el-table-column>
      <el-table-column prop="cardTypeName" label="卡/课" />
      <el-table-column prop="amount" label="金额" width="100"><template #default="{ row }">¥{{ row.amount }}</template></el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusName(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="payMethod" label="支付方式" width="100" />
      <el-table-column prop="payTime" label="支付时间" width="170" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 'PENDING'" size="small" type="success" @click="onConfirm(row)">确认收款</el-button>
          <el-button v-if="row.status === 'PAID'" size="small" type="danger" @click="onRefund(row)">退款</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination style="margin-top: 12px" v-model:current-page="query.pageNum" v-model:page-size="query.pageSize" :total="total" @current-change="(p: number) => load(p)" layout="total, prev, pager, next" />

    <el-dialog v-model="confirmVisible" title="确认收款" width="400">
      <p>订单号: {{ current?.orderNo }}</p>
      <p>金额: ¥{{ current?.amount }}</p>
      <el-input v-model="confirmRemark" placeholder="备注" />
      <template #footer>
        <el-button @click="confirmVisible = false">取消</el-button>
        <el-button type="primary" @click="doConfirm">确认</el-button>
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
const statusType = (s: string) => ({ PENDING: 'warning', PAID: 'success', CANCELLED: 'info', REFUNDED: 'danger' } as any)[s] || ''

const load = async (page = 1) => {
  query.pageNum = page
  const r: any = await orderApi.page(query)
  list.value = r.data.list
  total.value = r.data.total
}
const onConfirm = (row: any) => { current.value = row; confirmRemark.value = ''; confirmVisible.value = true }
const doConfirm = async () => {
  await orderApi.confirmPay({ orderId: current.value.id, payMethod: 'OFFLINE', remark: confirmRemark.value })
  ElMessage.success('已确认收款, 会员卡已发放')
  confirmVisible.value = false
  load()
}
const onRefund = async (row: any) => {
  const { value } = await ElMessageBox.prompt('请输入退款原因', '退款', { confirmButtonText: '确认退款', cancelButtonText: '取消' })
  await orderApi.refund(row.id, value)
  ElMessage.success('已退款')
  load()
}
onMounted(load)
</script>

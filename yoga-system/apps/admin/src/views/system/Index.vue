<template>
  <div class="page">
    <el-card>
      <template #header><b>系统配置</b></template>
      <el-form :model="form" label-width="200px" style="max-width: 600px;">
        <el-form-item label="预约停止时间(分钟)">
          <el-input-number v-model="form['booking.stop_minutes']" :min="0" />
          <span class="text-muted" style="margin-left: 8px;">课程开始前多少分钟停止预约</span>
        </el-form-item>
        <el-form-item label="免费取消窗口(分钟)">
          <el-input-number v-model="form['booking.cancel_minutes']" :min="0" />
          <span class="text-muted" style="margin-left: 8px;">课程开始前多少分钟内取消视为违约</span>
        </el-form-item>
        <el-form-item label="违约扣除次数">
          <el-input-number v-model="form['booking.no_show_penalty']" :min="0" />
        </el-form-item>
        <el-form-item label="迟到可签到(分钟)">
          <el-input-number v-model="form['booking.late_checkin_minutes']" :min="0" />
        </el-form-item>
        <el-form-item label="订单支付超时(分钟)">
          <el-input-number v-model="form['order.expire_minutes']" :min="1" />
        </el-form-item>
        <el-form-item label="启用通知">
          <el-switch v-model="form['notification.enabled']" active-value="true" inactive-value="false" />
        </el-form-item>
        <el-button type="primary" @click="onSave">保存</el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { systemApi } from '@/api'

const form = reactive<any>({})
const onSave = async () => {
  await systemApi.update({ ...form })
  ElMessage.success('已保存')
}
onMounted(async () => {
  const r: any = await systemApi.all()
  Object.assign(form, r.data)
})
</script>

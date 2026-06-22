<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2 class="page-title">⚙️ 系统设置</h2>
        <div class="page-subtitle">配置预约规则、订单超时、通知开关等</div>
      </div>
      <el-button type="primary" :icon="Check" @click="onSave">保存设置</el-button>
    </div>

    <el-row :gutter="16">
      <el-col :span="14">
        <div class="y-card">
          <div class="y-card-header">
            <span class="y-card-title">📋 预约规则</span>
            <span class="text-muted" style="font-size: 12px;">影响会员预约行为</span>
          </div>
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
              <span class="text-muted" style="margin-left: 8px;">爽约后扣除会员卡次数</span>
            </el-form-item>
            <el-form-item label="迟到可签到(分钟)">
              <el-input-number v-model="form['booking.late_checkin_minutes']" :min="0" />
              <span class="text-muted" style="margin-left: 8px;">课程开始后多少分钟内可签到</span>
            </el-form-item>
          </el-form>
        </div>
      </el-col>

      <el-col :span="10">
        <div class="y-card">
          <div class="y-card-header">
            <span class="y-card-title">💳 订单 & 通知</span>
            <span class="text-muted" style="font-size: 12px;">订单超时 & 消息推送</span>
          </div>
          <el-form :model="form" label-width="200px">
            <el-form-item label="订单支付超时(分钟)">
              <el-input-number v-model="form['order.expire_minutes']" :min="1" />
            </el-form-item>
            <el-form-item label="启用通知">
              <el-switch v-model="form['notification.enabled']" active-value="true" inactive-value="false" />
              <span class="text-muted" style="margin-left: 12px;">开启后, 系统将向会员发送短信/微信通知</span>
            </el-form-item>
          </el-form>
        </div>

        <div class="y-card" style="margin-top: 16px;">
          <div class="y-card-header">
            <span class="y-card-title">ℹ️ 使用提示</span>
          </div>
          <ul class="tip-list">
            <li>保存后设置立即生效</li>
            <li>如修改违约规则, 仅对新预约生效</li>
            <li>所有变更会记录到系统日志</li>
          </ul>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Check } from '@element-plus/icons-vue'
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

<style scoped>
.tip-list { list-style: none; padding: 0; margin: 0; }
.tip-list li {
  padding: 8px 0;
  font-size: 13px;
  color: var(--y-text-secondary);
  display: flex; align-items: center; gap: 6px;
}
.tip-list li::before {
  content: '·';
  color: var(--y-primary);
  font-size: 20px;
  font-weight: bold;
}
</style>

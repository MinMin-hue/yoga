<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h2 class="page-title">⚙️ 系统设置</h2>
        <div class="page-subtitle">业务规则、通知配置</div>
      </div>
      <el-button type="primary" :icon="Check" @click="onSave" :loading="saving">保存全部</el-button>
    </div>

    <el-row :gutter="16">
      <el-col :xs="24" :md="12">
        <div class="y-card">
          <div class="y-card-header">
            <div class="y-card-title">📅 预约规则</div>
          </div>
          <el-form :model="form" label-width="120px">
            <el-form-item label="取消时限">
              <el-input-number v-model="form.cancelHours" :min="0" :step="1" />
              <span class="text-muted" style="margin-left: 8px;">小时 (距开始时间)</span>
            </el-form-item>
            <el-alert type="info" :closable="false" style="margin-bottom: 12px">
              超过时限后, 会员不能再取消预约, 否则记为「爽约」, 系统自动扣 1 次
            </el-alert>
            <el-form-item label="爽约扣次">
              <el-input-number v-model="form.noShowPenalty" :min="0" :step="1" />
              <span class="text-muted" style="margin-left: 8px;">次</span>
            </el-form-item>
            <el-form-item label="迟到签到">
              <el-input-number v-model="form.lateCheckInMin" :min="0" :step="5" />
              <span class="text-muted" style="margin-left: 8px;">分钟 (课程开始后)</span>
            </el-form-item>
            <el-form-item label="最大预约数">
              <el-input-number v-model="form.maxAdvanceDays" :min="1" :max="30" />
              <span class="text-muted" style="margin-left: 8px;">天后</span>
            </el-form-item>
          </el-form>
        </div>
      </el-col>

      <el-col :xs="24" :md="12">
        <div class="y-card">
          <div class="y-card-header">
            <div class="y-card-title">💰 订单 & 通知</div>
          </div>
          <el-form :model="form" label-width="120px">
            <el-form-item label="订单超时">
              <el-input-number v-model="form.orderExpireMin" :min="1" :step="1" />
              <span class="text-muted" style="margin-left: 8px;">分钟未支付自动取消</span>
            </el-form-item>
            <el-form-item label="退款最长时间">
              <el-input-number v-model="form.refundMaxDays" :min="1" :step="1" />
              <span class="text-muted" style="margin-left: 8px;">天内的订单可退款</span>
            </el-form-item>
            <el-form-item label="开课前提醒">
              <el-input-number v-model="form.remindBeforeMin" :min="0" :step="5" />
              <span class="text-muted" style="margin-left: 8px;">分钟推送提醒</span>
            </el-form-item>
            <el-form-item label="启用通知">
              <el-switch v-model="form.notifyEnabled" active-text="开启" inactive-text="关闭" />
            </el-form-item>
            <el-alert v-if="form.notifyEnabled" type="success" :closable="false">
              通知已开启, 系统会在课程前 {{ form.remindBeforeMin }} 分钟推送提醒给会员
            </el-alert>
            <el-alert v-else type="warning" :closable="false">
              通知已关闭, 会员将不会收到开课提醒
            </el-alert>
          </el-form>
        </div>
      </el-col>
    </el-row>

    <div class="y-card" style="margin-top: 16px">
      <div class="y-card-header">
        <div class="y-card-title">💡 使用提示</div>
      </div>
      <ul class="tips">
        <li><el-icon><InfoFilled /></el-icon> 系统设置修改后立即生效, 无需重启服务</li>
        <li><el-icon><InfoFilled /></el-icon> 爽约扣次为 0 时不扣除, 仅记录标记</li>
        <li><el-icon><InfoFilled /></el-icon> 退款最长时间为 0 时表示不限制退款时限</li>
        <li><el-icon><InfoFilled /></el-icon> 通知目前仅支持站内消息, 短信 / 微信通知请联系运维配置</li>
      </ul>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Check, InfoFilled } from '@element-plus/icons-vue'
import { systemApi } from '@/api'

const saving = ref(false)
const form = reactive({
  cancelHours: 4, noShowPenalty: 1, lateCheckInMin: 10, maxAdvanceDays: 7,
  orderExpireMin: 30, refundMaxDays: 7, remindBeforeMin: 60, notifyEnabled: true
})

const load = async () => {
  try {
    const r: any = await systemApi.config()
    if (r.data) Object.assign(form, r.data)
  } catch {}
}
const onSave = async () => {
  saving.value = true
  try {
    await systemApi.saveConfig(form)
    ElMessage.success('已保存')
  } finally { saving.value = false }
}
onMounted(load)
</script>

<style scoped>
.tips { margin: 0; padding: 0 0 0 4px; list-style: none; }
.tips li {
  display: flex; align-items: center; gap: 8px;
  font-size: 13px; color: var(--y-text-secondary);
  padding: 8px 0;
  border-bottom: 1px dashed var(--y-border);
}
.tips li:last-child { border-bottom: none; }
.tips .el-icon { color: var(--y-primary); }
</style>

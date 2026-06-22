<template>
  <div class="page">
    <el-table :data="list" border>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="realName" label="姓名" />
      <el-table-column prop="username" label="账号" width="120" />
      <el-table-column prop="phone" label="手机号" width="130" />
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { courseApi } from '@/api'

const list = ref<any[]>([])
onMounted(async () => { const r: any = await courseApi.coachList(); list.value = r.data })
</script>

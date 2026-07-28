<template>
  <div>
    <el-button type="primary" @click="dialogVisible=true">创建批次</el-button>
    <el-table :data="batches" style="margin-top:16px" v-loading="loading">
      <el-table-column prop="title" label="标题" />
      <el-table-column label="状态" width="120">
        <template #default="{row}">
          <el-tag :type="row.status==='COLLECTING'?'success':'info'">{{ row.status==='COLLECTING'?'采集中':'已结束' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="180" />
      <el-table-column label="操作" width="240">
        <template #default="{row}">
          <el-button size="small" @click="$router.push(`/admin/batches/${row.id}`)">查看填报</el-button>
          <el-button v-if="row.status==='COLLECTING'" size="small" type="warning" @click="endBatch(row.id)">结束</el-button>
          <el-button size="small" type="danger" @click="deleteBatch(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" title="创建批次" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="create" :loading="creating">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import api from '../../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const batches = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const creating = ref(false)
const form = reactive({ title: '', description: '' })

onMounted(loadBatches)

async function loadBatches() {
  loading.value = true
  try {
    const { data } = await api.get('/admin/batches')
    batches.value = data
  } catch (e) {
  } finally { loading.value = false }
}

async function create() {
  creating.value = true
  try {
    await api.post('/admin/batches', form)
    ElMessage.success('创建成功')
    dialogVisible.value = false
    form.title = ''
    form.description = ''
    loadBatches()
  } catch (e) {
  } finally { creating.value = false }
}

async function endBatch(id) {
  try {
    await ElMessageBox.confirm('确定结束该批次？结束后无法重新开启', '提示', { type: 'warning' })
    await api.put('/admin/batches/' + id + '/status')
    ElMessage.success('批次已结束')
    loadBatches()
  } catch (e) {}
}

async function deleteBatch(id) {
  try {
    await ElMessageBox.confirm('确定删除该批次？', '提示', { type: 'warning' })
    await api.delete('/admin/batches/' + id)
    ElMessage.success('删除成功')
    loadBatches()
  } catch (e) {}
}
</script>

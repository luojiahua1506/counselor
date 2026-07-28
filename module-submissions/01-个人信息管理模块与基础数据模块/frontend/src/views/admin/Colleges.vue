<template>
  <div>
    <el-button type="primary" @click="showAdd">添加学院</el-button>
    <el-table :data="colleges" style="margin-top:16px" v-loading="loading">
      <el-table-column prop="name" label="学院名称" />
      <el-table-column prop="createdAt" label="创建时间" width="180" />
      <el-table-column label="操作" width="160">
        <template #default="{row}">
          <el-button size="small" @click="edit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="remove(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="isEdit?'编辑学院':'添加学院'" width="400px">
      <el-form><el-form-item label="名称"><el-input v-model="formName" /></el-form-item></el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="save" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const colleges = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const formName = ref('')
const saving = ref(false)

onMounted(loadData)

async function loadData() {
  loading.value = true
  try {
    const { data } = await api.get('/admin/colleges')
    colleges.value = data
  } catch (e) {
  } finally { loading.value = false }
}

function showAdd() { isEdit.value = false; formName.value = ''; dialogVisible.value = true }
function edit(row) { isEdit.value = true; editId.value = row.id; formName.value = row.name; dialogVisible.value = true }

async function save() {
  saving.value = true
  try {
    if (isEdit.value) {
      await api.put('/admin/colleges/' + editId.value, { name: formName.value })
    } else {
      await api.post('/admin/colleges', { name: formName.value })
    }
    ElMessage.success(isEdit.value ? '修改成功' : '添加成功')
    dialogVisible.value = false
    loadData()
  } catch (e) {
  } finally { saving.value = false }
}

async function remove(id) {
  try {
    await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' })
    await api.delete('/admin/colleges/' + id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) {}
}
</script>

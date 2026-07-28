<template>
  <div>
    <el-row :gutter="16" style="margin-bottom:16px">
      <el-col :span="8"><el-input v-model="keyword" placeholder="搜索姓名" clearable @change="loadData" /></el-col>
      <el-col :span="8"><el-select v-model="statusFilter" placeholder="状态筛选" clearable @change="loadData"><el-option label="全部" value="" /><el-option label="待审核" value="PENDING_REVIEW" /><el-option label="已激活" value="ACTIVE" /><el-option label="已禁用" value="DISABLED" /></el-select></el-col>
      <el-col :span="8"><el-button type="success" @click="exportExcel">导出Excel</el-button></el-col>
    </el-row>
    <el-table :data="counselors" v-loading="loading">
      <el-table-column label="头像" width="70"><template #default="{row}"><el-avatar :size="40" :src="row.photoUrl"><el-icon><UserFilled /></el-icon></el-avatar></template></el-table-column>
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="name" label="姓名" />
      <el-table-column label="学院"><template #default="{row}">{{ row.college?.name }}</template></el-table-column>
      <el-table-column prop="phone" label="电话" />
      <el-table-column label="状态" width="100"><template #default="{row}"><el-tag :type="statusTag(row.accountStatus)" size="small">{{ statusLabel(row.accountStatus) }}</el-tag></template></el-table-column>
      <el-table-column label="操作" width="180"><template #default="{row}"><el-button size="small" @click="$router.push('/admin/counselors/'+row.id)">详情</el-button><el-button v-if="row.accountStatus==='ACTIVE'" size="small" type="danger" @click="disable(row.id)">禁用</el-button></template></el-table-column>
    </el-table>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import api from '../../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
const counselors = ref([])
const loading = ref(false)
const keyword = ref('')
const statusFilter = ref('')
const total = ref(0)
onMounted(() => loadData())
async function loadData(page=0){loading.value=true;try{const params={page,size:20};if(keyword.value)params.keyword=keyword.value;if(statusFilter.value)params.status=statusFilter.value;const {data}=await api.get('/admin/counselors',{params});counselors.value=data.content||data;total.value=data.totalElements||0}catch(e){}finally{loading.value=false}}
function statusTag(s){const m={PENDING_REVIEW:'warning',ACTIVE:'success',DISABLED:'danger'};return m[s]||'info'}
function statusLabel(s){const m={PENDING_REVIEW:'待审核',ACTIVE:'已激活',DISABLED:'已禁用'};return m[s]||s}
async function disable(id){try{await ElMessageBox.confirm('确定禁用？','确认',{type:'warning'});await api.delete('/admin/counselors/'+id);ElMessage.success('已禁用');loadData()}catch(e){}}
function exportExcel(){api.get('/admin/export/counselors',{responseType:'blob'}).then(res=>{const url=window.URL.createObjectURL(new Blob([res.data]));const link=document.createElement('a');link.href=url;link.setAttribute('download','counselors.xlsx');document.body.appendChild(link);link.click()})}
</script>

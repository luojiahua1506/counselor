<template>
  <div>
    <h3>辅导员注册审核</h3>
    <el-table :data="pendingCounselors" v-loading="loading" style="margin-top:16px">
      <el-table-column label="头像" width="70"><template #default="{row}"><el-avatar :size="40" :src="row.photoUrl"><el-icon><UserFilled /></el-icon></el-avatar></template></el-table-column>
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="name" label="姓名" />
      <el-table-column label="学院"><template #default="{row}">{{ row.college?.name }}</template></el-table-column>
      <el-table-column prop="phone" label="电话" />
      <el-table-column prop="createdAt" label="注册时间" width="180" />
      <el-table-column label="操作" width="200"><template #default="{row}"><el-button size="small" type="success" @click="approve(row.id)">通过</el-button><el-button size="small" type="danger" @click="reject(row.id)">驳回</el-button></template></el-table-column>
    </el-table>
    <el-empty v-if="!loading && pendingCounselors.length===0" description="暂无待审核" />
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'
import api from '../../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
const pendingCounselors = ref([])
const loading = ref(false)
onMounted(loadData)
async function loadData(){loading.value=true;try{const {data}=await api.get('/admin/counselors',{params:{status:'PENDING_REVIEW',size:100}});pendingCounselors.value=data.content||data}catch(e){}finally{loading.value=false}}
async function approve(id){try{await ElMessageBox.confirm('确定通过？','提示',{type:'info'});await api.post('/admin/registrations/'+id+'/approve');ElMessage.success('已通过');loadData()}catch(e){}}
async function reject(id){try{const{value}=await ElMessageBox.prompt('请输入具体驳回原因','驳回注册',{inputValidator:v=>!!v?.trim()||'驳回原因不能为空',type:'warning'});await api.post('/admin/registrations/'+id+'/reject',{comment:value});ElMessage.success('已驳回');loadData()}catch(e){}}
</script>

<template>
  <div class="profile-page">
    <div class="profile-header">
      <el-avatar :size="80" :src="profile.photoUrl"><el-icon :size="40"><UserFilled /></el-icon></el-avatar>
      <div class="profile-title"><h3>{{ profile.name }}</h3><p>{{ profile.college }} | {{ profile.username }}</p></div>
      <el-button type="primary" size="small" @click="$router.push('/profile/edit')">编辑资料</el-button>
    </div>
    <el-tabs v-model="activeTab">
      <el-tab-pane label="基本信息" name="info">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="用户名">{{ profile.username }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ profile.name }}</el-descriptions-item>
          <el-descriptions-item label="性别">{{ profile.gender }}</el-descriptions-item>
          <el-descriptions-item label="学院">{{ profile.college }}</el-descriptions-item>
          <el-descriptions-item label="政治面貌">{{ profile.politicalStatus }}</el-descriptions-item>
          <el-descriptions-item label="最高学历">{{ profile.highestEducation }}</el-descriptions-item>
          <el-descriptions-item label="办公地址">{{ profile.officeAddress }}</el-descriptions-item>
          <el-descriptions-item label="电话">{{ profile.phone }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ profile.email }}</el-descriptions-item>
          <el-descriptions-item label="账号状态"><el-tag :type="profile.accountStatus==='ACTIVE'?'success':'warning'" size="small">{{ profile.accountStatus==='ACTIVE'?'正常':profile.accountStatus }}</el-tag></el-descriptions-item>
        </el-descriptions>
      </el-tab-pane>
      <el-tab-pane label="修改申请进度" name="requests">
        <el-timeline v-if="editRequests.length"><el-timeline-item v-for="r in editRequests" :key="r.id" :timestamp="r.createdAt" placement="top"><el-card><el-tag :type="statusTag(r.status)" size="small">{{ statusLabel(r.status) }}</el-tag><p v-if="r.adminComment" style="color:#909399;margin-top:8px">审核意见：{{ r.adminComment }}</p></el-card></el-timeline-item></el-timeline>
        <el-empty v-else description="暂无修改申请" />
      </el-tab-pane>
      <el-tab-pane label="填报记录" name="records">
        <el-table :data="records" v-if="records.length"><el-table-column prop="batchTitle" label="批次" /><el-table-column label="状态"><template #default="{row}"><el-tag :type="statusTag(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag></template></el-table-column><el-table-column prop="submittedAt" label="提交时间" /><el-table-column prop="reviewComment" label="审核意见" /></el-table>
        <el-empty v-else description="暂无填报记录" />
      </el-tab-pane>
      <el-tab-pane label="修改密码" name="password">
        <el-form :model="pwdForm" ref="pwdRef" label-width="80px" style="max-width:400px"><el-form-item label="原密码" prop="oldPassword" :rules="[{required:true,message:'请输入原密码'}]"><el-input v-model="pwdForm.oldPassword" type="password" show-password /></el-form-item><el-form-item label="新密码" prop="newPassword" :rules="[{required:true,min:6,message:'不少于6个字符'}]"><el-input v-model="pwdForm.newPassword" type="password" show-password /></el-form-item><el-form-item><el-button type="primary" @click="changePwd" :loading="pwdLoading">修改密码</el-button></el-form-item></el-form>
      </el-tab-pane>
    </el-tabs>
    <div style="margin-top:24px;text-align:center">
      <el-button type="danger" size="large" @click="handleLogout" style="width:100%;max-width:300px">退出登录</el-button>
    </div>
  </div>
</template>
<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import api from '../../utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'
const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const activeTab = ref(route.query.tab || (localStorage.getItem('counselor_mustChangePassword')==='1' ? 'password' : 'info'))
const profile = ref({})
const editRequests = ref([])
const records = ref([])
const pwdRef = ref()
const pwdLoading = ref(false)
const pwdForm = reactive({ oldPassword:'', newPassword:'' })
onMounted(async () => { try { const [p,e,r] = await Promise.all([api.get('/counselor/profile'),api.get('/counselor/edit-requests'),api.get('/counselor/submissions/records')]); profile.value=p.data; editRequests.value=e.data; records.value=r.data } catch(e) {} })
function statusTag(s){ const m={PENDING:'warning',APPROVED:'success',REJECTED:'danger',DRAFT:'info',SUBMITTED:'warning'}; return m[s]||'info' }
function statusLabel(s){ const m={PENDING:'审核中',APPROVED:'修改成功',REJECTED:'已驳回',DRAFT:'草稿',SUBMITTED:'已提交'}; return m[s]||s }
async function changePwd(){ const v=await pwdRef.value.validate().catch(()=>false); if(!v)return; pwdLoading.value=true; try{await api.put('/counselor/password',pwdForm);localStorage.setItem('counselor_mustChangePassword','0');ElMessage.success('密码修改成功');pwdForm.oldPassword='';pwdForm.newPassword='';activeTab.value='info'}catch(e){}finally{pwdLoading.value=false} }
function handleLogout() { auth.counselorLogout(); router.push('/login') }
</script>
<style scoped>
.profile-header { display:flex; align-items:center; gap:16px; margin-bottom:24px; padding:16px; background:#f5f7fa; border-radius:8px; }
.profile-title h3 { margin:0; font-size:20px; }
.profile-title p { margin:4px 0 0; color:#909399; font-size:13px; }
</style>

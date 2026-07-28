<template>
  <div class="admin-layout">
    <el-container>
      <el-aside v-show="!isMobile || sidebarOpen" :width="sidebarWidth" class="admin-sidebar">
        <div class="logo">{{ isCollapse && !isMobile ? '管理' : '管理员系统' }}</div>
        <el-menu :default-active="route.path" router :collapse="isCollapse && !isMobile" background-color="#304156" text-color="#bfcbd9" active-text-color="#409eff" @select="sidebarOpen=false">
          <el-menu-item index="/admin"><el-icon><DataAnalysis /></el-icon><span>仪表盘</span></el-menu-item>
          <el-menu-item index="/admin/batches"><el-icon><Collection /></el-icon><span>采集批次</span></el-menu-item>
          <el-menu-item index="/admin/counselors"><el-icon><UserFilled /></el-icon><span>辅导员管理</span></el-menu-item>
          <el-menu-item index="/admin/registrations"><el-icon><User /></el-icon><span>注册审核</span></el-menu-item>
          <el-menu-item index="/admin/profile-edits"><el-icon><Edit /></el-icon><span>资料修改审核</span></el-menu-item>
          <el-menu-item index="/admin/psych/dashboard"><el-icon><TrendCharts /></el-icon><span>心理数据大屏</span></el-menu-item>
          <el-menu-item index="/admin/psych/batches"><el-icon><Calendar /></el-icon><span>心理评估批次</span></el-menu-item>
          <el-menu-item index="/admin/psych/alerts"><el-icon><Warning /></el-icon><span>心理风险预警</span></el-menu-item>
          <el-menu-item index="/admin/colleges"><el-icon><School /></el-icon><span>学院管理</span></el-menu-item>
          <el-menu-item index="/admin/logs"><el-icon><Document /></el-icon><span>操作日志</span></el-menu-item>
        </el-menu>
      </el-aside>
      <div v-if="isMobile && sidebarOpen" class="mobile-overlay" @click="sidebarOpen=false"></div>
      <el-container>
        <el-header class="admin-header">
          <el-button v-if="isMobile" text @click="sidebarOpen=true"><el-icon :size="22"><Menu /></el-icon></el-button>
          <el-button v-else text @click="isCollapse=!isCollapse"><el-icon><Fold v-if="!isCollapse"/><Expand v-else/></el-icon></el-button>
          <span class="header-spacer"></span><span class="user-name">{{ auth.userName || localName }}</span>
          <el-button text size="small" @click="passwordVisible=true">修改密码</el-button>
          <el-button type="danger" text size="small" @click="logout">退出</el-button>
        </el-header>
        <el-main><router-view /></el-main>
      </el-container>
    </el-container>
    <el-dialog v-model="passwordVisible" title="修改管理员密码" width="min(92%,420px)"><el-form label-position="top"><el-form-item label="原密码"><el-input v-model="passwordForm.oldPassword" type="password" show-password/></el-form-item><el-form-item label="新密码"><el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="至少8位"/></el-form-item></el-form><template #footer><el-button @click="passwordVisible=false">取消</el-button><el-button type="primary" @click="changePassword">确认修改</el-button></template></el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import api from '../../utils/request'
import { ElMessage } from 'element-plus'
const route=useRoute(), router=useRouter(), auth=useAuthStore()
const isCollapse=ref(false), isMobile=ref(window.innerWidth<768), sidebarOpen=ref(false)
const passwordVisible=ref(false),passwordForm=ref({oldPassword:'',newPassword:''})
const localName=localStorage.getItem('admin_userName') || '管理员'
const sidebarWidth=computed(()=>isMobile.value?'230px':(isCollapse.value?'64px':'220px'))
function resize(){isMobile.value=window.innerWidth<768;if(!isMobile.value)sidebarOpen.value=false}
function logout(){auth.adminLogout();router.push('/admin/login')}
async function changePassword(){await api.put('/admin/password',passwordForm.value);ElMessage.success('管理员密码修改成功');passwordVisible.value=false;passwordForm.value={oldPassword:'',newPassword:''}}
onMounted(()=>window.addEventListener('resize',resize));onUnmounted(()=>window.removeEventListener('resize',resize))
</script>

<style scoped>
.admin-layout,.el-container{height:100%}.admin-sidebar{background:#304156;overflow-x:hidden;position:fixed;left:0;top:0;bottom:0;z-index:200}.admin-sidebar+.el-container{margin-left:220px}.logo{height:60px;line-height:60px;text-align:center;color:#fff;font-size:18px;font-weight:700}.el-menu{border-right:0}.mobile-overlay{position:fixed;inset:0;background:rgba(0,0,0,.5);z-index:199}.admin-header{background:#fff;border-bottom:1px solid #e4e7ed;display:flex;align-items:center;padding:0 16px;height:50px}.header-spacer{flex:1}.user-name{margin-right:12px;font-size:14px}.el-main{background:#f0f2f5;padding:20px;overflow:auto}@media(max-width:767px){.admin-sidebar+.el-container{margin-left:0}.el-main{padding:10px}.admin-header{padding:0 8px}.user-name{max-width:80px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}}
</style>

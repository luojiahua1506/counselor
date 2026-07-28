<template>
  <div class="profile-edit">
    <el-page-header @back="$router.push('/profile')" title="返回">
      <template #content><h3>编辑个人资料</h3></template>
    </el-page-header>
    <el-card style="margin-top:16px;max-width:600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="头像"><PhotoUpload v-model="form.photoUrl" /></el-form-item>
        <el-form-item label="姓名"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="性别"><el-select v-model="form.gender"><el-option label="男" value="男" /><el-option label="女" value="女" /></el-select></el-form-item>
        <el-form-item label="学院"><el-select v-model="form.collegeId"><el-option v-for="c in colleges" :key="c.id" :label="c.name" :value="c.id" /></el-select></el-form-item>
        <el-form-item label="政治面貌"><el-select v-model="form.politicalStatus"><el-option v-for="p in politicalStatuses" :key="p" :label="p" :value="p" /></el-select></el-form-item>
        <el-form-item label="最高学历"><el-select v-model="form.highestEducation"><el-option v-for="e in educations" :key="e" :label="e" :value="e" /></el-select></el-form-item>
        <el-form-item label="办公地址"><el-input v-model="form.officeAddress" /></el-form-item>
        <el-form-item label="电话"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
        <el-form-item>
          <el-button type="primary" @click="submit" :loading="loading">提交修改申请</el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>
<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '../../utils/request'
import PhotoUpload from '../../components/PhotoUpload.vue'
import { ElMessage } from 'element-plus'
const router = useRouter()
const loading = ref(false)
const colleges = ref([])
const politicalStatuses = ['中共党员','中共预备党员','共青团员','群众','民主党派']
const educations = ['博士','硕士','本科','大专']
const form = reactive({ name:'', gender:'', collegeId:null, politicalStatus:'', highestEducation:'', officeAddress:'', phone:'', email:'', photoUrl:'' })
onMounted(async () => {
  try {
    const [pRes, cRes] = await Promise.all([api.get('/counselor/profile'), api.get('/public/colleges')])
    colleges.value = cRes.data
    const p = pRes.data
    form.name = p.name||''; form.gender = p.gender||''; form.collegeId = p.collegeId||null
    form.politicalStatus = p.politicalStatus||''; form.highestEducation = p.highestEducation||''
    form.officeAddress = p.officeAddress||''; form.phone = p.phone||''; form.email = p.email||''
    form.photoUrl = p.photoUrl||''
  } catch(e) {}
})
async function submit() {
  loading.value = true
  try { await api.put('/counselor/profile', form); ElMessage.success('修改申请已提交，请等待管理员审核'); router.push('/profile') }
  catch(e) {}
  finally { loading.value = false }
}
</script>

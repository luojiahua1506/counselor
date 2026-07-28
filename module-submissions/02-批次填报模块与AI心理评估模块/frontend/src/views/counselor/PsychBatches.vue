<template>
  <div class="psych-page">
    <div class="page-head"><div><h2>心理评估</h2><p>了解近期心理状态，及时获得适合自己的支持建议</p></div><el-icon class="head-icon"><Opportunity /></el-icon></div>
    <el-alert title="评估结果仅用于心理状态筛查和自我了解，不构成医学诊断。" type="info" show-icon :closable="false" />
    <h3>可参加的评估</h3>
    <el-empty v-if="!batches.length" description="暂无进行中的心理评估" />
    <div class="batch-grid">
      <el-card v-for="item in batches" :key="item.id" shadow="hover" class="batch-card">
        <div class="batch-title">{{ item.title }}</div>
        <p>{{ item.description }}</p>
        <div class="batch-meta"><el-tag type="success">进行中</el-tag><span>{{ formatTime(item.endTime) }} 截止</span></div>
        <el-button v-if="item.recordStatus==='SUBMITTED'" type="success" plain @click="$router.push(`/psych/records/${item.recordId}/report`)">查看报告</el-button>
        <el-button v-else type="primary" @click="$router.push(`/psych/batches/${item.id}`)">{{ item.recordId ? '继续评估' : '开始评估' }}</el-button>
      </el-card>
    </div>
    <h3>我的评估记录</h3>
    <el-empty v-if="!records.length" description="暂无历史评估" />
    <div v-else class="record-list">
      <div v-for="item in records" :key="item.id" class="record-item">
        <div><strong>{{ item.batchTitle }}</strong><div class="muted">{{ item.status==='SUBMITTED' ? formatTime(item.submittedAt) : '草稿未提交' }}</div></div>
        <div class="record-action"><el-tag :type="riskType(item.riskLevel)">{{ item.status==='DRAFT' ? '草稿' : riskLabel(item.riskLevel) }}</el-tag><el-button v-if="item.status==='SUBMITTED'" text type="primary" @click="$router.push(`/psych/records/${item.id}/report`)">查看</el-button></div>
      </div>
    </div>
  </div>
</template>
<script setup>
import { ref, onMounted } from 'vue'; import api from '../../utils/request'
const batches=ref([]),records=ref([])
onMounted(async()=>{const [b,r]=await Promise.all([api.get('/counselor/psych/batches'),api.get('/counselor/psych/records')]);batches.value=b.data;records.value=r.data})
function formatTime(v){return v?String(v).replace('T',' ').slice(0,16):'长期有效'}
function riskLabel(v){return({LOW:'低风险',MEDIUM:'中等风险',ELEVATED:'较高风险',HIGH:'高风险'})[v]||'待评估'}
function riskType(v){return({LOW:'success',MEDIUM:'warning',ELEVATED:'danger',HIGH:'danger'})[v]||'info'}
</script>
<style scoped>
.psych-page{max-width:980px;margin:auto}.page-head{display:flex;justify-content:space-between;align-items:center;padding:22px;background:#eef7f3;border-left:4px solid #2f8f6b;margin-bottom:18px}.page-head h2{margin:0 0 6px}.page-head p,.batch-card p,.muted{color:#667085;font-size:13px}.head-icon{font-size:44px;color:#2f8f6b}.psych-page h3{margin:24px 0 14px}.batch-grid{display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:16px}.batch-card .el-button{width:100%;margin-top:16px}.batch-title{font-size:17px;font-weight:700}.batch-meta{display:flex;align-items:center;justify-content:space-between;color:#667085;font-size:12px}.record-list{border-top:1px solid #ebeef5}.record-item{display:flex;justify-content:space-between;align-items:center;padding:16px 4px;border-bottom:1px solid #ebeef5}.record-action{display:flex;align-items:center;gap:8px}@media(max-width:767px){.page-head{padding:16px}.batch-grid{grid-template-columns:1fr}.head-icon{font-size:34px}}
</style>

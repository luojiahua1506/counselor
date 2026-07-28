<template>
  <div class="report-page" v-loading="loading">
    <template v-if="report.id">
      <div class="report-head"><div><div class="eyebrow">心理评估报告</div><h2>{{ report.batchTitle }}</h2><p>{{ formatTime(report.submittedAt) }}</p></div><el-tag size="large" :type="riskType(report.riskLevel)">{{ riskLabel(report.riskLevel) }}</el-tag></div>
      <el-alert v-if="report.riskLevel==='HIGH'||report.selfHarmFlag" title="请优先获得现实中的支持" type="error" show-icon :closable="false"><p>请立即联系可信赖的人陪伴，并尽快联系学校心理中心或专业医疗机构。如存在马上伤害自己的危险，请拨打 120 或 110。</p></el-alert>
      <div class="score-grid"><div class="score-card"><span>压力感知</span><strong>{{ report.pssScore }}</strong><small>/ 40</small></div><div class="score-card"><span>焦虑状态</span><strong>{{ report.gadScore }}</strong><small>/ 21</small></div><div class="score-card"><span>情绪状态</span><strong>{{ report.phqScore }}</strong><small>/ 27</small></div></div>
      <section><h3>状态概述</h3><p>{{ report.statusSummary }}</p></section><section><h3>压力分析</h3><p>{{ report.stressAnalysis }}</p></section><section><h3>情绪分析</h3><p>{{ report.emotionAnalysis }}</p></section><section class="suggestion"><h3>行动建议</h3><p>{{ report.suggestions }}</p></section>
      <el-alert :title="report.disclaimer" type="info" :closable="false" show-icon />
      <div class="actions"><el-button @click="$router.push('/psych')">返回心理评估</el-button></div>
    </template>
  </div>
</template>
<script setup>
import {ref,onMounted} from 'vue';import {useRoute} from 'vue-router';import api from '../../utils/request';const route=useRoute(),report=ref({}),loading=ref(true);onMounted(async()=>{try{report.value=(await api.get(`/counselor/psych/records/${route.params.id}/report`)).data}finally{loading.value=false}});function riskLabel(v){return({LOW:'低风险',MEDIUM:'中等风险',ELEVATED:'较高风险',HIGH:'高风险'})[v]||v}function riskType(v){return({LOW:'success',MEDIUM:'warning',ELEVATED:'danger',HIGH:'danger'})[v]||'info'}function formatTime(v){return v?String(v).replace('T',' ').slice(0,16):''}
</script>
<style scoped>
.report-page{max-width:900px;margin:auto}.report-head{display:flex;justify-content:space-between;align-items:center;padding:22px 0;border-bottom:1px solid #ebeef5}.report-head h2{margin:4px 0}.report-head p,.eyebrow{color:#667085;font-size:13px}.score-grid{display:grid;grid-template-columns:repeat(3,1fr);gap:14px;margin:22px 0}.score-card{padding:20px;background:#f6f8fa;text-align:center;border-top:3px solid #2f8f6b}.score-card span{display:block;color:#667085}.score-card strong{font-size:34px;margin:8px 4px;color:#1d2939}.score-card small{color:#98a2b3}section{padding:18px 0;border-bottom:1px solid #ebeef5}section h3{font-size:16px;margin:0 0 8px}section p{line-height:1.8;color:#475467;margin:0;white-space:pre-line}.suggestion{background:#eef7f3;padding:18px;margin:18px 0;border-left:4px solid #2f8f6b}.actions{text-align:center;margin:22px}@media(max-width:600px){.score-grid{gap:8px}.score-card{padding:14px 4px}.score-card strong{font-size:27px}.report-head h2{font-size:19px}}
</style>

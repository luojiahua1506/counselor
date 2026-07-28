<template>
  <div class="psych-screen" v-loading="loading">
    <div class="screen-head">
      <div><span>PSYCHOLOGICAL WELLBEING</span><h1>辅导员心理健康数据大屏</h1></div>
      <el-button plain @click="$router.push('/admin/psych/alerts')">查看风险预警</el-button>
    </div>
    <div class="metric-grid">
      <div class="metric"><span>评估批次</span><strong>{{ data.batches || 0 }}</strong></div>
      <div class="metric"><span>已提交评估</span><strong>{{ data.submitted || 0 }}</strong></div>
      <div class="metric"><span>参与率</span><strong>{{ data.participationRate || 0 }}%</strong></div>
      <div class="metric warning"><span>待处理预警</span><strong>{{ data.pendingAlerts || 0 }}</strong></div>
    </div>
    <div class="panel-grid">
      <section class="panel"><h3>匿名风险分布</h3><div class="risk-chart"><div class="donut" :style="{ background: pie }"><div><strong>{{ data.submitted || 0 }}</strong><span>总评估数</span></div></div><div class="legend"><div v-for="item in risks" :key="item.key"><i :style="{ background: item.color }"></i><span>{{ item.label }}</span><b>{{ item.value }}</b></div></div></div></section>
      <section class="panel"><h3>预警跟进进度</h3><div class="progress-list"><div><span>待处理</span><el-progress :percentage="percent(data.pendingAlerts)" color="#f56c6c" /></div><div><span>处理中</span><el-progress :percentage="percent(data.processingAlerts)" color="#e6a23c" /></div><div><span>已完成</span><el-progress :percentage="percent(data.completedAlerts)" color="#67c23a" /></div></div><el-alert title="数据仅用于必要的心理关怀，不得用于绩效、晋升或纪律处理。" type="warning" :closable="false" show-icon /></section>
      <section class="panel"><h3>学院参与分布（匿名）</h3><div v-if="!collegeRows.length" class="empty">暂无数据</div><div v-for="row in collegeRows" :key="row.name" class="bar-row"><span>{{ row.name }}</span><div><i :style="{ width: row.percent + '%' }"></i></div><b>{{ row.value }}</b></div></section>
      <section class="panel"><h3>月度评估趋势</h3><div v-if="!trendRows.length" class="empty">暂无数据</div><div v-else class="trend"><div v-for="row in trendRows" :key="row.name"><b>{{ row.value }}</b><i :style="{ height: row.percent + '%' }"></i><span>{{ row.name }}</span></div></div></section>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import api from '../../utils/request'
const data = ref({ riskDistribution: {} })
const loading = ref(true)
const risks = computed(() => [
  { key: 'LOW', label: '低风险', color: '#36c98f', value: data.value.riskDistribution?.LOW || 0 },
  { key: 'MEDIUM', label: '中等风险', color: '#f1c75b', value: data.value.riskDistribution?.MEDIUM || 0 },
  { key: 'ELEVATED', label: '较高风险', color: '#ff8a5b', value: data.value.riskDistribution?.ELEVATED || 0 },
  { key: 'HIGH', label: '高风险', color: '#f45b69', value: data.value.riskDistribution?.HIGH || 0 }
])
const pie = computed(() => { const total = risks.value.reduce((sum, item) => sum + item.value, 0) || 1; let degree = 0; return `conic-gradient(${risks.value.map(item => { const start = degree; degree += item.value / total * 360; return `${item.color} ${start}deg ${degree}deg` }).join(',')})` })
const collegeRows = computed(() => makeRows(data.value.collegeDistribution))
const trendRows = computed(() => makeRows(data.value.monthlyTrend))
function makeRows(source) { const list = Object.entries(source || {}).map(([name, value]) => ({ name, value })); const max = Math.max(...list.map(item => item.value), 1); return list.map(item => ({ ...item, percent: Math.max(8, Math.round(item.value / max * 100)) })) }
function percent(value) { const total = (data.value.pendingAlerts || 0) + (data.value.processingAlerts || 0) + (data.value.completedAlerts || 0); return total ? Math.round(value / total * 100) : 0 }
onMounted(async () => { try { data.value = (await api.get('/admin/psych/dashboard')).data } finally { loading.value = false } })
</script>

<style scoped>
.psych-screen{min-height:calc(100vh - 90px);background:#10212b;color:#e9f4f1;padding:24px;border-radius:6px}.screen-head{display:flex;justify-content:space-between;align-items:center}.screen-head span{font-size:11px;color:#69d3ad}.screen-head h1{font-size:25px;margin:5px 0 20px;letter-spacing:0}.metric-grid{display:grid;grid-template-columns:repeat(4,1fr);gap:14px}.metric,.panel{background:#17323d;border:1px solid #28505b;padding:18px}.metric span{display:block;color:#9bb6b5;font-size:13px}.metric strong{font-size:34px;display:block;margin-top:8px}.metric.warning strong{color:#ff8a5b}.panel-grid{display:grid;grid-template-columns:1fr 1fr;gap:14px;margin-top:14px}.panel h3{margin:0 0 20px}.risk-chart{display:flex;align-items:center;justify-content:center;gap:36px}.donut{width:180px;height:180px;border-radius:50%;position:relative}.donut>div{position:absolute;inset:32px;border-radius:50%;background:#17323d;display:flex;flex-direction:column;align-items:center;justify-content:center}.donut strong{font-size:30px}.donut span{font-size:12px;color:#9bb6b5}.legend{min-width:150px}.legend>div{display:flex;align-items:center;gap:8px;margin:13px 0}.legend i{width:10px;height:10px}.legend span{flex:1;color:#bdd0cf}.progress-list>div{margin-bottom:20px}.progress-list span{display:block;margin-bottom:7px;color:#bdd0cf}.psych-screen :deep(.el-progress__text){color:#e9f4f1}.psych-screen .el-alert{margin-top:28px}.bar-row{display:grid;grid-template-columns:110px 1fr 30px;align-items:center;gap:10px;margin:14px 0;font-size:13px}.bar-row>div{height:9px;background:#25444e}.bar-row i{display:block;height:100%;background:#69d3ad}.trend{height:180px;display:flex;align-items:flex-end;gap:14px;padding-top:20px}.trend>div{height:100%;flex:1;display:flex;flex-direction:column;align-items:center;justify-content:flex-end;min-width:35px}.trend i{width:24px;background:#69d3ad;min-height:8px}.trend span,.trend b{font-size:11px;margin:5px 0}.empty{color:#9bb6b5;text-align:center;padding:40px}@media(max-width:900px){.metric-grid{grid-template-columns:1fr 1fr}.panel-grid{grid-template-columns:1fr}.psych-screen{padding:14px}.screen-head h1{font-size:20px}.risk-chart{gap:20px}.donut{width:140px;height:140px}.donut>div{inset:26px}}@media(max-width:480px){.screen-head{align-items:flex-start}.screen-head .el-button{padding:8px}.risk-chart{flex-direction:column}.metric strong{font-size:27px}.bar-row{grid-template-columns:90px 1fr 24px}}
</style>

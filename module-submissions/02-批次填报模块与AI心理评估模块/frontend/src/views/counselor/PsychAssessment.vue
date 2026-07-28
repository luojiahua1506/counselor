<template>
  <div class="assessment-page" v-loading="loading">
    <template v-if="detail.id">
      <div class="assessment-head"><el-button text @click="$router.back()"><el-icon><ArrowLeft /></el-icon></el-button><div><h2>{{ detail.title }}</h2><p>请根据最近两周至一个月的实际感受作答</p></div></div>
      <el-dialog v-model="consentVisible" title="心理评估知情同意说明" :close-on-click-modal="false" :show-close="false" width="min(92%, 620px)">
        <div class="notice">{{ detail.privacyNotice }}</div>
        <el-checkbox v-model="consent">我已阅读并理解以上说明，自愿参加本次评估</el-checkbox>
        <template #footer><el-button type="primary" :disabled="!consent" @click="acceptConsent">同意并开始</el-button></template>
      </el-dialog>
      <el-steps :active="step" finish-status="success" align-center><el-step title="压力"/><el-step title="焦虑"/><el-step title="情绪"/><el-step title="确认"/></el-steps>
      <div v-if="step<3" class="question-panel">
        <div class="scale-hint">{{ scaleName }}</div>
        <div v-for="(q,index) in currentQuestions" :key="q.id" class="question-item">
          <div class="question-text"><span>{{ globalIndex(index) }}.</span>{{ q.content }}</div>
          <el-radio-group v-model="answers[q.id]" class="answer-options">
            <el-radio-button v-for="option in q.options" :key="option.value" :value="option.value">{{ option.label }}</el-radio-button>
          </el-radio-group>
        </div>
      </div>
      <div v-else class="confirm-panel">
        <el-result icon="info" title="请确认后提交" sub-title="提交后将无法修改。系统会先按标准量表计算分数，再生成通俗解读。" />
        <div class="completion">已完成 {{ answeredCount }} / {{ detail.questions.length }} 题</div>
        <el-alert v-if="answeredCount<detail.questions.length" title="还有题目未完成，请返回补充后再提交。" type="warning" show-icon :closable="false" />
      </div>
      <div class="footer-actions"><el-button :disabled="step===0" @click="previous">上一步</el-button><el-button v-if="step<3" type="primary" @click="next">保存并下一步</el-button><el-button v-else type="primary" :loading="submitting" :disabled="answeredCount<detail.questions.length" @click="submit">提交评估</el-button></div>
    </template>
  </div>
</template>
<script setup>
import { ref, reactive, computed, onMounted } from 'vue'; import { useRoute, useRouter } from 'vue-router'; import { ElMessage, ElMessageBox } from 'element-plus'; import api from '../../utils/request'
const route=useRoute(),router=useRouter(),loading=ref(true),submitting=ref(false),detail=reactive({}),answers=reactive({}),step=ref(0),consent=ref(false),consentVisible=ref(false),recordId=ref(null)
const types=['PSS10','GAD7','PHQ9']; const scaleName=computed(()=>['PSS-10 压力感知量表','GAD-7 焦虑筛查量表','PHQ-9 情绪筛查量表'][step.value]); const currentQuestions=computed(()=>detail.questions?.filter(q=>q.scaleType===types[step.value])||[]); const answeredCount=computed(()=>Object.values(answers).filter(v=>v!==undefined&&v!==null).length)
onMounted(async()=>{try{Object.assign(detail,(await api.get(`/counselor/psych/batches/${route.params.id}`)).data);recordId.value=detail.recordId;if(detail.answers)Object.assign(answers,detail.answers);const saved=localStorage.getItem(`psych_answers_${detail.id}`);if(saved)Object.assign(answers,JSON.parse(saved));consentVisible.value=!recordId.value}catch(e){}finally{loading.value=false}})
function globalIndex(i){return (detail.questions?.findIndex(q=>q.id===currentQuestions.value[i].id)||0)+1}
function acceptConsent(){consentVisible.value=false}
async function save(){localStorage.setItem(`psych_answers_${detail.id}`,JSON.stringify(answers));const body={batchId:detail.id,consent:true,answers:{...answers}};const res=recordId.value?await api.put(`/counselor/psych/records/${recordId.value}`,body):await api.post('/counselor/psych/records',body);recordId.value=res.data.id}
async function next(){const missing=currentQuestions.value.some(q=>answers[q.id]===undefined);if(missing)return ElMessage.warning('请完成本页全部题目');await save();step.value++}
async function previous(){if(step.value>0){await save();step.value--}}
async function submit(){await ElMessageBox.confirm('提交后不能修改，确认提交本次心理评估吗？','确认提交',{type:'warning'});submitting.value=true;try{await save();await api.post(`/counselor/psych/records/${recordId.value}/submit`);localStorage.removeItem(`psych_answers_${detail.id}`);ElMessage.success('评估已完成');router.replace(`/psych/records/${recordId.value}/report`)}finally{submitting.value=false}}
</script>
<style scoped>
.assessment-page{max-width:900px;margin:auto;min-height:500px}.assessment-head{display:flex;align-items:flex-start;gap:8px;margin-bottom:20px}.assessment-head h2{margin:0 0 6px}.assessment-head p{margin:0;color:#667085}.notice{line-height:1.8;background:#f6f8fa;padding:14px;margin-bottom:16px;border-radius:6px}.question-panel,.confirm-panel{margin:24px 0;background:#fff}.scale-hint{padding:12px 16px;background:#eef7f3;color:#267454;font-weight:700;border-left:4px solid #2f8f6b}.question-item{padding:20px 8px;border-bottom:1px solid #ebeef5}.question-text{font-size:15px;line-height:1.7;margin-bottom:14px}.question-text span{color:#2f8f6b;font-weight:700;margin-right:6px}.answer-options{display:flex;flex-wrap:wrap}.completion{text-align:center;font-weight:700;margin-bottom:16px}.footer-actions{display:flex;justify-content:flex-end;gap:10px;position:sticky;bottom:0;background:#fff;padding:14px 0;border-top:1px solid #ebeef5}@media(max-width:767px){.assessment-head h2{font-size:19px}.question-item{padding:18px 0}.answer-options{display:grid;grid-template-columns:1fr 1fr;width:100%;gap:8px}.answer-options :deep(.el-radio-button__inner){width:100%;border:1px solid #dcdfe6;border-radius:4px}.footer-actions .el-button{flex:1}.el-steps{font-size:12px}}
</style>

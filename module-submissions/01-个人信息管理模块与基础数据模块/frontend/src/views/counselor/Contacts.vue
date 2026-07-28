<template>
  <div class="contacts-page">
    <h3>通讯录</h3>
    <el-input v-model="keyword" placeholder="搜索姓名..." clearable style="margin:16px 0;max-width:320px" />
    <div class="contact-cards" v-if="filteredContacts.length">
      <div v-for="c in filteredContacts" :key="c.id" class="contact-card">
        <el-avatar :size="56" :src="c.photoUrl"><el-icon :size="28"><UserFilled /></el-icon></el-avatar>
        <div class="c-info">
          <div class="c-name">{{ c.name }}</div>
          <div class="c-detail">{{ c.college }} | {{ c.politicalStatus }}</div>
          <div class="c-detail">{{ c.phone }}</div>
        </div>
      </div>
    </div>
    <el-empty v-else description="暂无数据" />
  </div>
</template>
<script setup>
import { ref, computed, onMounted } from 'vue'
import api from '../../utils/request'
const contacts = ref([])
const keyword = ref('')
const filteredContacts = computed(() => keyword.value ? contacts.value.filter(c=>c.name.includes(keyword.value)) : contacts.value)
onMounted(async () => { try { contacts.value = (await api.get('/counselor/contacts')).data } catch(e) {} })
</script>
<style scoped>
.contact-cards { display:grid; grid-template-columns: repeat(auto-fill, minmax(300px,1fr)); gap:12px; }
.contact-card { display:flex; align-items:center; gap:12px; padding:14px; background:#f5f7fa; border-radius:8px; }
.c-name { font-size:16px; font-weight:600; }
.c-detail { font-size:13px; color:#909399; margin-top:2px; }
</style>

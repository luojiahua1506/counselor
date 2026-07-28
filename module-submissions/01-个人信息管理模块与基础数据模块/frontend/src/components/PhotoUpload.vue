<template>
  <div class="photo-upload">
    <el-upload
      action="/api/public/upload"
      :show-file-list="false"
      :on-success="onSuccess"
      :on-error="onError"
      :before-upload="beforeUpload"
      accept="image/jpeg,image/png"
      drag
    >
      <div v-if="!modelValue" class="upload-placeholder">
        <el-icon :size="36"><Plus /></el-icon>
        <div>点击或拖拽上传照片</div>
        <div class="tip">支持 JPG、PNG，不超过 5MB</div>
      </div>
      <img v-else :src="modelValue" class="preview-img" alt="辅导员头像" />
    </el-upload>
    <div v-if="modelValue" class="preview-actions">
      <el-button size="small" type="primary" text>点击图片重新上传</el-button>
      <el-button size="small" type="danger" text @click.stop="$emit('update:modelValue', '')">删除照片</el-button>
    </div>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus'
defineProps({ modelValue: { type: String, default: '' } })
const emit = defineEmits(['update:modelValue'])

function beforeUpload(file) {
  if (file.size > 5 * 1024 * 1024) { ElMessage.error('照片不能超过 5MB'); return false }
  if (!['image/jpeg', 'image/png'].includes(file.type)) { ElMessage.error('仅支持 JPG、PNG 格式'); return false }
  return true
}
function onSuccess(response) {
  if (!response?.url) return ElMessage.error('上传失败，请重试')
  emit('update:modelValue', response.url + '&t=' + Date.now())
  ElMessage.success('照片上传成功')
}
function onError(error) {
  let message = '上传失败，请重试'
  try { message = JSON.parse(error?.message || '{}').message || message } catch (_) {}
  ElMessage.error(message)
}
</script>

<style scoped>
.photo-upload{max-width:300px}.upload-placeholder{padding:30px 0;text-align:center;color:#606266}.upload-placeholder .el-icon{margin-bottom:8px;color:#409eff}.tip{font-size:12px;color:#909399;margin-top:5px}.preview-img{width:100%;max-height:260px;object-fit:cover;border-radius:4px;display:block}.preview-actions{text-align:center;margin-top:8px}
</style>

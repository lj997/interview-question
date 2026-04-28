<template>
  <div>
    <div class="page-header">
      <h2 class="page-title">📕 错题本</h2>
      <el-button type="danger" @click="clearAllConfirm" :disabled="wrongList.length === 0">
        <el-icon><Delete /></el-icon>
        清空错题本
      </el-button>
    </div>

    <el-alert
      v-if="wrongList.length > 0"
      type="warning"
      :closable="false"
      style="margin-bottom: 20px;"
    >
      <template #title>
        共收录 <strong>{{ wrongList.length }}</strong> 道错题，其中标记为"未掌握"的题目会自动加入错题本
      </template>
    </el-alert>

    <div class="card" v-if="wrongList.length === 0 && !loading">
      <el-empty description="暂无错题，继续加油！">
        <el-button type="primary" @click="goToRandom">去随机抽题</el-button>
      </el-empty>
    </div>

    <div v-else>
      <el-card v-for="item in wrongList" :key="item.id" class="card" style="margin-bottom: 15px;">
        <template #header>
          <div class="card-header">
            <div style="display: flex; align-items: center; gap: 10px;">
              <el-tag type="info">{{ item.question?.category }}</el-tag>
              <span :class="'difficulty-' + item.question?.difficulty">
                {{ item.question?.difficulty === 1 ? '简单' : item.question?.difficulty === 2 ? '中等' : '困难' }}
              </span>
              <el-tag v-if="item.question?.company" type="success">{{ item.question?.company }}</el-tag>
              <el-tag type="danger">未掌握</el-tag>
              <el-text size="small" type="info">
                加入时间: {{ formatDate(item.addedAt) }}
              </el-text>
            </div>
            <div>
              <el-button type="primary" link @click="openNoteDialog(item)">
                <el-icon><Edit /></el-icon>
                重新作答
              </el-button>
              <el-button type="danger" link @click="removeFromWrong(item)">
                <el-icon><Close /></el-icon>
                移除
              </el-button>
            </div>
          </div>
        </template>
        <div style="white-space: pre-wrap; line-height: 1.8; font-size: 15px;">
          {{ item.question?.content }}
        </div>
        <el-divider v-if="item.note?.myAnswer" />
        <div v-if="item.note?.myAnswer" style="background: #fdf6ec; padding: 15px; border-radius: 4px;">
          <h4 style="margin-bottom: 10px; color: #e6a23c;">📝 我的作答</h4>
          <div style="white-space: pre-wrap; line-height: 1.8;">
            {{ item.note.myAnswer }}
          </div>
        </div>
      </el-card>
    </div>
  </div>

  <el-dialog v-model="showNoteDialog" title="重新作答" width="600px">
    <div v-if="currentWrong">
      <el-divider content-position="left">题目</el-divider>
      <div style="white-space: pre-wrap; padding: 15px; background: #f5f7fa; border-radius: 4px; margin-bottom: 20px;">
        {{ currentWrong.question?.content }}
      </div>
      <el-divider content-position="left">答案要点</el-divider>
      <div style="white-space: pre-wrap; padding: 15px; background: #f0f9eb; border-radius: 4px; margin-bottom: 20px;">
        {{ currentWrong.question?.answerPoints || '无' }}
      </div>
      <el-form :model="noteForm" label-width="100px">
        <el-form-item label="掌握程度">
          <el-radio-group v-model="noteForm.masteryLevel">
            <el-radio :value="0" style="color: #f56c6c;">未掌握</el-radio>
            <el-radio :value="1" style="color: #e6a23c;">了解</el-radio>
            <el-radio :value="2" style="color: #67c23a;">掌握</el-radio>
            <el-radio :value="3" style="color: #409eff;">熟练</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="重新作答">
          <el-input
            v-model="noteForm.myAnswer"
            type="textarea"
            :rows="6"
            placeholder="记录你的新回答思路..."
          />
        </el-form-item>
      </el-form>
    </div>
    <template #footer>
      <el-button @click="showNoteDialog = false">取消</el-button>
      <el-button type="primary" @click="saveNote" :loading="saving">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete, Edit, Close } from '@element-plus/icons-vue'
import { wrongQuestionApi, noteApi } from '../api'

const router = useRouter()

const loading = ref(false)
const saving = ref(false)
const showNoteDialog = ref(false)

const wrongList = ref([])
const currentWrong = ref(null)

const noteForm = reactive({
  questionId: null,
  myAnswer: '',
  masteryLevel: 0
})

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const loadWrongQuestions = async () => {
  loading.value = true
  try {
    wrongList.value = await wrongQuestionApi.getAll()
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const goToRandom = () => {
  router.push('/random')
}

const removeFromWrong = async (item) => {
  try {
    await ElMessageBox.confirm('确定要从错题本中移除这道题吗？', '提示', {
      type: 'warning'
    })
    await wrongQuestionApi.remove(item.id)
    ElMessage.success('已移除')
    loadWrongQuestions()
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
    }
  }
}

const clearAllConfirm = async () => {
  try {
    await ElMessageBox.confirm('确定要清空错题本吗？此操作不可恢复。', '警告', {
      type: 'warning',
      confirmButtonText: '确定清空',
      cancelButtonText: '取消'
    })
    
    for (const item of wrongList.value) {
      await wrongQuestionApi.remove(item.id)
    }
    
    ElMessage.success('已清空错题本')
    loadWrongQuestions()
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
    }
  }
}

const openNoteDialog = async (item) => {
  currentWrong.value = item
  noteForm.questionId = item.questionId
  
  if (item.note) {
    noteForm.myAnswer = item.note.myAnswer || ''
    noteForm.masteryLevel = item.note.masteryLevel
  } else {
    noteForm.myAnswer = ''
    noteForm.masteryLevel = 0
  }
  
  showNoteDialog.value = true
}

const saveNote = async () => {
  saving.value = true
  try {
    await noteApi.saveOrUpdate({
      questionId: noteForm.questionId,
      myAnswer: noteForm.myAnswer,
      masteryLevel: noteForm.masteryLevel
    })
    
    ElMessage.success('保存成功')
    showNoteDialog.value = false
    loadWrongQuestions()
  } catch (e) {
    console.error(e)
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadWrongQuestions()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}
</style>

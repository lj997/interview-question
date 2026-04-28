<template>
  <div>
    <div class="page-header">
      <h2 class="page-title">🔍 题目浏览与搜索</h2>
    </div>

    <div class="card filter-row">
      <el-input
        v-model="filters.keyword"
        placeholder="搜索题目内容（支持全文搜索）"
        clearable
        style="width: 300px;"
        @keyup.enter="loadQuestions"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-select v-model="filters.category" placeholder="全部分类" clearable style="width: 150px;">
        <el-option v-for="cat in categories" :key="cat" :label="cat" :value="cat" />
      </el-select>
      <el-select v-model="filters.difficulty" placeholder="全部难度" clearable style="width: 120px;">
        <el-option label="简单" :value="1" />
        <el-option label="中等" :value="2" />
        <el-option label="困难" :value="3" />
      </el-select>
      <el-select v-model="filters.company" placeholder="全部公司" clearable style="width: 150px;">
        <el-option v-for="c in companies" :key="c" :label="c" :value="c" />
      </el-select>
      <el-button type="primary" @click="loadQuestions">
        <el-icon><Search /></el-icon>
        搜索
      </el-button>
      <el-button @click="resetFilters">重置</el-button>
    </div>

    <el-card v-for="question in questionList" :key="question.id" class="card" style="margin-bottom: 15px;">
      <template #header>
        <div class="card-header">
          <div style="display: flex; align-items: center; gap: 10px;">
            <el-tag type="info">{{ question.category }}</el-tag>
            <span :class="'difficulty-' + question.difficulty">
              {{ question.difficulty === 1 ? '简单' : question.difficulty === 2 ? '中等' : '困难' }}
            </span>
            <el-tag v-if="question.company" type="success">{{ question.company }}</el-tag>
            <el-tag v-if="question.note" :type="getMasteryTagType(question.note.masteryLevel)">
              {{ getMasteryText(question.note.masteryLevel) }}
            </el-tag>
          </div>
          <div>
            <el-button type="primary" link @click="openNoteDialog(question)">
              <el-icon><Edit /></el-icon>
              笔记
            </el-button>
            <el-button type="primary" link @click="viewDetail(question)">
              <el-icon><View /></el-icon>
              详情
            </el-button>
          </div>
        </div>
      </template>
      <div style="white-space: pre-wrap; line-height: 1.8; font-size: 15px;">
        {{ question.content }}
      </div>
    </el-card>

    <el-empty v-if="questionList.length === 0 && !loading" description="暂无题目" />

    <el-pagination
      v-model:current-page="filters.page"
      v-model:page-size="filters.size"
      :page-sizes="[10, 20, 50]"
      :total="total"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="loadQuestions"
      @current-change="loadQuestions"
      style="margin-top: 20px; justify-content: flex-end;"
      v-if="total > 0"
    />
  </div>

  <el-dialog v-model="showNoteDialog" title="我的笔记" width="600px">
    <el-form :model="noteForm" label-width="100px">
      <el-form-item label="当前题目">
        <div style="padding: 10px; background: #f5f7fa; border-radius: 4px; max-height: 150px; overflow-y: auto;">
          {{ currentQuestion?.content }}
        </div>
      </el-form-item>
      <el-form-item label="掌握程度">
        <el-radio-group v-model="noteForm.masteryLevel">
          <el-radio :value="0" style="color: #f56c6c;">未掌握</el-radio>
          <el-radio :value="1" style="color: #e6a23c;">了解</el-radio>
          <el-radio :value="2" style="color: #67c23a;">掌握</el-radio>
          <el-radio :value="3" style="color: #409eff;">熟练</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="回答思路">
        <el-input
          v-model="noteForm.myAnswer"
          type="textarea"
          :rows="6"
          placeholder="记录你的回答思路、理解要点等..."
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="showNoteDialog = false">取消</el-button>
      <el-button type="primary" @click="saveNote" :loading="saving">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="showDetailDialog" title="题目详情" width="700px">
    <div v-if="currentQuestion">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="分类">{{ currentQuestion.category }}</el-descriptions-item>
        <el-descriptions-item label="难度">
          <span :class="'difficulty-' + currentQuestion.difficulty">
            {{ currentQuestion.difficulty === 1 ? '简单' : currentQuestion.difficulty === 2 ? '中等' : '困难' }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="来源公司">{{ currentQuestion.company || '无' }}</el-descriptions-item>
        <el-descriptions-item label="掌握程度">
          <el-tag v-if="currentQuestion.note" :type="getMasteryTagType(currentQuestion.note.masteryLevel)">
            {{ getMasteryText(currentQuestion.note.masteryLevel) }}
          </el-tag>
          <span v-else style="color: #909399;">未记录</span>
        </el-descriptions-item>
      </el-descriptions>
      <el-divider>题目内容</el-divider>
      <div style="white-space: pre-wrap; padding: 15px; background: #f5f7fa; border-radius: 4px; line-height: 1.8;">
        {{ currentQuestion.content }}
      </div>
      <el-divider>答案要点</el-divider>
      <div style="white-space: pre-wrap; padding: 15px; background: #f0f9eb; border-radius: 4px; line-height: 1.8;">
        {{ currentQuestion.answerPoints || '无' }}
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Edit, View } from '@element-plus/icons-vue'
import { questionApi, noteApi } from '../api'

const loading = ref(false)
const saving = ref(false)
const showNoteDialog = ref(false)
const showDetailDialog = ref(false)

const questionList = ref([])
const categories = ref([])
const companies = ref([])
const total = ref(0)
const currentQuestion = ref(null)

const filters = reactive({
  page: 1,
  size: 10,
  keyword: '',
  category: '',
  difficulty: null,
  company: ''
})

const noteForm = reactive({
  questionId: null,
  myAnswer: '',
  masteryLevel: 0
})

const getMasteryText = (level) => {
  const texts = ['未掌握', '了解', '掌握', '熟练']
  return texts[level] || '未知'
}

const getMasteryTagType = (level) => {
  const types = ['danger', 'warning', 'success', 'primary']
  return types[level] || 'info'
}

const loadQuestions = async () => {
  loading.value = true
  try {
    const params = {
      page: filters.page,
      size: filters.size,
      keyword: filters.keyword || undefined,
      category: filters.category || undefined,
      difficulty: filters.difficulty,
      company: filters.company || undefined
    }
    const res = await questionApi.getList(params)
    questionList.value = res.records
    total.value = res.total
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const loadCategories = async () => {
  try {
    categories.value = await questionApi.getCategories()
  } catch (e) {
    console.error(e)
  }
}

const loadCompanies = async () => {
  try {
    companies.value = await questionApi.getCompanies()
  } catch (e) {
    console.error(e)
  }
}

const resetFilters = () => {
  filters.page = 1
  filters.keyword = ''
  filters.category = ''
  filters.difficulty = null
  filters.company = ''
  loadQuestions()
}

const openNoteDialog = async (question) => {
  currentQuestion.value = question
  noteForm.questionId = question.id
  
  try {
    const note = await noteApi.getByQuestionId(question.id)
    if (note) {
      noteForm.myAnswer = note.myAnswer || ''
      noteForm.masteryLevel = note.masteryLevel
    } else {
      noteForm.myAnswer = ''
      noteForm.masteryLevel = 0
    }
  } catch (e) {
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
    loadQuestions()
  } catch (e) {
    console.error(e)
  } finally {
    saving.value = false
  }
}

const viewDetail = async (question) => {
  try {
    currentQuestion.value = await questionApi.getById(question.id)
    showDetailDialog.value = true
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  loadQuestions()
  loadCategories()
  loadCompanies()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>

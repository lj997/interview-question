<template>
  <div>
    <div class="page-header">
      <h2 class="page-title">📋 题库管理</h2>
      <div style="display: flex; gap: 10px;">
        <el-button type="primary" @click="showAddDialog = true">
          <el-icon><Plus /></el-icon>
          添加题目
        </el-button>
        <el-button type="success" @click="showImportDialog = true">
          <el-icon><Upload /></el-icon>
          批量导入
        </el-button>
      </div>
    </div>

    <div class="card filter-row">
      <el-input
        v-model="filters.keyword"
        placeholder="搜索题目内容"
        clearable
        style="width: 250px;"
        @keyup.enter="loadQuestions"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-select v-model="filters.category" placeholder="选择分类" clearable style="width: 150px;">
        <el-option v-for="cat in categories" :key="cat" :label="cat" :value="cat" />
      </el-select>
      <el-select v-model="filters.difficulty" placeholder="选择难度" clearable style="width: 120px;">
        <el-option label="简单" :value="1" />
        <el-option label="中等" :value="2" />
        <el-option label="困难" :value="3" />
      </el-select>
      <el-select v-model="filters.company" placeholder="来源公司" clearable style="width: 150px;">
        <el-option v-for="c in companies" :key="c" :label="c" :value="c" />
      </el-select>
      <el-button type="primary" @click="loadQuestions">搜索</el-button>
      <el-button @click="resetFilters">重置</el-button>
    </div>

    <el-table :data="questionList" style="width: 100%" v-loading="loading">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="content" label="题目内容" min-width="300">
        <template #default="{ row }">
          <div style="white-space: pre-wrap; word-break: break-all;">{{ row.content }}</div>
        </template>
      </el-table-column>
      <el-table-column prop="category" label="分类" width="100" />
      <el-table-column prop="difficulty" label="难度" width="80">
        <template #default="{ row }">
          <span :class="'difficulty-' + row.difficulty">
            {{ row.difficulty === 1 ? '简单' : row.difficulty === 2 ? '中等' : '困难' }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="company" label="来源公司" width="120" />
      <el-table-column prop="masteryLevel" label="掌握程度" width="100">
        <template #default="{ row }">
          <span v-if="row.note" :class="'mastery-' + row.note.masteryLevel">
            {{ getMasteryText(row.note.masteryLevel) }}
          </span>
          <span v-else style="color: #909399;">未记录</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="viewDetail(row)">查看</el-button>
          <el-button type="primary" link @click="editQuestion(row)">编辑</el-button>
          <el-button type="danger" link @click="deleteQuestion(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="filters.page"
      v-model:page-size="filters.size"
      :page-sizes="[10, 20, 50, 100]"
      :total="total"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="loadQuestions"
      @current-change="loadQuestions"
      style="margin-top: 20px; justify-content: flex-end;"
    />
  </div>

  <el-dialog v-model="showAddDialog" :title="editMode ? '编辑题目' : '添加题目'" width="600px">
    <el-form :model="questionForm" :rules="rules" ref="formRef" label-width="100px">
      <el-form-item label="题目内容" prop="content">
        <el-input
          v-model="questionForm.content"
          type="textarea"
          :rows="4"
          placeholder="请输入题目内容"
        />
      </el-form-item>
      <el-form-item label="答案要点" prop="answerPoints">
        <el-input
          v-model="questionForm.answerPoints"
          type="textarea"
          :rows="3"
          placeholder="请输入答案要点"
        />
      </el-form-item>
      <el-form-item label="分类" prop="category">
        <el-input v-model="questionForm.category" placeholder="如：Java、算法、数据库" />
      </el-form-item>
      <el-form-item label="难度" prop="difficulty">
        <el-radio-group v-model="questionForm.difficulty">
          <el-radio :value="1">简单</el-radio>
          <el-radio :value="2">中等</el-radio>
          <el-radio :value="3">困难</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="来源公司">
        <el-input v-model="questionForm.company" placeholder="如：阿里巴巴、字节跳动（可选）" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="showAddDialog = false">取消</el-button>
      <el-button type="primary" @click="submitQuestion" :loading="submitting">确定</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="showDetailDialog" title="题目详情" width="700px">
    <div v-if="currentQuestion">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="ID">{{ currentQuestion.id }}</el-descriptions-item>
        <el-descriptions-item label="分类">{{ currentQuestion.category }}</el-descriptions-item>
        <el-descriptions-item label="难度">
          <span :class="'difficulty-' + currentQuestion.difficulty">
            {{ currentQuestion.difficulty === 1 ? '简单' : currentQuestion.difficulty === 2 ? '中等' : '困难' }}
          </span>
        </el-descriptions-item>
        <el-descriptions-item label="来源公司">{{ currentQuestion.company || '无' }}</el-descriptions-item>
      </el-descriptions>
      <el-divider>题目内容</el-divider>
      <div style="white-space: pre-wrap; padding: 15px; background: #f5f7fa; border-radius: 4px;">
        {{ currentQuestion.content }}
      </div>
      <el-divider>答案要点</el-divider>
      <div style="white-space: pre-wrap; padding: 15px; background: #f0f9eb; border-radius: 4px;">
        {{ currentQuestion.answerPoints || '无' }}
      </div>
      <el-divider>我的笔记</el-divider>
      <el-form v-if="currentNote" label-width="100px">
        <el-form-item label="掌握程度">
          <el-tag :type="getMasteryTagType(currentNote.masteryLevel)">
            {{ getMasteryText(currentNote.masteryLevel) }}
          </el-tag>
        </el-form-item>
        <el-form-item label="回答思路">
          <div style="white-space: pre-wrap; padding: 10px; background: #f5f7fa; border-radius: 4px;">
            {{ currentNote.myAnswer || '无' }}
          </div>
        </el-form-item>
      </el-form>
      <el-empty v-else description="暂无笔记" />
    </div>
  </el-dialog>

  <el-dialog v-model="showImportDialog" title="批量导入题目" width="600px">
    <el-tabs v-model="importTab">
      <el-tab-pane label="JSON导入" name="json">
        <div class="card" style="margin-bottom: 15px;">
          <h4>JSON格式示例：</h4>
          <pre style="background: #f5f7fa; padding: 10px; border-radius: 4px; overflow-x: auto;">
[
  {
    "content": "题目内容1",
    "answerPoints": "答案要点1",
    "category": "Java",
    "difficulty": 2,
    "company": "阿里巴巴"
  }
]
          </pre>
        </div>
        <el-input
          v-model="jsonContent"
          type="textarea"
          :rows="8"
          placeholder="请粘贴JSON格式的题目数据"
        />
        <div style="margin-top: 15px;">
          <el-button type="primary" @click="importJson" :loading="importing">导入</el-button>
        </div>
      </el-tab-pane>
      <el-tab-pane label="CSV导入" name="csv">
        <div class="card" style="margin-bottom: 15px;">
          <h4>CSV格式要求：</h4>
          <p>第一行为表头，包含列：content, answer_points, category, difficulty, company</p>
        </div>
        <el-upload
          ref="uploadRef"
          :auto-upload="false"
          :limit="1"
          accept=".csv"
          :on-change="handleFileChange"
          drag
        >
          <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
          <div class="el-upload__text">
            将CSV文件拖到此处，或<em>点击上传</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">只能上传csv文件</div>
          </template>
        </el-upload>
        <div v-if="selectedFile" style="margin-top: 10px;">
          <el-tag>已选择: {{ selectedFile.name }}</el-tag>
        </div>
        <div style="margin-top: 15px;">
          <el-button type="primary" @click="importCsv" :loading="importing" :disabled="!selectedFile">
            导入
          </el-button>
        </div>
      </el-tab-pane>
    </el-tabs>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Upload, UploadFilled } from '@element-plus/icons-vue'
import { questionApi, noteApi } from '../api'

const loading = ref(false)
const submitting = ref(false)
const importing = ref(false)
const editMode = ref(false)
const showAddDialog = ref(false)
const showDetailDialog = ref(false)
const showImportDialog = ref(false)
const importTab = ref('json')

const questionList = ref([])
const categories = ref([])
const companies = ref([])
const total = ref(0)
const currentQuestion = ref(null)
const currentNote = ref(null)
const selectedFile = ref(null)
const jsonContent = ref('')

const filters = reactive({
  page: 1,
  size: 10,
  keyword: '',
  category: '',
  difficulty: null,
  company: ''
})

const questionForm = reactive({
  id: null,
  content: '',
  answerPoints: '',
  category: '',
  difficulty: 2,
  company: ''
})

const rules = {
  content: [{ required: true, message: '请输入题目内容', trigger: 'blur' }],
  category: [{ required: true, message: '请输入分类', trigger: 'blur' }],
  difficulty: [{ required: true, message: '请选择难度', trigger: 'change' }]
}

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

const editQuestion = (row) => {
  editMode.value = true
  Object.assign(questionForm, {
    id: row.id,
    content: row.content,
    answerPoints: row.answerPoints || '',
    category: row.category,
    difficulty: row.difficulty,
    company: row.company || ''
  })
  showAddDialog.value = true
}

const submitQuestion = async () => {
  submitting.value = true
  try {
    if (editMode.value) {
      await questionApi.update(questionForm.id, questionForm)
      ElMessage.success('更新成功')
    } else {
      await questionApi.create(questionForm)
      ElMessage.success('添加成功')
    }
    showAddDialog.value = false
    loadQuestions()
    loadCategories()
    loadCompanies()
  } catch (e) {
    console.error(e)
  } finally {
    submitting.value = false
  }
}

const deleteQuestion = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这道题目吗？', '提示', {
      type: 'warning'
    })
    await questionApi.delete(row.id)
    ElMessage.success('删除成功')
    loadQuestions()
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
    }
  }
}

const viewDetail = async (row) => {
  try {
    currentQuestion.value = await questionApi.getById(row.id)
    currentNote.value = currentQuestion.value.note
    showDetailDialog.value = true
  } catch (e) {
    console.error(e)
  }
}

const handleFileChange = (file) => {
  selectedFile.value = file.raw
}

const importJson = async () => {
  if (!jsonContent.value.trim()) {
    ElMessage.warning('请输入JSON内容')
    return
  }
  importing.value = true
  try {
    const res = await questionApi.importJson(jsonContent.value)
    ElMessage.success(res.message)
    showImportDialog.value = false
    jsonContent.value = ''
    loadQuestions()
    loadCategories()
    loadCompanies()
  } catch (e) {
    console.error(e)
  } finally {
    importing.value = false
  }
}

const importCsv = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请选择CSV文件')
    return
  }
  importing.value = true
  try {
    const res = await questionApi.importCsv(selectedFile.value)
    ElMessage.success(res.message)
    showImportDialog.value = false
    selectedFile.value = null
    loadQuestions()
    loadCategories()
    loadCompanies()
  } catch (e) {
    console.error(e)
  } finally {
    importing.value = false
  }
}

showAddDialog.value = false
onMounted(() => {
  loadQuestions()
  loadCategories()
  loadCompanies()
})
</script>

<template>
  <div>
    <div class="page-header">
      <h2 class="page-title">📅 面试记录</h2>
      <el-button type="primary" @click="showAddDialog = true">
        <el-icon><Plus /></el-icon>
        新增记录
      </el-button>
    </div>

    <div class="card">
      <el-input
        v-model="searchCompany"
        placeholder="搜索公司名称"
        clearable
        style="width: 250px; margin-bottom: 15px;"
        @keyup.enter="loadRecords"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
    </div>

    <el-timeline v-if="recordList.length > 0">
      <el-timeline-item
        v-for="record in recordList"
        :key="record.id"
        :timestamp="formatDate(record.interviewDate)"
        placement="top"
        :type="getTimelineType(record)"
      >
        <el-card>
          <template #header>
            <div class="card-header">
              <div style="display: flex; align-items: center; gap: 15px;">
                <h3 style="margin: 0;">{{ record.company }}</h3>
                <el-tag :type="getTimelineType(record)">{{ record.questions?.length || 0 }} 道题目</el-tag>
              </div>
              <div>
                <el-button type="primary" link @click="viewDetail(record)">
                  <el-icon><View /></el-icon>
                  详情
                </el-button>
                <el-button type="primary" link @click="editRecord(record)">
                  <el-icon><Edit /></el-icon>
                  编辑
                </el-button>
                <el-button type="danger" link @click="deleteRecord(record)">
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
              </div>
            </div>
          </template>
          <div v-if="record.summary">
            <h4 style="margin-bottom: 10px; color: #303133;">📝 经验总结</h4>
            <div style="white-space: pre-wrap; line-height: 1.8; color: #606266;">
              {{ record.summary }}
            </div>
          </div>
          <div v-else style="color: #909399;">
            暂无经验总结
          </div>
        </el-card>
      </el-timeline-item>
    </el-timeline>

    <el-empty v-else description="暂无面试记录" />

    <el-pagination
      v-model:current-page="filters.page"
      v-model:page-size="filters.size"
      :page-sizes="[10, 20, 50]"
      :total="total"
      layout="total, sizes, prev, pager, next"
      @size-change="loadRecords"
      @current-change="loadRecords"
      style="margin-top: 20px; justify-content: flex-end;"
      v-if="total > 0"
    />
  </div>

  <el-dialog v-model="showAddDialog" :title="editMode ? '编辑面试记录' : '新增面试记录'" width="700px">
    <el-form :model="recordForm" :rules="rules" ref="formRef" label-width="100px">
      <el-form-item label="面试公司" prop="company">
        <el-input v-model="recordForm.company" placeholder="请输入公司名称" />
      </el-form-item>
      <el-form-item label="面试日期" prop="interviewDate">
        <el-date-picker
          v-model="recordForm.interviewDate"
          type="date"
          placeholder="选择面试日期"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          style="width: 100%;"
        />
      </el-form-item>
      <el-form-item label="关联题目">
        <div style="margin-bottom: 10px;">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索题目添加"
            clearable
            style="width: 300px;"
          >
            <template #append>
              <el-button @click="searchQuestions" :loading="searching">搜索</el-button>
            </template>
          </el-input>
        </div>
        <div v-if="searchResults.length > 0" style="background: #f5f7fa; padding: 10px; border-radius: 4px; max-height: 200px; overflow-y: auto; margin-bottom: 10px;">
          <div
            v-for="q in searchResults"
            :key="q.id"
            style="padding: 8px; border-bottom: 1px solid #e4e7ed; cursor: pointer; display: flex; justify-content: space-between; align-items: center;"
            :class="{ 'is-selected': selectedQuestionIds.includes(q.id) }"
            @click="toggleQuestion(q.id)"
          >
            <div style="flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">
              {{ q.content }}
            </div>
            <el-tag v-if="selectedQuestionIds.includes(q.id)" type="success" size="small">已选</el-tag>
          </div>
        </div>
        <div v-if="selectedQuestions.length > 0">
          <h4 style="margin-bottom: 10px;">已选题目 ({{ selectedQuestions.length }} 道):</h4>
          <el-tag
            v-for="q in selectedQuestions"
            :key="q.id"
            closable
            @close="removeQuestion(q.id)"
            style="margin-right: 5px; margin-bottom: 5px;"
          >
            {{ q.content.length > 20 ? q.content.substring(0, 20) + '...' : q.content }}
          </el-tag>
        </div>
      </el-form-item>
      <el-form-item label="经验总结">
        <el-input
          v-model="recordForm.summary"
          type="textarea"
          :rows="5"
          placeholder="记录这次面试的经验教训、心得体会..."
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="showAddDialog = false">取消</el-button>
      <el-button type="primary" @click="submitRecord" :loading="submitting">确定</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="showDetailDialog" title="面试记录详情" width="800px">
    <div v-if="currentRecord">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="面试公司">{{ currentRecord.company }}</el-descriptions-item>
        <el-descriptions-item label="面试日期">{{ formatDate(currentRecord.interviewDate) }}</el-descriptions-item>
        <el-descriptions-item label="题目数量" :span="2">{{ currentRecord.questions?.length || 0 }} 道</el-descriptions-item>
      </el-descriptions>
      <el-divider>面试题目</el-divider>
      <div v-if="currentRecord.questions?.length > 0">
        <div
          v-for="(q, index) in currentRecord.questions"
          :key="q.id"
          style="margin-bottom: 20px; padding: 15px; background: #f5f7fa; border-radius: 8px;"
        >
          <div style="display: flex; gap: 10px; margin-bottom: 10px;">
            <el-tag type="info">第{{ index + 1 }}题</el-tag>
            <el-tag>{{ q.category }}</el-tag>
            <span :class="'difficulty-' + q.difficulty">
              {{ q.difficulty === 1 ? '简单' : q.difficulty === 2 ? '中等' : '困难' }}
            </span>
          </div>
          <div style="white-space: pre-wrap; line-height: 1.8;">{{ q.content }}</div>
        </div>
      </div>
      <el-empty v-else description="未关联题目" />
      <el-divider>经验总结</el-divider>
      <div v-if="currentRecord.summary" style="white-space: pre-wrap; line-height: 1.8; padding: 15px; background: #f0f9eb; border-radius: 8px;">
        {{ currentRecord.summary }}
      </div>
      <el-empty v-else description="暂无经验总结" />
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, View, Edit, Delete } from '@element-plus/icons-vue'
import { interviewRecordApi, questionApi } from '../api'

const loading = ref(false)
const submitting = ref(false)
const searching = ref(false)
const editMode = ref(false)
const showAddDialog = ref(false)
const showDetailDialog = ref(false)

const recordList = ref([])
const searchResults = ref([])
const total = ref(0)
const currentRecord = ref(null)
const searchKeyword = ref('')
const searchCompany = ref('')
const selectedQuestionIds = ref([])

const filters = reactive({
  page: 1,
  size: 10
})

const recordForm = reactive({
  id: null,
  company: '',
  interviewDate: '',
  summary: ''
})

const rules = {
  company: [{ required: true, message: '请输入公司名称', trigger: 'blur' }],
  interviewDate: [{ required: true, message: '请选择面试日期', trigger: 'change' }]
}

const selectedQuestions = computed(() => {
  return searchResults.value.filter(q => selectedQuestionIds.value.includes(q.id))
})

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

const getTimelineType = (record) => {
  if (record.summary && record.summary.includes('通过')) return 'success'
  if (record.summary && record.summary.includes('失败')) return 'danger'
  return 'primary'
}

const loadRecords = async () => {
  loading.value = true
  try {
    const params = {
      page: filters.page,
      size: filters.size,
      company: searchCompany.value || undefined
    }
    const res = await interviewRecordApi.getList(params)
    recordList.value = res.records
    total.value = res.total
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const searchQuestions = async () => {
  if (!searchKeyword.value.trim()) {
    ElMessage.warning('请输入搜索关键词')
    return
  }
  searching.value = true
  try {
    const res = await questionApi.getList({
      page: 1,
      size: 20,
      keyword: searchKeyword.value
    })
    searchResults.value = res.records
  } catch (e) {
    console.error(e)
  } finally {
    searching.value = false
  }
}

const toggleQuestion = (id) => {
  const index = selectedQuestionIds.value.indexOf(id)
  if (index > -1) {
    selectedQuestionIds.value.splice(index, 1)
  } else {
    selectedQuestionIds.value.push(id)
  }
}

const removeQuestion = (id) => {
  const index = selectedQuestionIds.value.indexOf(id)
  if (index > -1) {
    selectedQuestionIds.value.splice(index, 1)
  }
}

const viewDetail = async (record) => {
  try {
    currentRecord.value = await interviewRecordApi.getById(record.id)
    showDetailDialog.value = true
  } catch (e) {
    console.error(e)
  }
}

const editRecord = async (record) => {
  editMode.value = true
  try {
    const detail = await interviewRecordApi.getById(record.id)
    recordForm.id = detail.id
    recordForm.company = detail.company
    recordForm.interviewDate = detail.interviewDate
    recordForm.summary = detail.summary || ''
    
    if (detail.questions) {
      selectedQuestionIds.value = detail.questions.map(q => q.id)
      searchResults.value = detail.questions
    }
  } catch (e) {
    console.error(e)
  }
  showAddDialog.value = true
}

const submitRecord = async () => {
  submitting.value = true
  try {
    const data = {
      company: recordForm.company,
      interviewDate: recordForm.interviewDate,
      questionIds: selectedQuestionIds.value,
      summary: recordForm.summary
    }
    
    if (editMode.value) {
      await interviewRecordApi.update(recordForm.id, data)
      ElMessage.success('更新成功')
    } else {
      await interviewRecordApi.create(data)
      ElMessage.success('添加成功')
    }
    
    showAddDialog.value = false
    loadRecords()
  } catch (e) {
    console.error(e)
  } finally {
    submitting.value = false
  }
}

const deleteRecord = async (record) => {
  try {
    await ElMessageBox.confirm('确定要删除这条面试记录吗？', '提示', {
      type: 'warning'
    })
    await interviewRecordApi.delete(record.id)
    ElMessage.success('删除成功')
    loadRecords()
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
    }
  }
}

showAddDialog.value = false
onMounted(() => {
  loadRecords()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.is-selected {
  background: #ecf5ff !important;
}

:deep(.el-timeline-item__timestamp.is-top) {
  margin-bottom: 8px;
  font-size: 14px;
}
</style>

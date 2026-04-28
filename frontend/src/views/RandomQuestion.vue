<template>
  <div>
    <div class="page-header">
      <h2 class="page-title">🎲 随机抽题 - 模拟面试</h2>
    </div>

    <div v-if="!isInterviewing" class="card">
      <h3 style="margin-bottom: 20px;">设置抽题条件</h3>
      <el-form :model="randomParams" label-width="100px" style="max-width: 500px;">
        <el-form-item label="分类">
          <el-select v-model="randomParams.category" placeholder="全部" clearable style="width: 200px;">
            <el-option v-for="cat in categories" :key="cat" :label="cat" :value="cat" />
          </el-select>
        </el-form-item>
        <el-form-item label="难度">
          <el-select v-model="randomParams.difficulty" placeholder="全部" clearable style="width: 200px;">
            <el-option label="简单" :value="1" />
            <el-option label="中等" :value="2" />
            <el-option label="困难" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="抽题数量">
          <el-input-number v-model="randomParams.count" :min="1" :max="50" style="width: 200px;" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" @click="startRandom" :loading="loading">
            <el-icon><Promotion /></el-icon>
            开始抽题
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <div v-if="isAnimating" class="animation-container">
      <div class="cards-wrapper">
        <div
          v-for="(card, index) in animationCards"
          :key="card.id"
          class="animation-card"
          :style="getCardStyle(index)"
        >
          <div class="card-content">
            <div class="card-category">{{ card.category }}</div>
            <div class="card-question">{{ card.content }}</div>
          </div>
        </div>
      </div>
      <div class="animation-title">
        <span class="animate-text">正在抽取题目...</span>
      </div>
    </div>

    <div v-if="isInterviewing && !isAnimating" class="interview-container">
      <div class="interview-header">
        <el-progress
          :percentage="Math.round(((currentIndex + 1) / questions.length) * 100)"
          :stroke-width="20"
          :text-inside="true"
          style="margin-bottom: 20px;"
        />
        <div class="progress-info">
          <span>第 {{ currentIndex + 1 }} / {{ questions.length }} 题</span>
          <el-button-group>
            <el-button @click="prevQuestion" :disabled="currentIndex === 0">
              <el-icon><ArrowLeft /></el-icon>
              上一题
            </el-button>
            <el-button type="primary" @click="nextQuestion" v-if="currentIndex < questions.length - 1">
              下一题
              <el-icon><ArrowRight /></el-icon>
            </el-button>
            <el-button type="success" @click="finishInterview" v-else>
              结束面试
              <el-icon><Check /></el-icon>
            </el-button>
          </el-button-group>
        </div>
      </div>

      <div class="question-card" :key="currentQuestion?.id">
        <div class="question-header">
          <div class="question-tags">
            <el-tag type="info">{{ currentQuestion?.category }}</el-tag>
            <span :class="'difficulty-' + currentQuestion?.difficulty">
              {{ currentQuestion?.difficulty === 1 ? '简单' : currentQuestion?.difficulty === 2 ? '中等' : '困难' }}
            </span>
            <el-tag v-if="currentQuestion?.company" type="success">{{ currentQuestion?.company }}</el-tag>
          </div>
          <div>
            <el-button type="primary" link @click="showAnswer = !showAnswer">
              {{ showAnswer ? '隐藏答案' : '显示答案' }}
            </el-button>
            <el-button type="primary" link @click="openNoteDialog">
              <el-icon><Edit /></el-icon>
              记录笔记
            </el-button>
          </div>
        </div>

        <div class="question-content">
          <h4 style="margin-bottom: 15px; color: #303133;">📝 题目</h4>
          <div class="content-text">{{ currentQuestion?.content }}</div>
        </div>

        <transition name="fade">
          <div v-if="showAnswer" class="answer-content">
            <h4 style="margin-bottom: 15px; color: #67c23a;">💡 答案要点</h4>
            <div class="content-text answer-text">{{ currentQuestion?.answerPoints || '暂无答案要点' }}</div>
          </div>
        </transition>
      </div>

      <div class="question-nav">
        <div class="nav-title">题目导航</div>
        <div class="nav-dots">
          <div
            v-for="(q, index) in questions"
            :key="q.id"
            class="nav-dot"
            :class="{
              active: index === currentIndex,
              visited: index < currentIndex,
              hasNote: q.note
            }"
            @click="jumpToQuestion(index)"
            :title="`第${index + 1}题`"
          >
            {{ index + 1 }}
          </div>
        </div>
      </div>
    </div>
  </div>

  <el-dialog v-model="showNoteDialog" title="记录笔记" width="500px">
    <el-form :model="noteForm" label-width="100px">
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
          :rows="5"
          placeholder="记录你的回答思路..."
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="showNoteDialog = false">取消</el-button>
      <el-button type="primary" @click="saveNote" :loading="saving">保存</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="showFinishDialog" title="🎯 模拟面试结束" width="500px">
    <div style="text-align: center; padding: 20px;">
      <el-icon size="80" color="#67c23a"><CircleCheck /></el-icon>
      <h3 style="margin: 20px 0;">恭喜完成本次模拟面试！</h3>
      <el-descriptions :column="1" border>
        <el-descriptions-item label="抽取题目数">{{ questions.length }} 道</el-descriptions-item>
        <el-descriptions-item label="已记录笔记数">{{ notesCount }} 道</el-descriptions-item>
      </el-descriptions>
    </div>
    <template #footer>
      <el-button @click="resetInterview">重新抽题</el-button>
      <el-button type="primary" @click="goToBrowse">去题库浏览</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Promotion, ArrowLeft, ArrowRight, Check, Edit, CircleCheck } from '@element-plus/icons-vue'
import { questionApi, noteApi } from '../api'

const router = useRouter()

const loading = ref(false)
const saving = ref(false)
const isInterviewing = ref(false)
const isAnimating = ref(false)
const showAnswer = ref(false)
const showNoteDialog = ref(false)
const showFinishDialog = ref(false)

const categories = ref([])
const questions = ref([])
const currentIndex = ref(0)
const animationCards = ref([])
const animationInterval = ref(null)

const randomParams = reactive({
  category: '',
  difficulty: null,
  count: 10
})

const noteForm = reactive({
  questionId: null,
  myAnswer: '',
  masteryLevel: 0
})

const currentQuestion = computed(() => questions.value[currentIndex.value])

const notesCount = computed(() => {
  return questions.value.filter(q => q.note).length
})

const loadCategories = async () => {
  try {
    categories.value = await questionApi.getCategories()
  } catch (e) {
    console.error(e)
  }
}

const getCardStyle = (index) => {
  const total = animationCards.value.length
  const center = Math.floor(total / 2)
  const offset = index - center
  const rotate = offset * 5
  const translateX = offset * 30
  const zIndex = total - Math.abs(offset)
  const scale = 1 - Math.abs(offset) * 0.05
  
  return {
    transform: `translateX(${translateX}px) rotate(${rotate}deg) scale(${scale})`,
    zIndex
  }
}

const startRandom = async () => {
  if (randomParams.count < 1) {
    ElMessage.warning('请选择至少1道题目')
    return
  }
  
  loading.value = true
  try {
    const params = {
      category: randomParams.category || undefined,
      difficulty: randomParams.difficulty,
      count: randomParams.count
    }
    
    const result = await questionApi.randomSelect(params)
    
    if (result.length === 0) {
      ElMessage.warning('没有找到符合条件的题目')
      return
    }
    
    questions.value = result
    
    await playAnimation()
    
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const playAnimation = async () => {
  isAnimating.value = true
  
  const displayCards = questions.value.slice(0, Math.min(questions.value.length, 10))
  animationCards.value = [...displayCards, ...displayCards, ...displayCards]
  
  let rotation = 0
  animationInterval.value = setInterval(() => {
    rotation += 1
    if (rotation > animationCards.value.length - 10) {
      rotation = 0
    }
    
    const start = rotation % animationCards.value.length
    const display = []
    for (let i = 0; i < 10; i++) {
      const idx = (start + i) % animationCards.value.length
      display.push(animationCards.value[idx])
    }
  }, 100)
  
  await new Promise(resolve => setTimeout(resolve, 2500))
  
  clearInterval(animationInterval.value)
  isAnimating.value = false
  isInterviewing.value = true
  currentIndex.value = 0
  showAnswer.value = false
}

const prevQuestion = () => {
  if (currentIndex.value > 0) {
    currentIndex.value--
    showAnswer.value = false
  }
}

const nextQuestion = () => {
  if (currentIndex.value < questions.value.length - 1) {
    currentIndex.value++
    showAnswer.value = false
  }
}

const jumpToQuestion = (index) => {
  currentIndex.value = index
  showAnswer.value = false
}

const openNoteDialog = async () => {
  if (!currentQuestion.value) return
  
  noteForm.questionId = currentQuestion.value.id
  
  try {
    const note = await noteApi.getByQuestionId(currentQuestion.value.id)
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
    const savedNote = await noteApi.saveOrUpdate({
      questionId: noteForm.questionId,
      myAnswer: noteForm.myAnswer,
      masteryLevel: noteForm.masteryLevel
    })
    
    const q = questions.value.find(q => q.id === noteForm.questionId)
    if (q) {
      q.note = savedNote
    }
    
    ElMessage.success('保存成功')
    showNoteDialog.value = false
  } catch (e) {
    console.error(e)
  } finally {
    saving.value = false
  }
}

const finishInterview = () => {
  showFinishDialog.value = true
}

const resetInterview = () => {
  isInterviewing.value = false
  isAnimating.value = false
  questions.value = []
  currentIndex.value = 0
  showAnswer.value = false
  showFinishDialog.value = false
}

const goToBrowse = () => {
  showFinishDialog.value = false
  router.push('/browse')
}

onMounted(() => {
  loadCategories()
})
</script>

<style scoped>
.animation-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  position: relative;
}

.cards-wrapper {
  position: relative;
  width: 400px;
  height: 250px;
  perspective: 1000px;
}

.animation-card {
  position: absolute;
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  box-shadow: 0 10px 40px rgba(102, 126, 234, 0.4);
  transition: all 0.3s ease;
}

.card-content {
  height: 100%;
  padding: 20px;
  color: white;
  display: flex;
  flex-direction: column;
}

.card-category {
  font-size: 14px;
  opacity: 0.8;
  margin-bottom: 10px;
}

.card-question {
  flex: 1;
  font-size: 16px;
  line-height: 1.6;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 6;
  -webkit-box-orient: vertical;
}

.animation-title {
  margin-top: 40px;
}

.animate-text {
  font-size: 24px;
  font-weight: bold;
  background: linear-gradient(90deg, #667eea, #764ba2, #667eea);
  background-size: 200% auto;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  animation: gradient 2s linear infinite;
}

@keyframes gradient {
  0% { background-position: 0% center; }
  100% { background-position: 200% center; }
}

.interview-container {
  max-width: 900px;
  margin: 0 auto;
}

.interview-header {
  margin-bottom: 20px;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.question-card {
  background: white;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  padding: 30px;
  margin-bottom: 20px;
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
}

.question-tags {
  display: flex;
  gap: 10px;
  align-items: center;
}

.question-content {
  margin-bottom: 20px;
}

.answer-content {
  border-top: 1px solid #ebeef5;
  padding-top: 20px;
}

.content-text {
  white-space: pre-wrap;
  line-height: 1.8;
  font-size: 15px;
  color: #606266;
}

.answer-text {
  color: #67c23a;
}

.fade-enter-active,
.fade-leave-active {
  transition: all 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

.question-nav {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.nav-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 15px;
}

.nav-dots {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.nav-dot {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #f4f4f5;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.nav-dot:hover {
  background: #ecf5ff;
}

.nav-dot.active {
  background: #409eff;
  color: white;
  border-color: #409eff;
}

.nav-dot.visited {
  background: #f0f9eb;
  border-color: #67c23a;
}

.nav-dot.hasNote {
  position: relative;
}

.nav-dot.hasNote::after {
  content: '';
  position: absolute;
  top: -2px;
  right: -2px;
  width: 10px;
  height: 10px;
  background: #f56c6c;
  border-radius: 50%;
}
</style>

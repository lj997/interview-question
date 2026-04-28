import { createRouter, createWebHistory } from 'vue-router'
import QuestionManage from '../views/QuestionManage.vue'
import QuestionBrowse from '../views/QuestionBrowse.vue'
import RandomQuestion from '../views/RandomQuestion.vue'
import WrongQuestion from '../views/WrongQuestion.vue'
import InterviewRecords from '../views/InterviewRecords.vue'

const routes = [
  {
    path: '/',
    redirect: '/questions'
  },
  {
    path: '/questions',
    name: 'QuestionManage',
    component: QuestionManage
  },
  {
    path: '/browse',
    name: 'QuestionBrowse',
    component: QuestionBrowse
  },
  {
    path: '/random',
    name: 'RandomQuestion',
    component: RandomQuestion
  },
  {
    path: '/wrong',
    name: 'WrongQuestion',
    component: WrongQuestion
  },
  {
    path: '/interviews',
    name: 'InterviewRecords',
    component: InterviewRecords
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router

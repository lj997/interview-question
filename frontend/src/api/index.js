import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 30000
})

request.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res.data
  },
  error => {
    ElMessage.error(error.message || '网络错误')
    return Promise.reject(error)
  }
)

export const questionApi = {
  getList(params) {
    return request.get('/questions', { params })
  },
  getById(id) {
    return request.get(`/questions/${id}`)
  },
  create(data) {
    return request.post('/questions', data)
  },
  update(id, data) {
    return request.put(`/questions/${id}`, data)
  },
  delete(id) {
    return request.delete(`/questions/${id}`)
  },
  getCategories() {
    return request.get('/questions/categories')
  },
  getCompanies() {
    return request.get('/questions/companies')
  },
  randomSelect(params) {
    return request.get('/questions/random', { params })
  },
  importJson(jsonContent) {
    return request.post('/questions/import/json', jsonContent, {
      headers: { 'Content-Type': 'application/json' }
    })
  },
  importCsv(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request.post('/questions/import/csv', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  }
}

export const noteApi = {
  getByQuestionId(questionId) {
    return request.get(`/notes/question/${questionId}`)
  },
  saveOrUpdate(data) {
    return request.post('/notes', data)
  },
  deleteByQuestionId(questionId) {
    return request.delete(`/notes/question/${questionId}`)
  }
}

export const wrongQuestionApi = {
  getList(params) {
    return request.get('/wrong-questions', { params })
  },
  getAll() {
    return request.get('/wrong-questions/all')
  },
  addManual(questionId) {
    return request.post('/wrong-questions', null, { params: { questionId } })
  },
  remove(id) {
    return request.delete(`/wrong-questions/${id}`)
  },
  removeByQuestionId(questionId) {
    return request.delete(`/wrong-questions/question/${questionId}`)
  }
}

export const interviewRecordApi = {
  getList(params) {
    return request.get('/interview-records', { params })
  },
  getById(id) {
    return request.get(`/interview-records/${id}`)
  },
  create(data) {
    return request.post('/interview-records', data)
  },
  update(id, data) {
    return request.put(`/interview-records/${id}`, data)
  },
  delete(id) {
    return request.delete(`/interview-records/${id}`)
  }
}

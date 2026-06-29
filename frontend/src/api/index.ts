import axios from 'axios'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'

const api = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    const token = userStore.getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器 - v2.1 增强版（支持Refresh Token）
let isRefreshing = false
let refreshSubscribers: ((token: string) => void)[] = []

api.interceptors.response.use(
  (response) => {
    return response.data
  },
  async (error) => {
    const originalRequest = error.config

    if (error.response) {
      const { status, data } = error.response
      const message = data?.message || data?.error || ''

      if (status === 401) {
        // 尝试使用 Refresh Token 刷新
        const refreshToken = localStorage.getItem('refreshToken')
        if (refreshToken && originalRequest && !originalRequest._retry) {
          originalRequest._retry = true

          if (!isRefreshing) {
            isRefreshing = true
            try {
              const res = await axios.post('/api/auth/refresh', { refreshToken })
              if (res.data.code === 200) {
                const newToken = res.data.data.token
                const userStore = useUserStore()
                userStore.setToken(newToken)
                localStorage.setItem('token', newToken)
                refreshSubscribers.forEach(cb => cb(newToken))
                refreshSubscribers = []
                originalRequest.headers.Authorization = `Bearer ${newToken}`
                return api(originalRequest)
              }
            } catch (refreshError) {
              console.error('[API] Refresh Token 失败', refreshError)
            } finally {
              isRefreshing = false
            }
          } else {
            // 等待刷新完成
            return new Promise((resolve) => {
              refreshSubscribers.push((token: string) => {
                originalRequest.headers.Authorization = `Bearer ${token}`
                resolve(api(originalRequest))
              })
            })
          }
        }

        // Refresh 失败或没有 Refresh Token，跳转到登录
        const userStore = useUserStore()
        userStore.handleTokenExpired()
      } else if (status === 403) {
        ElMessage.error(message || '权限不足')
      } else if (status === 500) {
        ElMessage.error(message || '服务器内部错误')
      }
      // 400业务错误由调用方处理，不在拦截器中显示
    } else if (error.code === 'ECONNABORTED') {
      ElMessage.error('请求超时，请检查网络连接')
    } else {
      ElMessage.error('网络错误，请检查网络连接')
    }
    return Promise.reject(error)
  }
)

// ========== 认证 API ==========
export const authApi = {
  login: (data: { username: string; password: string }) =>
    api.post<any, any>('/auth/login', data),
  register: (data: any) =>
    api.post<any, any>('/auth/register', data),
  refresh: (refreshToken: string) =>
    api.post<any, any>('/auth/refresh', { refreshToken }),
}

// ========== 用户 API ==========
export const userApi = {
  getCurrentUser: () => api.get<any, any>('/users/me'),
  updateCurrentUser: (data: any) => api.put<any, any>('/users/me', data),
  uploadAvatar: (file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    return api.post<any, any>('/users/me/avatar', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  changePassword: (data: { oldPassword: string; newPassword: string }) =>
    api.put<any, any>('/users/me/password', data),
  getUsers: (params: { role?: string; keyword?: string; page?: number; size?: number }) =>
    api.get<any, any>('/users', { params }),
  getTeachers: () => api.get<any, any[]>('/users/teachers'),
  getStudentsByTeacher: (teacherId: number) =>
    api.get<any, any[]>(`/users/students/by-teacher/${teacherId}`),
  getStudentsByTeacherDepartment: (teacherId: number) =>
    api.get<any, any[]>(`/users/students/by-teacher/${teacherId}/department`),
  searchStudents: (teacherId: number, params: { studentNo?: string; realName?: string; major?: string; className?: string; useDepartmentFilter?: boolean }) =>
    api.get<any, any[]>(`/users/students/by-teacher/${teacherId}/search`, { params }),
  updateUserStatus: (userId: number, status: string) =>
    api.put<any, any>(`/users/${userId}/status`, null, { params: { status } }),
  getStats: () => api.get<any, any>('/users/stats'),
}

// ========== 实习 API ==========
export const internshipApi = {
  create: (data: any) => api.post<any, any>('/internships', data),
  update: (id: number, data: any) => api.put<any, any>(`/internships/${id}`, data),
  submit: (id: number) => api.post<any, any>(`/internships/${id}/submit`),
  review: (id: number, data: { comment: string; approved: boolean }) =>
    api.post<any, any>(`/internships/${id}/review`, data),
  complete: (id: number) => api.post<any, any>(`/internships/${id}/complete`),
  get: (id: number) => api.get<any, any>(`/internships/${id}`),
  getStudentInternships: (params: { page?: number; size?: number }) =>
    api.get<any, any>('/internships/student', { params }),
  getTeacherInternships: (params: { page?: number; size?: number }) =>
    api.get<any, any>('/internships/teacher', { params }),
  search: (params: any) => api.get<any, any>('/internships/search', { params }),
  getAll: () => api.get<any, any[]>('/internships/all'),
  delete: (id: number) => api.delete<any, any>(`/internships/${id}`),
  getStats: () => api.get<any, any>('/internships/stats'),
}

// ========== 汇报 API ==========
export const reportApi = {
  create: (data: any) => api.post<any, any>('/reports', data),
  update: (id: number, data: any) => api.put<any, any>(`/reports/${id}`, data),
  submit: (id: number) => api.post<any, any>(`/reports/${id}/submit`),
  review: (id: number, data: { comment: string; score?: number; approved: boolean }) =>
    api.post<any, any>(`/reports/${id}/review`, data),
  get: (id: number) => api.get<any, any>(`/reports/${id}`),
  getStudentReports: (params: { page?: number; size?: number }) =>
    api.get<any, any>('/reports/student', { params }),
  getTeacherReports: (params: { page?: number; size?: number }) =>
    api.get<any, any>('/reports/teacher', { params }),
  getByInternship: (internshipId: number) =>
    api.get<any, any[]>(`/reports/internship/${internshipId}`),
  search: (params: any) => api.get<any, any>('/reports/search', { params }),
  delete: (id: number) => api.delete<any, any>(`/reports/${id}`),
  getAverageScore: (studentId: number) =>
    api.get<any, any>(`/reports/score/${studentId}`),
  getStats: () => api.get<any, any>('/reports/stats'),
}

// ========== 企业 API ==========
export const companyApi = {
  create: (data: any) => api.post<any, any>('/companies', data),
  update: (id: number, data: any) => api.put<any, any>(`/companies/${id}`, data),
  delete: (id: number) => api.delete<any, any>(`/companies/${id}`),
  get: (id: number) => api.get<any, any>(`/companies/${id}`),
  search: (params: { keyword?: string; status?: string; page?: number; size?: number }) =>
    api.get<any, any>('/companies/search', { params }),
  getActive: () => api.get<any, any[]>('/companies/active'),
}

// ========== 签到打卡 API ==========
export const locationApi = {
  create: (data: any) => api.post<any, any>('/locations', data),
  getLatest: () => api.get<any, any>('/locations/latest'),
  getStudentRecords: () => api.get<any, any[]>('/locations/student'),
  getStudentRecordsPage: (params: { page?: number; size?: number }) =>
    api.get<any, any>('/locations/student/page', { params }),
  getTeacherRecords: (params: { page?: number; size?: number }) =>
    api.get<any, any>('/locations/teacher', { params }),
  search: (params: any) => api.get<any, any>('/locations/search', { params }),
}

// ========== 通知 API (v2.1新增) ==========
export const notificationApi = {
  getList: (params: { page?: number; size?: number }) =>
    api.get<any, any>('/notifications', { params }),
  getLatest: () => api.get<any, any>('/notifications/latest'),
  getUnreadCount: () => api.get<any, any>('/notifications/unread-count'),
  markAllRead: () => api.post<any, any>('/notifications/mark-all-read', {}),
}

// ========== 评价 API (v2.1新增) ==========
export const evaluationApi = {
  create: (data: any) => api.post<any, any>('/evaluations', data),
  getStudentEvaluations: () => api.get<any, any[]>('/evaluations/student'),
  getInternshipEvaluations: (internshipId: number) =>
    api.get<any, any[]>(`/evaluations/internship/${internshipId}`),
}

// ========== 仪表盘 API (v2.1新增) ==========
export const dashboardApi = {
  getStats: () => api.get<any, any>('/dashboard/stats'),
  getInternshipDistribution: () => api.get<any, any[]>('/dashboard/internship-distribution'),
  getScoreTrend: () => api.get<any, any[]>('/dashboard/score-trend'),
  getCheckinHeatmap: () => api.get<any, any[]>('/dashboard/checkin-heatmap'),
  getCompanyRanking: () => api.get<any, any[]>('/dashboard/company-ranking'),
}

// ========== 导出 API (v2.1新增) ==========
export const exportApi = {
  exportCheckins: () => api.get<any, Blob>('/export/checkins/excel', { responseType: 'blob' }),
  exportReports: () => api.get<any, Blob>('/export/reports/excel', { responseType: 'blob' }),
}

// ========== 文件 API ==========
export const fileApi = {
  upload: (file: File, type?: string, reportId?: number, internshipId?: number) => {
    const formData = new FormData()
    formData.append('file', file)
    if (type) formData.append('type', type)
    if (reportId) formData.append('reportId', reportId.toString())
    if (internshipId) formData.append('internshipId', internshipId.toString())
    return api.post<any, any>('/files/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  getReportFiles: (reportId: number) => api.get<any, any[]>(`/files/report/${reportId}`),
  getInternshipFiles: (internshipId: number) => api.get<any, any[]>(`/files/internship/${internshipId}`),
  delete: (id: number) => api.delete<any, any>(`/files/${id}`),
  getDownloadUrl: (id: number) => `/api/files/${id}`,
  getPreviewUrl: (id: number) => `/api/files/${id}/preview`,
  getFileTypes: () => api.get<any, any>('/files/types'),
}

// ========== 统计 API ==========
export const statsApi = {
  getOverview: () => api.get<any, any>('/stats/overview'),
}

export default api

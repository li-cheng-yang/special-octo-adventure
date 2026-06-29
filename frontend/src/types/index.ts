// API 类型定义

export interface User {
  id: number
  username: string
  realName: string
  email?: string
  phone?: string
  role: 'STUDENT' | 'TEACHER' | 'ADMIN'
  studentNo?: string
  department?: string
  major?: string
  className?: string
  avatar?: string
  status: 'ACTIVE' | 'INACTIVE' | 'GRADUATED'
  createTime?: string
}

export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  password: string
  realName: string
  email?: string
  phone?: string
  role: string
  studentNo?: string
  department?: string
  major?: string
  className?: string
}

export interface JwtResponse {
  token: string
  type: string
  id: number
  username: string
  realName: string
  role: string
}

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  currentPage: number
}

export interface Company {
  id: number
  name: string
  industry?: string
  address?: string
  city?: string
  province?: string
  contactName?: string
  contactPhone?: string
  contactEmail?: string
  description?: string
  logoUrl?: string
  website?: string
  status: 'ACTIVE' | 'INACTIVE' | 'BLACKLISTED'
  studentCount: number
  createTime?: string
  updateTime?: string
}

export interface CompanyCreateRequest {
  name: string
  industry?: string
  address?: string
  city?: string
  province?: string
  contactName?: string
  contactPhone?: string
  contactEmail?: string
  description?: string
  logoUrl?: string
  website?: string
}

export interface LocationRecord {
  id: number
  studentId: number
  studentName?: string
  studentNo?: string
  internshipId?: number
  company?: string
  longitude: number
  latitude: number
  address?: string
  city?: string
  province?: string
  recordType?: string
  remark?: string
  recordTime?: string
}

export interface LocationCreateRequest {
  longitude: number
  latitude: number
  address?: string
  city?: string
  province?: string
  recordType?: string
  remark?: string
  internshipId?: number
}

export interface Internship {
  id: number
  studentId: number
  studentName: string
  studentNo: string
  teacherId?: number
  teacherName?: string
  companyId?: number
  companyName: string
  position?: string
  address?: string
  companyContact?: string
  companyPhone?: string
  startDate: string
  endDate: string
  status: 'DRAFT' | 'PENDING' | 'ONGOING' | 'COMPLETED' | 'REJECTED'
  description?: string
  workContent?: string
  teacherComment?: string
  createTime?: string
  updateTime?: string
}

export interface InternshipCreateRequest {
  teacherId: number
  companyId?: number
  companyName: string
  position?: string
  address?: string
  companyContact?: string
  companyPhone?: string
  startDate: string
  endDate: string
  description?: string
  workContent?: string
}

export interface InternshipUpdateRequest {
  teacherId?: number
  companyId?: number
  companyName?: string
  position?: string
  address?: string
  companyContact?: string
  companyPhone?: string
  startDate?: string
  endDate?: string
  description?: string
  workContent?: string
  status?: string
}

export interface InternshipReviewRequest {
  comment: string
  approved: boolean
}

export interface Report {
  id: number
  internshipId: number
  company: string
  studentId: number
  studentName: string
  studentNo: string
  title: string
  reportType: 'WEEKLY' | 'MONTHLY' | 'FINAL'
  content?: string
  summary?: string
  problem?: string
  solution?: string
  plan?: string
  reportDate?: string
  weekNumber?: number
  status: 'DRAFT' | 'SUBMITTED' | 'REVIEWED' | 'RETURNED'
  reviewerId?: number
  reviewerName?: string
  reviewComment?: string
  score?: number
  reviewTime?: string
  createTime?: string
  updateTime?: string
}

export interface ReportCreateRequest {
  internshipId: number
  title: string
  reportType: string
  content?: string
  summary?: string
  problem?: string
  solution?: string
  plan?: string
  reportDate?: string
  weekNumber?: number
}

export interface ReportUpdateRequest {
  title?: string
  reportType?: string
  content?: string
  summary?: string
  problem?: string
  solution?: string
  plan?: string
  reportDate?: string
  weekNumber?: number
  status?: string
}

export interface ReportReviewRequest {
  comment: string
  score?: number
  approved: boolean
}

export interface FileItem {
  id: number
  fileName: string
  fileSize: number
  fileType: string
  storedName?: string
}

export interface Statistics {
  totalStudents: number
  totalTeachers: number
  totalAdmins: number
  draftInternships: number
  pendingInternships: number
  ongoingInternships: number
  completedInternships: number
  internshipStudents: number
  draftReports: number
  submittedReports: number
  reviewedReports: number
  returnedReports: number
}

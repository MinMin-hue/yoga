import http from '@/utils/http'

export const adminLogin = (data: { username: string; password: string }) =>
  http.post('/auth/admin/login', data)

export const memberApi = {
  page: (data: any) => http.post('/admin/member/page', data),
  detail: (id: number) => http.get(`/admin/member/${id}`),
  create: (data: any) => http.post('/admin/member/create', data),
  update: (data: any) => http.post('/admin/member/update', data),
  delete: (id: number) => http.post(`/admin/member/delete/${id}`)
}

export const cardApi = {
  typeList: (status?: number) => http.get('/admin/card-type/list', { params: { status } }),
  typePage: (pageNum = 1, pageSize = 20) =>
    http.post('/admin/card-type/page', null, { params: { pageNum, pageSize } }),
  typeCreate: (data: any) => http.post('/admin/card-type/create', data),
  typeUpdate: (data: any) => http.post('/admin/card-type/update', data),
  typeDelete: (id: number) => http.post(`/admin/card-type/delete/${id}`),
  cardDetail: (id: number) => http.get(`/admin/member-card/${id}`),
  cardListByMember: (memberId: number) =>
    http.get(`/admin/member-card/list-by-member/${memberId}`),
  cardActivate: (id: number) =>
    http.post('/admin/member-card/activate', { cardId: id }),
  cardRefund: (data: { cardId: number; reason?: string }) =>
    http.post('/admin/member-card/refund', data)
}

export const courseApi = {
  typeList: (status?: number) => http.get('/admin/course-type/list', { params: { status } }),
  typeUpsert: (data: any) => http.post('/admin/course-type/upsert', data),
  typeDelete: (id: number) => http.post(`/admin/course-type/delete/${id}`),
  roomList: () => http.get('/admin/room/list'),
  roomUpsert: (data: any) => http.post('/admin/room/upsert', data),
  roomDelete: (id: number) => http.post(`/admin/room/delete/${id}`),
  coachList: () => http.get('/admin/coach/list'),
  schedulePage: (params: any) => http.post('/admin/schedule/page', null, { params }),
  scheduleUpsert: (data: any) => http.post('/admin/schedule/upsert', data),
  scheduleCancel: (id: number) => http.post(`/admin/schedule/cancel/${id}`),
  scheduleDelete: (id: number) => http.post(`/admin/schedule/delete/${id}`)
}

export const bookingApi = {
  page: (data: any) => http.post('/admin/booking/page', data),
  bySchedule: (id: number) => http.get(`/admin/booking/by-schedule/${id}`),
  checkIn: (id: number) => http.post(`/admin/booking/check-in/${id}`),
  complete: (id: number) => http.post(`/admin/booking/complete/${id}`),
  cancel: (id: number, reason?: string) =>
    http.post(`/admin/booking/cancel/${id}`, null, { params: { reason } })
}

export const orderApi = {
  page: (data: any) => http.post('/admin/order/page', data),
  detail: (id: number) => http.get(`/admin/order/${id}`),
  confirmPay: (data: any) => http.post('/admin/order/confirm-pay', data),
  cancel: (id: number) => http.post(`/admin/order/cancel/${id}`),
  refund: (id: number, reason?: string) =>
    http.post(`/admin/order/refund/${id}`, null, { params: { reason } })
}

export const statisticsApi = {
  revenue: (range = 'month') => http.get('/statistics/revenue', { params: { range } }),
  course: (range = 'month') => http.get('/statistics/course', { params: { range } }),
  member: (range = 'month') => http.get('/statistics/member', { params: { range } }),
  coach: (range = 'month') => http.get('/statistics/coach', { params: { range } })
}

export const systemApi = {
  all: () => http.get('/system/config/all'),
  detail: () => http.get('/system/config/detail'),
  update: (data: Record<string, string>) => http.post('/system/config/update', data)
}

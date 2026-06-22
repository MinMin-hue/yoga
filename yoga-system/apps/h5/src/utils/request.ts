import { useUserStore } from '@/store/user'

const BASE_URL = '/api'

export const request = (options: UniApp.RequestOptions) => {
  const user = useUserStore()
  return new Promise<any>((resolve, reject) => {
    uni.request({
      ...options,
      url: BASE_URL + options.url,
      header: {
        'Content-Type': 'application/json',
        Authorization: user.token ? `Bearer ${user.token}` : '',
        ...(options.header || {})
      },
      success: (res) => {
        const data: any = res.data
        if (data.code === 0) return resolve(data)
        uni.showToast({ title: data.message || '请求失败', icon: 'none' })
        if (data.code === 401) {
          user.clear()
          uni.reLaunch({ url: '/pages/login/login' })
        }
        reject(data)
      },
      fail: (err) => {
        uni.showToast({ title: '网络异常', icon: 'none' })
        reject(err)
      }
    })
  })
}

export default {
  // auth
  memberLogin: (data: { phone: string; code?: string }) =>
    request({ url: '/auth/member/login', method: 'POST', data }),
  // h5
  profile: () => request({ url: '/h5/member/profile' }),
  myCards: () => request({ url: '/h5/member/cards' }),
  myRecords: () => request({ url: '/h5/member/records' }),
  myBookings: (status?: string) => request({ url: '/h5/booking/my', method: 'GET', data: status ? { status } : {} }),
  cardTypeList: () => request({ url: '/h5/card-type/list' }),
  courseTypeList: () => request({ url: '/h5/course-type/list' }),
  scheduleList: (date: string, courseTypeId?: number) => request({ url: '/h5/schedule/list', data: { date, courseTypeId } }),
  createBooking: (data: { scheduleId: number; cardId: number }) => request({ url: '/h5/booking/create', method: 'POST', data }),
  cancelBooking: (id: number, reason?: string) => request({ url: `/h5/booking/cancel/${id}`, method: 'POST', data: reason ? { reason } : {} }),
  createOrder: (data: any) => request({ url: '/h5/order/create', method: 'POST', data }),
  cancelOrder: (id: number) => request({ url: `/h5/order/cancel/${id}`, method: 'POST' })
}

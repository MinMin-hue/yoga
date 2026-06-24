import { request } from '@/utils/request'

// =================== 类型定义 ===================

export interface UserInfo {
  id: number
  nickname: string
  phone: string
  avatar?: string
}

export type CardKind = 'TIME' | 'TIMES' | 'MIXED'
export type CardStatus = 'PENDING' | 'ACTIVE' | 'EXPIRED' | 'NO_REMAIN' | 'REFUNDED'
export type BookingStatus = 'BOOKED' | 'CHECKED_IN' | 'COMPLETED' | 'CANCELLED' | 'NO_SHOW'
export type RecordType = 'PURCHASE' | 'RECHARGE' | 'CHECKIN' | 'REFUND' | 'PENALTY'

export interface CardType {
  id: number
  name: string
  cardKind: CardKind
  price: number
  description: string
  validDays?: number
  totalTimes?: number
}

export interface CourseType {
  id: number
  name: string
}

export interface Schedule {
  id: number
  startTime: string
  endTime: string
  capacity: number
  bookedCount: number
  checkinBefore: number
  courseTypeName: string
  coachName: string
  roomName?: string
}

export interface MemberCard {
  id: number
  cardNo: string
  cardTypeName: string
  status: CardStatus
  remainTimes?: number
  validTo?: string
}

export interface Booking {
  id: number
  bookingNo: string
  status: BookingStatus
  costTimes: number
  bookedAt: string
}

export interface ConsumeRecord {
  id: number
  type: RecordType
  timesDelta: number
  amount: number
  remark: string
  createdAt: string
}

export interface Order {
  id: number
  orderNo: string
}

// =================== 接口 ===================

export const api = {
  auth: {
    login: (phone: string) =>
      request<{ token: string; profile: UserInfo }>({
        url: '/auth/member/login',
        method: 'POST',
        data: { phone }
      })
  },
  h5: {
    profile: () => request<UserInfo>({ url: '/h5/member/profile' }),
    cards: () => request<{ cards: MemberCard[] }>({ url: '/h5/member/cards' }),
    records: () => request<{ records: ConsumeRecord[] }>({ url: '/h5/member/records' }),
    bookings: (status?: BookingStatus) =>
      request<{ list: Booking[] }>({
        url: '/h5/booking/my',
        data: status ? { status } : undefined
      }),
    cardTypes: () => request<CardType[]>({ url: '/h5/card-type/list' }),
    courseTypes: () => request<CourseType[]>({ url: '/h5/course-type/list' }),
    schedules: (date: string, courseTypeId?: number) =>
      request<Schedule[]>({ url: '/h5/schedule/list', data: { date, courseTypeId } }),
    createBooking: (scheduleId: number, cardId: number) =>
      request<void>({ url: '/h5/booking/create', method: 'POST', data: { scheduleId, cardId } }),
    cancelBooking: (id: number, reason?: string) =>
      request<void>({
        url: `/h5/booking/cancel/${id}`,
        method: 'POST',
        data: reason ? { reason } : undefined
      }),
    createOrder: (cardTypeId: number, amount: number) =>
      request<Order>({
        url: '/h5/order/create',
        method: 'POST',
        data: { orderType: 'PURCHASE_CARD', cardTypeId, amount }
      })
  }
}

export default api

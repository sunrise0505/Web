export interface User {
  id: number
  username: string
  displayName: string
  role: 'ADMIN' | 'STUDENT'
}
export interface Venue {
  id: number
  name: string
  category: string
  campus: string
  location: string
  description: string
  capacity: number
  openHour: number
  closeHour: number
  enabled: boolean
  theme: 'court' | 'pool' | 'studio' | 'field'
}
export type BookingStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED' | 'CHECKED_IN'
export type BookingAction = 'approve' | 'reject' | 'cancel' | 'check-in'
export interface Booking {
  id: number
  venueId: number
  venueName: string
  location: string
  userName: string
  date: string
  startHour: number
  durationHours: number
  participants: number
  purpose: string
  status: BookingStatus
  createdAt: string
  availableActions: BookingAction[]
}
export interface Slot {
  hour: number
  status: 'AVAILABLE' | 'BUSY' | 'EXPIRED' | 'CLOSED'
}
export interface Dashboard {
  venueCount: number
  openVenues: number
  todayBookings: number
  pendingBookings: number
  totalBookings: number
  trend: { date: string; count: number }[]
  recentBookings: Booking[]
}
export const statusLabels: Record<BookingStatus, string> = {
  PENDING: '待审核',
  APPROVED: '已通过',
  REJECTED: '未通过',
  CANCELLED: '已取消',
  CHECKED_IN: '已签到',
}
export const hourText = (hour: number) => `${String(hour).padStart(2, '0')}:00`
export function localDate(date = new Date()) {
  return new Intl.DateTimeFormat('en-CA', {
    timeZone: 'Asia/Shanghai',
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  }).format(date)
}
export const categories = ['羽毛球', '篮球', '游泳', '健身', '足球', '网球']
export const bookingPurposes = ['日常锻炼', '专项训练', '班级活动', '社团活动', '比赛活动', '课程教学']

export const campuses = [
  '中心校区',
  '洪家楼校区',
  '趵突泉校区',
  '千佛山校区',
  '兴隆山校区',
  '软件园校区',
  '威海校区',
  '青岛校区',
]
export const venueLocation = (v: Venue) =>
  v.location.startsWith(v.campus) ? v.location : `${v.campus} · ${v.location}`

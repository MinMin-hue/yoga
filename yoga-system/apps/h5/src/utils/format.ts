export const formatTime = (s?: string | null): string =>
  s && s.length >= 16 ? s.substring(11, 16) : ''

export const formatDate = (s?: string | null): string =>
  s ? s.replace('T', ' ').substring(0, 16) : ''

export const formatDateShort = (s?: string | null): string =>
  s && s.length >= 10 ? s.substring(0, 10) : ''

export const durationMinutes = (start?: string, end?: string): number => {
  if (!start || !end) return 0
  return Math.round((new Date(end).getTime() - new Date(start).getTime()) / 60000)
}

export const today = (): string => new Date().toISOString().slice(0, 10)

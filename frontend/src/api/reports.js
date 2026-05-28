import request from '@/utils/request'

export const reportsApi = {
  daily: (params) => request.get('/reports/daily', { params }),
  team: (params) => request.get('/reports/team', { params }),
  evaluate: () => request.get('/reports/evaluate'),
  export: (data) => request.post('/reports/export', data, { responseType: 'blob' })
}
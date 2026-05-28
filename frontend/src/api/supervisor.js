import request from '@/utils/request'

export const supervisorApi = {
  assign: (id, data) => request.post(`/supervisor/tickets/${id}/assign`, data),
  strategies: () => request.get('/supervisor/assign-strategies'),
  createStrategy: (data) => request.post('/supervisor/assign-strategies', data),
  updateStrategy: (id, data) => request.put(`/supervisor/assign-strategies/${id}`, data)
}
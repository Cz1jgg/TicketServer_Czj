import request from '@/utils/request'

export const ticketApi = {
  create: (data) => request.post('/tickets', data),
  list: (params) => request.get('/tickets', { params }),
  detail: (id) => request.get(`/tickets/${id}`),
  claim: (id) => request.post(`/tickets/${id}/claim`),
  cancel: (id, data) => request.post(`/tickets/${id}/cancel`, data),
  suspend: (id, data) => request.post(`/tickets/${id}/suspend`, data),
  resume: (id, data) => request.post(`/tickets/${id}/resume`, data),
  complete: (id, data) => request.post(`/tickets/${id}/complete`, data),
  reject: (id, data) => request.post(`/tickets/${id}/reject`, data),
  close: (id) => request.post(`/tickets/${id}/close`),
  evaluate: (id, data) => request.post(`/tickets/${id}/evaluate`, data),
  logs: (id) => request.get(`/tickets/${id}/logs`)
}
import request from '@/utils/request'

export const adminApi = {
  users: () => request.get('/admin/users'),
  user: (id) => request.get(`/admin/users/${id}`),
  createUser: (data) => request.post('/admin/users', data),
  updateUser: (id, data) => request.put(`/admin/users/${id}`, data),
  deleteUser: (id) => request.delete(`/admin/users/${id}`),
  roles: () => request.get('/admin/roles'),
  createRole: (data) => request.post('/admin/roles', data),
  forceClose: (id, data) => request.post(`/admin/tickets/${id}/force-close`, data)
}
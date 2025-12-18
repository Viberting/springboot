import axios from 'axios'

// 设置axios基础配置
axios.defaults.timeout = 10000 // 设置超时时间10秒

const API_BASE = '/api/admin/comment'

export const commentService = {
  // 获取评论列表
  getCommentList(data) {
    return axios.post(`${API_BASE}/list`, data)
  },

  // 获取评论详情
  getCommentDetail(id) {
    return axios.get(`${API_BASE}/detail/${id}`)
  },

  // 更新评论状态
  updateCommentStatus(id, status) {
    return axios.post(`${API_BASE}/updateStatus/${id}?status=${status}`)
  },

  // 删除评论
  deleteComment(id) {
    return axios.delete(`${API_BASE}/delete/${id}`)
  },

  // 批量删除评论
  batchDeleteComments(ids) {
    return axios.delete(`${API_BASE}/batchDelete`, { data: ids })
  }
}
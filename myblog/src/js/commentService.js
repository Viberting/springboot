import axios from 'axios'

// 设置axios基础配置（保留原有配置）
axios.defaults.timeout = 10000 // 设置超时时间10秒

// 区分管理员端和用户端API前缀（新增用户端前缀）
const API_BASE = '/api/admin/comment' // 原有：管理员端评论接口前缀
const USER_API_BASE = '/api/comment' // 新增：用户端评论接口前缀

export const commentService = {
  // ========== 原有管理员端评论功能（完全保留） ==========
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
  },

  // ========== 新增：用户端评论回复相关API ==========
  // 添加评论回复（适配后端Controller的insertReply接口）
  insertReply(data) {
    return axios.post(`${USER_API_BASE}/insertReply`, data, {
      withCredentials: true // 保留跨域凭证，适配登录态校验
    })
  },

  // 获取评论回复（适配后端Controller的getCommentReplies接口，修正参数名+请求方式）
  getCommentReplies(parentId, pageParams) {
    // 注意：后端Controller中getCommentReplies是POST请求，参数为parentId(Query) + pageParams(Body)
    return axios({
      method: 'post',
      url: `${USER_API_BASE}/getCommentReplies`,
      params: { parentId }, // URL参数：父评论ID（修正参数名从commentId→parentId，匹配后端）
      data: pageParams, // 请求体：分页参数（PageParams）
      withCredentials: true
    })
  },

  // 获取文章的完整评论树（适配后端Controller的getCommentTreeByArticleId接口）
  getCommentTreeByArticleId(articleId) {
    return axios.get(`${USER_API_BASE}/getCommentTreeByArticleId`, {
      params: { articleId }, // URL参数：文章ID
      withCredentials: true
    })
  }
}
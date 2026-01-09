import axios from 'axios'

// 保留原有核心配置 + 补充基础配置（兼顾原有超时和新的默认配置）
axios.defaults.timeout = 10000 // 基础超时时间10秒（新接口默认）
axios.defaults.withCredentials = true // 全局开启跨域凭证（兼容原有insertReply的配置）
const API_BASE = '/api/comment' // 统一接口前缀

// 原有方法：添加评论回复（保留原有超时配置，优先级高于全局）
export async function insertReply(params) {
  return await axios({
    method: 'post',
    url: `${API_BASE}/insertReply`, // 改用统一前缀，保证路径一致
    data: params,
    timeout: 3000000, // 保留原有超长超时配置（覆盖全局10秒）
    withCredentials: true // 显式保留跨域凭证配置
  })
}

// 新增：评论回复相关API方法集合（兼容原有导出方式，新增模块化封装）
export const commentReplyService = {
  // 复用原有insertReply逻辑，保证功能一致
  insertReply: (data) => {
    return axios({
      method: 'post',
      url: `${API_BASE}/insertReply`,
      data: data,
      timeout: 3000000,
      withCredentials: true
    })
  },

  // 新增：获取评论回复（分页查询，适配controller的参数格式）
  getCommentReplies: (parentId, pageParams) => {
    // 注意：controller中getCommentReplies是POST请求，参数为parentId(Query) + pageParams(Body)
    return axios({
      method: 'post',
      url: `${API_BASE}/getCommentReplies`,
      params: { parentId }, // URL参数：父评论ID
      data: pageParams, // 请求体：分页参数（PageParams）
      withCredentials: true
    })
  },

  // 新增：获取文章的完整评论树（GET请求，参数为文章ID）
  getCommentTreeByArticleId: (articleId) => {
    return axios.get(`${API_BASE}/getCommentTreeByArticleId`, {
      params: { articleId }, // 拼接为URL参数：?articleId=xxx
      withCredentials: true
    })
  }
}
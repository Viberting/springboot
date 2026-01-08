// 状态管理文件
import { defineStore } from 'pinia'
import { reactive,ref } from 'vue'

export const useStore = defineStore('my', () => {
  // 状态管理中添加article和page两个状态（全局变量），在整个vue项目中均可共享（访问、修改）这些状态
  const articleId = ref(0)
  const page = reactive({
    "pageParams":null
  })
  const commentId = ref(0) // 新增：评论ID（用于编辑回显）

  const home = reactive({
    "page":1
  })

  const user = reactive({
    "user": null
  })

  // 新增：用户管理相关状态（编辑用户ID、权限选择）
  const editUserId = ref(0); // 待编辑的用户ID
  const selectedAuthorityIds = ref([]); // 选中的权限ID列表

  // 全局保存搜索页面的查询条件、分页参数以及查询结果
  const searchState = reactive({
    articleCondition: { title: '', startDate: '', endDate: '' },
    pageParams: { page: 1, rows: 5, total: 0 },
    articleVOs: []
  })

    // ========== 新增：清空用户的方法 ==========
    const clearUser = () => {
        user.user = null; // 直接修改嵌套的 user 状态
    }


    return {articleId,page,commentId,home,searchState,user,editUserId, selectedAuthorityIds,clearUser}
})


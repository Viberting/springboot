<script setup>
import Top from "@/components/Top.vue";
import { useRoute, useRouter } from 'vue-router'
import {ref, computed, inject, reactive, onMounted} from 'vue'
// 新增：引入ElMessage（更友好的提示）
import { ElMessageBox, ElMessage } from 'element-plus'
import Comment from "@/components/Comment.vue"
import {useStore} from "@/stores/my.js"; // 引入Pinia
// 保留原有导入 + 新增评论回复服务导入
import { insertReply } from '@/js/commentReplyService.js';
import { commentReplyService } from '@/js/commentReplyService' // 新增：评论回复服务
// 新增：管理功能相关导入
import { Edit, Delete } from '@element-plus/icons-vue'
import { nullZeroBlank } from '@/js/tool.js'

const route = useRoute()
const axios = inject('axios')
const store = useStore()
const router = useRouter()

// ========== 原有：文章和评论基础数据 ==========
let articleAndComment = reactive({
  "article":{"content": ""} ,
  "comments": []
})

// ========== 原有：作者信息和关注状态相关响应式数据 ==========
const author = ref(null) // 存储作者信息
const isFollowed = ref(false) // 是否已关注该作者
const currentUserId = ref(null) //存储当前登录用户ID（用于身份校验）

// ========== 原有：回复相关核心变量 ==========
const commentContent = ref('')
const replyTo = ref(null) // 当前正在回复的评论作者
const replyParentId = ref(null) // 父评论ID
const replyRootId = ref(null) // 根评论ID
const isReplying = ref(false) // 是否处于回复模式

// ========== 新增：评论管理相关变量 ==========
const isAdminMode = ref(true) // 是否切换到管理员模式，默认为true
const deleteDialogVisible = ref(false) // 删除对话框显示状态
let selectedCommentId = ref(0) // 选中要删除的评论ID
const editCommentFunc = inject("editComment", () => {}) // 注入编辑评论方法（兼容非管理场景）

// 管理页-页面数据（评论列表 + 分页信息）
let myData = reactive({
  commentVOs: [],
  pageParams: { page: 1, rows: 10, total: 0 }
})

// 管理页-搜索表单
let searchForm = reactive({
  content: '',
  articleId: '',
  author: '',
  status: ''
})

// 计算属性：是否显示回复对象列（管理页）
const showReplyToColumn = computed(() => {
  return myData.commentVOs.some(comment => comment.replyTo)
})

// 跳转到用户个人主页（原有功能保留）
const gotoUserProfile = (userId) => {
  if (!userId) return
  router.push({
    name: 'otherUserProfile', // 对应路由中定义的otherUserProfile
    params: { userId }
  })
}

// ========== 原有：处理回复功能（接收子组件的reply事件） ==========
const handleReply = (comment) => {
  replyTo.value = comment.author
  replyParentId.value = comment.id
  replyRootId.value = comment.rootId || comment.id // 无rootId则用自身ID
  isReplying.value = true
  // 滚动到评论框，提升交互体验
  setTimeout(() => {
    document.querySelector('.comment-input-section')?.scrollIntoView({ behavior: 'smooth' })
  }, 100)
}

// ========== 原有：取消回复模式 ==========
const cancelReply = () => {
  isReplying.value = false
  replyTo.value = null
  replyParentId.value = null
  replyRootId.value = null
}

// ========== 原有：添加评论/回复（兼容原有普通评论 + 新增回复逻辑） ==========
function submit(){
  // 基础评论数据
  const commentData = {
    "articleId": route.params.articleId,
    "content": commentContent.value
  }

  // 如果是回复模式，补充回复相关字段
  if (isReplying.value) {
    commentData.parentId = replyParentId.value
    commentData.rootId = replyRootId.value
    commentData.replyTo = replyTo.value
  }

  axios({
    method: 'post',
    url: '/api/comment/insert',
    data: commentData,
    timeout: 3000000,
    withCredentials: true
  }).then((response) => {
    if(response.data.success){
      // 根据是否是回复，处理评论插入位置
      if (isReplying.value) {
        // 回复评论：找到父评论并添加到children数组
        const addToChildren = (comments) => {
          for (let i = 0; i < comments.length; i++) {
            const item = comments[i];
            if (item.id === replyParentId.value) {
              if (!item.children) item.children = []
              item.children.push(response.data.map.comment)
              return true
            } else if (item.children && item.children.length > 0) {
              if (addToChildren(item.children)) return true
            }
          }
          return false
        }
        // 未找到父评论则添加到顶级
        if (!addToChildren(articleAndComment.comments)) {
          articleAndComment.comments.unshift(response.data.map.comment)
        }
      } else {
        // 普通评论：添加到顶级评论顶部
        articleAndComment.comments.unshift(response.data.map.comment)
      }

      // 提示并重置状态
      ElMessageBox.alert(response.data.msg, '结果',{
        confirmButtonText: '确定',
        callback:( action )=>{
          commentContent.value = "" // 清空输入框
          cancelReply() // 取消回复模式
        }
      })
    } else {
      ElMessageBox.alert(response.data.msg, '结果')
    }
  }).catch((error) => {
    ElMessageBox.alert("未登录用户无法评论！", '结果')
  })
}

// ========== 原有：关注/取消关注作者方法（完全保留） ==========
// 关注作者
const followAuthor = () => {
  if (!author.value) return
  // 1. 校验是否登录
  if (!currentUserId.value) {
    ElMessage.error("请先登录后再关注！")
    return
  }

  // 2. 正常关注逻辑
  axios({
    method: 'post',
    url: '/api/follow/followAuthor',
    params: { authorId: author.value.id }, // 传递作者ID
    withCredentials: true
  }).then((res) => {
    if (res.data.success) {
      ElMessage.success(res.data.msg)
      isFollowed.value = true // 更新关注状态
      // 实时更新作者粉丝数
      author.value.fansCount += 1
    } else {
      ElMessage.error(res.data.msg)
    }
  }).catch(() => {
    ElMessage.error("关注失败，请先登录！")
  })
}

// 取消关注作者
const unfollowAuthor = () => {
  if (!author.value) return

  // 弹窗确认
  ElMessageBox.confirm(
      '此操作将取消关注该作者, 是否继续?',
      '提示',
      { type: 'warning' }
  ).then(async () => {
    // 确认后执行取消关注
    const res = await axios({
      method: 'post',
      url: '/api/follow/unfollowAuthor',
      params: { authorId: author.value.id },
      withCredentials: true
    })
    if (res.data.success) {
      ElMessage.success(res.data.msg)
      isFollowed.value = false
      author.value.fansCount -= 1
    } else {
      ElMessage.error(res.data.msg)
    }
  }).catch(() => {
    ElMessage.info('已取消操作')
  })
}

// ========== 原有：分页参数 + 刷新评论方法（增强：兼容评论树） ==========
let pageParams = { "page":1,"rows":2 }

// 刷新评论列表（原有方法增强：优先用评论树接口）
const refreshComments = () => {
  pageParams.page = 1;
  // 优先获取评论树，保证层级结构
  commentReplyService.getCommentTreeByArticleId(route.params.articleId).then((treeResponse) => {
    if(treeResponse.data.success && treeResponse.data.map.comments) {
      articleAndComment.comments = treeResponse.data.map.comments
      noMore.value = false;
    } else {
      // 降级使用原有分页接口
      axios({
        method: 'post',
        url: '/api/article/getArticleAndFirstPageCommentByArticleId?articleId=' + route.params.articleId,
        data: pageParams,
        timeout: 3000000,
        withCredentials: true
      }).then((response) => {
        if(response.data.success && response.data.map.article!=null){
          articleAndComment.comments = response.data.map.comments
          noMore.value = false;
        }
      })
    }
  }).catch((error) => {
    ElMessageBox.alert("系统错误！",'结果')
  })
};

// ========== 原有：页面初始化获取数据（增强：加载评论树） ==========
if (route.params.articleId) {  // 只有在有articleId参数时才执行
  axios({
    method: 'post',
    url: '/api/article/getArticleAndFirstPageCommentByArticleId?articleId=' + route.params.articleId,
    data: pageParams,
    timeout: 3000000,
    withCredentials: true
  }).then((response) => {
    if(response.data.success){
      if(response.data.map.article!=null){
        articleAndComment.article = response.data.map.article
        articleAndComment.comments = response.data.map.comments
        // 原有：赋值作者信息和关注状态
        author.value = response.data.map.author
        isFollowed.value = response.data.map.isFollowed
        // 原有：从Pinia获取当前登录用户ID
        if (store?.user?.user) {
          currentUserId.value = store.user.user.id
        }
  
        // 新增：加载完整评论树（替换扁平列表为树形结构）
        commentReplyService.getCommentTreeByArticleId(route.params.articleId).then((treeResponse) => {
          if(treeResponse.data.success && treeResponse.data.map.comments) {
            articleAndComment.comments = treeResponse.data.map.comments
          }
        })
      }else{
        ElMessageBox.alert('无文章！', '结果')
      }
    }else{
      ElMessageBox.alert(response.data.msg, '结果')
    }
  })
}

// ========== 原有：分页加载评论（完全保留） ==========
const noMore = ref(false)
const loading = ref(false)
const disabled = computed(()=>loading.value || noMore.value)

const load = () => {
  if (!route.params.articleId) return;  // 如果没有articleId参数，直接返回
  loading.value = true
  pageParams.page++
  axios({
    method: 'post',
    url: '/api/comment/getAPageCommentByArticleId?articleId=' + route.params.articleId,
    data: pageParams,
    timeout: 3000000,
    withCredentials: true
  }).then((response) => {
    if(response.data.success){
      let comments = response.data.map.comments
      if(comments != null && comments.length > 0){
        for (let index = 0; index < comments.length; index++)
          articleAndComment.comments.push(comments[index]);
      }else if(comments == null || comments.length == 0)
        noMore.value = true
    }else{
      ElMessageBox.alert(response.data.msg, '结果')
    }
    loading.value = false
  }).catch((error) => {
    ElMessageBox.alert("系统错误！",'结果')
    loading.value=false
  })
}

// ========== 新增：评论管理核心方法 ==========
// 初始化加载管理端评论列表
const getCommentPage = () => {
  // 如果是从编辑页返回，恢复之前的分页状态
  if (!nullZeroBlank(store.page.pageParams)) {
    myData.pageParams = store.page.pageParams
    store.page.pageParams = null // 重置状态
  }

  axios({
    method: 'post',
    url: '/api/comment/getCommentPage',
    data: myData.pageParams
  }).then((response) => {
    if (response.data.success) {
      myData.commentVOs = response.data.map.commentVOs || []
      myData.pageParams = response.data.map.pageParams
      if (myData.commentVOs.length === 0) {
        ElMessageBox.alert("暂无评论！", '提示')
      }
    } else {
      ElMessageBox.alert(response.data.msg, '错误')
    }
  }).catch(() => {
    ElMessageBox.alert("系统错误！", '错误')
  })
}

// 搜索评论（管理端）
const searchComments = () => {
  // 重置到第一页
  myData.pageParams.page = 1;

  // 创建搜索参数对象（优化参数处理）
  const searchParams = {
    ...myData.pageParams,
    content: searchForm.content.trim() || null,
    articleId: searchForm.articleId ? parseInt(searchForm.articleId) : null,
    author: searchForm.author.trim() || null,
    status: searchForm.status !== '' ? parseInt(searchForm.status) : null
  }

  // 发送搜索请求
  axios({
    method: 'post',
    url: '/api/comment/searchComments',
    data: {
      ...searchParams,
      pageParams: {
        page: searchParams.page,
        rows: searchParams.rows,
        total: 0
      }
    }
  }).then((response) => {
    if (response.data.success) {
      myData.commentVOs = response.data.map.commentVOs || []
      myData.pageParams = response.data.map.pageParams
      if (myData.commentVOs.length === 0) {
        ElMessageBox.alert("暂无符合条件的评论！", '提示')
      }
    } else {
      ElMessageBox.alert(response.data.msg, '错误')
    }
  }).catch(() => {
    ElMessageBox.alert("系统错误！", '错误')
  })
}

// 重置搜索（管理端）
const resetSearch = () => {
  searchForm.content = ''
  searchForm.articleId = ''
  searchForm.author = ''
  searchForm.status = ''
  // 重新加载所有评论
  myData.pageParams.page = 1;
  getCommentPage()
}

// 每页条数变化（管理端）
const handleSizeChange = (newRows) => {
  myData.pageParams.rows = newRows
  myData.pageParams.page = 1 // 重置为第一页
  // 判断是否在搜索状态，如果是则使用搜索方法，否则使用普通方法
  if (isSearching()) {
    searchComments()
  } else {
    getCommentPage()
  }
}

// 页码变化（管理端）
const handleCurrentChange = (newPage) => {
  myData.pageParams.page = newPage
  // 判断是否在搜索状态，如果是则使用搜索方法，否则使用普通方法
  if (isSearching()) {
    searchComments()
  } else {
    getCommentPage()
  }
}

// 检查是否在搜索状态（管理端）
const isSearching = () => {
  return searchForm.content !== '' ||
      searchForm.articleId !== '' ||
      searchForm.author !== '' ||
      searchForm.status !== ''
}

// 编辑评论（管理端）：保存评论ID和分页状态，跳转至编辑页
const editComment = (id) => {
  store.commentId = id // 保存要编辑的评论ID
  store.page.pageParams = { ...myData.pageParams } // 保存当前分页状态
  editCommentFunc()
}

// 显示删除确认对话框（管理端）
const showDeleteDialog = (id) => {
  selectedCommentId.value = id
  deleteDialogVisible.value = true
}

// 确认删除评论（管理端）
const confirmDelete = () => {
  axios({
    method: 'post',
    url: `/api/comment/deleteComment?id=${selectedCommentId.value}`
  }).then((response) => {
    if (response.data.success) {
      ElMessageBox.alert("删除成功！", '提示')
      deleteDialogVisible.value = false
      // 保持当前搜索状态并刷新列表
      if (isSearching()) {
        searchComments()
      } else {
        getCommentPage()
      }
      // 同步刷新前端评论列表
      refreshComments()
    } else {
      ElMessageBox.alert(response.data.msg, '错误')
    }
  }).catch(() => {
    ElMessageBox.alert("系统错误！", '错误')
  })
}

// 组件挂载时自动加载评论数据
onMounted(() => {
  getCommentPage()
})

</script>

<template>



  <!-- ========== 原有：作者信息卡片（完全保留） ========== -->
  <el-row v-if="!isAdminMode" style="margin: 20px 0;">
    <el-col :span="14" :offset="5">
      <el-card shadow="hover" style="padding: 12px;">
        <div style="display: flex; align-items: center; width: 100%;">
          <!-- 作者头像 -->
          <div style="margin-right: 20px;">
            <el-avatar
                :src="author?.avatar || '/default-avatar.png'"
                size="large"
                style="cursor: pointer;"
                @click="gotoUserProfile(author?.id)"
            >
              {{ author?.username?.charAt(0) || '未知' }}
            </el-avatar>
          </div>

          <!-- 作者基本信息 -->
          <div style="flex: 1;">
            <div style="font-size: 16px; font-weight: bold;">{{ author?.username || '未知作者' }}</div>
            <div style="color: #666; margin: 3px 0;">{{ author?.intro || '这个人很懒，还没有添加签名~' }}</div>
            <div style="color: #999; font-size: 12px;">
              关注 {{ author?.followCount || 0 }} · 粉丝 {{ author?.fansCount || 0 }}
            </div>
          </div>

          <!-- 关注/取消关注按钮 -->
          <div v-if="currentUserId !== author?.id">
            <el-button
                v-if="!isFollowed"
                type="primary"
                @click="followAuthor"
                icon="el-icon-user-plus"
            >
              关注
            </el-button>
            <el-button
                v-else
                type="default"
                @click="unfollowAuthor"
                icon="el-icon-user-minus"
            >
              取消关注
            </el-button>
          </div>
        </div>
      </el-card>
    </el-col>
  </el-row>

  <!-- ========== 原有：文章内容展示（完全保留） ========== -->
  <el-row v-if="!isAdminMode">
    <el-col :span="14" :offset="5">
      <div v-html="articleAndComment.article.content"></div>
    </el-col>
  </el-row>

  <!-- ========== 原有：用户端评论区（完全保留） ========== -->
  <el-row v-if="!isAdminMode" style="background-color:#f7f7f7;">
    <el-col :span="14" :offset="5">
      <ul v-infinite-scroll="load" :infinite-scroll-disabled="disabled" class="infinite-list">
        <!-- 评论输入框（增强：回复模式） -->
        <li class="infinite-list-item comment-input-section">
          <el-row>
            <el-col>
              <el-input
                  v-model="commentContent"
                  :autosize="{minRows: 4}"
                  type="textarea"
                  :placeholder="isReplying ? `回复 ${replyTo}：` : '评论不能为空！'"
                  minlength="2"
              />
            </el-col>
          </el-row>
          <el-row justify="end" style="margin-top: 10px;">
            <el-col :xs="8" :sm="6" :md="4">
              <el-button @click="submit" type="primary" round style="margin-right: 10px;">提交评论</el-button>
              <!-- 新增：取消回复按钮 -->
              <el-button v-if="isReplying" @click="cancelReply" type="info" round>取消</el-button>
            </el-col>
          </el-row>
        </li>

        <!-- 评论列表（增强：传递onReply回调） -->
        <li v-for="comment in articleAndComment.comments" :key="comment.id" class="infinite-list-item">
          <Comment :comment="comment" :onReply="handleReply"></Comment>
        </li>
      </ul>
      <p v-if="loading">加载中...</p>
      <p v-if="noMore" class="end-comment">已显示全部评论</p>
      <p></p>
    </el-col>
  </el-row>

  <!-- ========== 新增：管理员端评论管理区域 ========== -->
  <div v-if="isAdminMode" style="padding: 0 20px;">
    <!-- 搜索表单 -->
    <el-row style="margin: 10px 10px;">
      <el-col :span="24">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="评论内容">
            <el-input v-model="searchForm.content" placeholder="请输入评论内容" clearable />
          </el-form-item>
          <el-form-item label="文章ID">
            <el-input v-model="searchForm.articleId" placeholder="请输入文章ID" clearable />
          </el-form-item>
          <el-form-item label="评论作者">
            <el-input v-model="searchForm.author" placeholder="请输入评论作者" clearable />
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
              <el-option label="待审核" :value="0" />
              <el-option label="已审核" :value="1" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="searchComments">搜索</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </el-col>
    </el-row>

    <!-- 评论表格 -->
    <el-row style="margin-top: 10px;">
      <el-col :span="24">
        <el-table
            :data="myData.commentVOs"
            stripe
            border
            style="width: 100%"
            row-key="id"
            default-expand-all>
          <el-table-column label="关联文章" width="200">
            <template #default="scope">
              <router-link :to="{ path: '/article_comment/' + scope.row.articleId }" style="text-decoration: none;">
                {{ scope.row.articleTitle }}
              </router-link>
            </template>
          </el-table-column>
          <el-table-column prop="author" label="评论作者" width="120" />
          <el-table-column prop="content" label="评论内容" width="300" />
          <el-table-column label="类型" width="100">
            <template #default="scope">
              <el-tag v-if="scope.row.parentId && scope.row.parentId !== 0" type="warning">回复</el-tag>
              <el-tag v-else type="success">评论</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="回复对象" width="120" v-if="showReplyToColumn">
            <template #default="scope">
              {{ scope.row.replyTo || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="ip" label="IP地址" width="150" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag type="success">已审核</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="created" label="评论时间" width="170" />
          <el-table-column label="操作" width="150">
            <template #default="scope">
              <div class="button-group">
                <el-button type="primary" :icon="Edit" size="small" @click="editComment(scope.row.id)">编辑</el-button>
                <el-button type="danger" :icon="Delete" size="small" @click="showDeleteDialog(scope.row.id)">删除</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </el-col>
    </el-row>

    <!-- 分页组件 -->
    <el-row justify="center" align="middle" style="height: 60px;">
      <el-col :span="16">
        <el-pagination
            v-model:current-page="myData.pageParams.page"
            v-model:page-size="myData.pageParams.rows"
            :page-sizes="[5, 10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="myData.pageParams.total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :pager-count="7"
        />
      </el-col>
    </el-row>

    <!-- 删除确认对话框 -->
    <el-dialog v-model="deleteDialogVisible" title="确认删除" width="30%" center>
      <span>确定要删除这条评论吗？删除后将同时删除其所有回复。此操作不可恢复。</span>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="deleteDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmDelete">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
/* 原有样式（完全保留） */
.infinite-list{
  padding:0;
  margin:0;
  list-style:none;
}

.infinite-list-item{
  background-color: #fff;
  margin-bottom:10px;
  margin-top:10px;
  padding:20px;
}

:deep(.el-card) {
  border-radius: 8px;
}
.end-comment {
  text-align: center;
  color: #999;
  padding: 10px 0;
}

/* 新增：管理端样式 */
.search-form {
  display: flex;
  flex-wrap: wrap;
}
.search-form .el-form-item {
  margin-right: 20px;
  margin-bottom: 10px;
}

.button-group {
  display: flex;
  gap: 6px;
  justify-content: center;
  align-items: center;
}
</style>
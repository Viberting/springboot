<script setup>
import Top from "@/components/Top.vue";
import { useRoute, useRouter } from 'vue-router'
import {ref, computed, inject, reactive} from 'vue'
// 新增：引入ElMessage（更友好的提示）
import { ElMessageBox, ElMessage } from 'element-plus'
import Comment from "@/components/Comment.vue"
import {useStore} from "@/stores/my.js"; // 引入Pinia
// 保留原有导入 + 新增评论回复服务导入
import { insertReply } from '@/js/commentReplyService.js';
import { commentReplyService } from '@/js/commentReplyService' // 新增：评论回复服务

const route = useRoute()
const axios = inject('axios')
const store = useStore()
const router = useRouter()

let articleAndComment = reactive({
  "article":{"content": ""} ,
  "comments": []
})

// ========== 原有：作者信息和关注状态相关响应式数据 ==========
const author = ref(null) // 存储作者信息
const isFollowed = ref(false) // 是否已关注该作者
const currentUserId = ref(null) //存储当前登录用户ID（用于身份校验）

// ========== 新增：回复相关核心变量 ==========
const commentContent = ref('')
const replyTo = ref(null) // 当前正在回复的评论作者
const replyParentId = ref(null) // 父评论ID
const replyRootId = ref(null) // 根评论ID
const isReplying = ref(false) // 是否处于回复模式

// 跳转到用户个人主页（原有功能保留）
const gotoUserProfile = (userId) => {
  if (!userId) return
  router.push({
    name: 'otherUserProfile', // 对应路由中定义的otherUserProfile
    params: {userId}
  })
}

// ========== 新增：处理回复功能（接收子组件的reply事件） ==========
const handleReply = (comment) => {
  replyTo.value = comment.author
  replyParentId.value = comment.id
  replyRootId.value = comment.rootId || comment.id // 无rootId则用自身ID
  isReplying.value = true
  // 滚动到评论框，提升交互体验
  setTimeout(() => {
    document.querySelector('.comment-input-section')?.scrollIntoView({behavior: 'smooth'})
  }, 100)
}

// ========== 新增：取消回复模式 ==========
const cancelReply = () => {
  isReplying.value = false
  replyTo.value = null
  replyParentId.value = null
  replyRootId.value = null
}

// ========== 增强：添加评论/回复（兼容原有普通评论 + 新增回复逻辑） ==========
function submit() {
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
    if (response.data.success) {
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
      ElMessageBox.alert(response.data.msg, '结果', {
        confirmButtonText: '确定',
        callback: (action) => {
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
    params: {authorId: author.value.id}, // 传递作者ID
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
      {type: 'warning'}
  ).then(async () => {
    // 确认后执行取消关注
    const res = await axios({
      method: 'post',
      url: '/api/follow/unfollowAuthor',
      params: {authorId: author.value.id},
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
let pageParams = {"page": 1, "rows": 2}

// 刷新评论列表（原有方法增强：优先用评论树接口）
const refreshComments = () => {
  pageParams.page = 1;
  // 优先获取评论树，保证层级结构
  commentReplyService.getCommentTreeByArticleId(route.params.articleId).then((treeResponse) => {
    if (treeResponse.data.success && treeResponse.data.map.comments) {
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
        if (response.data.success && response.data.map.article != null) {
          articleAndComment.comments = response.data.map.comments
          noMore.value = false;
        }
      })
    }
  }).catch((error) => {
    ElMessageBox.alert("系统错误！", '结果')
  })
};

// ========== 原有：页面初始化获取数据（增强：加载评论树） ==========
axios({
  method: 'post',
  url: '/api/article/getArticleAndFirstPageCommentByArticleId?articleId=' + route.params.articleId,
  data: pageParams,
  timeout: 3000000,
  withCredentials: true
}).then((response) => {
  if (response.data.success) {
    if (response.data.map.article != null) {
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
        if (treeResponse.data.success && treeResponse.data.map.comments) {
          articleAndComment.comments = treeResponse.data.map.comments
        }
      })
    } else {
      ElMessageBox.alert('无文章！', '结果')
    }
  } else {
    ElMessageBox.alert(response.data.msg, '结果')
  }
})

// ========== 原有：分页加载评论（完全保留） ==========
const noMore = ref(false)
const loading = ref(false)
const disabled = computed(() => loading.value || noMore.value)

const load = () => {
  loading.value = true
  pageParams.page++
  axios({
    method: 'post',
    url: '/api/comment/getAPageCommentByArticleId?articleId=' + route.params.articleId,
    data: pageParams,
    timeout: 3000000,
    withCredentials: true
  }).then((response) => {
    if (response.data.success) {
      let comments = response.data.map.comments
      if (comments != null && comments.length > 0) {
        for (let index = 0; index < comments.length; index++)
          articleAndComment.comments.push(comments[index]);
      } else if (comments == null || comments.length == 0)
        noMore.value = true
    } else {
      ElMessageBox.alert(response.data.msg, '结果')
    }
    loading.value = false
  }).catch((error) => {
    ElMessageBox.alert("系统错误！", '结果')
    loading.value = false
  })
}
</script>

<template>
  <el-affix>
    <Top/>
  </el-affix>

  <!-- 原有：作者信息卡片（完全保留） -->
  <el-row style="margin: 20px 0;">
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

  <!-- 原有：文章内容展示（完全保留） -->
  <el-row>
    <el-col :span="14" :offset="5">
      <div v-html="articleAndComment.article.content"></div>
    </el-col>
  </el-row>

  <!-- 原有：评论区（增强：回复模式UI） -->
  <el-row style="background-color:#f7f7f7;">
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
</template>

<style scoped>
.infinite-list {
  padding: 0;
  margin: 0;
  list-style: none;
}

.infinite-list-item {
  background-color: #fff;
  margin-bottom: 10px;
  margin-top: 10px;
  padding: 20px;
}

/* 原有：作者卡片样式（完全保留） */
:deep(.el-card) {
  border-radius: 8px;
}

.end-comment {
  text-align: center;
  color: #999;
  padding: 10px 0;
}
</style>
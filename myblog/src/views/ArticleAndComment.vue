<script setup>
import Top from "@/components/Top.vue";
import { useRoute, useRouter } from 'vue-router'
import {ref, computed, inject, reactive} from 'vue'
// 新增：引入ElMessage（更友好的提示）
import { ElMessageBox, ElMessage } from 'element-plus'
import Comment from "@/components/Comment.vue"
import {useStore} from "@/stores/my.js"; // 引入Pinia

const route = useRoute()
const axios = inject('axios')
const store = useStore()
const router = useRouter()

let articleAndComment = reactive({
  "article":{"content": ""} ,
  "comments": []
})

// ========== 新增：作者信息和关注状态相关响应式数据 ==========
const author = ref(null) // 存储作者信息
const isFollowed = ref(false) // 是否已关注该作者
const currentUserId = ref(null) //存储当前登录用户ID（用于身份校验）
// 跳转到用户个人主页
const gotoUserProfile = (userId) => {
  if (!userId) return
  router.push({
    name: 'otherUserProfile', // 对应路由中定义的otherUserProfile
    params: { userId }
  })
}

// 添加评论
const commentContent = ref('')
function submit(){
  axios({
    method: 'post',
    url: '/api/comment/insert',
    data: {
      "articleId":route.params.articleId,
      "content":commentContent.value
    },
    timeout: 3000000,
    withCredentials: true
  }).then((response) => {
    if(response.data.success){
      articleAndComment.comments.unshift(response.data.map.comment);
      ElMessageBox.alert(response.data.msg, '结果',{
        confirmButtonText: '确定',
        callback:( action )=>{
          commentContent.value="" // 清空评论
        }
      })
    }else{
      ElMessageBox.alert(response.data.msg, '结果')
    }
  }).catch((error) => {
    ElMessageBox.alert("未登录用户无法评论！", '结果')
  })
}

// ========== 新增：关注/取消关注方法、关注作者方法增加身份校验  ==========
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
// 文章详情页的取消关注作者方法（添加弹窗）
const unfollowAuthor = () => {
  if (!author.value) return

  // 新增：弹窗确认
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

let pageParams = { "page":1,"rows":2 }
// 打开新闻时获取数据（修改：新增作者信息和关注状态的赋值）
axios({
  method: 'post',
  url: '/api/article/getArticleAndFirstPageCommentByArticleId?articleId='
      +route.params.articleId,
  data: pageParams,
  timeout: 3000000,
  withCredentials: true
}).then((response) => {
  if(response.data.success){
    if(response.data.map.article!=null){
      articleAndComment.article=response.data.map.article
      articleAndComment.comments=response.data.map.comments
      // ========== 新增：赋值作者信息和关注状态 ==========
      author.value = response.data.map.author
      isFollowed.value = response.data.map.isFollowed

      // 新增：从Pinia获取当前登录用户ID
      if (store?.user?.user) {
        currentUserId.value = store.user.user.id
      }
    }else{
      ElMessageBox.alert('无文章！', '结果')
    }
  }else{
    ElMessageBox.alert(response.data.msg, '结果')
  }
})

// 分页展示评论（原有逻辑，仅修复错误提示）
const noMore = ref(false)
const loading = ref(false)
const disabled = computed(()=>loading.value || noMore.value)

const load = () => {
  loading.value = true
  pageParams.page++
  axios({
    method: 'post',
    url: '/api/comment/getAPageCommentByArticleId?articleId='
        +route.params.articleId,
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
      // 修复：错误提示用response.data.msg
      ElMessageBox.alert(response.data.msg, '结果')
    }
    loading.value = false
  }).catch((error) => {
    ElMessageBox.alert("系统错误！",'结果')
    loading.value=false
  })
}
</script>

<template>
  <el-affix>
    <Top />
  </el-affix>

  <!-- ========== 新增：作者信息卡片（隐藏本人的关注按钮，体验更优） ========== -->
  <el-row style="margin: 20px 0;">
    <el-col :span="14" :offset="5">
      <el-card shadow="hover" style="padding: 12px;">
        <!-- 用flex布局替代el-col的span分配，彻底自定义间距 -->
        <div style="display: flex; align-items: center; width: 100%;">
          <!-- 作者头像 -->
          <div style="margin-right: 20px;"> <!-- 这里的margin-right绝对生效 -->
            <el-avatar
                :src="author?.avatar || '/default-avatar.png'"
                size="large"
                style="cursor: pointer;"
                @click="gotoUserProfile(author?.id)"
            >
              {{ author?.username?.charAt(0) || '未知' }}
            </el-avatar>
          </div>

          <!-- 作者基本信息（占满中间空间） -->
          <div style="flex: 1;">
            <div style="font-size: 16px; font-weight: bold;">{{ author?.username || '未知作者' }}</div>
            <div style="color: #666; margin: 3px 0;">{{ author?.intro || '这个人很懒，还没有添加签名~' }}</div>
            <div style="color: #999; font-size: 12px;">
              关注 {{ author?.followCount || 0 }} · 粉丝 {{ author?.fansCount || 0 }}
            </div>
          </div>

          <!-- 关注/取消关注按钮（右对齐） -->
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

  <!-- 原有：文章内容展示 -->
  <el-row>
    <el-col :span="14" :offset="5">
      <div v-html="articleAndComment.article.content"></div>
    </el-col>
  </el-row>

  <!-- 原有：评论区 -->
  <el-row style="background-color:#f7f7f7;">
    <el-col :span="14" :offset="5">
      <ul v-infinite-scroll="load" :infinite-scroll-disabled="disabled" class="infinite-list">
        <li class="infinite-list-item">
          <el-row>
            <el-col>
              <el-input v-model="commentContent" :autosize="{minRows: 4}"
                        type="textarea" placeholder="评论不能为空！" minlength="2" />
            </el-col>
          </el-row>
          <el-row justify="end">
            <el-col :xs="8" :sm="6" :md="4">
              <el-button @click="submit" type="primary" round style="margin-top: 5px;">提交评论</el-button>
            </el-col>
          </el-row>
        </li>

        <li v-for="comment in articleAndComment.comments" :key="comment.id" class="infinite-list-item">
          <Comment :comment="comment"></Comment>
        </li>
      </ul>
      <p v-if="loading">加载中...</p>
      <p v-if="noMore" class="end-comment">已显示全部评论</p>
      <p></p>
    </el-col>
  </el-row>
</template>

<style scoped>
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

/* ========== 新增：作者卡片样式优化（可选） ========== */
:deep(.el-card) {
  border-radius: 8px;
}
.end-comment {
  text-align: center;
  color: #999;
  padding: 10px 0;
}
</style>
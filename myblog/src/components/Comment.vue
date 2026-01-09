<script setup>
// 定义组件的属性：保留原有comment，新增onReply回调（用于回复功能）
const props = defineProps(['comment', 'onReply'])

// 原有功能：获取评论作者首字（用于图片加载失败时显示）
const getAuthorFirstChar = () => {
  return props.comment.author?.charAt(0) || '未'
}

// 原有功能：时间格式化函数
const formatTime = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  // 格式化：年-月-日 时:分:秒
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}:${String(date.getSeconds()).padStart(2, '0')}`
}

// 新增：回复功能处理函数
const handleReply = (comment) => {
  if (props.onReply) {
    props.onReply(comment)
  }
}
</script>

<template>
  <!-- 顶级评论主体（保留原有所有样式和逻辑） -->
  <el-row style="margin-bottom:10px">
    <el-col>
      <div class="comment-header">
        <div class="avatar-container">
          <!-- 1. 优先加载用户头像，无则用默认图 -->
          <img
              class="avatar"
              :src="props.comment.userAvatar || '/api/images/default-avatar.png'"
              @error="(e) => { e.target.style.display = 'none'; e.target.nextElementSibling.style.display = 'block' }"
              v-show="props.comment.userAvatar"
          />
          <!-- 2. 仅当无头像 或 图片加载失败时，才显示首字占位 -->
          <span
              class="avatar-placeholder"
              v-show="!props.comment.userAvatar"
          >
          {{ getAuthorFirstChar() }}
          </span>
        </div>
        <span class="comment-author" v-html="props.comment.author"></span>
        <!-- 新增：显示回复@的用户名（仅顶级评论有replyTo时展示） -->
        <span class="reply-to" v-if="props.comment.replyTo">回复 {{ props.comment.replyTo }}</span>
      </div>
    </el-col>
  </el-row>

  <el-row style="margin-bottom:10px">
    <el-col class="comment-content">
      <div v-html="props.comment.content"></div>
    </el-col>
  </el-row>

  <!-- 时间展示（保留原有逻辑） -->
  <el-row style="margin-bottom:10px" justify="end">
    <el-col :span="3">
      <div class="comment-time">{{ formatTime(props.comment.created) }}</div>
    </el-col>
  </el-row>

  <!-- 新增：回复按钮（顶级评论） -->
  <el-row style="margin-bottom:10px" justify="start">
    <el-col :span="3">
      <el-button type="text" size="small" @click="handleReply(props.comment)">回复</el-button>
    </el-col>
  </el-row>

  <!-- 新增：显示回复的子评论列表 -->
  <div v-if="props.comment.children && props.comment.children.length > 0" class="replies">
    <div v-for="reply in props.comment.children" :key="reply.id" class="reply-item">
      <!-- 子评论主体（复用顶级评论的头像/首字占位逻辑） -->
      <el-row style="margin-bottom:10px; background-color: #f9f9f9; padding: 10px; border-radius: 4px;">
        <el-col>
          <div class="comment-header">
            <div class="avatar-container">
              <img
                  class="avatar"
                  :src="reply.userAvatar || '/api/images/default-avatar.png'"
                  @error="(e) => { e.target.style.display = 'none'; e.target.nextElementSibling.style.display = 'block' }"
                  v-show="reply.userAvatar"
              />
              <span
                  class="avatar-placeholder"
                  v-show="!reply.userAvatar"
              >
              {{ reply.author?.charAt(0) || '未' }}
              </span>
            </div>
            <span class="comment-author" v-html="reply.author"></span>
            <span class="reply-to" v-if="reply.replyTo">回复 {{ reply.replyTo }}</span>
          </div>
        </el-col>
      </el-row>

      <el-row style="margin-bottom:10px; padding-left: 20px;">
        <el-col class="comment-content">
          <div v-html="reply.content"></div>
        </el-col>
      </el-row>

      <el-row style="margin-bottom:10px; padding-left: 20px;" justify="end">
        <el-col :span="3">
          <div class="comment-time">{{ formatTime(reply.created) }}</div>
        </el-col>
      </el-row>

      <!-- 子评论的回复按钮 -->
      <el-row style="margin-bottom:10px; padding-left: 20px;" justify="start">
        <el-col :span="3">
          <el-button type="text" size="small" @click="handleReply(reply)">回复</el-button>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<style scoped>
/* 原有样式全部保留 */
.comment-header {
  height:40px;
  display: flex;
  align-items: center;
}

.avatar-container {
  position: relative;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: 1px solid #eaeaea;
  overflow: hidden;
}

.avatar {
  width: 100%;
  height: 100%;
  object-fit: cover; /* 防止图片变形 */
}

.avatar-placeholder {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  line-height: 40px;
  text-align: center;
  font-size: 16px;
  color: #666;
  background-color: #f5f5f5;
}

.comment-author {
  font-size:11px;
  line-height:40px;
  margin-left:20px;
  height:40px;
  color:#5f5f5f;
}

.comment-content {
  font-size:18px;
}

.comment-time {
  font-size:12px;
  color:#5f5f5f;
}

/* 新增：回复相关样式 */
.reply-to {
  font-size: 11px;
  color: #666;
  margin-left: 10px;
}

.replies {
  margin-left: 30px;
}

.reply-item {
  border-left: 2px solid #e0e0e0;
  padding-left: 15px;
  margin-top: 10px;
}
</style>
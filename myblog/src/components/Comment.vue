<script setup>
//定义组件的comment属性。组件的属性在组件内部一般是只读，不修改
const props = defineProps(['comment'])

// 新增：时间格式化函数
const formatTime = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  // 格式化：年-月-日 时:分:秒
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}:${String(date.getSeconds()).padStart(2, '0')}`
}
</script>

<template>
  <el-row style="margin-bottom:10px">
    <el-col>
      <div class="comment-header">
        <img
            class="avatar"
            :src="props.comment.userAvatar || '/api/images/cf2d4ee5-d28f-4ec5-8849-3a9875f34bdb_default-avatar.png'"
        />
        <span class="comment-author" v-html="props.comment.author"></span>
      </div>
    </el-col>
  </el-row>

  <el-row style="margin-bottom:10px">
    <el-col class="comment-content">
      <div v-html="props.comment.content"></div>
    </el-col>
  </el-row>

  <!-- justify=“end”是el-col的水平对齐方式 -->
  <el-row style="margin-bottom:10px" justify="end">
    <el-col :span="3">
      <div class="comment-time">{{ formatTime(props.comment.created) }}</div>
    </el-col>
  </el-row>
</template>

<style scoped>
.comment-header {
  height:40px;
  display: flex;
  align-items: center;
}

.avatar {
  width: 40px;
  height: 40px;
  border: 1px solid #eaeaea;
  border-radius: 50%;
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
</style>
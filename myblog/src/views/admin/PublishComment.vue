<template>
  <el-row>
    <el-col :span="24">
      <h4 style="margin-left: 10px; margin-top: 10px;">{{ header }}</h4>
    </el-col>
  </el-row>

  <el-row style="margin: 20px 10px;">
    <el-col :span="20" :offset="2">
      <el-form :model="comment" label-width="80px">
        <el-form-item label="关联文章ID">
          <el-input v-model="comment.articleId" placeholder="请输入评论所属文章的ID" />
        </el-form-item>
        <el-form-item label="评论作者">
          <el-input v-model="comment.author" placeholder="请输入评论作者" />
        </el-form-item>
        <el-form-item label="评论内容">
          <el-input
              v-model="comment.content"
              type="textarea"
              :rows="8"
              placeholder="请输入评论内容"
          />
        </el-form-item>
        <el-form-item label="IP地址">
          <el-input v-model="comment.ip" placeholder="请输入IP地址" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="comment.status" placeholder="请选择状态">
            <el-option label="待审核" :value="0" />
            <el-option label="已审核" :value="1" />
          </el-select>
        </el-form-item>
      </el-form>
    </el-col>
  </el-row>

  <el-row>
    <el-col :span="24" align="right" style="margin-right: 20px;">
      <el-button @click="gotoCommentManage">返回列表</el-button>
      <el-button type="primary" @click="saveComment">保存评论</el-button>
    </el-col>
  </el-row>
</template>

<script setup>
import { reactive, inject, ref, onMounted } from 'vue'
import { ElMessageBox } from 'element-plus'
import { useStore } from '@/stores/my'

const store = useStore()
const axios = inject('axios')
const header = ref("新增评论") // 页面标题
let type = ref("add") // 操作类型：add/edit
let comment = reactive({
  id: 0,
  articleId: '',
  author: '',
  content: '',
  ip: '',
  status: 0
})

// 从AdminMain.vue注入跳转方法
const gotoCommentManage = inject("gotoCommentManage")

// 页面加载时判断是否为编辑状态
onMounted(() => {
  if (store.commentId > 0) {
    // 编辑状态：查询评论信息回显
    type.value = "edit"
    header.value = "编辑评论"
    getCommentById(store.commentId)
  }
})

// 根据ID查询评论（编辑回显）
function getCommentById(id) {
  axios({
    method: 'post',
    url: `/api/comment/selectById?id=${id}`
  }).then((response) => {
    if (response.data.success) {
      const resComment = response.data.map.comment
      comment.id = resComment.id
      comment.articleId = resComment.articleId
      comment.author = resComment.author
      comment.content = resComment.content
      comment.ip = resComment.ip
      comment.status = resComment.status
    } else {
      ElMessageBox.alert(response.data.msg, '错误')
      gotoCommentManage() // 查询失败返回列表
    }
  }).catch(() => {
    ElMessageBox.alert("系统错误！", '错误')
    gotoCommentManage()
  })
}

// 保存评论（新增/编辑）
function saveComment() {
  // 简单表单校验
  if (!comment.articleId || !comment.author || !comment.content) {
    ElMessageBox.alert("请填写完整信息！", '提示')
    return
  }

  axios({
    method: 'post',
    url: `/api/comment/saveComment?type=${type.value}`,
    data: comment,
    timeout: 30000
  }).then((response) => {
    ElMessageBox.alert(response.data, '结果')
    if (response.data === "操作成功！") {
      store.commentId = 0 // 重置编辑状态
      gotoCommentManage() // 返回列表页
    }
  }).catch(() => {
    ElMessageBox.alert("系统错误！", '错误')
  })
}
</script>
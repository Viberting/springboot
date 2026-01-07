<template>
  <el-row>
    <el-col :span="24">
      <h4 style="margin-left: 10px; margin-top: 10px;">评论管理</h4>
    </el-col>
  </el-row>

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
      <el-table :data="myData.commentVOs" stripe border style="width: 100%">
        <el-table-column label="关联文章" width="200">
          <template #default="scope">
            <router-link :to="{ path: '/article_comment/' + scope.row.articleId }" style="text-decoration: none;">
              {{ scope.row.articleTitle }}
            </router-link>
          </template>
        </el-table-column>
        <el-table-column prop="author" label="评论作者" width="120" />
        <el-table-column prop="content" label="评论内容" width="400" />
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
    <span>确定要删除这条评论吗？</span>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="deleteDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmDelete">确定</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { reactive, inject, ref } from 'vue'
import { ElMessageBox } from 'element-plus'
import { Edit, Delete } from '@element-plus/icons-vue'
import { useStore } from '@/stores/my'
import { nullZeroBlank } from '@/js/tool.js'

const store = useStore()
const axios = inject('axios')
const deleteDialogVisible = ref(false) // 删除对话框显示状态
let selectedCommentId = ref(0) // 选中要删除的评论ID
const editCommentFunc = inject("editComment") // 从AdminMain.vue注入的跳转方法

// 页面数据（评论列表 + 分页信息）
let myData = reactive({
  commentVOs: [],
  pageParams: { page: 1, rows: 10, total: 0 }
})

// 搜索表单
let searchForm = reactive({
  content: '',
  articleId: '',
  author: '',
  status: ''
})

// 初始化加载评论列表
getCommentPage()

// 分页查询评论
function getCommentPage() {
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

// 搜索评论
function searchComments() {
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

// 重置搜索
function resetSearch() {
  searchForm.content = ''
  searchForm.articleId = ''
  searchForm.author = ''
  searchForm.status = ''
  // 重新加载所有评论
  myData.pageParams.page = 1;
  getCommentPage()
}

// 每页条数变化
function handleSizeChange(newRows) {
  myData.pageParams.rows = newRows
  myData.pageParams.page = 1 // 重置为第一页
  // 判断是否在搜索状态，如果是则使用搜索方法，否则使用普通方法
  if (isSearching()) {
    searchComments()
  } else {
    getCommentPage()
  }
}

// 页码变化
function handleCurrentChange(newPage) {
  myData.pageParams.page = newPage
  // 判断是否在搜索状态，如果是则使用搜索方法，否则使用普通方法
  if (isSearching()) {
    searchComments()
  } else {
    getCommentPage()
  }
}

// 检查是否在搜索状态
function isSearching() {
  return searchForm.content !== '' || 
         searchForm.articleId !== '' || 
         searchForm.author !== '' || 
         searchForm.status !== ''
}

// 编辑评论：保存评论ID和分页状态，跳转至编辑页
function editComment(id) {
  store.commentId = id // 保存要编辑的评论ID
  store.page.pageParams = { ...myData.pageParams } // 保存当前分页状态
  editCommentFunc()
}

// 显示删除确认对话框
function showDeleteDialog(id) {
  selectedCommentId.value = id
  deleteDialogVisible.value = true
}

// 确认删除评论
function confirmDelete() {
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
    } else {
      ElMessageBox.alert(response.data.msg, '错误')
    }
  }).catch(() => {
    ElMessageBox.alert("系统错误！", '错误')
  })
}
</script>

<style scoped>
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
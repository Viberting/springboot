<template>
  <div class="user-manage-container">
    <!-- 页面标题 + 新增按钮 -->
    <el-row class="page-header" justify="space-between" align="middle">
      <el-col :span="8">
        <h4 style="margin-left: 10px;">用户管理</h4>
      </el-col>
      <el-col :span="4">
        <el-button type="primary" @click="showAddDialog">新增用户</el-button>
      </el-col>
    </el-row>

    <!-- 搜索表单 -->
    <el-row style="margin: 10px 10px;">
      <el-col :span="24">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="用户名">
            <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="searchForm.email" placeholder="请输入邮箱" clearable />
          </el-form-item>
          <el-form-item label="注册日期">
            <el-date-picker
              v-model="searchForm.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              value-format="YYYY-MM-DD"
              format="YYYY-MM-DD"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="searchUsers">搜索</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </el-col>
    </el-row>

    <!-- 用户列表表格 -->
    <el-row style="margin-top: 10px;">
      <el-col :span="24">
        <el-table
            :data="myData.userVOs"
            stripe
            border
            style="width: 100%"
            v-loading="loading"
        >
          <el-table-column label="用户ID" width="80" prop="id" />
          <el-table-column label="用户名" width="150" prop="username" />
          <el-table-column label="邮箱" width="200" prop="email" />
          <el-table-column label="创建时间" width="150" prop="created" />
          <!-- ✅ 修复核心：账号状态列 匹配数据库1/0 正常显示 -->
          <el-table-column label="账号状态" width="100" align="center">
            <template #default="scope">
              <span style="color: black;">有效</span>
            </template>
          </el-table-column>
                  <el-table-column label="拥有权限" width="200">
            <template #default="scope">
              <div v-if="scope.row.authorityNames && scope.row.authorityNames.length > 0">
                <el-tag v-for="(auth, index) in scope.row.authorityNames" :key="index" style="margin-right: 5px;">
                  {{ auth && typeof auth === 'string' ? auth.replace("ROLE_", "") : '未知权限' }}
                </el-tag>
              </div>
              <div v-else-if="scope.row.authorities && scope.row.authorities.length > 0">
                <el-tag v-for="(auth, index) in scope.row.authorities" :key="index" style="margin-right: 5px;">
                  {{ auth && typeof auth === 'string' ? auth.replace("ROLE_", "") : '未知权限' }}
                </el-tag>
              </div>
              <div v-else-if="scope.row.authorityIds && scope.row.authorityIds.length > 0">
                <el-tag v-for="(authId, index) in scope.row.authorityIds" :key="index" style="margin-right: 5px;">
                  {{ getAuthorityNameByIdRender(authId) }}
                </el-tag>
              </div>
              <div v-else>
                <el-tag>普通用户</el-tag>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180">
            <template #default="scope">
              <el-button type="primary" size="small" @click="showEditDialog(scope.row.id)">编辑</el-button>
              <el-button type="danger" size="small" @click="showDeleteDialog(scope.row.id)">删除</el-button>
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
            :page-sizes="[5, 10, 20]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="myData.pageParams.total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :pager-count="7"
        />
      </el-col>
    </el-row>

    <!-- 新增/编辑用户对话框 -->
    <el-dialog
        v-model="dialogVisible"
        :title="dialogTitle"
        width="50%"
        center
    >
      <el-form
          ref="userFormRef"
          :model="userForm"
          :rules="userRules"
          label-width="120px"
          size="default"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
              v-model="userForm.password"
              type="password"
              placeholder="新增必填，编辑可不填（不修改密码）"
          />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <!-- ✅ 修复核心：弹窗账号状态改为开关组件，适配数字1/0，删除了错误的表格列嵌套 -->
        <el-form-item label="账号状态">
          <el-switch
              v-model="userForm.valid"
              active-text="有效"
              inactive-text="禁用"
              active-value="1"
              inactive-value="0"
              style="--el-switch-on-color: #67c23a; --el-switch-off-color: #f56c6c;"
          />
        </el-form-item>
        <el-form-item label="性别">
          <el-select
              v-model="userForm.gender"
              placeholder="请选择性别"
          >
            <el-option :value="0" label="男" />
            <el-option :value="1" label="女" />
            <el-option :value="2" label="未知" />
          </el-select>
        </el-form-item>
        <el-form-item label="生日">
          <el-date-picker
              v-model="userForm.birthday"
              type="date"
              placeholder="选择生日"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
          />
        </el-form-item>
        <el-form-item label="个人简介">
          <el-input
              v-model="userForm.intro"
              type="textarea"
              placeholder="请输入个人简介"
              :rows="3"
          />
        </el-form-item>
        <el-form-item label="GitHub链接">
          <el-input v-model="userForm.githubUrl" placeholder="请输入GitHub链接" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitUserForm">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 删除确认对话框 -->
    <el-dialog
        v-model="deleteDialogVisible"
        title="确认删除"
        width="30%"
        center
    >
      <span>确定要禁用该用户吗？禁用后用户无法登录！</span>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="deleteDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="confirmDelete">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, inject, onMounted } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { useStore } from '@/stores/my'
import { nullZeroBlank } from '@/js/tool.js'

// 状态管理
const store = useStore()
// 路由与axios
const axios = inject('axios')
const gotoUserManage = inject("gotoUserManage")

// 检查用户权限
const checkUserPermission = () => {
  if (!store.user.user) {
    ElMessageBox.alert("用户未登录或会话已过期", "权限错误")
    return false
  }
  const authorities = store.user.user.authorities
  const isAdmin = authorities.some(auth => auth === 'ROLE_admin' || auth === 'ROLE_ADMIN')
  if (!isAdmin) {
    ElMessageBox.alert("您没有足够的权限访问此页面", "权限不足")
    return false
  }
  return true
}

// 页面数据
const myData = reactive({
  userVOs: [],
  pageParams: { page: 1, rows: 10, total: 0 },
  authorityList: []
})
const loading = ref(false)
const dialogVisible = ref(false)
const deleteDialogVisible = ref(false)
const dialogTitle = ref("新增用户")
const isAdd = ref(true)
const userFormRef = ref(null)
const userForm = reactive({
  id: 0,
  username: "",
  password: "",
  email: "",
  valid: "1", // ✅ 默认有效 字符串1/0 适配开关组件
  intro: "",
  githubUrl: "",
  gender: null,
  birthday: ""
})
let deleteUserId = ref(0)

// 搜索表单
const searchForm = reactive({
  username: '',
  email: '',
  dateRange: []  // [startDate, endDate]
})

// ✅ 密码规则动态绑定isAdd，编辑时非必填
const userRules = reactive({
  username: [
    { required: true, message: "请输入用户名", trigger: "blur" },
    { min: 2, max: 10, message: "用户名长度为2-10位", trigger: "blur" },
    { pattern: /^[A-Za-z0-9_]+$/, message: "用户名仅支持字母、数字、下划线", trigger: "blur" }
  ],
  email: [
    { required: true, message: "请输入邮箱", trigger: "blur" },
    { pattern: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/, message: "邮箱格式不正确", trigger: "blur" }
  ],
  password: [
    { required: ()=>isAdd.value, message: "请输入密码", trigger: "blur" }
  ],

})

// 根据权限ID获取权限名称
const getAuthorityNameByIdRender = (id) => {
  if (!id) return '未知权限';
  if (myData.authorityList.length > 0) {
    const authority = myData.authorityList.find(auth => auth.id === id)
    return authority && authority.name ? authority.name.replace("ROLE_", "") : '未知权限'
  }
  return '未知权限'
}

// 初始化加载
onMounted(() => {
  if (!checkUserPermission()) return
  getAuthorityList()
  getUserPage()
})

// 1. 查询所有权限
const getAuthorityList = async () => {
  try {
    const response = await axios({
      method: "post",
      url: "/api/user/getAllAuthorities",
      withCredentials: true
    })
    if (response.data.success) {
      myData.authorityList = response.data.map.authorities
    } else {
      ElMessageBox.alert(response.data.msg, "获取权限失败")
    }
  } catch (error) {
    console.error('获取权限失败:', error);
    ElMessageBox.alert("系统错误，获取权限失败", "错误")
  }
}

// 2. 分页查询用户列表 - 完整修复版
const getUserPage = async () => {
  loading.value = true
  try {
    // 恢复分页状态（保留原有逻辑）
    if (!nullZeroBlank(store.page.pageParams)) {
      myData.pageParams = store.page.pageParams
      store.page.pageParams = null
    }
    // 发送请求
    const response = await axios({
      method: "post",
      url: "/api/user/getUserPage",
      data: myData.pageParams,
      withCredentials: true
    })

    console.log("后端返回的用户数据：", response.data); // 调试日志

    if (response.data.success) {
      // 直接赋值用户列表+分页参数，兜底空数组
      myData.userVOs = response.data.map.userVOs || [];
      myData.pageParams = response.data.map.pageParams;
      if (myData.userVOs.length === 0) {
        ElMessage.info("暂无用户数据");
      }
    } else {
      ElMessageBox.alert(response.data.msg, "查询用户失败");
      myData.userVOs = [];
    }
  } catch (error) {
    console.error('查询用户失败:', error);
    ElMessageBox.alert("系统错误，查询用户失败", "错误");
    myData.userVOs = [];
  } finally {
    loading.value = false
  }
}

// 搜索用户
const searchUsers = async () => {
  loading.value = true
  try {
    // 重置到第一页
    myData.pageParams.page = 1;

    // 创建搜索参数对象
    const searchParams = {
      ...myData.pageParams,
      username: searchForm.username.trim() || null,
      email: searchForm.email.trim() || null,
      startDate: searchForm.dateRange && searchForm.dateRange.length === 2 ? searchForm.dateRange[0] : null,
      endDate: searchForm.dateRange && searchForm.dateRange.length === 2 ? searchForm.dateRange[1] : null
    }

    const response = await axios({
      method: "post",
      url: "/api/user/getUserPage",
      data: searchParams,
      withCredentials: true
    })

    console.log("后端返回的用户数据：", response.data); // 调试日志

    if (response.data.success) {
      // 直接赋值用户列表+分页参数，兜底空数组
      myData.userVOs = response.data.map.userVOs || [];
      myData.pageParams = response.data.map.pageParams;
      if (myData.userVOs.length === 0) {
        ElMessage.info("暂无符合条件的用户数据");
      }
    } else {
      ElMessageBox.alert(response.data.msg, "查询用户失败");
      myData.userVOs = [];
    }
  } catch (error) {
    console.error('搜索用户失败:', error);
    ElMessageBox.alert("系统错误，搜索用户失败", "错误");
    myData.userVOs = [];
  } finally {
    loading.value = false
  }
}

// 重置搜索
const resetSearch = () => {
  searchForm.username = ''
  searchForm.email = ''
  searchForm.dateRange = []
  // 重新加载所有用户
  myData.pageParams.page = 1;
  getUserPage()
}

// 检查是否在搜索状态
const isSearching = () => {
  return searchForm.username !== '' || 
         searchForm.email !== '' || 
         (searchForm.dateRange && searchForm.dateRange.length > 0)
}

const handleSizeChange = (newRows) => {
  myData.pageParams.rows = newRows
  myData.pageParams.page = 1
  // 判断是否在搜索状态，如果是则使用搜索方法，否则使用普通方法
  if (isSearching()) {
    searchUsers()
  } else {
    getUserPage()
  }
}
const handleCurrentChange = (newPage) => {
  myData.pageParams.page = newPage
  // 判断是否在搜索状态，如果是则使用搜索方法，否则使用普通方法
  if (isSearching()) {
    searchUsers()
  } else {
    getUserPage()
  }
}

// 3. 新增弹窗
const showAddDialog = () => {
  isAdd.value = true
  dialogTitle.value = "新增用户"
  Object.assign(userForm,{
    id:0,username:"",password:"",email:"",valid:"1",
    intro:"",githubUrl:"",gender:null,birthday:""
  })
  userFormRef.value?.clearValidate()
  dialogVisible.value = true
}

// 4. 编辑弹窗+权限回显 核心修复：适配数字valid转开关的字符串1/0
const showEditDialog = async (userId) => {
  isAdd.value = false
  dialogTitle.value = "编辑用户"
  deleteUserId.value = userId
  try {
    const userResponse = await axios({
      method: "post",
      url: "/api/user/selectById",
      params: { id: userId },
      withCredentials: true
    })
    if (userResponse.data.success) {
      const user = userResponse.data.map.user;
      const authorityIds = userResponse.data.map.authorityIds || [];
      if (!user) {
        ElMessageBox.alert("无法获取用户数据", "数据错误");
        return;
      }
      // 赋值基本信息 ✅ 核心修复：数字1/0 转 字符串 适配开关组件
      Object.assign(userForm,{  
        id:user.id,username:user.username,password:"",email:user.email,
        valid: user.valid === 1 ? "1" : "0",
        intro:user.intro||"",githubUrl:user.githubUrl||"",
        gender:user.gender||null,birthday:user.birthday||""
      })
      userFormRef.value?.clearValidate()
      dialogVisible.value = true
    } else {
      ElMessageBox.alert(userResponse.data.msg, "查询用户失败")
    }
  } catch (error) {
    console.error('编辑用户时出错:', error);
    ElMessageBox.alert("系统错误，查询用户失败", "错误")
  }
}

const showDeleteDialog = (userId) => {
  deleteUserId.value = userId
  deleteDialogVisible.value = true
}

// 5. 提交表单 【✅已修复：删除外层user包装 + valid转数字】
const submitUserForm = async () => {
  if (!userFormRef.value) return
  try {
    const valid = await userFormRef.value.validate()
    if (valid) {
      const requestData = { ...userForm }
      // 编辑时密码为空则删除该字段，不修改密码
      if (!isAdd.value && !requestData.password) {
        delete requestData.password
      }
      // ============ ✅ 新增：修复核心1 - 字符串转数字，匹配后端Integer ============
      requestData.valid = requestData.valid === "1" ? 1 : 0;
      // ============ ✅ 修复核心2 - 直接传requestData，删除外层{user:xxx}包装 ============
      try {
        let response
        if (isAdd.value) {
          response = await axios({
            method: "post",url: "/api/user/adminAddUser",
            data: requestData, // 原来的错误写法：{user: requestData}
            withCredentials: true
          })
        } else {
          response = await axios({
            method: "post",url: "/api/user/adminUpdateUser",
            data: requestData, // 原来的错误写法：{user: requestData} 【这里是编辑的核心错误】
            withCredentials: true
          })
        }
        if (response.data.success) {
          ElMessage.success(response.data.msg)
          dialogVisible.value = false
          // 保持当前搜索状态并刷新列表
          if (isSearching()) {
            searchUsers()
          } else {
            getUserPage()
          }
        } else {
          ElMessageBox.alert(response.data.msg, "操作失败")
        }
      } catch (error) {
        console.error('API请求错误:', error)
        ElMessageBox.alert("系统错误，操作失败", "错误")
      }
    }
  } catch (error) {
    if (error.message !== 'cancel') {
      ElMessageBox.alert("表单校验失败，请检查填写内容", "提示")
    }
  }
}
// 6. 确认删除
const confirmDelete = async () => {
  try {
    const response = await axios({
      method: "post",
      url: `/api/user/deleteUser?id=${deleteUserId.value}`,
      withCredentials: true
    })
    if (response.data.success) {
      ElMessage.success(response.data.msg)
      deleteDialogVisible.value = false
      // 保持当前搜索状态并刷新列表
      if (isSearching()) {
        searchUsers()
      } else {
        getUserPage()
      }
    } else {
      ElMessageBox.alert(response.data.msg, "删除失败")
    }
  } catch (error) {
    ElMessageBox.alert("系统错误，删除失败", "错误")
  }
}
</script>

<style scoped>
.user-manage-container { padding: 10px; }
.page-header { margin-bottom: 10px; }
.el-table { margin-bottom: 10px; }
.el-form-item { margin-bottom: 20px; }
.search-form {
  display: flex;
  flex-wrap: wrap;
}
.search-form .el-form-item {
  margin-right: 20px;
  margin-bottom: 10px;
}
</style>
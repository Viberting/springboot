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
          <el-table-column label="账号状态" width="100" prop="validStr" />
          <el-table-column label="拥有权限" width="200">
            <template #default="scope">
              <div v-if="scope.row.authorityNames && scope.row.authorityNames.length > 0">
                <el-tag v-for="(auth, index) in scope.row.authorityNames" :key="index" style="margin-right: 5px;">
                  {{ auth ? auth.replace("ROLE_", "") : '未知权限' }}
                </el-tag>
              </div>
              <div v-else-if="scope.row.authorities && scope.row.authorities.length > 0">
                <!-- 备用字段名称 -->
                <el-tag v-for="(auth, index) in scope.row.authorities" :key="index" style="margin-right: 5px;">
                  {{ auth ? auth.replace("ROLE_", "") : '未知权限' }}
                </el-tag>
              </div>
              <div v-else-if="scope.row.authorityIds && scope.row.authorityIds.length > 0">
                <!-- 如果只有权限ID，尝试在authorityList中查找对应权限 -->
                <el-tag v-for="(authId, index) in scope.row.authorityIds" :key="index" style="margin-right: 5px;">
                  {{ getAuthorityNameById(authId) }}
                </el-tag>
              </div>
              <div v-else>
                <el-tag>无权限</el-tag>
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
        <el-form-item label="密码" :required="isAdd">
          <el-input
              v-model="userForm.password"
              type="password"
              placeholder="新增必填，编辑可不填（不修改密码）"
          />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="账号状态" prop="valid">
          <el-switch
              v-model="userForm.valid"
              active-label="有效"
              inactive-label="禁用"
          />
        </el-form-item>
        <el-form-item label="分配权限" prop="selectedAuthorityIds">
          <el-select
              v-model="userForm.selectedAuthorityIds"
              multiple
              placeholder="请选择权限"
              @change="handleAuthorityChange"
          >
            <el-option
                v-for="auth in myData.authorityList"
                :key="auth.id"
                :label="auth.name.replace('ROLE_', '')"
                :value="auth.id"
            />
          </el-select>
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
import { ElMessageBox, ElMessage, ElLoading } from 'element-plus'
import { useStore } from '@/stores/my'
import { nullZeroBlank } from '@/js/tool.js'

// 状态管理
const store = useStore()
// 路由与axios
const axios = inject('axios')
const gotoUserManage = inject("gotoUserManage") // 从AdminMain注入的跳转方法

// 检查用户权限
const checkUserPermission = () => {
  if (!store.user.user) {
    ElMessageBox.alert("用户未登录或会话已过期", "权限错误")
    // 可以选择重定向到登录页
    // window.location.href = '/login';
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
  userVOs: [], // 用户列表
  pageParams: { page: 1, rows: 10, total: 0 }, // 分页信息
  authorityList: [] // 所有权限列表（下拉选择用）
})

// 加载状态
const loading = ref(false)
// 对话框状态
const dialogVisible = ref(false) // 新增/编辑对话框
const deleteDialogVisible = ref(false) // 删除确认对话框
const dialogTitle = ref("新增用户") // 对话框标题
const isAdd = ref(true) // 是否为新增操作
// 表单引用与数据
const userFormRef = ref(null)
const userForm = reactive({
  id: 0,
  username: "",
  password: "",
  email: "",
  valid: true // 默认为有效
})
// 选中的权限ID（用于分配权限）
const selectedAuthorityIds = ref([])
console.log('selectedAuthorityIds initial value:', selectedAuthorityIds.value)

// 权限选择变更处理函数
const handleAuthorityChange = (val) => {
  console.log('权限选择变更:', val)
  selectedAuthorityIds.value = val
  userForm.selectedAuthorityIds = val
}

// 根据权限ID获取权限名称
const getAuthorityNameById = (id) => {
  if (myData.authorityList && myData.authorityList.length > 0) {
    const authority = myData.authorityList.find(auth => auth.id === id)
    return authority ? authority.name.replace("ROLE_", "") : '未知权限'
  }
  return '未知权限'
}

// 待删除的用户ID
let deleteUserId = ref(0)

// 表单校验规则
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
  selectedAuthorityIds: [
    { required: true, message: "请至少选择一个权限", trigger: "change", type: "array", min: 1 }
  ]
})

// 在表单数据中添加 selectedAuthorityIds 字段，用于表单验证
userForm.selectedAuthorityIds = []

// 页面加载时初始化
onMounted(() => {
  // 检查用户权限
  // 先检查用户权限，再获取数据
  if (!checkUserPermission()) {
    console.log('权限检查失败');
    return
  }
  
  console.log('开始获取权限列表和用户数据');
  getAuthorityList() // 查询所有权限
  getUserPage() // 查询用户列表
})

// 1. 查询所有权限（用于下拉选择）
const getAuthorityList = async () => {
  try {
    const response = await axios({
      method: "post",
      url: "/api/user/getAllAuthorities",
      withCredentials: true
    }).catch(error => {
      console.error('获取权限列表请求失败:', error);
      if (error.response && error.response.status === 401) {
        ElMessageBox.alert("认证已过期，请重新登录", "权限错误");
      } else {
        ElMessageBox.alert("网络错误或服务器不可用", "请求失败");
      }
      throw error; // 重新抛出错误以保持原有逻辑
    })
    console.log('权限列表接口返回:', response);
    
    // 检查响应是否为HTML（可能重定向到了登录页）
    const contentType = response.headers['content-type'];
    if (contentType && contentType.includes('text/html')) {
      console.warn('权限列表请求被重定向到登录页面');
      ElMessageBox.alert("认证已过期，请重新登录", "会话过期");
      // 可以选择重定向到登录页
      // window.location.href = '/login';
      return;
    }
    
    if (response.data.success) {
      myData.authorityList = response.data.map.authorities
      console.log('获取到的权限列表:', myData.authorityList);
    } else {
      console.log('获取权限失败:', response.data.msg);
      ElMessageBox.alert(response.data.msg, "获取权限失败")
    }
  } catch (error) {
    console.error('获取权限失败:', error);
    ElMessageBox.alert("系统错误，获取权限失败", "错误")
  }
}

// 2. 分页查询用户列表
const getUserPage = async () => {
  loading.value = true
  try {
    // 恢复之前的分页状态（从编辑页返回时）
    if (!nullZeroBlank(store.page.pageParams)) {
      myData.pageParams = store.page.pageParams
      store.page.pageParams = null
    }

    const response = await axios({
      method: "post",
      url: "/api/user/getUserPage",
      data: myData.pageParams,
      withCredentials: true
    }).catch(error => {
      console.error('获取用户列表请求失败:', error);
      if (error.response && error.response.status === 401) {
        ElMessageBox.alert("认证已过期，请重新登录", "权限错误");
      } else {
        ElMessageBox.alert("网络错误或服务器不可用", "请求失败");
      }
      throw error; // 重新抛出错误以保持原有逻辑
    })

    console.log('用户列表接口返回:', response);
    
    // 检查响应是否为HTML（可能重定向到了登录页）
    const contentType = response.headers['content-type'];
    if (contentType && contentType.includes('text/html')) {
      console.warn('用户列表请求被重定向到登录页面');
      ElMessageBox.alert("认证已过期，请重新登录", "会话过期");
      // 可以选择重定向到登录页
      // window.location.href = '/login';
      return;
    }
    
    if (response.data.success) {
      // 尝试多种可能的数据结构
      console.log('完整响应数据:', response.data);
      
      // 检查不同可能的数据结构
      if (response.data.map && response.data.map.users) {
        // 标准结构
        myData.userVOs = response.data.map.users || []
        myData.pageParams = response.data.map.pageParams
      } else if (response.data.map && response.data.map.userVOs) {
        // 备用结构 - 可能是userVOs而不是users
        myData.userVOs = response.data.map.userVOs || []
        myData.pageParams = response.data.map.pageParams
      } else if (response.data.map && Array.isArray(Object.values(response.data.map)[0])) {
        // 如果map的第一个值是数组，尝试找到用户数组
        const mapKeys = Object.keys(response.data.map);
        for (const key of mapKeys) {
          if (Array.isArray(response.data.map[key]) && response.data.map[key].length > 0 && response.data.map[key][0].hasOwnProperty('username')) {
            // 假设包含username属性的是用户数组
            myData.userVOs = response.data.map[key];
            break;
          }
        }
        // 如果没找到用户数组但数组为空，也尝试
        if (myData.userVOs.length === 0) {
          for (const key of mapKeys) {
            if (Array.isArray(response.data.map[key])) {
              myData.userVOs = response.data.map[key];
              break;
            }
          }
        }
        // 分页参数处理
        if (response.data.map.pageParams) {
          myData.pageParams = response.data.map.pageParams;
        } else {
          // 尝试找到包含page, rows, total的属性
          for (const key of mapKeys) {
            const obj = response.data.map[key];
            if (obj && typeof obj === 'object' && obj.page !== undefined && obj.rows !== undefined && obj.total !== undefined) {
              myData.pageParams = obj;
              break;
            }
          }
        }
      } else {
        // 如果没有map结构，直接检查data
        console.warn('响应数据结构与预期不同:', response.data);
        ElMessageBox.alert('响应数据结构异常，请检查后端接口', '警告');
        myData.userVOs = [];
        myData.pageParams = { page: 1, rows: 10, total: 0 };
      }
      
      console.log('最终获取到的用户数据:', myData.userVOs);
      console.log('最终分页参数:', myData.pageParams);
      
      if (myData.userVOs.length === 0 && myData.pageParams.total > 0) {
        console.warn('警告：总数不为0但用户数组为空，可能的数据结构问题');
        ElMessage.warning('数据总数不匹配，请联系管理员检查数据结构');
      } else if (myData.userVOs.length === 0) {
        ElMessage.info("暂无用户数据");
      }
    } else {
      console.log('后端返回错误:', response.data.msg);
      ElMessageBox.alert(response.data.msg, "查询用户失败");
    }
  } catch (error) {
    console.error('查询用户失败:', error);
    ElMessageBox.alert("系统错误，查询用户失败", "错误")
  } finally {
    loading.value = false
  }
}

// 3. 分页大小变化
const handleSizeChange = (newRows) => {
  myData.pageParams.rows = newRows
  myData.pageParams.page = 1
  getUserPage()
}

// 4. 页码变化
const handleCurrentChange = (newPage) => {
  myData.pageParams.page = newPage
  getUserPage()
}

// 5. 显示新增用户对话框
const showAddDialog = () => {
  isAdd.value = true
  dialogTitle.value = "新增用户"
  // 重置表单
  userForm.id = 0
  userForm.username = ""
  userForm.password = ""
  userForm.email = ""
  userForm.valid = true
  userForm.selectedAuthorityIds = []
  selectedAuthorityIds.value = []
  // 清除表单验证状态
  if (userFormRef.value) {
    userFormRef.value.clearValidate()
  }
  // 显示对话框
  dialogVisible.value = true
}

// 6. 显示编辑用户对话框
const showEditDialog = async (userId) => {
  isAdd.value = false
  dialogTitle.value = "编辑用户"
  deleteUserId.value = userId
  try {
    // 查询用户信息与拥有的权限
    const response = await axios({
      method: "post",
      url: `/api/user/selectById?id=${userId}`,
      withCredentials: true
    }).catch(error => {
      console.error('获取用户详情请求失败:', error);
      if (error.response && error.response.status === 401) {
        ElMessageBox.alert("认证已过期，请重新登录", "权限错误");
      } else {
        ElMessageBox.alert("网络错误或服务器不可用", "请求失败");
      }
      throw error; // 重新抛出错误以保持原有逻辑
    })
    if (response.data.success) {
      const user = response.data.map.user
      const authorityIds = response.data.map.authorityIds
      // 回显表单数据
      userForm.id = user.id
      userForm.username = user.username
      userForm.email = user.email
      userForm.valid = user.valid
      userForm.password = "" // 编辑时密码为空（不修改）
      userForm.selectedAuthorityIds = authorityIds
      selectedAuthorityIds.value = authorityIds
      // 清除表单验证状态
      if (userFormRef.value) {
        userFormRef.value.clearValidate()
      }
      // 显示对话框
      dialogVisible.value = true
    } else {
      ElMessageBox.alert(response.data.msg, "查询用户失败")
    }
  } catch (error) {
    ElMessageBox.alert("系统错误，查询用户失败", "错误")
  }
}

// 7. 显示删除确认对话框
const showDeleteDialog = (userId) => {
  deleteUserId.value = userId
  deleteDialogVisible.value = true
}

// 8. 提交新增/编辑用户
const submitUserForm = async () => {
  if (!userFormRef.value) return
  try {
    // 验证整个表单
    const valid = await userFormRef.value.validate()
    if (valid) {
      // 构建请求参数
      const requestData = { ...userForm }
      // 编辑时密码为空则删除该字段（不修改密码）
      if (!isAdd.value && (requestData.password === "" || requestData.password === null)) {
        delete requestData.password
      }

      try {
        let response
        if (isAdd.value) {
          // 新增用户
          response = await axios({
            method: "post",
            url: "/api/user/addUser",
            data: requestData,
            params: { authorityIds: selectedAuthorityIds.value },
            withCredentials: true
          }).catch(error => {
            console.error('添加用户请求失败:', error);
            if (error.response && error.response.status === 401) {
              ElMessageBox.alert("认证已过期，请重新登录", "权限错误");
            } else {
              ElMessageBox.alert("网络错误或服务器不可用", "请求失败");
            }
            throw error; // 重新抛出错误以保持原有逻辑
          })
        } else {
          // 编辑用户
          response = await axios({
            method: "post",
            url: "/api/user/updateUser",
            data: requestData,
            params: { authorityIds: selectedAuthorityIds.value },
            withCredentials: true
          }).catch(error => {
            console.error('更新用户请求失败:', error);
            if (error.response && error.response.status === 401) {
              ElMessageBox.alert("认证已过期，请重新登录", "权限错误");
            } else {
              ElMessageBox.alert("网络错误或服务器不可用", "请求失败");
            }
            throw error; // 重新抛出错误以保持原有逻辑
          })
        }

        if (response.data.success) {
          ElMessage.success(response.data.msg)
          dialogVisible.value = false
          getUserPage() // 刷新用户列表
        } else {
          ElMessageBox.alert(response.data.msg, "操作失败")
        }
      } catch (error) {
        ElMessageBox.alert("系统错误，操作失败", "错误")
      }
    }
  } catch (error) {
    // 如果用户点击了取消按钮，会抛出 cancel 错误，我们忽略它
    if (error.message !== 'cancel') {
      console.log("表单校验失败，请检查填写内容");
      ElMessageBox.alert("表单校验失败，请检查填写内容", "提示")
    }
  }
}

// 9. 确认删除用户（逻辑删除）
const confirmDelete = async () => {
  try {
    const response = await axios({
      method: "post",
      url: `/api/user/deleteUser?id=${deleteUserId.value}`,
      withCredentials: true
    }).catch(error => {
      console.error('删除用户请求失败:', error);
      if (error.response && error.response.status === 401) {
        ElMessageBox.alert("认证已过期，请重新登录", "权限错误");
      } else {
        ElMessageBox.alert("网络错误或服务器不可用", "请求失败");
      }
      throw error; // 重新抛出错误以保持原有逻辑
    })
    if (response.data.success) {
      ElMessage.success(response.data.msg)
      deleteDialogVisible.value = false
      getUserPage() // 刷新用户列表
    } else {
      ElMessageBox.alert(response.data.msg, "删除失败")
    }
  } catch (error) {
    ElMessageBox.alert("系统错误，删除失败", "错误")
  }
}

</script>

<style scoped>
.user-manage-container {
  padding: 10px;
}

.page-header {
  margin-bottom: 10px;
}

/* 表格样式优化 */
.el-table {
  margin-bottom: 10px;
}

/* 对话框表单间距 */
.el-form-item {
  margin-bottom: 20px;
}
</style>
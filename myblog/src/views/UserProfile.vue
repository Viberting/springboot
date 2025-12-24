<template>
  <div class="user-profile">
    <!-- 个人信息头部 -->
    <div class="profile-header" :style="{ backgroundImage: 'url(' + profileForm.bgImage + ')' }">
      <div class="header-overlay"></div>
      <div class="profile-info">
        <div class="avatar-container">
          <img :src="profileForm.avatar" alt="用户头像" class="avatar" @click="editAvatar">
          <el-icon class="edit-avatar-icon"><Edit /></el-icon>
        </div>
        <h2>{{ profileForm.username }}</h2>
        <p v-if="profileForm.intro" class="user-intro">{{ profileForm.intro }}</p>
        <p v-else class="user-intro-placeholder">这个人很懒，还没有添加个人简介</p>
        
        <div class="user-stats">
          <div class="stat-item">
            <span class="stat-number">0</span>
            <span class="stat-label">文章</span>
          </div>
          <div class="stat-item">
            <span class="stat-number">0</span>
            <span class="stat-label">关注</span>
          </div>
          <div class="stat-item">
            <span class="stat-number">0</span>
            <span class="stat-label">粉丝</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 编辑按钮 -->
    <div class="edit-button-container">
      <el-button 
        v-if="isCurrentUser"
        type="primary" 
        round 
        @click="toggleEditMode"
        :icon="isEditing ? 'View' : 'Edit'"
      >
        {{ isEditing ? '查看资料' : '编辑资料' }}
      </el-button>
    </div>

    <!-- 详细信息 -->
    <div class="profile-details">
      <el-row :gutter="20">
        <el-col :span="16">
          <el-card class="info-card">
            <template #header>
              <div class="card-header">
                <span>基本信息</span>
              </div>
            </template>
            
            <div v-if="!isEditing" class="info-display">
              <el-descriptions :column="1" size="large">
                <el-descriptions-item label="用户名">{{ profileForm.username }}</el-descriptions-item>
                <el-descriptions-item label="邮箱">{{ profileForm.email }}</el-descriptions-item>
                <el-descriptions-item label="性别">{{ profileForm.gender === 1 ? '男' : profileForm.gender === 0 ? '女' : '未设置' }}</el-descriptions-item>
                <el-descriptions-item label="生日">{{ profileForm.birthday || '未设置' }}</el-descriptions-item>
                <el-descriptions-item label="GitHub">
                  <a v-if="profileForm.githubUrl" :href="profileForm.githubUrl" target="_blank">{{ profileForm.githubUrl }}</a>
                  <span v-else>未设置</span>
                </el-descriptions-item>
              </el-descriptions>
            </div>
            
            <!-- 用户统计信息 -->
            <div v-if="!isEditing" class="user-stats-display">
              <div class="stat-item">
                <span class="stat-number">{{ userStats.articles }}</span>
                <span class="stat-label">文章</span>
              </div>
              <div class="stat-item">
                <span class="stat-number">{{ userStats.following }}</span>
                <span class="stat-label">关注</span>
              </div>
              <div class="stat-item">
                <span class="stat-number">{{ userStats.followers }}</span>
                <span class="stat-label">粉丝</span>
              </div>
            </div>
            
            <div v-else class="info-edit">
              <el-form 
                ref="profileFormRef" 
                :model="profileForm" 
                :rules="profileRules" 
                label-width="100px"
                label-position="left"
              >
                <el-form-item label="用户名" prop="username">
                  <el-input v-model="profileForm.username" disabled />
                </el-form-item>
                
                <el-form-item label="邮箱" prop="email">
                  <el-input v-model="profileForm.email" />
                </el-form-item>
                
                <el-form-item label="性别">
                  <el-select v-model="profileForm.gender" placeholder="请选择性别" clearable>
                    <el-option label="男" :value="1" />
                    <el-option label="女" :value="0" />
                  </el-select>
                </el-form-item>
                
                <el-form-item label="出生日期">
                  <el-date-picker
                    v-model="profileForm.birthday"
                    type="date"
                    placeholder="选择出生日期"
                    format="YYYY-MM-DD"
                    value-format="YYYY-MM-DD"
                    style="width: 100%"
                  />
                </el-form-item>
                
                <el-form-item label="GitHub链接">
                  <el-input v-model="profileForm.githubUrl" placeholder="请输入GitHub链接" />
                </el-form-item>
              </el-form>
            </div>
          </el-card>
          
          <el-card class="intro-card" style="margin-top: 20px;">
            <template #header>
              <div class="card-header">
                <span>个人简介</span>
              </div>
            </template>
            
            <div v-if="!isEditing" class="intro-display">
              <p v-if="profileForm.intro">{{ profileForm.intro }}</p>
              <p v-else class="placeholder-text">还没有添加个人简介</p>
            </div>
            
            <div v-else class="intro-edit">
              <el-input 
                v-model="profileForm.intro" 
                type="textarea"
                :rows="4"
                placeholder="请输入个人简介"
              />
            </div>
          </el-card>
        </el-col>
        
        <el-col :span="8">
          <el-card class="security-card">
            <template #header>
              <div class="card-header">
                <span>安全设置</span>
              </div>
            </template>
            
            <div v-if="!isEditing" class="security-display">
              <p>修改密码</p>
              <p class="security-tip">出于安全考虑，密码已被隐藏</p>
            </div>
            
            <div v-else class="security-edit">
              <el-form 
                ref="passwordFormRef" 
                :model="passwordForm" 
                :rules="passwordRules"
              >
                <el-form-item label="原密码" prop="oldPassword">
                  <el-input 
                    v-model="passwordForm.oldPassword" 
                    type="password"
                    show-password
                  />
                </el-form-item>
                
                <el-form-item label="新密码" prop="newPassword">
                  <el-input 
                    v-model="passwordForm.newPassword" 
                    type="password"
                    show-password
                  />
                </el-form-item>
                
                <el-form-item label="确认密码" prop="confirmPassword">
                  <el-input 
                    v-model="passwordForm.confirmPassword" 
                    type="password"
                    show-password
                  />
                </el-form-item>
              </el-form>
            </div>
          </el-card>
          
          <el-card class="bg-card" style="margin-top: 20px;">
            <template #header>
              <div class="card-header">
                <span>背景图片</span>
              </div>
            </template>
            
            <div class="bg-display">
              <div class="bg-preview" @click="editBgImage">
                <img v-if="profileForm.bgImage" :src="profileForm.bgImage" alt="背景图片">
                <div v-else class="bg-placeholder">
                  <el-icon><Picture /></el-icon>
                  <p>点击设置背景图片</p>
                </div>
                <div v-if="isEditing" class="edit-bg-overlay">
                  <el-icon><Edit /></el-icon>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
    
    <!-- 操作按钮 -->
    <div v-if="isEditing && isCurrentUser" class="action-buttons">
      <el-button type="primary" @click="submitForm(profileFormRef)">保存修改</el-button>
      <el-button @click="cancelEdit">取消</el-button>
    </div>
    
    <!-- 用户文章列表 -->
    <div class="articles-section" v-if="!isEditing">
      <el-card class="articles-card">
        <template #header>
          <div class="card-header">
            <span>文章列表 ({{ userStats.articles }})</span>
          </div>
        </template>
        
        <div v-if="userArticles.length > 0" class="articles-list">
          <div v-for="article in userArticles" :key="article.id" class="article-item">
            <h3 class="article-title">
              <router-link :to="`/article_comment/${article.id}`">
                {{ article.title }}
              </router-link>
            </h3>
            <div class="article-meta">
              <span class="article-date">{{ formatDate(article.created) }}</span>
              <span class="article-hits">阅读 {{ article.hits || 0 }}</span>
            </div>
          </div>
          
          <div class="pagination-container" v-if="totalArticles > articlesPageSize">
            <el-pagination
              v-model:current-page="articlesPage"
              :page-size="articlesPageSize"
              :total="totalArticles"
              layout="prev, pager, next"
              @current-change="handleArticlesPageChange"
              small
            />
          </div>
        </div>
        <div v-else class="no-articles">
          <p v-if="articlesLoading">加载中...</p>
          <p v-else>暂无文章</p>
        </div>
      </el-card>
    </div>
    
    <!-- 文件上传组件 -->
    <el-dialog v-model="showUploadDialog" title="上传图片" width="400px" center>
      <el-upload
        ref="uploadRef"
        class="upload-demo"
        :auto-upload="false"
        :show-file-list="false"
        :on-change="handleFileChange"
        :before-upload="beforeUpload"
        accept="image/*"
        drag
      >
        <div v-if="!uploadFile" class="el-upload__text">
          <el-icon><Plus /></el-icon>
          <div class="el-upload__text">点击或拖拽上传</div>
        </div>
        <div v-else class="upload-preview">
          <img :src="previewUrl" alt="预览图片" style="max-width: 100%; max-height: 200px;">
          <p>{{ uploadFile.name }}</p>
        </div>
      </el-upload>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showUploadDialog = false">取消</el-button>
          <el-button type="primary" @click="confirmUpload" :disabled="!uploadFile">上传</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, inject, onMounted } from 'vue'
import { ElMessage, ElMessageBox, ElUpload } from 'element-plus'
import { Edit, Picture, View, Plus } from '@element-plus/icons-vue'
import { useStore } from '@/stores/my'
import { useRouter, useRoute } from 'vue-router'

const axios = inject('axios')
const profileFormRef = ref()
const passwordFormRef = ref()
const uploadRef = ref()
const store = useStore()
const router = useRouter()
const route = useRoute()

// 编辑模式状态
const isEditing = ref(false)

// 用户类型标识（当前用户/其他用户）
const isCurrentUser = ref(true)
const userId = ref(null)

// 用户统计信息
const userStats = reactive({
  articles: 0,
  following: 0,
  followers: 0
})

// 用户文章列表
const userArticles = ref([])
const articlesLoading = ref(false)
const articlesPage = ref(1)
const articlesPageSize = ref(5)
const totalArticles = ref(0)

// 上传相关状态
const showUploadDialog = ref(false)
const uploadFile = ref(null)
const previewUrl = ref('')
const uploadType = ref('') // 'avatar' or 'bg'

// 表单数据
const profileForm = reactive({
  username: '',
  email: '',
  avatar: '',
  intro: '',
  githubUrl: '',
  bgImage: '',
  gender: null,
  birthday: ''
})

// 密码表单数据
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 表单验证规则
const profileRules = reactive({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
})

// 密码验证规则
const passwordRules = reactive({
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度应在 6 到 20 个字符之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
})

// 确认密码验证函数
function validateConfirmPassword(rule, value, callback) {
  if (value && value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

// 格式化日期函数
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 切换编辑模式
const toggleEditMode = () => {
  isEditing.value = !isEditing.value
  if (!isEditing.value) {
    // 退出编辑模式时重置密码表单
    passwordFormRef.value?.resetFields()
  }
}

// 取消编辑
const cancelEdit = () => {
  isEditing.value = false
  getUserProfile() // 重新获取原始数据
}

// 点击头像编辑
const editAvatar = () => {
  if (isEditing.value && isCurrentUser.value) {
    uploadType.value = 'avatar'
    showUploadDialog.value = true
  }
}

// 点击背景图片编辑
const editBgImage = () => {
  if (isEditing.value && isCurrentUser.value) {
    uploadType.value = 'bg'
    showUploadDialog.value = true
  }
}

// 处理文件选择
const handleFileChange = (file) => {
  // 重置之前的预览
  if (previewUrl.value) {
    URL.revokeObjectURL(previewUrl.value)
  }
  
  // 创建新的预览URL
  previewUrl.value = URL.createObjectURL(file.raw)
  uploadFile.value = file.raw
}

// 上传前检查
const beforeUpload = (file) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'image/gif'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG) {
    ElMessage.error('图片只能是 JPG、PNG 或 GIF 格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

// 确认上传
const confirmUpload = async () => {
  if (!uploadFile.value) {
    ElMessage.error('请选择文件')
    return
  }

  const formData = new FormData()
  formData.append('file', uploadFile.value)
  formData.append('type', uploadType.value) // 添加类型标识

  try {
    // 使用后端提供的上传接口
    const response = await axios.post('/api/article/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })

    if (response.data.success) {
      // 更新对应的图片字段
      if (uploadType.value === 'avatar') {
        profileForm.avatar = response.data.map.url
        ElMessage.success('头像上传成功')
      } else if (uploadType.value === 'bg') {
        profileForm.bgImage = response.data.map.url
        ElMessage.success('背景图片上传成功')
      }
      
      // 关闭对话框并重置状态
      showUploadDialog.value = false
      uploadFile.value = null
      if (previewUrl.value) {
        URL.revokeObjectURL(previewUrl.value)
        previewUrl.value = ''
      }
    } else {
      ElMessage.error(response.data.msg || '上传失败')
    }
  } catch (error) {
    console.error('上传错误:', error)
    ElMessage.error('上传失败，请稍后重试')
  }
}

// 提交表单
const submitForm = async (formEl) => {
  if (!formEl) return
  
  // 只有当前用户才能更新信息
  if (!isCurrentUser.value) {
    ElMessage.warning('无法编辑其他用户的信息')
    return
  }
  
  // 验证基本信息
  await formEl.validate(async (valid, fields) => {
    if (valid) {
      // 如果有密码输入，则验证密码表单
      let passwordValid = true
      if (passwordForm.newPassword || passwordForm.oldPassword || passwordForm.confirmPassword) {
        passwordValid = await new Promise((resolve) => {
          passwordFormRef.value.validate((valid) => {
            resolve(valid)
          })
        })
      }
      
      if (passwordValid) {
        // 发送更新请求
        const updateData = { ...profileForm }
        
        // 如果有密码修改，添加密码字段
        if (passwordForm.newPassword) {
          updateData.oldPassword = passwordForm.oldPassword
          updateData.newPassword = passwordForm.newPassword
        }
        
        axios.put('/api/user/profile', updateData)
          .then(response => {
            if (response.data.success) {
              ElMessage.success('个人信息更新成功')
              isEditing.value = false
              // 更新store中的用户信息
              if (store.user.user) {
                Object.assign(store.user.user, profileForm)
              }
            } else {
              ElMessage.error(response.data.msg || '更新失败')
            }
          })
          .catch(error => {
            ElMessage.error('系统错误，请稍后重试')
            console.error(error)
          })
      } else {
        ElMessage.error('请填写正确的密码信息')
      }
    } else {
      ElMessage.error('请填写正确的表单信息')
    }
  })
}

// 获取当前用户信息
const getUserProfile = () => {
  axios.get('/api/user/profile')
    .then(response => {
      if (response.data.success) {
        const user = response.data.map.user
        profileForm.username = user.username
        profileForm.email = user.email
        profileForm.avatar = user.avatar || 'https://cube.elemecdn.com/3/7c/3ea3bd547d9a5f0d2a4f3c9c4b3d1png.png'
        profileForm.intro = user.intro
        profileForm.githubUrl = user.githubUrl
        profileForm.bgImage = user.bgImage || ''
        profileForm.gender = user.gender
        profileForm.birthday = user.birthday
        
        // 获取用户统计数据
        getUserStats(userId.value)
        // 获取用户文章列表
        getUserArticles(userId.value)
      } else {
        ElMessage.error(response.data.msg || '获取用户信息失败')
      }
    })
    .catch(error => {
      ElMessage.error('系统错误，请稍后重试')
      console.error(error)
    })
}

// 获取其他用户信息
const getUserProfileById = (userId) => {
  axios.get(`/api/user/${userId}`)
    .then(response => {
      if (response.data.success) {
        const user = response.data.map.user
        profileForm.username = user.username
        profileForm.email = '' // 不显示其他用户的邮箱
        profileForm.avatar = user.avatar || 'https://cube.elemecdn.com/3/7c/3ea3bd547d9a5f0d2a4f3c9c4b3d1png.png'
        profileForm.intro = user.intro
        profileForm.githubUrl = user.githubUrl
        profileForm.bgImage = user.bgImage || ''
        profileForm.gender = user.gender
        profileForm.birthday = user.birthday
        
        // 如果是其他用户，禁用编辑模式
        isEditing.value = false
        
        // 获取用户统计数据
        getUserStats(userId)
        // 获取用户文章列表
        getUserArticles(userId)
      } else {
        ElMessage.error(response.data.msg || '获取用户信息失败')
      }
    })
    .catch(error => {
      ElMessage.error('系统错误，请稍后重试')
      console.error(error)
    })
}

// 获取用户统计数据
const getUserStats = (userId) => {
  // 这里需要调用后端API获取用户统计数据
  // 目前先模拟数据
  userStats.articles = 0
  userStats.following = 0
  userStats.followers = 0
}

// 获取用户文章列表
const getUserArticles = (userId) => {
  articlesLoading.value = true
  axios.get(`/api/user/${userId}/articles`, {
    params: {
      page: articlesPage.value,
      size: articlesPageSize.value
    }
  })
  .then(response => {
    if (response.data.success) {
      userArticles.value = response.data.map.articles || []
      totalArticles.value = response.data.map.pageParams?.total || 0
      
      // 更新文章统计
      userStats.articles = totalArticles.value
    } else {
      ElMessage.error(response.data.msg || '获取用户文章失败')
    }
  })
  .catch(error => {
    ElMessage.error('系统错误，请稍后重试')
    console.error(error)
  })
  .finally(() => {
    articlesLoading.value = false
  })
}

// 切换文章分页
const handleArticlesPageChange = (page) => {
  articlesPage.value = page
  getUserArticles(userId.value)
}

// 组件挂载时获取用户信息
onMounted(() => {
  // 检查路由参数，确定是否查看其他用户
  if (route.params.userId) {
    userId.value = parseInt(route.params.userId)
    isCurrentUser.value = false
    // 查看其他用户信息，不需要登录检查
    getUserProfileById(userId.value)
  } else {
    // 检查用户是否已登录
    if (!store.user.user) {
      // 如果未登录，重定向到登录页面
      router.push('/login')
      return
    }
    isCurrentUser.value = true
    userId.value = store.user.user.id
    getUserProfile()
  }
})
</script>

<style scoped>
.user-profile {
  padding: 0;
  min-height: calc(100vh - 80px);
  background-color: #f5f5f5;
}

/* 个人信息头部 */
.profile-header {
  position: relative;
  height: 300px;
  background-size: cover;
  background-position: center;
  border-radius: 8px;
  margin: 20px;
  overflow: hidden;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.header-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(to bottom, rgba(0,0,0,0.3), rgba(0,0,0,0.7));
}

.profile-info {
  position: relative;
  z-index: 1;
  color: white;
  padding: 40px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  text-align: center;
}

.avatar-container {
  position: relative;
  cursor: pointer;
}

.avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  border: 3px solid white;
  object-fit: cover;
}

.edit-avatar-icon {
  position: absolute;
  bottom: 5px;
  right: 5px;
  background: white;
  border-radius: 50%;
  padding: 5px;
  color: #409eff;
  font-size: 16px;
  display: none;
}

.avatar-container:hover .edit-avatar-icon {
  display: block;
}

.user-intro {
  font-size: 16px;
  margin: 10px 0;
  max-width: 600px;
  line-height: 1.5;
}

.user-intro-placeholder {
  font-size: 16px;
  margin: 10px 0;
  color: rgba(255, 255, 255, 0.7);
  font-style: italic;
}

.user-stats {
  display: flex;
  margin-top: 20px;
}

.stat-item {
  margin: 0 20px;
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  display: block;
}

.stat-label {
  font-size: 14px;
  opacity: 0.9;
}

/* 编辑按钮 */
.edit-button-container {
  display: flex;
  justify-content: center;
  margin-top: -20px;
  z-index: 2;
  position: relative;
}

/* 详细信息区域 */
.profile-details {
  padding: 40px 20px 20px;
}

.info-card, .intro-card, .security-card, .bg-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.card-header {
  font-weight: bold;
  font-size: 16px;
}

.info-display, .intro-display, .security-display {
  padding: 10px 0;
}

.security-tip {
  color: #999;
  font-size: 14px;
  margin-top: 10px;
}

.placeholder-text {
  color: #999;
  font-style: italic;
}

/* 背景图片预览 */
.bg-preview {
  position: relative;
  cursor: pointer;
  border-radius: 4px;
  overflow: hidden;
  height: 180px;
}

.bg-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.bg-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  background-color: #f5f5f5;
  color: #999;
}

.bg-placeholder .el-icon {
  font-size: 36px;
  margin-bottom: 10px;
}

.edit-bg-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
  display: none;
}

.bg-preview:hover .edit-bg-overlay {
  display: flex;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  justify-content: center;
  padding: 20px;
  gap: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .profile-header {
    height: 200px;
  }
  
  .profile-info {
    padding: 20px;
  }
  
  .avatar {
    width: 80px;
    height: 80px;
  }
  
  .user-stats {
    margin-top: 10px;
  }
  
  .stat-item {
    margin: 0 10px;
  }
  
  .stat-number {
    font-size: 20px;
  }
  
  .profile-details {
    padding: 30px 10px 10px;
  }
  
  .action-buttons {
    padding: 15px 10px;
  }
}

/* 上传预览样式 */
.upload-preview {
  text-align: center;
}

.upload-preview img {
  border-radius: 4px;
  margin-bottom: 10px;
}

.upload-preview p {
  margin: 0;
  word-break: break-all;
}

/* 用户统计信息 */
.user-stats-display {
  display: flex;
  margin-top: 20px;
  padding: 15px 0;
  border-top: 1px solid #eee;
}

.user-stats-display .stat-item {
  flex: 1;
  text-align: center;
}

.user-stats-display .stat-number {
  font-size: 20px;
  font-weight: bold;
  display: block;
  color: #409eff;
}

.user-stats-display .stat-label {
  font-size: 14px;
  color: #666;
}

/* 文章列表 */
.articles-section {
  padding: 0 20px 20px;
}

.articles-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.article-item {
  padding: 15px 0;
  border-bottom: 1px solid #eee;
}

.article-item:last-child {
  border-bottom: none;
}

.article-title {
  margin: 0 0 8px 0;
  font-size: 16px;
}

.article-title a {
  color: #409eff;
  text-decoration: none;
}

.article-title a:hover {
  color: #66b1ff;
}

.article-meta {
  font-size: 12px;
  color: #999;
}

.article-meta span {
  margin-right: 15px;
}

.no-articles {
  text-align: center;
  padding: 30px 0;
  color: #999;
}

.pagination-container {
  text-align: center;
  margin-top: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .profile-header {
    height: 200px;
  }
  
  .profile-info {
    padding: 20px;
  }
  
  .avatar {
    width: 80px;
    height: 80px;
  }
  
  .user-stats {
    margin-top: 10px;
  }
  
  .stat-item {
    margin: 0 10px;
  }
  
  .stat-number {
    font-size: 20px;
  }
  
  .profile-details {
    padding: 30px 10px 10px;
  }
  
  .action-buttons {
    padding: 15px 10px;
  }
  
  .articles-section {
    padding: 0 10px 10px;
  }
  
  .article-title {
    font-size: 14px;
  }
}

/* 上传预览样式 */
.upload-preview {
  text-align: center;
}

.upload-preview img {
  border-radius: 4px;
  margin-bottom: 10px;
}

.upload-preview p {
  margin: 0;
  word-break: break-all;
}
</style>
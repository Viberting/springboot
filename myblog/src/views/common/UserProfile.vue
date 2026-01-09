<template>
  <div class="user-profile">
    <!-- 背景图（放大+层级修复） -->
    <div class="profile-header"
         :style="{ backgroundImage: 'url(' + (profileForm.bgImage || 'https://picsum.photos/1920/600') + ')' }"
         :class="{ 'bg-clickable': isEditing && isCurrentUser }">
      <div class="header-overlay" ></div>

      <!-- 返回主页按钮 -->
      <div class="back-home-btn" @click="router.push('/')">
        <el-button type="default" round icon="Back">返回主页</el-button>
      </div>

      <!-- 背景图编辑提示 -->
      <div v-if="isEditing && isCurrentUser" class="bg-edit-tip" @click.stop="openBgUploadDialog">
        <el-icon><Edit /></el-icon>
        <span>点击更换背景图</span>
      </div>

      <!-- 头像+用户名+数据统计 -->
      <div class="profile-info">
        <div class="avatar-container">
          <img :src="profileForm.avatar || 'https://cube.elemecdn.com/3/7c/3ea3bd547d9a5f0d2a4f3c9c4b3d1png.png'" alt="用户头像" class="avatar" @click.stop="editAvatar">
          <el-icon class="edit-avatar-icon"><Edit /></el-icon>
        </div>
        <h2 class="username">{{ profileForm.username }}</h2>
        <!-- 文章数+粉丝+关注（横向排列） -->
        <div class="user-stats-group">
          <div class="stat-item" >
            <span class="stat-number">{{ userStats.articles }}</span>
            <span class="stat-label">文章</span>
          </div>
          <div class="stat-item" @click="gotoFollowList('follow')"> <!-- 直接传字符串 -->
            <span class="stat-number">{{ userStats.following }}</span>
            <span class="stat-label">关注</span>
          </div>
          <div class="stat-item" @click="gotoFollowList('fans')"> <!-- 直接传字符串 -->
            <span class="stat-number">{{ userStats.followers }}</span>
            <span class="stat-label">粉丝</span>
          </div>
        </div>
      </div>

      <!-- 编辑/安全设置按钮 -->
      <div class="edit-button-container-top">
        <el-button
            v-if="isCurrentUser && !isEditing"
            type="primary"
            round
            @click="toggleEditMode"
        >
          编辑资料
        </el-button>
        <el-button
            v-if="isCurrentUser && isEditing"
            type="primary"
            round
            @click="toggleEditMode"
        >
          查看资料
        </el-button>
        <el-button
            v-if="isCurrentUser"
            type="default"
            round
            @click="openSecurityDialog"
            style="margin-left:10px"
        >
          安全设置
        </el-button>
      </div>
    </div>

    <!-- 合并：签名 + 基本信息 为一个卡片 -->
    <div class="profile-details">
      <el-row :gutter="20">
        <el-col :span="24">
          <el-card class="info-card">
            <!-- 卡片头部 -->
            <template #header>
              <div class="card-header">
                <span>个人信息</span>
              </div>
            </template>

            <!-- 签名区域（放在基本信息上方） -->
            <div class="signature-section">
              <span class="signature-label">签名：</span>
              <span v-if="profileForm.intro" class="signature-text">{{ profileForm.intro }}</span>
              <span v-else class="signature-placeholder">这个人很懒，还没有添加签名</span>
            </div>

            <!-- 基本信息区域 -->
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

            <!-- 编辑模式下的表单（包含签名+基本信息） -->
            <div v-else class="info-edit">
              <el-form ref="profileFormRef" :model="profileForm" :rules="profileRules" label-width="100px" label-position="left">
                <!-- 签名输入框 -->
                <el-form-item label="签名">
                  <el-input v-model="profileForm.intro" type="textarea" :rows="2" placeholder="请输入签名" />
                </el-form-item>

                <!-- 基本信息输入项 -->
                <el-form-item label="用户名" prop="username">
                  <el-input v-model="profileForm.username" />
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
                  <el-date-picker v-model="profileForm.birthday" type="date" placeholder="选择出生日期" format="YYYY-MM-DD" value-format="YYYY-MM-DD" style="width: 100%" />
                </el-form-item>
                <el-form-item label="GitHub链接">
                  <el-input v-model="profileForm.githubUrl" placeholder="请输入GitHub链接" />
                </el-form-item>
              </el-form>
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

    <!-- 文章列表 -->
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
              <router-link :to="`/article_comment/${article.id}`">{{ article.title }}</router-link>
            </h3>
            <div class="article-meta">
              <span class="article-date">{{ formatDate(article.created) }}</span>
              <span class="article-hits">阅读 {{ article.hits !== undefined ? article.hits : 0 }}</span>
            </div>
          </div>
          <div class="pagination-container" v-if="totalArticles > articlesPageSize">
            <el-pagination v-model:current-page="articlesPage" :page-size="articlesPageSize" :total="totalArticles" layout="prev, pager, next" @current-change="handleArticlesPageChange" small />
          </div>
        </div>
        <div v-else class="no-articles">
          <p v-if="articlesLoading">加载中...</p>
          <p v-else>暂无文章</p>
        </div>
      </el-card>
    </div>

    <!-- 上传弹窗 -->
    <el-dialog v-model="showUploadDialog" :title="uploadType === 'avatar' ? '上传头像' : '上传背景图'" width="400px" center>
      <el-upload ref="uploadRef" class="upload-demo" :auto-upload="false" :show-file-list="false" :on-change="handleFileChange" :before-upload="beforeUpload" accept="image/*" drag>
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

    <!-- 安全设置弹窗 -->
    <el-dialog v-model="showSecurityDialog" title="安全设置" width="400px" center>
      <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showSecurityDialog = false">取消</el-button>
          <el-button type="primary" @click="submitPassword">保存修改</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, inject, onMounted, onUnmounted, getCurrentInstance } from 'vue'
import { ElMessage, ElUpload } from 'element-plus'
import { Edit, Plus } from '@element-plus/icons-vue'
import { useStore } from '@/stores/my.js'
import { useRouter, useRoute } from 'vue-router'

const axios = inject('axios')
const profileFormRef = ref()
const passwordFormRef = ref()
const uploadRef = ref()
const store = useStore()
const router = useRouter()
const route = useRoute()
const instance = getCurrentInstance()
let cancelToken = null

// 安全设置弹窗
const showSecurityDialog = ref(false)
const isEditing = ref(false)
const isCurrentUser = ref(true)
const userId = ref(null)

// 用户统计
const userStats = reactive({
  articles: 0,
  following: 0,
  followers: 0
})

// 文章列表
const userArticles = ref([])
const articlesLoading = ref(false)
const articlesPage = ref(1)
const articlesPageSize = ref(5)
const totalArticles = ref(0)

// 上传相关
const showUploadDialog = ref(false)
const uploadFile = ref(null)
const previewUrl = ref('')
const uploadType = ref('')

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

// 密码表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 验证规则
const profileRules = reactive({
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }, { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }],
  email: [{ required: true, message: '请输入邮箱地址', trigger: 'blur' }, { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }]
})

const passwordRules = reactive({
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }, { min: 6, max: 20, message: '密码长度应在 6 到 20 个字符之间', trigger: 'blur' }],
  confirmPassword: [{ required: true, message: '请确认新密码', trigger: 'blur' }, { validator: validateConfirmPassword, trigger: 'blur' }]
})

// 确认密码验证
function validateConfirmPassword(rule, value, callback) {
  if (value && value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// 切换编辑模式
const toggleEditMode = () => {
  isEditing.value = !isEditing.value
  if (!isEditing.value && passwordFormRef.value) {
    passwordFormRef.value.resetFields()
  }
}

// 取消编辑
const cancelEdit = () => {
  isEditing.value = false
  getUserProfile()
}

// 更换头像
const editAvatar = () => {
  if (isEditing.value && isCurrentUser.value) {
    uploadType.value = 'avatar'
    showUploadDialog.value = true
  }
}

// 更换背景图
const openBgUploadDialog = () => {
  // 打印状态，方便你在控制台看具体值
  console.log('点击更换背景图：', {
    isEditing: isEditing.value,
    isCurrentUser: isCurrentUser.value
  });

  // 必须同时满足“编辑模式+自己的主页”才弹窗
  if (isEditing.value && isCurrentUser.value) {
    uploadType.value = 'bg'
    showUploadDialog.value = true
  } else if (!isEditing.value && isCurrentUser.value) {
    // 给用户提示，避免误以为没反应
    ElMessage.info('请先点击右上角「编辑资料」进入编辑模式');
  } else {
    ElMessage.info('无法编辑他人的背景图');
  }
}

// 打开安全设置弹窗
const openSecurityDialog = () => {
  showSecurityDialog.value = true
}

// 提交密码修改（核心：对接后端接口）
const submitPassword = async () => {
  if (!passwordFormRef.value) return;
  passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const res = await axios.post(
            '/api/user/updatePassword',
            {
              oldPassword: passwordForm.oldPassword,
              newPassword: passwordForm.newPassword
            },
            { withCredentials: true }
        );
        if (res.data.success) {
          ElMessage.success('密码修改成功，请重新登录');
          showSecurityDialog.value = false;
          passwordFormRef.value.resetFields();

          store.clearUser(); // 清空 Pinia 中的用户状态
          router.push('/login'); // 跳转到登录页
        } else {
          ElMessage.error(res.data.msg || '密码修改失败');
        }
      } catch (err) {
        ElMessage.error('系统错误，请稍后重试');
        console.error('密码修改失败:', err);
      }
    } else {
      ElMessage.error('请填写正确的密码信息');
    }
  });
};

// 处理文件上传
const handleFileChange = (file) => {
  if (previewUrl.value) URL.revokeObjectURL(previewUrl.value)
  previewUrl.value = URL.createObjectURL(file.raw)
  uploadFile.value = file.raw
}
const beforeUpload = (file) => {
  const isImage = file.type.includes('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isImage) ElMessage.error('只能上传图片格式！')
  if (!isLt2M) ElMessage.error('图片大小不能超过 10MB！')
  return isImage && isLt2M
}
const confirmUpload = async () => {
  if (!uploadFile.value) return ElMessage.error('请选择文件')
  const formData = new FormData()
  formData.append('file', uploadFile.value)
  formData.append('type', uploadType.value)
  try {
    const res = await axios.post('/api/user/uploadImage', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      withCredentials: true
    })
    if (res.data.success) {
      if (uploadType.value === 'avatar') profileForm.avatar = res.data.map.url
      if (uploadType.value === 'bg') profileForm.bgImage = res.data.map.url
      ElMessage.success(`${uploadType.value === 'avatar' ? '头像' : '背景图'}上传成功`)
      showUploadDialog.value = false
      uploadFile.value = null
      previewUrl.value && URL.revokeObjectURL(previewUrl.value)
      previewUrl.value = ''
      if (store.user.user) Object.assign(store.user.user, profileForm)
    } else {
      ElMessage.error(res.data.msg || '上传失败')
    }
  } catch (err) {
    ElMessage.error('上传失败，请稍后重试')
    console.error('上传错误:', err)
  }
}

// 提交资料修改
const submitForm = async (formEl) => {
  if (!formEl || !isCurrentUser.value) return
  await formEl.validate(async (valid) => {
    if (valid) {
      try {
        const res = await axios.post('/api/user/updateProfile', profileForm, { withCredentials: true })
        if (res.data.success) {
          ElMessage.success('资料更新成功')
          isEditing.value = false
          if (store.user.user) Object.assign(store.user.user, profileForm)
        } else {
          ElMessage.error(res.data.msg || '更新失败')
        }
      } catch (err) {
        ElMessage.error('系统错误，请稍后重试')
        console.error(err)
      }
    } else {
      ElMessage.error('请填写正确的表单信息')
    }
  })
}

// ========== 修复1：完整赋值所有用户信息字段 ==========
const getUserProfile = async () => {
  if (cancelToken) cancelToken.cancel('取消请求')
  cancelToken = axios.CancelToken.source()
  try {
    const res = await axios.get('/api/user/profile', {
      cancelToken: cancelToken.token,
      withCredentials: true
    })
    if (!instance) return
    console.log("getUserProfile返回：", res.data) // 新增：打印返回值，方便调试
    if (res.data.success) {
      const user = res.data.map.userProfile
      // 完整赋值所有字段，包括bgImage/gender/birthday/githubUrl
      profileForm.username = user.username || ''
      profileForm.email = user.email || ''
      profileForm.avatar = user.avatar || ''
      profileForm.intro = user.intro || ''
      profileForm.githubUrl = user.githubUrl || ''
      profileForm.bgImage = user.bgImage || ''
      // 兼容后端返回字符串的情况，转为数字
      profileForm.gender = user.gender !== null ? Number(user.gender) : null
      profileForm.birthday = user.birthday || ''
    } else {
      ElMessage.error(res.data.msg || '获取用户信息失败')
    }
  } catch (err) {
    if (!axios.isCancel(err) && instance) ElMessage.error('系统错误，请稍后重试')
  }
}

// ========== 修复2：完整赋值其他用户信息字段 ==========
const getUserProfileById = async (targetUserId) => {
  if (cancelToken) cancelToken.cancel('取消请求')
  cancelToken = axios.CancelToken.source()
  try {
    const res = await axios.get(`/api/user/selectById/${targetUserId}`, {
      cancelToken: cancelToken.token,
      withCredentials: true
    })
    if (!instance) return
    console.log("getUserProfileById返回：", res.data) // 新增：打印返回值
    if (res.data.success) {
      const user = res.data.map.user
      profileForm.username = user.username || ''
      profileForm.email = '不可查看喔~' // 其他用户隐藏邮箱
      profileForm.avatar = user.avatar || ''
      profileForm.intro = user.intro || ''
      profileForm.githubUrl = user.githubUrl || ''
      profileForm.bgImage = user.bgImage || ''
      profileForm.gender = user.gender !== null ? Number(user.gender) : null
      profileForm.birthday = user.birthday || ''
    } else {
      ElMessage.error(res.data.msg || '获取用户信息失败')
    }
  } catch (err) {
    if (!axios.isCancel(err) && instance) ElMessage.error('系统错误，请稍后重试')
  }
}

// ========== 修复3：增强fullStats接口调试，兼容字段类型 ==========
const getUserStats = async (targetUserId) => {
  if (cancelToken) cancelToken.cancel('取消请求')
  cancelToken = axios.CancelToken.source()
  try {
    const res = await axios.get(`/api/user/${targetUserId}/fullStats`, {
      cancelToken: cancelToken.token,
      withCredentials: true
    })
    console.log("fullStats返回：", res.data) // 强制打印，确认后端返回值
    if (res.data.success) {
      // 兼容后端返回字符串的情况，转为数字
      userStats.articles = res.data.map.articleCount ? Number(res.data.map.articleCount) : 0
      userStats.following = res.data.map.followCount ? Number(res.data.map.followCount) : 0
      userStats.followers = res.data.map.fansCount ? Number(res.data.map.fansCount) : 0
    } else {
      Object.assign(userStats, { articles: 0, following: 0, followers: 0 })
      ElMessage.warning('获取统计数据失败，使用默认值')
    }
  } catch (err) {
    Object.assign(userStats, { articles: 0, following: 0, followers: 0 })
    console.error("fullStats接口错误：", err)
    ElMessage.error('获取统计数据失败')
  }
}

// ========== 修复4：文章列表逻辑不变，保留调试日志 ==========
const getUserArticles = async (targetUserId) => {
  if (cancelToken) cancelToken.cancel('取消请求')
  cancelToken = axios.CancelToken.source()
  articlesLoading.value = true
  try {
    const res = await axios.get(`/api/user/${targetUserId}/articles`, {
      params: { page: articlesPage.value, size: articlesPageSize.value },
      cancelToken: cancelToken.token,
      withCredentials: true
    })
    console.log("用户文章数据：", res.data);
    if (res.data.success) {
      userArticles.value = res.data.map.articles || []
      userArticles.value.forEach(article => {
        console.log(`文章${article.id}阅读量：`, article.hits);
      });
      totalArticles.value = res.data.map.pageParams?.total || 0
      userStats.articles = totalArticles.value
    } else {
      ElMessage.error(res.data.msg || '获取用户文章失败')
    }
  } catch (err) {
    if (!axios.isCancel(err) && instance) ElMessage.error('系统错误，请稍后重试')
  } finally {
    if (instance) articlesLoading.value = false
  }
}

// 分页切换
const handleArticlesPageChange = (page) => {
  articlesPage.value = page
  getUserArticles(userId.value)
}

// 个人主页的 script 中新增
const gotoFollowList = (tab = 'follow') => {
  router.push({
    path: '/user/follow',
    query: {
      userId: userId.value,
      activeTab: tab // 原逻辑正确，但实际调用时可能传了事件对象
    }
  })
}

// ========== 修复5：确保接口调用顺序，增加userId空值检查 ==========
onMounted(async () => {
  // 先检查登录状态
  if (!store.user.user && !route.params.userId) {
    router.push('/login')
    return
  }

  if (route.params.userId) {
    userId.value = parseInt(route.params.userId)
    if (isNaN(userId.value)) { // 检查userId是否有效
      ElMessage.error('用户ID无效')
      router.push('/')
      return
    }
    isCurrentUser.value = false // 访问他人主页，设为false
    await getUserProfileById(userId.value)
    await getUserStats(userId.value) // 改为await，确保统计数据先加载
    await getUserArticles(userId.value)
  } else {
    isCurrentUser.value = true  // 访问自己主页，设为true
    userId.value = store.user.user.id
    await getUserProfile()
    await getUserStats(userId.value) // 改为await
    await getUserArticles(userId.value)
  }
  console.log("最终userId：", userId.value) // 确认userId是否正确
  console.log("最终profileForm：", profileForm) // 确认字段是否赋值
  console.log("最终userStats：", userStats) // 确认统计数据是否赋值
})

// 卸载
onUnmounted(() => {
  if (cancelToken) cancelToken.cancel('组件卸载')
  if (previewUrl.value) URL.revokeObjectURL(previewUrl.value)
})
</script>

<style scoped>
.user-profile {
  padding: 0;
  min-height: calc(100vh - 80px);
  background-color: #f5f5f5;
}

/* 返回主页按钮 */
.back-home-btn {
  position: absolute;
  top: 20px;
  left: 20px;
  z-index: 4;
}

/* 核心：重置全局样式 + 自定义灰色按钮 */
.back-home-btn .el-button {
  background: #e5e5e5 !important; /* 浅灰色底色 */
  color: #333 !important; /* 深灰色文字 */
  border: 1px solid #dcdcdc !important; /* 灰色边框 */
  cursor: pointer;
  border-radius: 20px !important; /* 强制统一圆角（和 round 按钮匹配） */
}

/* hover 效果：加深灰色，提升交互感 */
.back-home-btn .el-button:hover {
  background: #d9d9d9 !important;
  border-color: #ccc !important;
  color: #111 !important;
}

/* 背景图 */
.profile-header {
  position: relative;
  height: 350px;
  background-size: cover;
  background-position: center;
  border-radius: 8px;
  margin: 20px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
}

.header-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(to bottom, rgba(0,0,0,0.2), rgba(0,0,0,0.6));
  z-index: 1;
}

/* 头像+用户名+数据统计 */
.profile-info {
  position: relative;
  z-index: 3;
  color: #fff;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
}

.avatar-container {
  position: relative;
  cursor: pointer;
}

.avatar {
  width: 130px;
  height: 130px;
  border-radius: 50%;
  border: 4px solid #fff;
  object-fit: cover;
}

.edit-avatar-icon {
  position: absolute;
  bottom: 5px;
  right: 5px;
  background: #fff;
  border-radius: 50%;
  padding: 6px;
  color: #409eff;
  font-size: 18px;
  display: none;
}

.avatar-container:hover .edit-avatar-icon {
  display: block;
}

.username {
  font-size: 28px;
  margin: 15px 0 8px;
  font-weight: 600;
}

/* 数据统计组 */
.user-stats-group {
  display: flex;
  gap: 30px;
  color: #fff;
  margin-top: 10px;
}

.user-stats-group .stat-item {
  text-align: center;
}

.user-stats-group .stat-number {
  font-size: 22px;
  font-weight: 700;
  display: block;
}

.user-stats-group .stat-label {
  font-size: 14px;
  opacity: 0.9;
}

/* 编辑/安全设置按钮 */
.edit-button-container-top {
  position: absolute;
  top: 20px;
  right: 20px;
  z-index: 3;
  display: flex;
  gap: 10px;
}

/* 合并后的个人信息卡片 */
.profile-details {
  padding: 0 20px 20px;
}

.info-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
  margin-bottom: 20px;
}

.card-header {
  font-weight: 600;
  font-size: 16px;
  color: #333;
}

/* 签名区域（卡片内） */
.signature-section {
  padding: 15px 0;
  text-align: center;
  font-size: 18px;
  border-bottom: 1px solid #eee; /* 签名和基本信息之间加分割线 */
  margin-bottom: 15px;
}

.signature-label {
  font-weight: 600;
  color: #333;
  margin-right: 8px;
}

.signature-text {
  color: #666;
}

.signature-placeholder {
  color: #999;
  font-style: italic;
}

/* 基本信息区域 */
.info-display {
  padding: 0 10px 10px;
}

/* 编辑模式下的表单 */
.info-edit {
  padding: 10px;
}

/* 操作按钮 */
.action-buttons {
  display: flex;
  justify-content: center;
  padding: 20px;
  gap: 20px;
}

/* 文章列表 */
.articles-section {
  padding: 0 20px 20px;
}

.articles-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
}

.article-item {
  padding: 15px 0;
  border-bottom: 1px solid #eee;
}

.article-title a {
  color: #409eff;
  text-decoration: none;
}

.article-title a:hover {
  color: #66b1ff;
}

.article-meta {
  color: #999;
  font-size: 14px;
  margin-top: 5px;
  display: flex;
  gap: 15px;
}

.pagination-container {
  text-align: center;
  margin-top: 20px;
}

.no-articles {
  text-align: center;
  padding: 40px 0;
  color: #999;
}

/* 背景图编辑提示 */
.bg-edit-tip {
  position: absolute;
  bottom: 20px;
  left: 20px;
  z-index: 4;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #fff;
  background: rgba(0,0,0,0.5);
  padding: 6px 12px;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  pointer-events: auto; /* 强制允许点击，覆盖父元素的点击限制 */
  user-select: none; /* 防止文字选中干扰点击 */
}

.bg-edit-tip:hover {
  background: rgba(0,0,0,0.7); /* 新增：hover 加深背景，增强反馈 */
}

.bg-clickable {
  cursor: pointer;
}

/* 响应式 */
@media (max-width: 768px) {
  .profile-header {
    height: 280px;
  }
  .user-stats-group {
    gap: 15px;
  }
  .user-stats-group .stat-number {
    font-size: 18px;
  }
  .avatar {
    width: 100px;
    height: 100px;
  }
  .username {
    font-size: 22px;
  }
  .edit-button-container-top {
    flex-direction: column;
    gap: 5px;
    top: 15px;
    right: 15px;
  }
  .edit-button-container-top .el-button {
    padding: 5px 10px;
    font-size: 12px;
  }
  .signature-section {
    font-size: 16px;
  }
}
</style>
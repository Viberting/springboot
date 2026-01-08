<template>
  <div class="follow-page">
    <!-- 新增：返回个人中心按钮 -->
    <div class="back-home-btn" style="margin-bottom: 20px;">
      <el-button type="default" round icon="House" @click="gotoProfile">返回个人中心</el-button>
    </div>

    <!-- 标签切换（关注/粉丝） -->
    <div class="tab-header">
      <div
          class="tab-item"
          :class="{ active: activeTab === 'follow' }"
          @click="activeTab = 'follow'"
      >
        关注
      </div>
      <div
          class="tab-item"
          :class="{ active: activeTab === 'fans' }"
          @click="activeTab = 'fans'"
      >
        粉丝
      </div>
    </div>

    <!-- 关注列表 -->
    <div v-if="activeTab === 'follow'">
      <div class="list-item" v-for="item in followList" :key="item.id">
        <!-- 头像+用户名+简介 -->
        <div class="user-info">
          <el-avatar
              :src="item.avatar || '/default-avatar.png'"
              size="small"
              style="cursor: pointer"
              @click="gotoUserProfile(item.id)"
          />
          <div class="text">
            <div class="username">{{ item.username }}</div>
            <div class="intro">{{ item.intro || '这个人很懒，还没有添加签名~' }}</div>
          </div>
        </div>
        <!-- 已关注按钮（点击选择取消关注） -->
        <el-button
            type="default"
            size="small"
            @click="unfollow(item.id)"
        >
          已关注
        </el-button>
      </div>
      <div v-if="followList.length === 0" class="empty">暂无关注</div>
    </div>

    <!-- 粉丝列表 -->
    <div v-if="activeTab === 'fans'">
      <div class="list-item" v-for="item in fansList" :key="item.id">
        <!-- 头像+用户名+简介 -->
        <div class="user-info">
          <el-avatar
              :src="item.avatar || '/default-avatar.png'"
              size="small"
              style="cursor: pointer"
              @click="gotoUserProfile(item.id)"
          />
          <div class="text">
            <div class="username">{{ item.username }}</div>
            <div class="intro">{{ item.intro || '这个人很懒，还没有添加签名~' }}</div>
          </div>
        </div>
        <!-- 回关/已互粉按钮 -->
        <el-button
            :type="item.isFollowed ? 'default' : 'primary'"
            size="small"
            @click="item.isFollowed ? unfollow(item.id) : followBack(item.id)"
        >
          {{ item.isFollowed ? '已互粉' : '回关' }}
        </el-button>
      </div>
      <div v-if="fansList.length === 0" class="empty">暂无粉丝</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, inject, watch} from 'vue'
import { useStore } from '@/stores/my'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus' // 补充导入
import { House } from '@element-plus/icons-vue'

const axios = inject('axios')
const store = useStore()
const route = useRoute()
const router = useRouter()

const gotoProfile = () => {
  // 如果是他人的关注/粉丝列表，返回该用户主页；否则返回自己主页
  if (currentUserId.value && currentUserId.value !== store.user.user?.id) {
    router.push({
      name: 'otherUserProfile',
      params: { userId: currentUserId.value }
    })
  } else {
    router.push('/user_profile')
  }
}

// 跳转到用户个人主页
const gotoUserProfile = (userId) => {
  if (!userId) return
  router.push({
    name: 'otherUserProfile',
    params: { userId }
  })
}

// 从路由参数获取标签和用户ID
const activeTab = ref(route.query.activeTab || 'follow') // 从路由获取默认标签
const followList = ref([]) // 关注列表
const fansList = ref([]) // 粉丝列表
// 优先从路由获取userId，否则用当前登录用户ID
const currentUserId = ref(route.query.userId || store.user.user?.id) // 从路由获取用户ID

// 获取关注列表
const getFollowList = async () => {
  if (!currentUserId.value) return
  try {
    const res = await axios.get(`/api/follow/list?userId=${currentUserId.value}`, {
      withCredentials: true
    })
    if (res.data.success) {
      followList.value = res.data.map.follows || [] // 兼容后端返回null
    }
  } catch (err) {
    ElMessage.error('获取关注列表失败')
  }
}

// 获取粉丝列表
const getFansList = async () => {
  if (!currentUserId.value) return // 新增空值校验
  try {
    const res = await axios.get(`/api/follow/fans?userId=${currentUserId.value}`, {
      withCredentials: true
    })
    if (res.data.success) {
      fansList.value = res.data.map.fans || [] // 兼容后端返回null
    }
  } catch (err) {
    ElMessage.error('获取粉丝列表失败')
  }
}

// 取消关注
const unfollow = async (followedId) => {
  if (!currentUserId.value || !followedId) return

  try {
    // 弹窗确认
    await ElMessageBox.confirm(
        '此操作将取消关注该用户, 是否继续?',
        '提示',
        { type: 'warning' }
    )

    // 完全复用文章详情页的参数名（authorId）
    const res = await axios({
      method: 'post',
      url: '/api/follow/unfollowAuthor',
      params: { authorId: followedId }, // 改为和文章详情页一致的authorId
      withCredentials: true
    })

    if (res.data.success) {
      ElMessage.success('取消关注成功!')
      activeTab.value === 'follow' ? getFollowList() : getFansList()
    }
  } catch (err) {
    if (err === 'cancel') {
      ElMessage.info('已取消操作')
    } else {
      ElMessage.error('取消关注失败')
    }
  }
}

// 回关粉丝
const followBack = async (followedId) => {
  if (!currentUserId.value || !followedId) return // 新增空值校验
  try {
    const res = await axios.post(
        '/api/follow/followBack',
        { followerId: currentUserId.value, followedId },
        { withCredentials: true }
    )
    if (res.data.success) {
      ElMessage.success('回关成功')
      getFansList() // 刷新粉丝列表
    }
  } catch (err) {
    ElMessage.error('回关失败')
  }
}

// 切换标签时刷新对应列表
watch(activeTab, (newTab) => {
  newTab === 'follow' ? getFollowList() : getFansList()
})

// 初始化时根据路由参数加载对应列表
onMounted(() => {
  if (!currentUserId.value) {
    ElMessage.warning('用户ID无效，返回个人主页')
    router.push('/user_profile')
    return
  }
  activeTab.value === 'follow' ? getFollowList() : getFansList()
})
</script>

<style scoped>
.back-home-btn .el-button {
  background: #409eff;
  color: #fff;
  border: none;
}

.back-home-btn .el-button:hover {
  background: #66b1ff;
}
.follow-page {
  padding: 20px;
  background-color: #f5f5f5; /* 改为个人主页的浅色底色 */
  color: #333; /* 文字改为深色，方便阅读 */
  min-height: calc(100vh - 80px);
}
.tab-header {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  border-bottom: 1px solid #ddd; /* 分割线改为浅色 */
  padding-bottom: 10px;
}
.tab-item {
  cursor: pointer;
  font-size: 16px;
  padding: 0 5px;
}
.tab-item.active {
  color: #409eff; /* 改为个人主页的主色 */
  border-bottom: 2px solid #409eff;
}
.list-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 0;
  border-bottom: 1px solid #eee; /* 分割线改为浅色 */
  background-color: #fff; /* 列表项加白色背景，和个人主页一致 */
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 10px;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}
.text .username {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}
.text .intro {
  font-size: 12px;
  color: #666; /* 简介改为灰色 */
  margin-top: 2px;
}
.empty {
  text-align: center;
  padding: 40px 0;
  color: #999;
  background-color: #fff;
  border-radius: 8px;
  padding: 20px;
}
</style>

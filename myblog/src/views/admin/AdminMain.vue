<script setup>
// 引入
import { provide, reactive, inject } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from '@/stores/my'
import { ElMessage } from 'element-plus'
//引入菜单项使用的图标，使用下拉菜单
import { View,DArrowRight} from '@element-plus/icons-vue'

// 创建一个响应式对象aside_state，它有两个属性。{}内为json格式
const aside_state = reactive({collapse:false,width:"200px"})
// toggleCollapse函数的功能：图标切换及切换后的效果
function toggleCollapse(){
  aside_state.collapse = !aside_state.collapse
  //切换左侧栏的宽度
  if(aside_state.collapse){
    aside_state.width="70px"
  }else{
    aside_state.width="200px"
  }
}

const router = useRouter()
const store = useStore()
const axios = inject('axios')

function editArticle(){ //跳转至编辑文章
  router.push({name:"publishArticle"})
}
provide("editArticle",editArticle)

function gotoArticleManage(){ //跳转至文章管理(文章列表)
  router.push({name:"manageArticle"})
}
provide("gotoArticleManage",gotoArticleManage)

// 用户退出登录功能
function logout() {
  axios({
    method: 'post',
    url: '/api/logout'
  }).then(() => {
    // 显示退出成功的消息提示
    ElMessage.success('退出成功')
    // 清除用户状态
    store.user.user = null
    // 跳转到博客首页（与普通用户退出一样）
    router.push({ name: 'home' })
  }).catch(() => {
    // 即使请求出错也清除本地用户信息并跳转到首页
    ElMessage.success('退出成功')
    store.user.user = null
    router.push({ name: 'home' })
  })
}
</script>

<template>
  <div class="common-layout">
    <el-container>
      <el-container>
        <el-aside :width="aside_state.width">
          <el-row>
            <el-col class="left-top">
              <img src="@/assets/bloglogo.jpg" width="50" height="50" />
              <!-- v-if在收缩状态时，隐藏文字 -->
              <span v-if="!aside_state.collapse" class="big-text">MyBlog</span>
            </el-col>
          </el-row>
          <el-row>
            <el-col>
              <!-- collapse通过响应式对象aside_state的collapse属性来控制缩放的状态 -->
              <!-- collapse-transition缩放时不需要自带的动画效果 -->
              <el-menu router active-text-color="white" background-color="#545c64" text-color="#a7b1c2"
                :collapse="aside_state.collapse"
                :collapse-transition="false">
                <el-menu-item index="/admin_Main/dashboard">
                  <el-icon>
                    <odometer />
                  </el-icon>
                  <span>仪表盘</span>
                </el-menu-item>
                <el-menu-item index="/admin_Main/publish_article">
                  <el-icon>
                    <Edit />
                  </el-icon>
                  <span>发表文章</span>
                </el-menu-item>
                <el-menu-item index="/admin_Main/manage_article">
                  <el-icon>
                    <Memo />
                  </el-icon>
                  <span>文章管理</span>
                </el-menu-item>
                <el-menu-item index="/admin_Main/comments">
                  <el-icon>
                    <Comment />
                  </el-icon>
                  <span>评论管理</span>
                </el-menu-item>
                <el-menu-item>
                  <el-icon>
                    <Filter />
                  </el-icon>
                  <span>分类/标签</span>
                </el-menu-item>
                <el-menu-item>
                  <el-icon>
                    <Setting />
                  </el-icon>
                  <span>系统设置</span>
                </el-menu-item>
              </el-menu>
            </el-col>
          </el-row>
        </el-aside>

        <el-main style="padding:0">
          <el-row align="middle" style="height:70px;" justify="space-between">
            <el-col :span="4">
              <span @click="toggleCollapse" class="toggle-menu">
                <el-icon :size="30">
                  <!-- 当条件为真时，对应的图标才显示 -->
                  <Fold v-if="!aside_state.collapse" />
                  <Expand v-if="aside_state.collapse" />
                </el-icon>
              </span>
            </el-col>
            <el-col :span="1">
              <el-dropdown trigger="click" style="crusor:pointer">
                <span class="el-dropdown_link">
                  <img src="@/assets/me.jpg" class="img-circle" />
                </span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item :icon="View" style="color:black" @click="$router.push('/')">查看网站</el-dropdown-item>
                    <el-dropdown-item :icon="DArrowRight" style="color:black" @click="logout">用户注销</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </el-col>
          </el-row>
          <el-row>
            <el-col>
              <RouterView />
            </el-col>
          </el-row>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>
<style scoped>
/* 让侧边栏整体采用纵向flex布局，内容居中，宽度变化时有动画过渡 */
.el-aside {
  display: flex;
  flex-direction: column;
  /* 展开时让内容铺满宽度以便左对齐（收缩时被下面的规则覆盖为居中） */
  align-items: stretch;
  padding: 0;
  transition: width 0.2s;
  /* 让侧栏高度随内容增长（不强制限制高度），并取消内部滚动（覆盖 element-plus 的默认样式） */
  height: auto !important;
  overflow: visible !important;
}

/* 菜单整体宽度铺满侧边栏，去掉右边框，背景色设置，宽度变化时有动画 */
.el-menu {
  width: 100%;
  border-right: none;
  background: #545c64;
  transition: width 0.2s;
  /* 去掉顶部内边距，保证菜单从侧栏顶部紧贴开始，使第一项与 logo 底部对齐 */
  padding-top: 0;
}

/* 收缩状态下，logo区域和菜单宽度都变为70px，内容居中 */
.el-aside[style*="width: 70px"] .left-top,
.el-aside[style*="width: 70px"] .el-menu {
  width: 70px !important;
  justify-content: center;
  align-items: center;
}

/* 菜单项采用flex居中，统一高度，保证图标和文字垂直对齐 */
.el-menu-item {
  display: flex;
  align-items: center;
  /* 展开时左对齐内容，图标与文字对齐至同一左侧线 */
  justify-content: flex-start;
  height: 50px;
  padding-left: 16px;
}


.img-circle{
  /* 以圆来显示图片 */
  border-radius: 50%;
  height: 36px;
  width: 36px;
}

.toggle-menu{
  margin-left: 5px;
  color: darkgray;
}

.toggle-menu:hover{
  color: black;
  cursor: pointer;
}

.big-text{
  margin-left: 10px;
  font-size: 20px;
  color: #2f353f;
}

/* logo区域，横向排列，内容居中，宽度和高度与侧边栏一致，背景色设置 */
.left-top{
  /* 默认展开状态：左对齐，使 logo 与菜单项在水平方向对齐 */
  display: flex;
  justify-content: flex-start;
  align-items: center;

  height: 70px;
  width: 100%;
  background-color:#eee ;
  flex-direction: row;
  transition: width 0.2s;
  padding-left: 12px;
}
</style>
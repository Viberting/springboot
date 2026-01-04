<script setup>
// 导入Element Plus的图标组件
import{ref,inject} from 'vue'
import { useRouter } from 'vue-router'//引入inject。通过它使用祖先组件provide的资源（函数，响应式变量等）
import { useStore } from '@/stores/my'
import { ElMessageBox } from 'element-plus'

const size = ref(30)
const toLogin=inject("toLogin")

const store = useStore()
const userName = ref('')
const isLogined = ref(false)
if(store.user.user!=null){//如果时已登录，显示用户名称
  userName.value="hi! "+store.user.user.username
  isLogined.value=true//控制登录，退出的显示
}

const toHome=inject("toHome")
const axios = inject('axios')
const router = useRouter()

function toProfile() {
  router.push('/user_profile')
}

function toExit(){
  axios({
    method: 'post',
    url: '/api/logout'//要与Security设置的退出网址一致
  }).then((response) => {
    ElMessageBox.alert(response.data.msg, '结果')
    store.user.user=null//清除状态user
    isLogined.value=false
    userName.value=""
    toHome()
  }).catch((err) => {//请求失败返回的数据
    ElMessageBox.alert("系统错误！", '结果')
  });
}
</script>

<template>
  <el-row class="top" align="middle">
    <el-col :span="3"></el-col>
    <el-col :span="12">
      <a href="/">CrazyStone个人博客小站</a>
    </el-col>
    <el-col :span="6">
      <el-space :size="size">
        <a @click="toLogin" v-if="!isLogined">登录</a>
        <a @click="toExit" v-if="isLogined">退出</a>
        <a @click="toProfile" v-if="isLogined">个人中心</a>
        <span v-html="userName"></span>
        <router-link title="查询" :to="{path:'/search'}" style="text-decoration:none;">
          <el-icon>
            <Search class="searchIcon" />
          </el-icon>
        </router-link>
      </el-space>
    </el-col>
    <el-col :span="3"></el-col>
  </el-row>
</template>

<style scoped>
* {
  background: #5f9ea0;
  color: #fff;
  font-size: 22px;
}

.top {
  height: 80px;
}

a {
  text-decoration: none;
  cursor: pointer;
}

a:hover {
  color: #10D07A;
}

.searchIcon {
  color: black;
}

.searchIcon:hover {
  color: #10D07A;
}
</style>

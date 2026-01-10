<script setup>
import { reactive,ref,inject } from 'vue'
import { ElMessageBox } from 'element-plus'
import qs from 'qs'
import { useStore } from '@/stores/my'
import { useRouter } from 'vue-router' // 新增：引入路由
const store = useStore()
const axios = inject('axios')
const toAdminMain = inject('toAdminMain')
const toHome = inject('toHome')

// 新增：创建路由实例，用于跳转到注册页
const router = useRouter()

const formSize = ref('default')
const ruleFormRef = ref(null)
const user = reactive({
  "username": '',
  "password": ''
})

//设置表单验证规则
const rules = reactive({
  username: [
    { required: true, message: '请输入用户名!', trigger: 'blur' },
    { min: 2, max: 10, message: '长度在 2 到 10 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码!', trigger: 'blur' },
    { min: 4, max: 15, message: '长度在 4 到 15 个字符', trigger: 'blur' },
    { pattern: /[A-Za-z0-9_\-\u4e00-\u9fa5]+/, message: '请输入正确的密码', trigger: ['blur','change'] }
  ]
})

//表单验证
const submitForm = async(formEl) => {
  if(!formEl) return
  await formEl.validate((valid,fields) => {
    if(valid){//如果通过表单验证，则可以提交表单数据
      axios({
        headers:{
          'Content-Type':'application/x-www-form-urlencoded'
        },
        method: 'post',
        url: '/api/login',//要和服务器端Security设置的登录网址一致
        data: qs.stringify(user),
      })
          .then((response) => {
            if(response.data.success){
              // 登录成功后，将用户信息保存到全局状态store.user.user中
              store.user.user=response.data.map.user
              // 根据用户身份跳转到不同的页面
              if(response.data.map.user.authorities[0]=='ROLE_common'){
                toHome()//普通用户跳转到主页
              }else if(response.data.map.user.authorities[0]=='ROLE_admin'){
                toAdminMain()//管理员跳转到管理页面
              }
            }else{
              // 检查是否是用户名不存在的错误
              if (response.data.msg && (response.data.msg.includes('用户名') || response.data.msg.includes('不存在') || response.data.msg.includes('user') || response.data.msg.toLowerCase().includes('not found'))) {
                ElMessageBox.alert('该用户不存在，请注册', '提示')
              } else {
                ElMessageBox.alert(response.data.msg, '结果')
              }
            }
          }).catch((error) => { //请求失败返回的数据
        ElMessageBox.alert('系统错误！', '结果')
      })
    }else{
      ElMessageBox.alert('验证失败！', '结果')
    }
  })
}

// 新增：跳转到注册页面的方法
const toRegisterPage = () => {
  router.push({ name: 'register' }) // 跳转到注册路由（需确保路由中已定义name为register的路由）
  // 若路由只配置了path，可改用：router.push('/register')
}
</script>

<template>
  <div class="log">
    <el-row justify="center">
      <el-col :span="10">
        <h1 class="log-title">~欢迎登录博客~</h1>
      </el-col>
    </el-row>
    <el-row justify="center">
      <el-col :span="8">
        <el-form ref="ruleFormRef" :model="user" status-icon
                 :rules="rules" label-width="120px" size="default">
          <el-form-item label="用户名：" prop="username">
            <el-input v-model="user.username" />
          </el-form-item>
          <el-form-item label="密码：" prop="password">
            <el-input v-model="user.password" type="password" />
          </el-form-item>
          <el-form-item>
            <!-- 原有登录按钮 -->
            <el-button type="primary" @click="submitForm(ruleFormRef)">登录</el-button>
            <!-- 新增：注册按钮，样式与登录按钮区分，添加间距 -->
            <el-button type="text" @click="toRegisterPage" style="margin-left: 15px;">
              还没有账号？去注册
            </el-button>
          </el-form-item>
        </el-form>
      </el-col>
    </el-row>
    <el-row justify="center">
      <el-col :span="8">
        <p style="margin: 80px; color:#2E2D3C">
          2022 @ Password By <a style="color: #0e90d2">CrazyStone</a></p>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.log-title{
  font-size: 4rem;
  color: black;
  margin-top: 180px;
}

.log{
  width: 100%;
  height: 100%;
  background: url('@/assets/loginbackground.jpg') #000 no-repeat;
  background-size: cover;
  background-position: center;
  min-height: 100vh;
}

/* 新增：注册按钮样式优化（可选） */
:deep(.el-button--text) {
  color: #409eff; /* 与主题色一致 */
  font-size: 14px;
}
:deep(.el-button--text):hover {
  color: #66b1ff;
}
</style>
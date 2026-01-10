<template>
  <div class="register-container">
    <!-- 注册标题 -->
    <el-row justify="center">
      <el-col :span="10">
        <h1 class="register-title">~用户注册~</h1>
      </el-col>
    </el-row>

    <!-- 注册表单 -->
    <el-row justify="center">
      <el-col :span="8">
        <el-form
            ref="registerFormRef"
            :model="registerForm"
            :rules="registerRules"
            status-icon
            label-width="120px"
            size="default"
        >
          <!-- 用户名 -->
          <el-form-item label="用户名" prop="username">
            <el-input
                v-model="registerForm.username"
                placeholder="请输入2-10位用户名"
                clearable
            />
          </el-form-item>

          <!-- 密码 -->
          <el-form-item label="密码" prop="password">
            <el-input
                v-model="registerForm.password"
                type="password"
                placeholder="请输入6-15位非空密码"
                clearable
            />
          </el-form-item>

          <!-- 确认密码 -->
          <el-form-item label="确认密码" prop="repassword">
            <el-input
                v-model="registerForm.repassword"
                type="password"
                placeholder="请再次输入密码"
                clearable
            />
          </el-form-item>

          <!-- 邮箱（可选） -->
          <el-form-item label="邮箱" prop="email">
            <el-input
                v-model="registerForm.email"
                placeholder="可选"
                clearable
            />
          </el-form-item>

          <!-- 提交按钮 -->
          <el-form-item>
            <el-button type="primary" @click="submitRegister">注册</el-button>
            <el-button @click="toLoginPage" style="margin-left: 10px;">已有账号？去登录</el-button>
          </el-form-item>
        </el-form>
      </el-col>
    </el-row>

    <!-- 版权信息 -->
    <el-row justify="center" style="margin-top: 50px;">
      <el-col :span="8">
        <p style="color: #666;">2022 © Powered By CrazyStone</p>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { reactive, ref, inject } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import axios from 'axios'

// 路由实例
const router = useRouter()

// 表单引用（用于手动触发校验）
const registerFormRef = ref(null)

// 注册表单数据
const registerForm = reactive({
  username: '', // 用户名
  password: '', // 密码
  repassword: '', // 确认密码
  email: '' // 邮箱（可选）
})

// 表单校验规则
const registerRules = reactive({
  // 用户名校验：非空 + 长度2-10位
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 10, message: '用户名长度需为2-10位', trigger: 'blur' },
    { pattern: /^[A-Za-z0-9_]+$/, message: '用户名仅支持字母、数字和下划线', trigger: 'blur' }
  ],
  // 密码校验：非空 + 长度6-15位 + 非空字符
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 15, message: '密码长度需为6-15位', trigger: 'blur' },
    { pattern: /^\S+$/, message: '密码不能包含空格', trigger: 'blur' }
  ],
  // 确认密码校验：非空 + 与密码一致
  repassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        // 校验两次密码是否一致
        if (value !== registerForm.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback() // 校验通过
        }
      },
      trigger: 'blur'
    }
  ],
  // 邮箱校验（可选，格式正确）
  email: [
    {
      pattern: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/,
      message: '请输入正确的邮箱格式（如xxx@xxx.com）',
      trigger: 'blur',
      required: false // 非必填
    }
  ]
})

// 提交注册
const submitRegister = async () => {
  // 1. 触发表单校验
  if (!registerFormRef.value) return
  await registerFormRef.value.validate(async (isValid, errorFields) => {
    if (isValid) {
      // 2. 校验通过，提交注册数据（移除repassword，后端不需要）
      const registerData = { ...registerForm }
      delete registerData.repassword // 删除确认密码字段

      // 3. 调用后端注册接口
      try {
        const response = await axios({
          method: 'post',
          url: '/api/user/register',
          data: registerData
        })

        // 4. 处理注册结果
        if (response.data.success) {
          ElMessage.success(response.data.msg)
          // 注册成功，跳转到登录页
          router.push({ name: 'login' })
        } else {
          ElMessageBox.alert(response.data.msg, '注册失败')
        }
      } catch (error) {
        ElMessageBox.alert('系统错误，请稍后再试', '错误')
      }
    } else {
      // 校验失败，提示用户
      ElMessageBox.alert('请检查表单填写是否正确', '校验失败')
    }
  })
}

// 跳转到登录页
const toLoginPage = () => {
  router.push({ name: 'login' })
}
</script>

<style scoped>
/* 注册页面样式 */
.register-container {
  width: 100%;
  height: 100vh;
  background: url('@/assets/loginbackground.jpg') no-repeat center;
  background-size: cover;
  padding-top: 120px;
}

.register-title {
  font-size: 3rem;
  color: #333;
  margin-bottom: 40px;
  text-align: center;
}

/* 按钮样式优化 */
.el-button--primary {
  background-color: #409eff;
  border-color: #409eff;
}

.el-button--primary:hover {
  background-color: #66b1ff;
  border-color: #66b1ff;
}
</style>
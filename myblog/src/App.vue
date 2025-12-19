<script setup>
import { RouterView, useRouter,useRoute } from 'vue-router';
import { provide } from 'vue'; //引入provide
const router = useRouter();
//路由器跳转至index.vue,即在下面的<RouterView />位置显示index.vue
//目的是确保打开网站时，网页显示index.vue内容
//注意：如果当前文件中没有<RouterView />，则无法显示index.vue
const route = useRoute();
router.push({ name: 'home' });
const toHome = () => {
  router.push({ name: 'home' });
};
const toAdminMain = () => {
  router.push({ name: 'adminMain' });
}; //函数的功能为：路由器跳转
//provid可以将父组件的数据和函数传到孙子组件
//第二个参数为提供的函数名称，第一个为自定义的别名（在后代组件中使用）

// 新增：跳转到注册页
const toRegister = () => {
  router.push({ name: 'register' })
}

function toArticle(){
  router.push({ name: 'articleAndComment', params: { articleId: 13 } })//设置跳转参数
}

function toLogin(){
  router.push({ name: 'login' })
}

//provide可以将父组件的数据和函数传到孙子组件
//第二个参数为提供的函数名称，第一个为自定义的别名（在后代组件中使用）
provide("toAdminMain", toAdminMain);
provide("toArticle", toArticle);
provide("toLogin", toLogin);
provide("toHome", toHome);
provide("toRegister", toRegister)

</script>

<template>
  <RouterView />  
</template>

<style scoped></style>

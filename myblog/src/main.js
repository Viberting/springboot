import { createApp } from "vue";
import { createPinia } from "pinia";

import App from "./App.vue";
import router from "./router";

/*  import the fontawesonme core */
import { library, config } from '@fortawesome/fontawesome-svg-core'
// 手动引入样式并禁止自动注入（与 Vue 3 + FontAwesome v6/7 推荐用法兼容）
import '@fortawesome/fontawesome-svg-core/styles.css'
config.autoAddCss = false
/*  import font awesome icon component */
import { FontAwesomeIcon } from '@fortawesome/vue-fontawesome'
// 引入图标名为weibo和github的两个图标，import时，更改为（fa+首字母大写的图标名）
import { faWeibo, faGithub,faQq } from '@fortawesome/free-brands-svg-icons'
// add icon to the library  加入库后，以后vue文件可以使用这些图标
library.add(faWeibo)
library.add(faGithub)
library.add(faQq)

const app = createApp(App);
app.component('font-awesome-icon', FontAwesomeIcon)

app.use(createPinia());
app.use(router);

//导入element-plus
import ElementPlus from "element-plus";
import "element-plus/dist/index.css";
// 全局设置elemt-plus组件中文字为中文
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
app.use(ElementPlus,{
  locale: zhCn
})
//全局导入ElementPlus的图标
import * as ElementPlusIconsVue from "@element-plus/icons-vue";
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component);
}

//引入axios和vue-axios
import axios from 'axios'
//vue-axios是将axios集成到Vue.js的小包装器，可以像插件一样进行安装
import VueAxios from 'vue-axios'

// 添加请求拦截器，确保每次请求都携带cookie
axios.interceptors.request.use(config => {
  config.withCredentials = true;
  return config;
});

// 添加响应拦截器来处理认证失败的情况
axios.interceptors.response.use(
  response => {
    // 检查响应是否是HTML内容（可能是重定向到登录页）
    const contentType = response.headers['content-type'];
    if (contentType && contentType.includes('text/html')) {
      // 检查请求URL是否包含需要认证的操作
      const url = response.config.url;
      if (needsAuthentication(url)) {
        // 如果是需要认证的API返回HTML，提示用户登录
        console.log('需要认证的API请求：' + url);
        alert('登录已过期，请重新登录');
        // 重定向到登录页
        window.location.href = '/#/login';
      }
      // 如果是不需要认证的API返回HTML，完全静默处理（不显示任何警告）
    }
    return response;
  },
  error => {
    // 处理401 Unauthorized错误
    if (error.response && error.response.status === 401) {
      // 检查请求URL是否包含需要认证的操作
      const url = error.config.url;
      if (needsAuthentication(url)) {
        // 如果是需要认证的API返回401，提示用户登录
        console.log('401错误，需要认证的API请求：' + url);
        alert('登录已过期，请重新登录');
        // 重定向到登录页
        window.location.href = '/#/login';
      }
      // 如果是不需要认证的API返回401，完全静默处理（不显示任何警告）
    }
    return Promise.reject(error);
  }
);

// 辅助函数：判断URL是否需要认证
function needsAuthentication(url) {
  // 需要认证的操作包括：发布评论、编辑、删除、用户管理等
  // 不需要认证的操作：获取文章、获取评论列表（只读）
  const authRequiredPaths = [
    '/api/comment/insert',
    '/api/comment/saveComment',
    '/api/comment/delete',
    '/api/comment/update',
    '/api/comment/deleteComment',
    '/api/comment/insertReply',
    '/api/follow/',
    '/api/user/',
    '/api/article/publish',
    '/api/article/update',
    '/api/article/delete',
    '/api/logout'
  ];
  
  // 不需要认证的路径（明确允许未登录访问）
  const noAuthPaths = [
    '/api/article/getArticleAndFirstPageCommentByArticleId',
    '/api/comment/getAPageCommentByArticleId',
    '/api/comment/getCommentTreeByArticleId',
    '/api/comment/getCommentReplies'
  ];
  
  // 优先检查不需要认证的路径
  for (const path of noAuthPaths) {
    if (url.includes(path)) {
      return false; // 不需要认证
    }
  }
  
  // 再检查需要认证的路径
  for (const path of authRequiredPaths) {
    if (url.includes(path)) {
      return true; // 需要认证
    }
  }
  
  // 默认行为：如果路径包含/api/，且未在白名单中，则需要认证
  return url.includes('/api/') && !url.includes('/api/images/');
}

app.use(VueAxios, axios)
//provide 'axios'，其它地方可以注入，从而可以使用axios
app.provide('axios', axios)

// 引入并注册图片裁剪插件vue-cropper
import VueCropper from 'vue-cropper';
import 'vue-cropper/dist/index.css';
app.use(VueCropper);

app.mount("#app");

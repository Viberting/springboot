<script setup>
import { reactive, ref, inject, provide, nextTick } from 'vue'
import Editor from '@tinymce/tinymce-vue'
import { ElMessageBox } from 'element-plus'
import { useStore } from '@/stores/my'
import Cropper from "@/components/Cropper.vue";
import { undefine, nullZeroBlank } from "@/js/tool.js"

const store = useStore()
let type = "add"
const header = ref("发布文章")
// 注入axios，本文件可以使用axios
const axios = inject('axios')
const image_upload_handler = (blobInfo, progress) => new Promise((resolve, reject) => {
  const xhr = new XMLHttpRequest();
  xhr.withCredentials = false;
  xhr.open('POST', 'http://localhost:5173/api/article/upload');

  xhr.upload.onprogress = (e) => {
    progress(e.loaded / e.total * 100);
  };

  xhr.onload = () => {
    if (xhr.status === 403) {
      reject({ message: 'HTTP Error: ' + xhr.status, remove: true });
      return;
    }
    if (xhr.status < 200 || xhr.status >= 300) {
      reject('HTTP Error: ' + xhr.status);
      return;
    }
    const json = JSON.parse(xhr.response);
    if (!json || !json.map || typeof json.map.url != 'string') {
      reject('Invalid JSON: ' + xhr.responseText);
      return;
    }
    resolve(json.map.url);
  };

  xhr.onerror = () => {
    reject('Image upload failed due to a XHR Transport error. Code: ' + xhr.status);
  };

  const formData = new FormData();
  formData.append('file', blobInfo.blob(), blobInfo.filename());

  xhr.send(formData);
});

const apiKey = ref('bkybykccm7np91con9v5ozoo3wtv4b90l6ei9a1kje6fm5cw')
const init = reactive({
  language: "zh_CN", //语言类型
  placeholder: "在这里输入文字",
  plugins: ['image', 'code'],
  toolbar: 'image',
  images_file_types: 'jpg,jpeg,png,gif,bmp',
  images_upload_handler: image_upload_handler,
  convert_urls: false

})

let article = reactive({ "title": "", "tags": "", "content": "", thumbnail: "" })
if (store.articleId > 0) {
  type = "edit"
  header.value = "编辑文章"
  axios({
    method: 'post',
    url: '/api/article/selectById?id=' + store.articleId
  }).then((response) => {
    if (response.data.success) {
      let nowArticle = response.data.map.article
      article.id = nowArticle.id
      article.title = nowArticle.title
      article.tags = nowArticle.tags
      article.content = nowArticle.content
      article.thumbnail=nowArticle.thumbnail
      if(     !undefine(article.thumbnail) 
          &&  !nullZeroBlank(article.thumbnail)
          &&   article.thumbnail.indexOf("/api")==0){
            cropper1.value.setThumbnail(article.thumbnail)
      }
      
    } else {
      ElMessageBox.alert(response.data.msg, '结果')
    }
    store.articleId = 0
  }).catch((error) => {  //请求失败返回的数据
    ElMessageBox.alert("系统错误！", '结果')
    store.articleId = 0
  })
}

const cropper1 = ref(null)
function publishArticle() {
  let thumbnail = cropper1.value.getThumbnail()
  if (undefine(thumbnail) || nullZeroBlank(thumbnail) 
            || thumbnail.indexOf("/api") != 0) {//添加文章时
    article.thumbnail = ""
  } else {//编辑文章时
    article.thumbnail = thumbnail
  }
  //发送axios的post请求，经过反向代理，最终访问http://localhost:8080/article/publishArticle
  axios({
    method: 'post',
    url: '/api/article/publishArticle?type=' + type,
    data: article,
    timeout: 3000000
  }).then((response) => {
    //response.data代表后端服务器返回的json格式的数据
    ElMessageBox.alert(response.data, '结果')
    if("添加成功！"==response.data){
      clearData()//清空数据
      window.scrollTo(0, 0)//滚动到顶端    
    }
  }).catch((error) => {  //请求失败返回的数据
    ElMessageBox.alert("系统错误！", '结果')
  })
}

const gotoArticleManage = inject("gotoArticleManage")


let isShowCropper = ref(true)
//刷新子组件Cropper，让子组件恢复初始状态
function freshCropper() {
  isShowCropper.value = false
  //nextTick将回调延迟到下次 DOM 更新循环之后执行。
  //在修改数据（isShowCropper.value = false）之后立即使用它，然后等待 DOM 更新。
  nextTick(() => {
    isShowCropper.value = true;
  })
}
provide("freshCropper", freshCropper)
//清空数据
function clearData() {
  article.title=""
  article.tags=""
  article.content=""
  article.thumbnail=""
  cropper1.value.clearData()//清空子组件（剪裁图片）数据
}
</script>
<template>
  <el-row>
    <el-col :span="24">
      <h4>{{ header }}</h4>
    </el-col>
  </el-row>
  <el-row>
    <el-col :span="12">
      <el-input v-model="article.title" placeholder="请输入文章标题（必须）" clearable />
    </el-col>
    <el-col :span="12">
      <el-input v-model="article.tags" :rows="1" type="textarea" placeholder="请输入文章标签,可以多行" />
    </el-col>
  </el-row>
  <el-row>
    <el-col :span="24">
      <div id="editor">
        <editor v-model="article.content" :init="init" :api-key="apiKey" />
      </div>
    </el-col>
  </el-row>
  <el-row>
    <el-col :span="24">
      <Cropper ref="cropper1" v-if="isShowCropper" />
    </el-col>
  </el-row>
  <el-row>
    <el-col :span="24">
      <div align="right">
        <el-button @click="gotoArticleManage">返回列表</el-button>
        <el-button type="primary" @click="publishArticle">保存文章</el-button>
      </div>
    </el-col>
  </el-row>
</template>
<style scoped>

</style>